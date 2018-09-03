package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.validator.RuleValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.Rule
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.VertexInfo
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse

class RuleMapper (val properties: Map<String, String>): IMapper {

    private val rule = Rule(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        if (!RuleValidator().canInsertVertex(this.rule)) {
            return ProtoResponse.createVertexErrorResponse("@RCVE-001 Empty Rule properties")
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
            return ProtoResponse.createVertexErrorResponse("@RCVE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(this.rule.mapperToVertexData(VertexLabel.RULE.label))
    }

    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.EdgeResponse {
        return ProtoResponse.createEdgeErrorResponse("@RCEE-001 Impossible create a edge with target id ${this.rule.id}")
    }


    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val rule = RuleValidator().hasVertex(this.rule.id)
                ?: return ProtoResponse.createVertexErrorResponse(
                        "RUPE-001 Impossible find Rule with id ${this.rule.id}"
                )

        if (!RuleValidator().canUpdateVertexProperty(properties)) {
            return ProtoResponse.createVertexErrorResponse("@RUPE-002 Rule property can be updated")
        }
        try {
            for (property in properties) {
                rule.property(property.name, property.value)
            }
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@RUPE-003 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(rule))
    }

    override fun delete(): AccessControlServer.VertexResponse {
        val rule = RuleValidator().hasVertex(this.rule.id)
                ?: return ProtoResponse.createVertexErrorResponse(
                        "@RDE-001 Impossible find Rule with id ${this.rule.id}"
                )
        try {
            rule.property(PropertyLabel.ENABLE.label, false)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@RDE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse()
    }

}