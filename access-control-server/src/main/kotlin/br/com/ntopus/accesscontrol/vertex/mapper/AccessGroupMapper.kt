package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.validator.AccessGroupValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.AccessGroup
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.VertexInfo
import br.com.ntopus.accesscontrol.vertex.mapper.factory.accessGroup.AccessGroupEdgeFactory
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse

class AccessGroupMapper(val properties: Map<String, String>) : IMapper {

    private val graph = GraphFactory.open()
    private val accessGroup = AccessGroup(properties)

    override fun insert(): AccessControlServer.VertexResponse {
        if (!AccessGroupValidator().canInsertVertex(this.accessGroup)) {
            return ProtoResponse.createVertexErrorResponse("@AGCVE-001 Empty Access Group properties")
        }
        try {
            val accessGroup = graph.addVertex(VertexLabel.ACCESS_GROUP.label)
            accessGroup.property(PropertyLabel.NAME.label, this.accessGroup.name)
            accessGroup.property(PropertyLabel.CODE.label, this.accessGroup.code)
            accessGroup.property(PropertyLabel.CREATION_DATE.label, this.accessGroup.creationDate)
            accessGroup.property(PropertyLabel.ENABLE.label, this.accessGroup.enable)
            if (!this.accessGroup.description.isEmpty()) {
                accessGroup.property(PropertyLabel.DESCRIPTION.label, this.accessGroup.description)
            }
            graph.tx().commit()
            this.accessGroup.id = accessGroup.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@AGCVE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(this.accessGroup.mapperToVertexData(VertexLabel.ACCESS_GROUP.label))
    }

    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.EdgeResponse {
        if (!AccessGroupValidator().isCorrectVertexTarget(target)) {
            return ProtoResponse.createEdgeErrorResponse("@AGCEE-001 Impossible create this edge with target id ${target.id}")
        }
        val vSource = AccessGroupValidator().hasVertex(this.accessGroup.id)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@AGCEE-002 Impossible find Access Group with id ${this.accessGroup.id}"
                )
        val vTarget = AccessGroupValidator().hasVertexTarget(target)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@AGCEE-003 Impossible find ${target.label.capitalize()} with id ${target.id}"
                )
        val edgeForTarget = AccessGroupEdgeFactory.edgeForTarget(target, edgeTarget)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@AGCEE-004 Impossible create a edge from Access Group with id ${this.accessGroup.id}"
                )
        return edgeForTarget.createEdge(vSource, vTarget, target)
    }

    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val accessGroup = AccessGroupValidator().hasVertex(this.accessGroup.id)
                ?: return ProtoResponse.createVertexErrorResponse("@AGUPE-001 Impossible find Access Group with id ${this.accessGroup.id}")
        if (!AccessGroupValidator().canUpdateVertexProperty(properties)) {
            return ProtoResponse.createVertexErrorResponse("@AGUPE-002 Access Group property can be updated")
        }

        try {
            for (property in properties) {
                accessGroup.property(property.name, property.value)
            }
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@AGUPE-003 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(accessGroup))
    }

    override fun delete(): AccessControlServer.VertexResponse {
        val accessGroup = AccessGroupValidator().hasVertex(this.accessGroup.id)
                ?: return ProtoResponse.createVertexErrorResponse("@AGDE-001 Impossible find Access Group with id ${this.accessGroup.id}")
        try {
            accessGroup.property(PropertyLabel.ENABLE.label, false)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse( "@AGDE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse()
    }
}