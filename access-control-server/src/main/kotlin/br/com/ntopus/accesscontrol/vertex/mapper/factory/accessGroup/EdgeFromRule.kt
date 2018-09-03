package br.com.ntopus.accesscontrol.vertex.mapper.factory.accessGroup

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.mapper.factory.ICreateEdge
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex

class EdgeFromRule(val edgeLabel: String = EdgeLabel.ADD.label): ICreateEdge() {
    override fun createEdge(vSource: Vertex, vTarget: Vertex, target: VertexInfo): AccessControlServer.EdgeResponse {
        if (this.toString(edgeLabel).isEmpty()) {
            return this.createAddEdgeFromRule(vSource, vTarget, target)
        }
        return when(this.toString(edgeLabel)) {
            EdgeLabel.REMOVE.label -> this.createRemoveEdgeFromRule(vSource, vTarget, target)
            EdgeLabel.ADD.label ->  this.createAddEdgeFromRule(vSource, vTarget, target)
            else -> ProtoResponse.createEdgeErrorResponse(
                    "@AGCEE-005 Impossible create a edge from Access Group with id ${vSource.id()}"
            )
        }
    }

    private fun createAddEdgeFromRule(vSource: Vertex, vTarget: Vertex, target: VertexInfo): AccessControlServer.EdgeResponse {
        val edge : Edge
        try {
            edge = vSource.addEdge(EdgeLabel.ADD.label, vTarget)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createEdgeErrorResponse("@AGCEE-003 ${e.message.toString()}")
        }
        val source = VertexInfo(vSource.id() as Long, VertexLabel.ACCESS_GROUP.label)
        val property = listOf(Property(PropertyLabel.ID.label, edge.id().toString()))
        return ProtoResponse.createEdgeSuccessResponse(EdgeData(source, target, EdgeLabel.ADD.label, property))
    }

    private fun createRemoveEdgeFromRule(vSource: Vertex, vTarget: Vertex, target: VertexInfo): AccessControlServer.EdgeResponse {
        val edge: Edge
        try {
            edge = vSource.addEdge(EdgeLabel.REMOVE.label, vTarget)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createEdgeErrorResponse("@AGCEE-004 ${e.message.toString()}")
        }
        val source = VertexInfo(vSource.id() as Long, VertexLabel.ACCESS_GROUP.label)
        val property = listOf(Property(PropertyLabel.ID.label, edge.id().toString()))
        return ProtoResponse.createEdgeSuccessResponse(EdgeData(source, target, EdgeLabel.REMOVE.label, property))
    }

    private fun toString(value: Any?): String {
        if (value.toString() == "null") {
            return ""
        }
        return value.toString()
    }
}