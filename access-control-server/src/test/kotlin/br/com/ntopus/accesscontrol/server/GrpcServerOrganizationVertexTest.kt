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
class GrpcServerOrganizationVertexTest: GrpcServerTestHelper(), IVertexTests {

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
        this.id = this.createDefaultOrganization(date)!!
        this.createDefaultUnitOrganization(Date())
    }

    @Test
    override fun addVertex() {
        val properties:List<Property> = listOf(Property("code", "2"),
                Property("name", "New Organization"),
                Property("observation", "This is a observation"))
        val vertex = VertexData("organization", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        assertNotNull(format.parse(responseConverter["creationDate"]))
        assertEquals("organization", response.data.label)
        Assert.assertEquals("New Organization", responseConverter["name"])
        Assert.assertEquals("2", responseConverter["code"])
        Assert.assertEquals("This is a observation", responseConverter["observation"])
        Assert.assertEquals(true, responseConverter["enable"]!!.toBoolean())
        this.assertAgentMapper(
                "organization", "2", "New Organization",
                format.parse(responseConverter["creationDate"]), "This is a observation",
                true, responseConverter["id"]
        )
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(id).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("organization", id, "1", "Kofre", date, "This is a Organization", true, response)
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("organization", "1")
        val converter = Converter.create().
                toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("organization", id, "1", "Kofre", date, "This is a Organization", true, response)
    }

    @Test
    override fun createVertexWithExtraProperty() {
        val properties:List<Property> = listOf(Property("code", "2"),
                Property("name", "New Organization"),
                Property("description", "This is a description"),
                Property("observation", "This is a observation"))
        val vertex = VertexData("organization", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()

        Assert.assertNotNull(format.parse(responseConverter["creationDate"]))
        Assert.assertEquals("New Organization", responseConverter["name"])
        Assert.assertEquals("2", responseConverter["code"])
        Assert.assertEquals("This is a observation", responseConverter["observation"])
        Assert.assertEquals(true, responseConverter["enable"]!!.toBoolean())
        val g = GraphFactory.open().traversal()
        val userStorage = g.V().hasLabel("organization").has("code", "2").next()
        val values = AbstractMapper.parseMapVertex(userStorage)
        Assert.assertEquals("New Organization", AbstractMapper.parseMapValue(values["name"].toString()))
        Assert.assertEquals("2", AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals("This is a observation", AbstractMapper.parseMapValue(values["observation"].toString()))
        Assert.assertEquals("", AbstractMapper.parseMapValue(values["description"].toString()))
        Assert.assertEquals(true, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
    }

    @Test
    override fun cantCreateVertexThatExist() {
        val properties: List<Property> = listOf(Property("code", "1"), Property("name", "Test"))
        val vertex = VertexData("organization", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@OCVE-002 Adding this property for key [code] and value [1] violates a uniqueness constraint [vByOrganizationCode]", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAgentMapper(
                "organization", "1", "Kofre",
                date, "This is a Organization",
                true, id.toString()
        )
    }

    @Test
    override fun cantCreateVertexWithRequiredPropertyEmpty() {
        val code: List<Property> = listOf(Property("code", "2"))
        val organization = VertexData("organization", code)
        var converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, organization)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@OCVE-001 Empty Organization properties", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("organization").has("code", "2").hasNext())
        val name: List<Property> = listOf(Property("name", "test"))
        val organization1 = VertexData("organization", name)
        converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, organization1)
        val response1 = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response1.status)
        Assert.assertEquals("@OCVE-001 Empty Organization properties", response1.message)
        Assert.assertFalse(response1.hasData())
        Assert.assertFalse(g.V().hasLabel("organization").has("code", "2").hasNext())
    }
}