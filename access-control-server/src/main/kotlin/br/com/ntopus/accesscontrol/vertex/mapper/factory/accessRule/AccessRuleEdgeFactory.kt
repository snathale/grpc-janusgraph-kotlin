package br.com.ntopus.accesscontrol.vertex.mapper.factory.accessRule

import br.com.ntopus.accesscontrol.vertex.data.VertexInfo
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.mapper.factory.ICreateEdge
import br.com.ntopus.accesscontrol.vertex.mapper.factory.IEdgeFactory

object AccessRuleEdgeFactory: IEdgeFactory {

    override fun edgeForTarget(target: VertexInfo, edgeLabel: String?): ICreateEdge? {
        return when(target.label) {
            VertexLabel.ORGANIZATION.label,
            VertexLabel.UNIT_ORGANIZATION.label,
            VertexLabel.GROUP.label-> EdgeFromDefaultVertex()
            VertexLabel.ACCESS_GROUP.label -> EdgeFromAccessGroup()
            else -> null
        }
    }
}