package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.User
import br.com.ntopus.accesscontrol.vertex.validator.UserValidator
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.proto.ProtoVertexResponse

class UserMapper (val properties: Map<String, String>): IMapper {

    private val user = User(properties)

    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        try {
            if (!UserValidator().canInsertVertex(this.user)) {
                return ProtoVertexResponse.createErrorResponse("@UCVE-001 Empty User properties")
//                return ProtoVertexResponseError(message = "@UCVE-001 Empty User properties")
            }
            val user = graph.addVertex(VertexLabel.USER.label)
            user.property(PropertyLabel.NAME.label, this.user.name)
            user.property(PropertyLabel.CODE.label, this.user.code)
            if (!this.user.observation.isEmpty()){
                user.property(PropertyLabel.OBSERVATION.label, this.user.observation)
            }
            user.property(PropertyLabel.CREATION_DATE.label, this.user.creationDate)
            user.property(PropertyLabel.ENABLE.label, this.user.enable)
            graph.tx().commit()
            this.user.id = user.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
//            return ProtoVertexResponseError(message = "@UCVE-002 ${e.message.toString()}")
            return ProtoVertexResponse.createErrorResponse("@UCVE-002 ${e.message.toString()}")
        }
        val vertexData = VertexData(VertexLabel.USER.label, this.userMapperFromVertexData())
        return ProtoVertexResponse.createSuccessResponse(vertexData)
    }

    private fun userMapperFromVertexData(): List<Property> {
        var list: List<Property> = listOf()
        list+= Property(PropertyLabel.ID.label, this.user.id.toString())
        list+= Property(PropertyLabel.CODE.label, this.user.code)
        list+= Property(PropertyLabel.CREATION_DATE.label, this.user.formatDate())
        list+= Property(PropertyLabel.OBSERVATION.label, this.user.observation)
        list+= Property(PropertyLabel.ENABLE.label, this.user.enable.toString())
        return list
    }
//
//    override fun createEdge(target: VertexInfo, edgeTarget: String): JSONResponse {
//        if (!UserValidator().isCorrectVertexTarget(target)) {
//            return FAILResponse(data = "@UCEE-001 Impossible create edge with target code ${target.code}")
//        }
//        val userStorage = UserValidator().hasVertex(this.user.code)
//                ?: return FAILResponse(data = "@UCEE-002 Impossible find User with code ${this.user.code}")
//
//        val accessRuleStorage = UserValidator().hasVertexTarget(target)
//                ?: return FAILResponse(data ="@UCEE-003 Impossible find Access Rule with code ${target.code}")
//        try {
//            userStorage.addEdge(EdgeLabel.ASSOCIATED.label, accessRuleStorage)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@UCEE-003 ${e.message.toString()}")
//        }
//        val response = EdgeCreated(VertexInfo(VertexLabel.USER.label, this.user.code), target, EdgeLabel.ASSOCIATED.label)
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun updateProperty(properties: List<Property>): JSONResponse {
//        val user = UserValidator().hasVertex(this.user.code)
//                ?: return FAILResponse(data = "@UUPE-001 Impossible find User with code ${this.user.code}")
//
//        if (!UserValidator().canUpdateVertexProperty(properties)) {
//            return FAILResponse(data = "@UUPE-002 User property can be updated")
//        }
//
//        try {
//            for (property in properties) {
//                user.property(property.name, property.value)
//            }
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@UUPE-004 ${e.message.toString()}")
//        }
//        val traversal = graph.traversal().V().
//                hasLabel(VertexLabel.USER.label).has(PropertyLabel.CODE.label, this.user.code).next()
//        val values = AbstractMapper.parseMapVertex(traversal)
//        val response = AgentResponse(
//                user.id() as Long,
//                this.user.code,
//                AbstractMapper.parseMapValue(values[PropertyLabel.NAME.label].toString()),
//                AbstractMapper.parseMapValueDate(values[PropertyLabel.CREATION_DATE.label].toString())!!,
//                AbstractMapper.parseMapValue(values[PropertyLabel.ENABLE.label].toString()).toBoolean(),
//                AbstractMapper.parseMapValue((values[PropertyLabel.OBSERVATION.label].toString()))
//        )
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun delete(): JSONResponse {
//        val user = UserValidator().hasVertex(this.user.code)
//                ?: return FAILResponse(data = "@UDE-001 Impossible find User with code ${this.user.code}")
//        try {
//            user.property(PropertyLabel.ENABLE.label, false)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@UDE-002 ${e.message.toString()}")
//        }
//        return SUCCESSResponse(data = null)
//    }
}