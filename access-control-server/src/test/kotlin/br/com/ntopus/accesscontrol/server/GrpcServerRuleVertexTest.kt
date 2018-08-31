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
class GrpcServerRuleVertexTest: GrpcServerTestHelper(), IVertexTests {

    @get: Rule
    val grpcCleanup: GrpcCleanupRule = GrpcCleanupRule()

    var stub: AccessControlServiceGrpc.AccessControlServiceBlockingStub? = null

    private val date: Date = Date()

    var id: Long = 0

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
        this.id = this.createDefaultRules(date)!!
    }

    @Test
    override fun addVertex() {
        val properties:List<Property> = listOf(Property("code", "3"),
                Property("name", "REMOVE_USER"),
                Property("description", "This is a Rule Remove User"),
                Property("enable", "true"))
        val vertex = VertexData("rule", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data)
        val propertiesMap = responseConverter.properties.map { it.name to it.value }.toMap()
        assertNotNull(format.parse(propertiesMap["creationDate"]))
        Assert.assertEquals("REMOVE_USER", propertiesMap["name"])
        Assert.assertEquals("3", propertiesMap["code"])
        Assert.assertEquals("This is a Rule Remove User", propertiesMap["description"])
        Assert.assertEquals(true, propertiesMap["enable"]!!.toBoolean())
        this.assertPermissionMapper("rule", "3", "REMOVE_USER", format.parse(propertiesMap["creationDate"]), "This is a Rule Remove User", true, propertiesMap["id"])
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(id).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse("rule", id, "1", "ADD_USER", date, "This is a Rule Add User", true, response)
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("rule", "1")
        val converter = Converter.create().
                toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse("rule", id, "1", "ADD_USER", date, "This is a Rule Add User", true, response)
    }

    @Test
    override fun createVertexWithExtraProperty() {
        val properties:List<Property> = listOf(Property("code", "3"),
                Property("name", "REMOVE_USER"),
                Property("description", "This is a description"),
                Property("enable", "false"),
                Property("observation", "This is a test"))
        val rule = VertexData("rule", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, rule)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data)
        val propertiesMap = responseConverter.properties.map { it.name to it.value }.toMap()
        Assert.assertEquals("REMOVE_USER", propertiesMap["name"])
        Assert.assertEquals("3", propertiesMap["code"])
        Assert.assertEquals("This is a description", propertiesMap["description"])
        Assert.assertEquals(false, propertiesMap["enable"]!!.toBoolean())
        val g = GraphFactory.open().traversal()
        val vertex = g.V().hasLabel("rule").has("code", "3").next()
        val values = AbstractMapper.parseMapVertex(vertex)
        Assert.assertEquals("REMOVE_USER", AbstractMapper.parseMapValue(values["name"].toString()))
        Assert.assertEquals("3", AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals("This is a description", AbstractMapper.parseMapValue(values["description"].toString()))
        Assert.assertEquals(false, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
        Assert.assertNotEquals("This is a test", AbstractMapper.parseMapValue(values["observation"].toString()))
    }

    @Test
    override fun cantCreateVertexThatExist() {
        val properties: List<Property> = listOf(
                Property("code", "1"),
                Property("name", "REMOVE_USER"),
                Property("description", "Description Test"))
        val vertex = VertexData("rule", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@RCVE-002 Adding this property for key [code] and value [1] violates a uniqueness constraint [vByRuleCode]", response.message)
        Assert.assertFalse(response.hasData())
    }

    @Test
    override fun cantCreateVertexWithRequiredPropertyEmpty() {
        val properties: List<Property> = listOf(
                Property("name", "REMOVE_USER"),
                Property("description", "Description Test"))
        val vertex = VertexData("rule", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@RCVE-001 Empty Rule properties", response.message)
        Assert.assertFalse(response.hasData())
    }

    @Test
    override fun updateProperty() {
        val properties : List<Property> = listOf(Property("name", "Test"), Property("description", "Property updated"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(id).setLabel("rule").addAllProperty(converter).build())
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse("rule", id,"1", "Test", date, "Property updated", true, response)
        this.assertPermissionMapper("rule", "1", "Test", date, "Property updated", true,  id.toString())
    }

    @Test
    override fun cantUpdateDefaultProperty() {
        val properties : List<Property> = listOf(Property("name", "Test"), Property("code", "2"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(id).setLabel("rule").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@RUPE-002 Rule property can be updated", response.message)
        Assert.assertFalse(response.hasData())
        this.assertPermissionMapper("rule", "1", "ADD_USER", date, "This is a Rule Add User", true, id.toString())
    }

    @Test
    override fun cantUpdatePropertyFromVertexThatNotExist() {
        val properties : List<Property> = listOf(Property("name", "Test"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(1).setLabel("rule").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("RUPE-001 Impossible find Rule with id 1", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("rule").has("name", "Test").hasNext())
    }

    @Test
    override fun deleteVertex() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(id).setLabel("rule").build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertFalse(response.hasData())
        this.assertPermissionMapper("rule", "1", "ADD_USER", date,"This is a Rule Add User", false, id.toString())
    }

    @Test
    override fun cantDeleteVertexThatNotExist() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(1).setLabel("rule").build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@RDE-001 Impossible find Rule with id 1", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("rule").hasId(1).hasNext())
    }
}