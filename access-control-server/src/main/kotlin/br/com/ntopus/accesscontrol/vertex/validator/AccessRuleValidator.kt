package br.com.ntopus.accesscontrol.vertex.validator

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.base.ICommon
import br.com.ntopus.accesscontrol.vertex.data.VertexInfo
import org.apache.tinkerpop.gremlin.structure.Vertex

class AccessRuleValidator: DefaultValidator() {

    override fun hasVertex(id: Long): Vertex? {
        return try {
            graph.traversal().V(id).hasLabel(VertexLabel.ACCESS_RULE.label).next()
        } catch (e: Exception) {
            null
        }
    }

    override fun canInsertVertex(vertex: ICommon): Boolean {
        if (vertex.code.isEmpty()) {
            return false
        }
        return true
    }

    override fun hasVertexTarget(target: VertexInfo): Vertex? {
//        ConfiguredGraphFactory.create("")
        val g = graph.traversal()
        return try {
            when(target.label) {
                VertexLabel.ACCESS_GROUP.label -> g.V(target.id).hasLabel(VertexLabel.ACCESS_GROUP.label).next()
                VertexLabel.GROUP.label -> g.V(target.id).hasLabel(VertexLabel.GROUP.label).next()
                VertexLabel.UNIT_ORGANIZATION.label -> g.V(target.id).hasLabel(VertexLabel.UNIT_ORGANIZATION.label).next()
                VertexLabel.ORGANIZATION.label -> g.V().hasLabel(VertexLabel.ORGANIZATION.label).next()
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }

    override fun isCorrectVertexTarget(target: VertexInfo): Boolean {
        return when(target.label) {
            VertexLabel.ACCESS_GROUP.label -> target.label == VertexLabel.ACCESS_GROUP.label
            VertexLabel.GROUP.label -> target.label == VertexLabel.GROUP.label
            VertexLabel.UNIT_ORGANIZATION.label -> target.label == VertexLabel.UNIT_ORGANIZATION.label
            VertexLabel.ORGANIZATION.label -> target.label == VertexLabel.ORGANIZATION.label
            else -> false
        }
    }

    override fun hasProperty(code: String, property: Property): Boolean {
        val g = graph.traversal()
        return g.V().hasLabel(VertexLabel.ACCESS_RULE.label).has(property.name, property.value) != null
    }

    override fun canUpdateVertexProperty(properties: List<Property>): Boolean {
        for (value in properties) {
            if (value.name != PropertyLabel.NAME.label
                    && value.name != PropertyLabel.EXPIRATION_DATE.label
                    && value.name != PropertyLabel.ENABLE.label) {
                return false
            }
        }
        return true
    }
}