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
class GrpcServerGroupVertexTest: GrpcServerTestHelper(), IVertexTests {

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
        this.id = this.createDefaultGroup(date)!!

    }

    @Test
    override fun addVertex() {
        val properties:List<Property> = listOf(Property("code", "2"),
                Property("name", "RH"),
                Property("observation", "This is a RH Group"))
        val vertex = VertexData("group", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        Assert.assertEquals("RH", responseConverter["name"])
        Assert.assertEquals("2", responseConverter["code"])
        Assert.assertEquals("This is a RH Group", responseConverter["observation"])
        assertNotNull(format.parse(responseConverter["creationDate"]))
        Assert.assertEquals(true, responseConverter["enable"]!!.toBoolean())
        this.assertAgentMapper("group", "2", "RH", format.parse(responseConverter["creationDate"]), "This is a RH Group", true, responseConverter["id"])
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(id).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("group", id, "1", "Marketing", date, "This is a Marketing Group", true, response)
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("group", "1")
        val converter = Converter.create().
                toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("group", id, "1", "Marketing", date, "This is a Marketing Group", true, response)
    }

    @Test
    override fun createVertexWithExtraProperty() {
        val properties:List<Property> = listOf(Property("code", "2"),
                Property("name", "New Group"),
                Property("description", "This is a description"),
                Property("observation", "This is a observation"))
        val vertex = VertexData("group", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        Assert.assertEquals("New Group", responseConverter["name"])
        Assert.assertEquals("2", responseConverter["code"])
        Assert.assertEquals("This is a observation", responseConverter["observation"])
        assertNotNull(format.parse(responseConverter["creationDate"]))
        Assert.assertEquals(true, responseConverter["enable"]!!.toBoolean())
        this.assertAgentMapper("group", "2", "New Group", format.parse(responseConverter["creationDate"]), "This is a observation", true, responseConverter["id"])
    }

    @Test
    override fun cantCreateVertexThatExist() {
        val properties: List<Property> = listOf(Property("code", "1"), Property("name", "Test"))
        val vertex = VertexData("group", properties)
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@GCVE-002 Adding this property for key [code] and value [1] violates a uniqueness constraint [vByGroupCode]", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAgentMapper("group", "1", "Marketing", date, "This is a Marketing Group", true)
    }

    @Test
    override fun cantCreateVertexWithRequiredPropertyEmpty() {
        val code: List<Property> = listOf(Property("code", "2"))
        val group = VertexData("group", code)
        var converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, group)
        var response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@GCVE-001 Empty Group properties", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("group").has("code", "2").hasNext())
        val name: List<Property> = listOf(Property("name", "test"))
        val group1 = VertexData("group", name)
        converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, group1)
        response = stub!!.addVertex(
                AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@GCVE-001 Empty Group properties", response.message)
        Assert.assertFalse(response.hasData())
        Assert.assertFalse(g.V().hasLabel("group").has("code", "2").hasNext())
    }

    @Test
    override fun updateProperty() {
        val properties : List<Property> = listOf(Property("name", "Group Test"), Property("observation", "Property updated"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(id).setLabel("group").addAllProperty(converter).build())
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAgentVertexGrpcResponse("group", id, "1", "Group Test", date, "Property updated", true, response)
        this.assertAgentMapper("group", "1", "Group Test", date, "Property updated", true , id.toString())
    }

    @Test
    override fun cantUpdateDefaultProperty() {
        val properties : List<Property> = listOf(Property("name", "Group Test"), Property("code", "2"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(id).setLabel("group").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@GUPE-002 Group property can be updated", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAgentMapper("group", "1", "Marketing", date, "This is a Marketing Group", true, id.toString())
    }

    @Test
    override fun cantUpdatePropertyFromVertexThatNotExist() {
        val properties : List<Property> = listOf(Property("name", "Group Test"), Property("enable", "false"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(1).setLabel("group").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@GUPE-001 Impossible find Group with id 1", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("group").has("name", "Group Test")
                .has("enable", false).hasId(1).hasNext())

    }

    @Test
    override fun deleteVertex() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(id).setLabel("group").build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertFalse(response.hasData())
        this.assertAgentMapper("group", "1", "Marketing", date, "This is a Marketing Group", false, id.toString())
    }

    @Test
    override fun cantDeleteVertexThatNotExist() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(id+1).setLabel("group").build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@GDE-001 Impossible find Group with id ${id+1}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("group").hasId(id+1).hasNext())
    }
}