//package br.com.ntopus.accesscontrol.model.vertex.mapper.graph
//
//import br.com.ntopus.accesscontrol.vertex.data.EdgeLabel
//import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
//import br.com.ntopus.accesscontrol.vertex.base.FAILResponse
//import br.com.ntopus.accesscontrol.vertex.base.JSONResponse
//import br.com.ntopus.accesscontrol.vertex.base.SUCCESSResponse
//import br.com.ntopus.accesscontrol.vertex.mapper.EdgeCreated
//import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
//import org.apache.tinkerpop.gremlin.structure.Vertex
//
//class EdgeFromRule(val edgeLabel: String = EdgeLabel.ADD.label): ICreateEdge() {
//    override fun createEdge(vSource: Vertex, vTarget: Vertex, target: VertexInfo, sourceCode: String): JSONResponse {
//        if (this.toString(edgeLabel).isEmpty()) {
//            return this.createAddEdgeFromRule(vSource, vTarget, target, sourceCode)
//        }
//        return when(this.toString(edgeLabel)) {
//            EdgeLabel.REMOVE.label -> this.createRemoveEdgeFromRule(vSource, vTarget, target, sourceCode)
//            EdgeLabel.ADD.label ->  this.createAddEdgeFromRule(vSource, vTarget, target, sourceCode)
//            else -> FAILResponse(
//                    data = "@AGCEE-005 Impossible create a edge from Access Group with code $sourceCode"
//            )
//        }
//    }
//
//    private fun createAddEdgeFromRule(vSource: Vertex, vTarget: Vertex, target: VertexInfo, sourceCode: String): JSONResponse {
//        try {
//            vSource.addEdge(EdgeLabel.ADD.label, vTarget)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@AGCEE-003 ${e.message.toString()}")
//        }
//        val source = VertexInfo(VertexLabel.ACCESS_GROUP.label, sourceCode)
//        return SUCCESSResponse(data = EdgeCreated(source, target, EdgeLabel.ADD.label))
//    }
//
//    private fun createRemoveEdgeFromRule(vSource: Vertex, vTarget: Vertex, target: VertexInfo, sourceCode: String): JSONResponse {
//        try {
//            vSource.addEdge(EdgeLabel.REMOVE.label, vTarget)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@AGCEE-004 ${e.message.toString()}")
//        }
//        val source = VertexInfo(VertexLabel.ACCESS_GROUP.label, sourceCode)
//        return SUCCESSResponse(data = EdgeCreated(source, target, EdgeLabel.REMOVE.label))
//    }
//
//    private fun toString(value: Any?): String {
//        if (value.toString() == "null") {
//            return ""
//        }
//        return value.toString()
//    }
//}