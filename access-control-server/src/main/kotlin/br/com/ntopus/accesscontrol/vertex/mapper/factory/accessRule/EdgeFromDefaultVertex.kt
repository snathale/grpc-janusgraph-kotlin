package br.com.ntopus.accesscontrol.vertex.mapper.factory.accessRule

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.mapper.factory.ICreateEdge
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex

class EdgeFromDefaultVertex: ICreateEdge()  {
    override fun createEdge(vSource: Vertex, vTarget: Vertex, target: VertexInfo): AccessControlServer.EdgeResponse {
        val edge: Edge
        try {
            edge = vSource.addEdge(EdgeLabel.PROVIDE.label, vTarget)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createEdgeErrorResponse("@ARCEE-004 ${e.message.toString()}")
        }
        val source = VertexInfo(vSource.id() as Long, VertexLabel.ACCESS_RULE.label)
        val property = listOf(Property(PropertyLabel.ID.label, edge.id().toString()))
        return ProtoResponse.createEdgeSuccessResponse(EdgeData(source, target, EdgeLabel.PROVIDE.label, property))
    }
}