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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(JUnit4::class)
class GrpcServerAccessGroupVertexTest: GrpcServerTestHelper(), IVertexTests {

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
        this.id = this.createDefaultAccessGroup(date)!!
        this.createDefaultRules(Date())

    }

    @Test
    override fun addVertex() {
        val properties:List<Property> = listOf(Property("code", "2"),
                Property("name", "New Access Group"),
                Property("description", "This is a description"),
                Property("enable", "false"))
        val vertex = VertexData("accessGroup", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        assertEquals("accessGroup", response.data.label)
        val propertiesMap = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        Assert.assertEquals("New Access Group", propertiesMap["name"])
        Assert.assertEquals("2", propertiesMap["code"])
        Assert.assertEquals("This is a description", propertiesMap["description"])
        assertNotNull(format.parse(propertiesMap["creationDate"]))
        Assert.assertEquals(false, propertiesMap["enable"]!!.toBoolean())
        this.assertPermissionMapper("accessGroup", "2",
                "New Access Group", format.parse(propertiesMap["creationDate"]),
                "This is a description", false, propertiesMap["id"])
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(id).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse(
                "accessGroup", id, "1",
                "Operator", date, "This is a Operator Access Group",
                true, response)
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("accessGroup", "1")
        val converter = Converter.create().
                toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse(
                "accessGroup", id, "1",
                "Operator", date, "This is a Operator Access Group",
                true, response)
    }

    @Test
    override fun createVertexWithExtraProperty() {
        val properties:List<Property> = listOf(Property("code", "2"),
                Property("name", "New Access Group"),
                Property("description", "This is a description"),
                Property("enable", "false"),
                Property("observation", "This is a test"))
        val vertex = VertexData("accessGroup", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val propertiesMap = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        Assert.assertEquals("New Access Group", propertiesMap["name"])
        Assert.assertEquals("2", propertiesMap["code"])
        Assert.assertEquals("This is a description", propertiesMap["description"])
        assertNotNull(format.parse(propertiesMap["creationDate"]))
        Assert.assertEquals(false, propertiesMap["enable"]!!.toBoolean())
        val g = GraphFactory.open().traversal()
        val userStorage = g.V().hasLabel("accessGroup").has("code", "2").next()
        val values = AbstractMapper.parseMapVertex(userStorage)
        Assert.assertEquals("New Access Group", AbstractMapper.parseMapValue(values["name"].toString()))
        Assert.assertEquals("2", AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals("This is a description", AbstractMapper.parseMapValue(values["description"].toString()))
        Assert.assertEquals(false, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
        Assert.assertNotEquals("This is a test", AbstractMapper.parseMapValue(values["observation"].toString()))
    }

    @Test
    override fun cantCreateVertexThatExist() {
        val properties: List<Property> = listOf(
                Property("code", "1"),
                Property("name", "Access Group Name"),
                Property("description", "Description Test"))
        val vertex = VertexData("accessGroup", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGCVE-002 Adding this property for key [code] and value [1] violates a uniqueness constraint [vByAccessGroupCode]", response.message)
        Assert.assertFalse(response.hasData())
        this.assertPermissionMapper("accessGroup", "1", "Operator", date, "This is a Operator Access Group", true, id.toString())
    }

    @Test
    override fun cantCreateVertexWithRequiredPropertyEmpty() {
        val code: List<Property> = listOf(Property("code", "2"))
        val accessGroup = VertexData("accessGroup", code)
        var converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, accessGroup)
        var response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGCVE-001 Empty Access Group properties", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("accessGroup").has("code", "2").hasNext())
        val name: List<Property> = listOf(Property("name", "test"))
        val accessGroup1 = VertexData("accessGroup", name)
        converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, accessGroup1)
        response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGCVE-001 Empty Access Group properties", response.message)
        Assert.assertFalse(response.hasData())
        Assert.assertFalse(g.V().hasLabel("accessGroup").has("name", "test").hasNext())
    }

    override fun updateProperty() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cantUpdateDefaultProperty() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cantUpdatePropertyFromVertexThatNotExist() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteVertex() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cantDeleteVertexThatNotExist() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}