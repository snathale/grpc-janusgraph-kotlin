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
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
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
class GrpcServerUserVertexTest: GrpcServerTestHelper(), IVertexTests {

    @get: Rule
    val grpcCleanup: GrpcCleanupRule = GrpcCleanupRule()

    var stub: AccessControlServiceGrpc.AccessControlServiceBlockingStub? = null

    private val date: Date = Date()

    var userId: Long = 0

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
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data)
        assertEquals("user", responseConverter.label)
        assertEquals("id", responseConverter.properties[0].name)
        assertEquals("name", responseConverter.properties[1].name)
        assertEquals("User Test 2", responseConverter.properties[1].value)
        assertEquals("code", responseConverter.properties[2].name)
        assertEquals("2", responseConverter.properties[2].value)
        assertEquals("creationDate", responseConverter.properties[3].name)
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        assertNotNull(format.parse(responseConverter.properties[3].value))
        assertEquals("observation", responseConverter.properties[4].name)
        assertEquals("User Test 2 Observation", responseConverter.properties[4].value)
        assertEquals("enable", responseConverter.properties[5].name)
        assertTrue(responseConverter.properties[5].value.toBoolean())
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("user", "1")
        val converter = Converter.create().
                toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertUserVertexGrpcResponse(userId, "1", "UserTest", date, "This is UserTest", true, response)
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(userId).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertUserVertexGrpcResponse(userId, "1", "UserTest", date, "This is UserTest", true, response)
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
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data)
        assertEquals("user", responseConverter.label)
        assertEquals("id", responseConverter.properties[0].name)
        assertEquals("name", responseConverter.properties[1].name)
        assertEquals("User Test 2", responseConverter.properties[1].value)
        assertEquals("code", responseConverter.properties[2].name)
        assertEquals("2", responseConverter.properties[2].value)
        assertEquals("creationDate", responseConverter.properties[3].name)
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        assertNotNull(format.parse(responseConverter.properties[3].value))
        assertEquals("enable", responseConverter.properties[4].name)
        assertTrue(responseConverter.properties[4].value.toBoolean())
        val g = GraphFactory.open().traversal()
        val userStorage = g.V().hasLabel("user").has("code", "2").next()
        val values = AbstractMapper.parseMapVertex(userStorage)
        Assert.assertEquals("User Test 2", AbstractMapper.parseMapValue(values["name"].toString()))
        Assert.assertEquals("2", AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals(responseConverter.properties[0].value, AbstractMapper.parseMapValue(values["id"].toString()))
        Assert.assertEquals("", AbstractMapper.parseMapValue(values["observation"].toString()))
        Assert.assertEquals(true, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
        Assert.assertEquals("", AbstractMapper.parseMapValue(values["description"].toString()))
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
        Assert.assertFalse(g.V().hasLabel(VertexLabel.USER.label).has(PropertyLabel.CODE.label, "2").hasNext())
        val name: List<Property> = listOf(Property("name", "test"))
        val user1 = VertexData("user", name)
        converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, user1)
        val response1 = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response1.status)
        Assert.assertEquals("@UCVE-001 Empty User properties", response1.message)
        Assert.assertFalse(response1.hasData())
        Assert.assertFalse(g.V().hasLabel(VertexLabel.USER.label).has(PropertyLabel.CODE.label, "2").hasNext())
    }
}
