package br.com.ntopus.accesscontrol.server.helper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
import net.badata.protobuf.converter.Converter
import org.junit.Assert
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals

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

//    fun assertEdgeCreatedSuccess(source: VertexInfo, target: VertexInfo, response: CreateEdgeSuccess, edgeLabel: String) {
//        Assert.assertEquals("SUCCESS", response.status)
//        Assert.assertEquals(source.label, response.data.source.label)
//        Assert.assertEquals(source.code, response.data.source.code)
//        Assert.assertEquals(target.label, response.data.target.label)
//        Assert.assertEquals(target.code, response.data.target.code)
//        Assert.assertEquals(edgeLabel, response.data.edgeLabel)
//    }
//
//    fun assertUserMapper(code: String, name: String, creationDate: Date, observation: String, enable: Boolean) {
//        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
//        val g = GraphFactory.open().traversal()
//        val userStorage = g.V().hasLabel("user").has("code", code).next()
//        val values = AbstractMapper.parseMapVertex(userStorage)
//        Assert.assertEquals(name, AbstractMapper.parseMapValue(values["name"].toString()))
//        Assert.assertEquals(code, AbstractMapper.parseMapValue(values["code"].toString()))
//        Assert.assertEquals(observation, AbstractMapper.parseMapValue(values["observation"].toString()))
//        Assert.assertEquals(format.format(creationDate), AbstractMapper.parseMapValueDate(values["creationDate"].toString()))
//        Assert.assertEquals(enable, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
//    }

    fun assertAccessRuleVertexGrpcResponse(code: String, enable: Boolean, expirationDate: Date, response: AccessControlServer.VertexResponse, id: Long? = null) {
        val properties= Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
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
        if (expirationDate!=null){
            Assert.assertEquals(format.format(expirationDate), AbstractMapper.parseMapValueDate(values["expirationDate"].toString()))
        }
        if (id!=null) {
            Assert.assertEquals(id.toString(), AbstractMapper.parseMapValue(values["id"].toString()))
        }
        Assert.assertEquals(code, AbstractMapper.parseMapValue(values["code"].toString()))
        Assert.assertEquals(enable, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
    }
//
//
//    fun assertAccessGroupResponseSuccess(code: String, name: String, enable: Boolean, creationDate: Date, description: String, response: CreatePermissionSuccess) {
//        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
//        val objDate = format.parse(response.data.creationDate)
//        Assert.assertEquals("SUCCESS", response.status)
//        Assert.assertEquals(format.format(creationDate), format.format(objDate))
//        Assert.assertEquals(code, response.data.code)
//        Assert.assertEquals(name, response.data.name)
//        Assert.assertEquals(enable, response.data.enable)
//        Assert.assertEquals(description, response.data.description)
//    }
//
//    fun assertRuleMapper(code: String, name: String, description: String, creationDate: Date, enable: Boolean) {
//        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
//        val g = GraphFactory.open().traversal()
//        val vertex = g.V().hasLabel("rule").has("code", code).next()
//        val values = AbstractMapper.parseMapVertex(vertex)
//        Assert.assertEquals(format.format(creationDate), AbstractMapper.parseMapValueDate(values["creationDate"].toString()))
//        Assert.assertEquals(name, AbstractMapper.parseMapValue(values["name"].toString()))
//        Assert.assertEquals(code, AbstractMapper.parseMapValue(values["code"].toString()))
//        Assert.assertEquals(description, AbstractMapper.parseMapValue(values["description"].toString()))
//        Assert.assertEquals(enable, AbstractMapper.parseMapValue(values["enable"].toString()).toBoolean())
//    }

    fun assertAgentMapper(label: String,  code: String, name: String, creationDate: Date, observation: String, enable: Boolean, id:String? = null) {
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

    fun assertPermissionMapper(label: String, code: String, name: String, creationDate: Date, description: String, enable: Boolean, id:String? = null) {
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
        assertEquals(label, responseConverter.label)
        assertEquals(id.toString(), properties["id"])
        assertEquals(name, properties["name"])
        assertEquals(code, properties["code"])
        assertEquals(description, properties["description"])
        assertEquals(enable, properties["enable"]!!.toBoolean())
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        assertEquals(format.format(creationDate), (properties["creationDate"]))
    }

    fun assertAgentVertexGrpcResponse(label: String, id: Long, code: String, name: String, creationDate: Date, observation: String, enable: Boolean, response: AccessControlServer.VertexResponse) {
        val responseConverter = Converter.create().toDomain(VertexData::class.java, response.data).properties.map { it.name to it.value }.toMap()
        assertEquals(label, response.data.label)
        assertEquals(id.toString(), responseConverter["id"])
        assertEquals(name, responseConverter["name"])
        assertEquals(code, responseConverter["code"])
        assertEquals(observation, responseConverter["observation"])
        assertEquals(enable, responseConverter["enable"]!!.toBoolean())
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        assertEquals(format.format(creationDate), (responseConverter["creationDate"]))
    }
}