package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.AccessRule
import br.com.ntopus.accesscontrol.vertex.validator.AccessRuleValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.proto.ProtoVertexResponse
import java.text.SimpleDateFormat

class AccessRuleMapper (val properties: Map<String, String>): IMapper {
    private val accessRule = AccessRule(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        if (!AccessRuleValidator().canInsertVertex(this.accessRule)) {
            return ProtoVertexResponse.createErrorResponse("@ARCVE-001 Empty Access Rule properties")
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
            return ProtoVertexResponse.createErrorResponse("@ARCVE-002 ${e.message.toString()}")
        }
        return ProtoVertexResponse.createSuccessResponse(this.accessRule.mapperToVertexData())
    }

    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val accessRule = AccessRuleValidator().hasVertex(this.accessRule.id!!)
                ?: return ProtoVertexResponse.createErrorResponse("@ARUPE-001 Impossible find Access Rule with code ${this.accessRule.code}")
        if (!AccessRuleValidator().canUpdateVertexProperty(properties)) {
            return ProtoVertexResponse.createErrorResponse("@ARUPE-002 Access Rule property can be updated")
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
            return ProtoVertexResponse.createErrorResponse("@ARUPE-002 ${e.message.toString()}")
        }
        return ProtoVertexResponse.createSuccessResponse(AbstractMapper.parseVertexToVertexData(accessRule))
    }

    override fun delete(): AccessControlServer.VertexResponse {
        val accessRule = AccessRuleValidator().hasVertex(this.accessRule.id!!)
                ?: return ProtoVertexResponse.createErrorResponse("@ARDE-001 Impossible find Access Rule with code ${this.accessRule.code}")
        try {
            accessRule.property(PropertyLabel.ENABLE.label, false)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoVertexResponse.createErrorResponse("@ARDE-002 ${e.message.toString()}")
        }
        return ProtoVertexResponse.createSuccessResponse()
    }

//    override fun createEdge(target: VertexInfo, edgeTarget: String): JSONResponse {
//        if (!AccessRuleValidator().isCorrectVertexTarget(target)) {
//            return FAILResponse(data = "@ARCEE-001 Impossible create this edge with target code ${target.code}")
//        }
//        val vAccessGroup = AccessRuleValidator().hasVertex(this.accessRule.code)
//                ?: return FAILResponse(data = "@ARCEE-002 Impossible find Access Rule with code ${this.accessRule.code}")
//        val vTarget = AccessRuleValidator().hasVertexTarget(target)
//                ?: return FAILResponse(data = "@ARCEE-003 Impossible find ${target.label.capitalize()} with code ${target.code}")
//        return when(target.label) {
//            VertexLabel.ORGANIZATION.label,
//            VertexLabel.UNIT_ORGANIZATION.label,
//            VertexLabel.GROUP.label-> this.createProvideEdge(vAccessGroup, vTarget, target)
//            VertexLabel.ACCESS_GROUP.label -> this.createOwnEdge(vAccessGroup, vTarget, target)
//            else -> FAILResponse(data = "@ARCEE-006 Impossible create a edge from Access Rule with code ${this.accessRule.code}")
//        }
//    }
//
//    private fun createProvideEdge(vSource: Vertex, vTarget: Vertex, target: VertexInfo): JSONResponse {
//        try {
//            vSource.addEdge(EdgeLabel.PROVIDE.label, vTarget)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@ARCEE-004 ${e.message.toString()}")
//        }
//        val source = VertexInfo(VertexLabel.ACCESS_RULE.label, this.accessRule.code)
//        return SUCCESSResponse(data = EdgeCreated(source, target, EdgeLabel.PROVIDE.label))
//    }
//
//    private fun createOwnEdge(vSource: Vertex, vTarget: Vertex, target: VertexInfo): JSONResponse {
//        try {
//            vSource.addEdge(EdgeLabel.OWN.label,vTarget)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@ARCEE-005 ${e.message.toString()}")
//        }
//        val source = VertexInfo(VertexLabel.ACCESS_RULE.label, this.accessRule.code)
//        return SUCCESSResponse(data = EdgeCreated(source, target, EdgeLabel.OWN.label))
//    }
}