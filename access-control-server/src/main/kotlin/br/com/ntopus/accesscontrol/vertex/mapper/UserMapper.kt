package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.User
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.validator.UserValidator
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse

class UserMapper(val properties: Map<String, String>) : IMapper {
    private val user = User(properties)

    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        try {
            if (!UserValidator().canInsertVertex(this.user)) {
                return ProtoResponse.createVertexErrorResponse("@UCVE-001 Empty User properties")
            }
            val user = graph.addVertex(VertexLabel.USER.label)
            user.property(PropertyLabel.NAME.label, this.user.name)
            user.property(PropertyLabel.CODE.label, this.user.code)
            if (!this.user.observation.isEmpty()) {
                user.property(PropertyLabel.OBSERVATION.label, this.user.observation)
            }
            user.property(PropertyLabel.CREATION_DATE.label, this.user.creationDate)
            user.property(PropertyLabel.ENABLE.label, this.user.enable)
            graph.tx().commit()
            this.user.id = user.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@UCVE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(this.user.mapperToVertexData(VertexLabel.USER.label))
    }


    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.VertexResponse {
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
        return ProtoResponse.createVertexSuccessResponse()
    }

    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val user = UserValidator().hasVertex(this.user.id)
                ?: return ProtoResponse.createVertexErrorResponse("@UUPE-001 Impossible find User with id ${this.user.id}")

        if (!UserValidator().canUpdateVertexProperty(properties)) {
            return ProtoResponse.createVertexErrorResponse("@UUPE-002 User property can be updated")
        }

        try {
            for (property in properties) {
                user.property(property.name, property.value)
            }
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@UUPE-004 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(user))
    }

    override fun delete(): AccessControlServer.VertexResponse {
        val user = UserValidator().hasVertex(this.user.id)
                ?: return ProtoResponse.createVertexErrorResponse("@UDE-001 Impossible find User with id ${this.user.id}")
        try {
            user.property(PropertyLabel.ENABLE.label, false)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@UDE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse()
    }
}