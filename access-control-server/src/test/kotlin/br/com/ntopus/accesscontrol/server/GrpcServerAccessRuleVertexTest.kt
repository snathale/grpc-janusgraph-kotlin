package br.com.ntopus.accesscontrol.server

import br.com.ntopus.accesscontrol.AccessControlService
import br.com.ntopus.accesscontrol.VertexByCode
import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.importer.JanusGraphSchemaImporter
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.proto.AccessControlServiceGrpc
import br.com.ntopus.accesscontrol.server.helper.GrpcServerTestHelper
import br.com.ntopus.accesscontrol.server.helper.IVertexTests
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import net.badata.protobuf.converter.Converter
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class GrpcServerAccessRuleVertexTest: GrpcServerTestHelper(), IVertexTests {

    @get: Rule
    val grpcCleanup: GrpcCleanupRule = GrpcCleanupRule()

    var stub: AccessControlServiceGrpc.AccessControlServiceBlockingStub? = null

    private val date: Date = Date()

    private var id: Long = 0

    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    @Before
    fun setUp() {

        val graph = GraphFactory.setInstance("janusgraph-inmemory.properties")
        JanusGraphSchemaImporter().writeGraphSONSchema(graph.open(), javaClass.classLoader.getResource("schema.json").file)
        // Generate a unique in-process server name.
        val serverName = InProcessServerBuilder.generateName()

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(
                InProcessServerBuilder.forName(serverName).directExecutor().addService(
                        AccessControlService()
                ).build().start()
        )
        stub = AccessControlServiceGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        )
        this.createDefaultOrganization(Date())
        this.createDefaultUnitOrganization(Date())
        this.createDefaultGroup(Date())
        this.createDefaultAccessGroup(Date())
        this.id = this.createDefaultAccessRule(date)!!
    }

    @Test
    override fun addVertex() {
        val expirationDate = this.addDays(Date(), 1)
        val properties:List<Property> = listOf(
                Property("code", "2"),
                Property("expirationDate", format.format(expirationDate)))
        val vertex = VertexData("accessRule", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        this.assertAccessRuleVertexGrpcResponse("2", true, expirationDate, response)
        this.assertAccessRuleMapper("2", true, expirationDate)
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(id).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAccessRuleVertexGrpcResponse("1", true, date, response, id)
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("accessRule", "1")
        val converter = Converter.create().
                toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAccessRuleVertexGrpcResponse("1", true, date, response, id)
    }

    @Test
    override fun createVertexWithExtraProperty() {
        val properties:List<Property> = listOf(
                Property("code", "2"),
                Property("enable", "true"),
                Property("name", "Access Rule"))
        val vertex = VertexData("accessRule", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        val propertiesMap= Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        Assert.assertEquals(null, propertiesMap["expirationDate"])
        Assert.assertEquals("2", propertiesMap["code"])
        Assert.assertEquals(true, propertiesMap["enable"]!!.toBoolean())
        val g = GraphFactory.open().traversal()
        val userStorage = g.V().hasLabel("accessRule").has("code", "2").next()
        val values = AbstractMapper.parseMapVertex(userStorage)
        Assert.assertEquals("", AbstractMapper.parseMapValue(values["expirationDate"].toString()))
        Assert.assertEquals("2", AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals(true, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
        Assert.assertEquals("", AbstractMapper.parseMapValue(values["name"].toString()))
    }

    @Test
    override fun cantCreateVertexThatExist() {
        val properties: List<Property> = listOf(Property("code", "1"), Property("expirationDate", format.format(this.date)))
        val vertex = VertexData("accessRule", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARCVE-002 Adding this property for key [code] and value [1] violates a uniqueness constraint [vByAccessRuleCode]", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAccessRuleMapper("1", true, date, id)
    }

    @Test
    override fun cantCreateVertexWithRequiredPropertyEmpty() {
        val enable: List<Property> = listOf(Property("enable", "true"))
        val accessRule = VertexData("accessRule", enable)
        var converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, accessRule)
        var response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARCVE-001 Empty Access Rule properties", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertEquals(1, g.V().hasLabel("accessRule").has("enable", true).count().next())
        val expirationDate = this.addDays(Date(), 1)
        val property:List<Property> = listOf(
                Property("expirationDate", format.format(expirationDate)))
        val accessRule1 = VertexData("accessRule", property)
        converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, accessRule1)
        response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARCVE-001 Empty Access Rule properties", response.message)
        Assert.assertFalse(g.V().hasLabel("accessRule").has("expirationDate", expirationDate).hasNext())
    }

    @Test
    override fun updateProperty() {
        val expirationDate = this.addMinutes(date, 5)
        val properties : List<Property> = listOf(Property("expirationDate", format.format(expirationDate)), Property("name", "Group Test"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(id).setLabel("accessRule").addAllProperty(converter).build())
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAccessRuleVertexGrpcResponse("1", true, expirationDate, response)
        this.assertAccessRuleMapper("1", true, expirationDate, id)
    }

    @Test
    override fun cantUpdateDefaultProperty() {
        val properties : List<Property> = listOf(Property("name", "Group Test"), Property("code", "2"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(id).setLabel("accessRule").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARUPE-002 Access Rule property can be updated", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAccessRuleMapper("1", true, date, id)
    }

    @Test
    override fun cantUpdatePropertyFromVertexThatNotExist() {
        val g = GraphFactory.open().traversal()
        val properties : List<Property> = listOf(Property("enable", "false"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(id+1).setLabel("accessRule").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARUPE-001 Impossible find Access Rule with id ${id+1}", response.message)
        Assert.assertFalse(response.hasData())
        Assert.assertFalse(g.V().hasLabel("accessRule").hasId(id+1).has("enable", "false").hasNext())
    }

    @Test
    override fun deleteVertex() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(id).setLabel("accessRule").build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertFalse(response.hasData())
        this.assertAccessRuleMapper("1", false, date, id)
    }

    @Test
    override fun cantDeleteVertexThatNotExist() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(id+1).setLabel("accessRule").build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARDE-001 Impossible find Access Rule with id ${id+1}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("accessRule").hasId(id+1).hasNext())
    }
}