package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.User
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.validator.UserValidator
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse
import org.apache.tinkerpop.gremlin.structure.Edge

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


    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.EdgeResponse {
        if (!UserValidator().isCorrectVertexTarget(target)) {
            return ProtoResponse.createEdgeErrorResponse(
                    "@UCEE-001 Impossible create edge with target id ${target.id}"
            )
        }
        val userStorage = UserValidator().hasVertex(this.user.id)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@UCEE-002 Impossible find User with id ${this.user.id}"
                )

        val accessRuleStorage = UserValidator().hasVertexTarget(target)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@UCEE-003 Impossible find Access Rule with id ${target.id}"
                )
        val edge: Edge
        try {
            edge = userStorage.addEdge(EdgeLabel.ASSOCIATED.label, accessRuleStorage)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createEdgeErrorResponse("@UCEE-003 ${e.message.toString()}")
        }
        val response = EdgeData(VertexInfo(this.user.id, VertexLabel.USER.label), target, EdgeLabel.ASSOCIATED.label,
                listOf(Property(PropertyLabel.ID.label, edge.id().toString())))
        return ProtoResponse.createEdgeSuccessResponse(response)
    }

    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val user = UserValidator().hasVertex(this.user.id)
                ?: return ProtoResponse.createVertexErrorResponse(
                        "@UUPE-001 Impossible find User with id ${this.user.id}"
                )

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
                ?: return ProtoResponse.createVertexErrorResponse(
                        "@UDE-001 Impossible find User with id ${this.user.id}"
                )
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