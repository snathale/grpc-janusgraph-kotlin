package br.com.ntopus.accesscontrol.factory

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel

abstract class VertexDataFactory {
    companion object {
        private val graph = GraphFactory.open().traversal()
        fun createFactory(id: Long): VertexData {
            val property = listOf(Property(PropertyLabel.ID.label, id.toString()))
            try {
                val g = graph.V(id).next()
                return when (g.label()) {
                    VertexLabel.USER.label -> VertexData(VertexLabel.USER.label, property)
                    VertexLabel.ORGANIZATION.label -> VertexData(VertexLabel.ORGANIZATION.label, property)
                    VertexLabel.UNIT_ORGANIZATION.label -> VertexData(VertexLabel.UNIT_ORGANIZATION.label, property)
                    VertexLabel.GROUP.label -> VertexData(VertexLabel.GROUP.label, property)
                    VertexLabel.RULE.label -> VertexData(VertexLabel.RULE.label, property)
                    VertexLabel.ACCESS_GROUP.label -> VertexData(VertexLabel.ACCESS_GROUP.label, property)
                    VertexLabel.ACCESS_RULE.label -> VertexData(VertexLabel.ACCESS_RULE.label, property)
                    else -> throw IllegalArgumentException("@VFE-001 Vertex label not Found")
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("@VFE-002 Vertex not Found")
            }
        }
    }
}