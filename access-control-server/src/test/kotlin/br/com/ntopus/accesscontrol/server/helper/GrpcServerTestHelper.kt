package br.com.ntopus.accesscontrol.server.helper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
import net.badata.protobuf.converter.Converter
import org.apache.tinkerpop.gremlin.structure.Direction
import org.junit.Assert
import java.text.SimpleDateFormat
import java.util.*

abstract class GrpcServerTestHelper {

    fun createDefaultOrganization(date: Date): Long? {
        val graph = GraphFactory.open()
        return try {
            val organization = graph.addVertex(VertexLabel.ORGANIZATION.label)
            organization.property(PropertyLabel.NAME.label, "Kofre")
            organization.property(PropertyLabel.CODE.label, 1)
            organization.property(PropertyLabel.OBSERVATION.label, "This is a Organization")
            organization.property(PropertyLabel.CREATION_DATE.label, date)
            organization.property(PropertyLabel.ENABLE.label, true)
            graph.tx().commit()
            organization.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    fun createDefaultUnitOrganization(date: Date): Long? {
        val graph = GraphFactory.open()
        return try {
            val unitOrganization = graph.addVertex(VertexLabel.UNIT_ORGANIZATION.label)
            unitOrganization.property(PropertyLabel.NAME.label, "Bahia")
            unitOrganization.property(PropertyLabel.CODE.label, 1)
            unitOrganization.property(PropertyLabel.OBSERVATION.label, "This is a Unit Organization")
            unitOrganization.property(PropertyLabel.CREATION_DATE.label, date)
            unitOrganization.property(PropertyLabel.ENABLE.label, true)
            graph.tx().commit()
            unitOrganization.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    fun createDefaultGroup(date: Date): Long? {
        val graph = GraphFactory.open()
        return try {
            val group = graph.addVertex(VertexLabel.GROUP.label)
            group.property(PropertyLabel.NAME.label, "Marketing")
            group.property(PropertyLabel.CODE.label, 1)
            group.property(PropertyLabel.OBSERVATION.label, "This is a Marketing Group")
            group.property(PropertyLabel.CREATION_DATE.label, date)
            group.property(PropertyLabel.ENABLE.label, true)
            graph.tx().commit()
            group.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    fun createDefaultUser(date: Date): Long? {
        val graph = GraphFactory.open()
        return try {
            val user = graph.addVertex(VertexLabel.USER.label)
            user.property(PropertyLabel.NAME.label, "UserTest")
            user.property(PropertyLabel.CODE.label, 1)
            user.property(PropertyLabel.OBSERVATION.label, "This is UserTest")
            user.property(PropertyLabel.CREATION_DATE.label, date)
            user.property(PropertyLabel.ENABLE.label, true)
            graph.tx().commit()
            user.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    fun createDefaultAccessRule(date: Date): Long? {
        val graph = GraphFactory.open()
        return try {
            val accessRule = graph.addVertex(VertexLabel.ACCESS_RULE.label)
            accessRule.property(PropertyLabel.CODE.label, 1)
            accessRule.property(PropertyLabel.ENABLE.label, true)
            accessRule.property(PropertyLabel.EXPIRATION_DATE.label, date)
            graph.tx().commit()
            accessRule.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    fun createDefaultAccessGroup(date: Date): Long? {
        val graph = GraphFactory.open()
        return try {
            val unitOrganization = graph.addVertex(VertexLabel.ACCESS_GROUP.label)
            unitOrganization.property(PropertyLabel.NAME.label, "Operator")
            unitOrganization.property(PropertyLabel.CODE.label, 1)
            unitOrganization.property(PropertyLabel.DESCRIPTION.label, "This is a Operator Access Group")
            unitOrganization.property(PropertyLabel.CREATION_DATE.label, date)
            unitOrganization.property(PropertyLabel.ENABLE.label, true)
            graph.tx().commit()
            unitOrganization.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    fun createDefaultRules(date: Date): Long? {
        val graph = GraphFactory.open()
        return try {
            val rule1 = graph.addVertex(VertexLabel.RULE.label)
            rule1.property(PropertyLabel.NAME.label, "ADD_USER")
            rule1.property(PropertyLabel.CODE.label, 1)
            rule1.property(PropertyLabel.DESCRIPTION.label, "This is a Rule Add User")
            rule1.property(PropertyLabel.CREATION_DATE.label, date)
            rule1.property(PropertyLabel.ENABLE.label, true)
            val rule2 = graph.addVertex(VertexLabel.RULE.label)
            rule2.property(PropertyLabel.NAME.label, "EDIT_USER")
            rule2.property(PropertyLabel.CODE.label, 2)
            rule2.property(PropertyLabel.DESCRIPTION.label, "This is a Rule Edit User")
            rule2.property(PropertyLabel.CREATION_DATE.label, this.addMinutes(date, 1))
            rule2.property(PropertyLabel.ENABLE.label, true)
            graph.tx().commit()
            rule1.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            null
        }
    }

    fun createDefaultEdge(source: VertexInfo, target: VertexInfo, edgeLabel: String): String? {
        val graph = GraphFactory.open()
        val g = graph.traversal()
        return try {
            val vUser = g.V(source.id).hasLabel(source.label).next()
            val vAccessRule = g.V(target.id).hasLabel(target.label).next()
            val edge = vUser.addEdge(edgeLabel, vAccessRule)
            g.tx().commit()
            edge.id().toString()
        } catch (e: Exception) {
            g.tx().rollback()
            null
        }
    }



    fun addDays(date: Date, days: Int): Date {
        val cal = GregorianCalendar()
        cal.setTime(date)
        cal.add(Calendar.DATE, days)
        return cal.getTime()
    }

    fun addMinutes(date: Date, minutes: Int): Date {
        val cal = GregorianCalendar()
        cal.setTime(date)
        cal.add(Calendar.MINUTE, minutes)
        return cal.getTime()
    }

    fun assertEdgeCreatedSuccess(source: VertexInfo, target: VertexInfo, edgeLabel: String, response: AccessControlServer.EdgeResponse) {
        val properties = Converter.create().toDomain(EdgeData::class.java, response.data).properties!!.map { it.name to it.value }.toMap()
        Assert.assertEquals("success", response.status)
        Assert.assertEquals(source.label, response.data.source.label)
        Assert.assertEquals(source.id, response.data.source.id)
        Assert.assertEquals(target.label, response.data.target.label)
        Assert.assertEquals(target.id, response.data.target.id)
        Assert.assertEquals(edgeLabel, response.data.edgeLabel)
        Assert.assertNotNull(properties["id"])
    }

    fun assertHasEdge(source: VertexInfo, target: VertexInfo, edgeLabel: String) {
        val g = GraphFactory.open().traversal()
        val vSource = g.V(source.id).hasLabel(source.label).next()
        val vTarget = g.V(target.id).hasLabel(target.label).next()
        Assert.assertTrue(vSource.edges(Direction.OUT, edgeLabel).hasNext())
        Assert.assertTrue(vTarget.edges(Direction.IN, edgeLabel).hasNext())
    }

    fun assertAccessRuleVertexGrpcResponse(code: String, enable: Boolean, expirationDate: Date, response: AccessControlServer.VertexResponse, id: Long? = null) {
        val properties = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val objExpirationDate = format.parse(properties["expirationDate"])
        Assert.assertEquals("success", response.status)
        Assert.assertEquals(format.format(expirationDate), format.format(objExpirationDate))
        Assert.assertEquals(code, properties["code"])
        Assert.assertEquals(enable, properties["enable"]!!.toBoolean())
        if (id != null) {
            Assert.assertEquals(id.toString(), properties["id"])
        }
    }

    fun assertAccessRuleMapper(code: String, enable: Boolean, expirationDate: Date? = null, id: Long? = null) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val g = GraphFactory.open().traversal()
        val vertex = g.V().hasLabel("accessRule").has("code", code).next()
        val values = AbstractMapper.parseMapVertex(vertex)
        if (expirationDate != null) {
            Assert.assertEquals(format.format(expirationDate), AbstractMapper.parseMapValueDate(values["expirationDate"].toString()))
        }
        if (id != null) {
            Assert.assertEquals(id.toString(), AbstractMapper.parseMapValue(values["id"].toString()))
        }
        Assert.assertEquals(code, AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals(enable, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
    }

    fun assertAgentMapper(label: String, code: String, name: String, creationDate: Date, observation: String, enable: Boolean, id: String? = null) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val g = GraphFactory.open().traversal()
        val vertex = g.V().hasLabel(label).has("code", code).next()
        val values = AbstractMapper.parseMapVertex(vertex)
        Assert.assertEquals(format.format(creationDate), AbstractMapper.parseMapValueDate(values["creationDate"].toString()))
        Assert.assertEquals(name, AbstractMapper.parseMapValue(values["name"].toString()))
        Assert.assertEquals(code, AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals(observation, AbstractMapper.parseMapValue(values["observation"].toString()))
        Assert.assertEquals(enable, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
        if (id != null) {
            Assert.assertEquals(id, AbstractMapper.parseMapValue(values["id"].toString()))
        }
    }

    fun assertPermissionMapper(label: String, code: String, name: String, creationDate: Date, description: String, enable: Boolean, id: String? = null) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val g = GraphFactory.open().traversal()
        val vertex = g.V().hasLabel(label).has("code", code).next()
        val values = AbstractMapper.parseMapVertex(vertex)
        Assert.assertEquals(format.format(creationDate), AbstractMapper.parseMapValueDate(values["creationDate"].toString()))
        Assert.assertEquals(name, AbstractMapper.parseMapValue(values["name"].toString()))
        Assert.assertEquals(code, AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals(description, AbstractMapper.parseMapValue(values["description"].toString()))
        Assert.assertEquals(enable, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
        if (id != null) {
            Assert.assertEquals(id, AbstractMapper.parseMapValue(values["id"].toString()))
        }
    }

    fun assertPermissionVertexGrpcResponse(label: String, id: Long, code: String, name: String, creationDate: Date, description: String, enable: Boolean, response: AccessControlServer.VertexResponse) {
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data)
        val properties = responseConverter.properties.map { it.name to it.value }.toMap()
        Assert.assertEquals(label, responseConverter.label)
        Assert.assertEquals(id.toString(), properties["id"])
        Assert.assertEquals(name, properties["name"])
        Assert.assertEquals(code, properties["code"])
        Assert.assertEquals(description, properties["description"])
        Assert.assertEquals(enable, properties["enable"]!!.toBoolean())
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        Assert.assertEquals(format.format(creationDate), (properties["creationDate"]))
    }

    fun assertAgentVertexGrpcResponse(label: String, id: Long, code: String, name: String, creationDate: Date, observation: String, enable: Boolean, response: AccessControlServer.VertexResponse) {
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        Assert.assertEquals(label, response.data.label)
        Assert.assertEquals(id.toString(), responseConverter["id"])
        Assert.assertEquals(name, responseConverter["name"])
        Assert.assertEquals(code, responseConverter["code"])
        Assert.assertEquals(observation, responseConverter["observation"])
        Assert.assertEquals(enable, responseConverter["enable"]!!.toBoolean())
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        Assert.assertEquals(format.format(creationDate), (responseConverter["creationDate"]))
    }

    fun assertEdgeGrpcResponse(label: String, id: String, source: VertexInfo, target: VertexInfo, response: AccessControlServer.EdgeResponse) {
        val responseConverter = Converter.create().toDomain(EdgeData::class.java, response.data).properties!!.map { it.name to it.value }.toMap()
        Assert.assertEquals(label, response.data.edgeLabel)
        Assert.assertEquals(id, responseConverter["id"])
        Assert.assertEquals(source.id, response.data.source.id)
        Assert.assertEquals(source.label, response.data.source.label)
        Assert.assertEquals(target.id, response.data.target.id)
        Assert.assertEquals(target.label, response.data.target.label)
    }
}