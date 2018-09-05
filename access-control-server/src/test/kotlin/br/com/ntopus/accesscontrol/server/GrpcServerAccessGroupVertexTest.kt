package br.com.ntopus.accesscontrol.server

import br.com.ntopus.accesscontrol.AccessControlService
import br.com.ntopus.accesscontrol.VertexByCode
import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.importer.JanusGraphSchemaImporter
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.proto.AccessControlServiceGrpc
import br.com.ntopus.accesscontrol.server.helper.GrpcServerTestHelper
import br.com.ntopus.accesscontrol.server.helper.IVertexTests
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import net.badata.protobuf.converter.Configuration
import net.badata.protobuf.converter.Converter
import net.badata.protobuf.converter.FieldsIgnore
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

    private var accessGroupId: Long = 0

    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    private var ruleId: Long = 0

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
        this.accessGroupId = this.createDefaultAccessGroup(date)!!
        this.ruleId = this.createDefaultRules(Date())!!

    }

    @Test
    override fun addVertex() {
        val properties: List<Property> = listOf(Property("code", "2"),
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
                "This is a description", false, propertiesMap["accessGroupId"])
    }

    @Test
    override fun getVertexById() {
        val response = stub!!.getVertexById(
                AccessControlServer.GetVertexByIdRequest.newBuilder().setId(accessGroupId).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse(
                "accessGroup", accessGroupId, "1",
                "Operator", date, "This is a Operator Access Group",
                true, response)
    }

    @Test
    override fun getVertexByCode() {
        val vertex = VertexByCode("accessGroup", "1")
        val converter = Converter.create().toProtobuf(AccessControlServer.GetVertexByCodeRequest::class.java, vertex)
        val response = stub!!.getVertexByCode(
                AccessControlServer.GetVertexByCodeRequest.newBuilder(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse(
                "accessGroup", accessGroupId, "1",
                "Operator", date, "This is a Operator Access Group",
                true, response)
    }

    @Test
    override fun createVertexWithExtraProperty() {
        val properties: List<Property> = listOf(Property("code", "2"),
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
        this.assertPermissionMapper("accessGroup", "1", "Operator", date, "This is a Operator Access Group", true, accessGroupId.toString())
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

    @Test
    override fun updateProperty() {
        val properties: List<Property> = listOf(Property("name", "Operator Updated"), Property("description", "Property updated"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(accessGroupId).setLabel("accessGroup").addAllProperty(converter).build())
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertPermissionVertexGrpcResponse("accessGroup", accessGroupId, "1", "Operator Updated", date, "Property updated", true, response)
        this.assertPermissionMapper("accessGroup", "1", "Operator Updated", date, "Property updated", true, accessGroupId.toString())
    }

    @Test
    override fun cantUpdateDefaultProperty() {

        val properties: List<Property> = listOf(Property("name", "Operator Updated"), Property("code", "2"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(accessGroupId).setLabel("accessGroup").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGUPE-002 Access Group property can be updated", response.message)
        Assert.assertFalse(response.hasData())
        this.assertPermissionMapper("accessGroup", "1", "Operator", date, "This is a Operator Access Group", true, accessGroupId.toString())
    }

    @Test
    override fun cantUpdatePropertyFromVertexThatNotExist() {
        val properties: List<Property> = listOf(Property("name", "Operator Updated"), Property("description", "Property updated"))
        val converter = Converter.create().toProtobuf(AccessControlServer.Property::class.java, properties)
        val response = stub!!.updateVertexProperty(
                AccessControlServer.UpdateVertexPropertyRequest.newBuilder().setId(accessGroupId + 1).setLabel("accessGroup").addAllProperty(converter).build())
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGUPE-001 Impossible find Access Group with id ${accessGroupId + 1}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("accessGroup").has("name", "Operator Updated").has("description", "Property updated").hasNext())
    }

    @Test
    override fun deleteVertex() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(accessGroupId).setLabel("accessGroup").build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertFalse(response.hasData())
        this.assertPermissionMapper("accessGroup", "1", "Operator", date, "This is a Operator Access Group", false, accessGroupId.toString())
    }

    @Test
    override fun cantDeleteVertexThatNotExist() {
        val response = stub!!.deleteVertex(
                AccessControlServer.DeleteVertexRequest.newBuilder().setId(accessGroupId + 1).setLabel("accessGroup").build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGDE-001 Impossible find Access Group with id ${accessGroupId + 1}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V().hasLabel("accessGroup").hasId(accessGroupId + 1).hasNext())
    }

    @Test
    override fun cantCreateEdgeWithSourceThatNotExist() {
        val source = VertexInfo(this.accessGroupId + 1, "accessGroup")
        val target = VertexInfo(this.ruleId, "rule")
        val edge = EdgeData(source, target, "inherit")
        val ignore = FieldsIgnore().add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGCEE-002 Impossible find Access Group with id ${source.id}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V(source.id).hasLabel("accessGroup").hasId(source.id).hasNext())
    }

    @Test
    override fun cantCreateEdgeWithTargetThatNotExist() {
        val source = VertexInfo(this.accessGroupId, "accessGroup")
        val target = VertexInfo(this.ruleId + 1, "rule")
        val edge = EdgeData(source, target, "inherit")
        val ignore = FieldsIgnore().add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGCEE-003 Impossible find Rule with id ${target.id}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V(target.id).hasLabel("rule").hasId(source.id).hasNext())
    }

    @Test
    override fun cantCreateEdgeWithIncorrectTarget() {
        val user = this.createDefaultUser(Date())!!
        val source = VertexInfo(this.accessGroupId, "accessGroup")
        val target = VertexInfo(user, "user")
        val edge = EdgeData(source, target)
        val ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("error", response.status)
        Assert.assertEquals("@AGCEE-001 Impossible create this edge with target id ${target.id}", response.message)
        Assert.assertFalse(response.hasData())
        val g = GraphFactory.open().traversal()
        Assert.assertFalse(g.V(accessGroupId).hasLabel("accessGroup").both().hasNext())
    }

    @Test
    fun createRemoveEdge() {
        val source = VertexInfo(this.accessGroupId, "accessGroup")
        val target = VertexInfo(this.ruleId, "rule")
        val edge = EdgeData(source, target, "remove")
        val ignore = FieldsIgnore().add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        assertEquals("success", response.status)
        assertEquals("", response.message)
        this.assertEdgeCreatedSuccess(source, target, "remove", response)
        this.assertHasEdge(source, target, "remove")
    }

    fun createNewAccessGroup(): Long? {
        val graph = GraphFactory.open()
        return try {
            val unitOrganization = graph.addVertex(VertexLabel.ACCESS_GROUP.label)
            unitOrganization.property(PropertyLabel.NAME.label, "Administrator")
            unitOrganization.property(PropertyLabel.CODE.label, 2)
            unitOrganization.property(PropertyLabel.DESCRIPTION.label, "This is a Admin Access Group")
            unitOrganization.property(PropertyLabel.CREATION_DATE.label, Date())
            unitOrganization.property(PropertyLabel.ENABLE.label, true)
            graph.tx().commit()
            unitOrganization.id() as Long
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    @Test
    fun createInheritEdge() {
        val localAccessGroup = this.createNewAccessGroup()!!
        val source = VertexInfo(this.accessGroupId, "accessGroup")
        val target = VertexInfo(localAccessGroup, "accessGroup")
        val edge = EdgeData(source, target)
        val ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
        val config = Configuration.builder().addIgnoredFields(ignore).build()
        val converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        val response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertEquals("", response.message)
        this.assertEdgeCreatedSuccess(source, target, "inherit", response)
        this.assertHasEdge(source, target, "inherit")
    }

    @Test
    override fun createEdge() {
        var source = VertexInfo(this.accessGroupId, "accessGroup")
        val target = VertexInfo(this.ruleId, "rule")
        var edge = EdgeData(source, target, "add")
        var ignore = FieldsIgnore().add(EdgeData::class.java, "edgeLabel").add(EdgeData::class.java, "properties")
        var config = Configuration.builder().addIgnoredFields(ignore).build()
        var converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        var response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertEquals("", response.message)
        this.assertEdgeCreatedSuccess(source, target, "add", response)
        this.assertHasEdge(source, target, "add")

        val localAccessGroup = this.createNewAccessGroup()!!
        source = VertexInfo(localAccessGroup, "accessGroup")
        ignore = FieldsIgnore().add(EdgeData::class.java, "properties")
        edge = EdgeData(source, target, "add")
        config = Configuration.builder().addIgnoredFields(ignore).build()
        converter = Converter.create(config).toProtobuf(AccessControlServer.Edge::class.java, edge)
        response = stub!!.addEdge(
                AccessControlServer.AddEdgeRequest.newBuilder().setEdge(converter).build()
        )
        Assert.assertEquals("success", response.status)
        Assert.assertEquals("", response.message)
        this.assertEdgeCreatedSuccess(source, target, "add", response)
        this.assertHasEdge(source, target, "add")
    }
}