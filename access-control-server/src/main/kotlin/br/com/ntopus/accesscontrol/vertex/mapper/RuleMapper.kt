package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.model.vertex.validator.RuleValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.Rule
import br.com.ntopus.accesscontrol.vertex.proto.ProtoVertexResponse

class RuleMapper (val properties: Map<String, String>): IMapper {
    private val rule = Rule(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        if (!RuleValidator().canInsertVertex(this.rule)) {
            return ProtoVertexResponse.createErrorResponse("@RCVE-001 Empty Rule properties")
        }
        try {
            val rule = graph.addVertex(VertexLabel.RULE.label)
            rule.property(PropertyLabel.NAME.label, this.rule.name)
            rule.property(PropertyLabel.CODE.label, this.rule.code)
            rule.property(PropertyLabel.CREATION_DATE.label, this.rule.creationDate)
            rule.property(PropertyLabel.ENABLE.label, this.rule.enable)
            if (!this.rule.description.isEmpty()) {
                rule.property(PropertyLabel.DESCRIPTION.label, this.rule.description)
            }
            graph.tx().commit()
            this.rule.id = rule.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoVertexResponse.createErrorResponse("@RCVE-002 ${e.message.toString()}")
        }
        val response = PermissionResponse(
                this.rule.id!!,
                this.rule.code, this.rule.name, this.rule.formatDate(), this.rule.description, this.rule.enable
        )
        return ProtoVertexResponse.createSuccessResponse(this.rule.mapperToVertexData())
    }

//    override fun createEdge(target: VertexInfo, edgeTarget: String): JSONResponse {
//        return FAILResponse(data = "@RCEE-001 Impossible create a edge with target code ${target.code}")
//    }
//
//    override fun updateProperty(properties: List<Property>): JSONResponse {
//        val rule = RuleValidator().hasVertex(this.rule.code)
//                ?: return FAILResponse(data = "RUPE-001 Impossible find Rule with code ${this.rule.code}")
//
//        if (!RuleValidator().canUpdateVertexProperty(properties)) {
//            return FAILResponse(data = "@RUPE-002 Rule property can be updated")
//        }
//        try {
//            for (property in properties) {
//                rule.property(property.name, property.value)
//            }
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@RUPE-003 ${e.message.toString()}")
//        }
//        val traversal = graph.traversal().V().hasLabel(VertexLabel.RULE.label)
//                .has(PropertyLabel.CODE.label, this.rule.code).next()
//        val values = AbstractMapper.parseMapVertex(traversal)
//        val response = PermissionResponse(
//                rule.id() as Long,
//                this.rule.code,
//                AbstractMapper.parseMapValue(values[PropertyLabel.NAME.label].toString()),
//                AbstractMapper.parseMapValueDate(values[PropertyLabel.CREATION_DATE.label].toString())!!,
//                AbstractMapper.parseMapValue((values[PropertyLabel.DESCRIPTION.label].toString())),
//                AbstractMapper.parseMapValue(values[PropertyLabel.ENABLE.label].toString()).toBoolean()
//        )
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun delete(): JSONResponse {
//        val rule = RuleValidator().hasVertex(this.rule.code)
//                ?: return FAILResponse(data = "@RDE-001 Impossible find Rule with code ${this.rule.code}")
//        try {
//            rule.property(PropertyLabel.ENABLE.label, false)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@RDE-002 ${e.message.toString()}")
//        }
//        return SUCCESSResponse(data = null)
//    }

}