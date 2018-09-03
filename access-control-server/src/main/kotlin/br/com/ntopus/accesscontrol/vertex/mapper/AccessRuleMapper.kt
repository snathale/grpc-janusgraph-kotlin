package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.AccessRule
import br.com.ntopus.accesscontrol.vertex.validator.AccessRuleValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.mapper.factory.accessRule.AccessRuleEdgeFactory
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse
import org.apache.tinkerpop.gremlin.structure.Vertex
import java.text.SimpleDateFormat

class AccessRuleMapper (val properties: Map<String, String>): IMapper {

    private val accessRule = AccessRule(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        if (!AccessRuleValidator().canInsertVertex(this.accessRule)) {
            return ProtoResponse.createVertexErrorResponse("@ARCVE-001 Empty Access Rule properties")
        }
        try {
            val accessRule = graph.addVertex(VertexLabel.ACCESS_RULE.label)
            accessRule.property(PropertyLabel.CODE.label, this.accessRule.code)
            accessRule.property(PropertyLabel.ENABLE.label, this.accessRule.enable)
            if (this.accessRule.expirationDate != null) {
                accessRule.property(PropertyLabel.EXPIRATION_DATE.label, this.accessRule.expirationDate)
            }
            graph.tx().commit()
            this.accessRule.id = accessRule.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@ARCVE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(this.accessRule.mapperToVertexData())
    }

    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val accessRule = AccessRuleValidator().hasVertex(this.accessRule.id)
                ?: return ProtoResponse.createVertexErrorResponse("@ARUPE-001 Impossible find Access Rule with id ${this.accessRule.id}")
        if (!AccessRuleValidator().canUpdateVertexProperty(properties)) {
            return ProtoResponse.createVertexErrorResponse("@ARUPE-002 Access Rule property can be updated")
        }
        try {
            for (property in properties) {
                if (property.name == PropertyLabel.EXPIRATION_DATE.label) {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    accessRule.property(property.name, format.parse(property.value))
                    continue
                }
                accessRule.property(property.name, property.value)
            }
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@ARUPE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(accessRule))
    }

    override fun delete(): AccessControlServer.VertexResponse {
        val accessRule = AccessRuleValidator().hasVertex(this.accessRule.id)
                ?: return ProtoResponse.createVertexErrorResponse(
                        "@ARDE-001 Impossible find Access Rule with id ${this.accessRule.id}"
                )
        try {
            accessRule.property(PropertyLabel.ENABLE.label, false)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@ARDE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse()
    }

    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.EdgeResponse {
        if (!AccessRuleValidator().isCorrectVertexTarget(target)) {
            return ProtoResponse.createEdgeErrorResponse(
                    "@ARCEE-001 Impossible create this edge with target id ${target.id}"
            )
        }
        val vSource = AccessRuleValidator().hasVertex(this.accessRule.id)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@ARCEE-002 Impossible find Access Rule with id ${this.accessRule.id}"
                )
        val vTarget = AccessRuleValidator().hasVertexTarget(target)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@ARCEE-003 Impossible find ${target.label.capitalize()} with id ${target.id}"
                )
        val edgeForTarget = AccessRuleEdgeFactory.edgeForTarget(target)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@ARCEE-006 Impossible create a edge from Access Rule with id ${this.accessRule.id}"
                )
        return edgeForTarget.createEdge(vSource, vTarget, target)
    }
}