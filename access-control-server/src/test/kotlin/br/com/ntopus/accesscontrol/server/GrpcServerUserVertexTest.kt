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
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import net.badata.protobuf.converter.Converter
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class GrpcServerUserVertexTest : GrpcServerTestHelper(), IVertexTests {

    @get: Rule
    val grpcCleanup: GrpcCleanupRule = GrpcCleanupRule()

    var stub: AccessControlServiceGrpc.AccessControlServiceBlockingStub? = null

    private val date: Date = Date()

    private var userId: Long = 0

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
        this.userId = this.createDefaultUser(date)!!
        this.createDefaultAccessGroup(Date())

    }

    @Test
    override fun addVertex() {
        val properties: List<Property> = listOf(
                Property("code", "2"),
                Property("name", "User Test 2"),
                Property("observation", "User Test 2 Observation"))
        val vertex = VertexData("user", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        assertEquals("user", response.data.label)
        val propertiesMap = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        assertEquals("User Test 2", propertiesMap["name"])
        assertEquals("2", propertiesMap["code"])
        assertNotNull(format.parse(propertiesMap["creationDate"]))
        assertEquals("User Test 2 Observation", propertiesMap["observation"])
        assertTrue(propertiesMap["enable"]!!.toBoolean())
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("user", "1")
        val converter = Converter.create().toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("user", userId, "1", "UserTest", date, "This is UserTest", true, response)
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(userId).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("user", userId, "1", "UserTest", date, "This is UserTest", true, response)
    }

    @Test
    fun getVertexByIdThatNotExist() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(1).build()
        )
        assertEquals("error", response.status)
        assertEquals("@GVIE-001 Vertex not found", response.message)
        Assert.assertFalse(response.hasData())
    }

    @Test
    fun getVertexByCodeThatNotExist() {
        val vertex = VertexByCode("user", "2")
        val converter = Converter.create().toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("error", response.status)
        assertEquals("@GVCE-001 Vertex not found", response.message)
        Assert.assertFalse(response.hasData())
    }

    @Test
    override fun createVertexWithExtraProperty() {
        val properties: List<Property> = listOf(
                Property("code", "2"),
                Property("name", "User Test 2"),
                Property("description", "User Test 2 Description"))
        val vertex = VertexData("user", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val propertiesMap = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        assertEquals("User Test 2", propertiesMap["name"])
        assertEquals("2", propertiesMap["code"])
        assertNotNull(format.parse(propertiesMap["creationDate"]))
        assertTrue(propertiesMap["enable"]!!.toBoolean())
        this.assertAgentMapper("user", "2", "User Test 2", format.parse(propertiesMap["creationDate"]), "", true, propertiesMap["id"])
    }

    @Test
    override fun cantCreateVertexThatExist() {
        val properties: List<Property> = listOf(Property("code", "1"), Property("name", "Test"))
        val vertex = VertexData("user", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@UCVE-002 Adding this property for key [code] and value [1] violates a uniqueness constraint [vByUserCode]", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAgentMapper("user", "1", "UserTest", date, "This is UserTest", true)
    }

    @Test
    override fun cantCreateVertexWithRequiredPropertyEmpty() {
        val code: List<Property> = listOf(Property("code", "2"))
        val user = VertexData("user", code)
        var converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, user)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@UCVE-001 Empty User properties", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("user").has("code", "2").hasNext())
        val name: List<Property> = listOf(Property("name", "test"))
        val user1 = VertexData("user", name)
        converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, user1)
        val response1 = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response1.status)
        Assert.assertEquals("@UCVE-001 Empty User properties", response1.message)
        Assert.assertFalse(response1.hasData())
        Assert.assertFalse(g.V().hasLabel("user").has("name", "test").hasNext())
    }

    @Test
    override fun updateProperty() {
        val properties: List<Property> = listOf(Property("name", "Test"), Property("observation", "Property updated"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(userId).setLabel("user").addAllProperty(converter).build())
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("user", userId,"1", "Test", date, "Property updated", true, response)
        this.assertAgentMapper("user", "1", "Test", date, "Property updated", true)
    }

    @Test
    override fun cantUpdateDefaultProperty() {
        val properties : List<Property> = listOf(Property("name", "Test"), Property("code", "2"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(userId).setLabel("user").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@UUPE-002 User property can be updated", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAgentMapper("user", "1", "UserTest", date, "This is UserTest", true, userId.toString())
    }

    @Test
    override fun cantUpdatePropertyFromVertexThatNotExist() {
        val properties : List<Property> = listOf(Property("name", "Test"), Property("code", "2"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(userId+1).setLabel("user").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@UUPE-001 Impossible find User with id ${userId+1}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("user").has("name", "Test").has("code", "2").hasNext())
    }

    @Test
    override fun deleteVertex() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(userId).setLabel("user").build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertFalse(response.hasData())
        this.assertAgentMapper("user", "1", "UserTest", date, "This is UserTest", false, userId.toString())
    }

    @Test
    override fun cantDeleteVertexThatNotExist() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(userId+1).setLabel("user").build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@UDE-001 Impossible find User with id ${userId+1}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("user").hasId(userId+1).hasNext())
    }
}
