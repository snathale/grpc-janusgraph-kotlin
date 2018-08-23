//package br.com.ntopus.accesscontrol.model.vertex.mapper.graph
//
//import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
//import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
//
//class AccessGroupEdgeFactory {
//    fun edgeForTarget(target: VertexInfo, edgeLabel: String): ICreateEdge? {
//        return when (target.label) {
//            VertexLabel.ACCESS_GROUP.label -> EdgeFromAccessGroup()
//            VertexLabel.RULE.label -> EdgeFromRule(edgeLabel)
//            else -> null
//        }
//    }
//}