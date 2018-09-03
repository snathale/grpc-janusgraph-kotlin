package br.com.ntopus.accesscontrol.vertex.mapper.factory.accessGroup

import br.com.ntopus.accesscontrol.vertex.data.VertexInfo
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.mapper.factory.ICreateEdge
import br.com.ntopus.accesscontrol.vertex.mapper.factory.IEdgeFactory

object AccessGroupEdgeFactory: IEdgeFactory {

    override fun edgeForTarget(target: VertexInfo, edgeLabel: String?): ICreateEdge? {
        return when (target.label) {
            VertexLabel.ACCESS_GROUP.label -> EdgeFromAccessGroup()
            VertexLabel.RULE.label -> EdgeFromRule(edgeLabel!!)
            else -> null
        }
    }
}