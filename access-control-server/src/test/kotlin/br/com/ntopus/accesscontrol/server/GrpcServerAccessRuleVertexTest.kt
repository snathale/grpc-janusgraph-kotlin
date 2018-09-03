package br.com.ntopus.accesscontrol.server

import br.com.ntopus.accesscontrol.AccessControlService
import br.com.ntopus.accesscontrol.VertexByCode
import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.importer.JanusGraphSchemaImporter
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.proto.AccessControlServiceGrpc
import br.com.ntopus.accesscontrol.server.helper.GrpcServerTestHelper
import br.com.ntopus.accesscontrol.server.helper.IVertexTests
import br.com.ntopus.accesscontrol.vertex.data.EdgeData
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.data.VertexInfo
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import net.badata.protobuf.converter.Configuration
import net.badata.protobuf.converter.Converter
import net.badata.protobuf.converter.FieldsIgnore
import org.apache.tinkerpop.gremlin.structure.Direction
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class GrpcServerAccessRuleVertexTest: GrpcServerTestHelper(), IVertexTests {
    @get: Rule
    val grpcCleanup: GrpcCleanupRule = GrpcCleanupRule()

    var stub: AccessControlServiceGrpc.AccessControlServiceBlockingStub? = null

    private val date: Date = Date()

    private var accessRuleId: Long = 0

    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    private var groupId: Long = 0

    private var accessGroupId: Long = 0

    private var organizationId: Long = 0

    private var unitOrganizationId: Long = 0

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
        this.organizationId = this.createDefaultOrganization(Date())!!
        this.unitOrganizationId = this.createDefaultUnitOrganization(Date())!!
        this.groupId = this.createDefaultGroup(Date())!!
        this.accessGroupId = this.createDefaultAccessGroup(Date())!!
        this.accessRuleId = this.createDefaultAccessRule(date)!!
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
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(accessRuleId).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAccessRuleVertexGrpcResponse("1", true, date, response, accessRuleId)
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
        this.assertAccessRuleVertexGrpcResponse("1", true, date, response, accessRuleId)
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
        this.assertAccessRuleMapper("1", true, date, accessRuleId)
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
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(accessRuleId).setLabel("accessRule").addAllProperty(converter).build())
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertAccessRuleVertexGrpcResponse("1", true, expirationDate, response)
        this.assertAccessRuleMapper("1", true, expirationDate, accessRuleId)
    }

    @Test
    override fun cantUpdateDefaultProperty() {
        val properties : List<Property> = listOf(Property("name", "Group Test"), Property("code", "2"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(accessRuleId).setLabel("accessRule").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARUPE-002 Access Rule property can be updated", response.message)
        Assert.assertFalse(response.hasData())
        this.assertAccessRuleMapper("1", true, date, accessRuleId)
    }

    @Test
    override fun cantUpdatePropertyFromVertexThatNotExist() {
        val g = GraphFactory.open().traversal()
        val properties : List<Property> = listOf(Property("enable", "false"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(accessRuleId+1).setLabel("accessRule").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARUPE-001 Impossible find Access Rule with id ${accessRuleId+1}", response.message)
        Assert.assertFalse(response.hasData())
        Assert.assertFalse(g.V().hasLabel("accessRule").hasId(accessRuleId+1).has("enable", "false").hasNext())
    }

    @Test
    override fun deleteVertex() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(accessRuleId).setLabel("accessRule").build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertFalse(response.hasData())
        this.assertAccessRuleMapper("1", false, date, accessRuleId)
    }

    @Test
    override fun cantDeleteVertexThatNotExist() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(accessRuleId+1).setLabel("accessRule").build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARDE-001 Impossible find Access Rule with id ${accessRuleId+1}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("accessRule").hasId(accessRuleId+1).hasNext())
    }

    @Test
    override fun cantCreateEdgeWithSourceThatNotExist() {
        val source = VertexInfo(this.accessRuleId+1, "accessRule")
        val target = VertexInfo(this.groupId, "group")
        val edge = EdgeData(source, target)
        val ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARCEE-002 Impossible find Access Rule with id ${source.id}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("accessRule").hasId(source.id).hasNext())
    }

    @Test
    override fun cantCreateEdgeWithTargetThatNotExist() {
        val source = VertexInfo(this.accessRuleId, "accessRule")
        val target = VertexInfo(this.accessGroupId+1,"accessGroup")
        val edge = EdgeData(source, target)
        val ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARCEE-003 Impossible find AccessGroup with id ${target.id}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V(target.id).hasLabel("accessGroup").hasNext())
    }

    @Test
    override fun cantCreateEdgeWithIncorrectTarget() {
        val user = this.createDefaultUser(Date())!!
        val source = VertexInfo(this.accessRuleId, "accessRule")
        val target = VertexInfo(user,"user")
        val edge = EdgeData(source, target)
        val ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@ARCEE-001 Impossible create this edge with target id ${target.id}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V(accessRuleId).hasLabel("user").both().hasNext())
    }

    @Test
    override fun createEdge() {
        val source = VertexInfo(this.accessRuleId, "accessRule")
        val target = VertexInfo(this.accessGroupId, "accessGroup")
        val edge = EdgeData(source, target)
        val ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertEdgeCreatedSuccess(source, target,"own", response)
        this.assertHasEdge(source, target, "own")
    }

    @Test
    fun createProvideEdge() {
        val source = VertexInfo(this.accessRuleId,"accessRule")
        val targets = listOf(
                VertexInfo(this.organizationId,"organization"),
                VertexInfo(this.unitOrganizationId,"unitOrganization"),
                VertexInfo(this.groupId,"group")
        )
        for (target in targets) {
            val edge = EdgeData(source, target)
            val ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
            val config = Configuration.builder().addIgnoredFields(ignore).build()
            val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
            val response = stub!!.addEdge(
                    AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
            )
            assertEquals("success", response.status)
            assertEquals("", response.message)

            this.assertEdgeCreatedSuccess(source, target,"provide", response)
            this.assertHasEdge(source, target, "provide")
        }
    }
}