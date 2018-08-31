package br.com.ntopus.accesscontrol.vertex.validator

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
import br.com.ntopus.accesscontrol.vertex.validator.DefaultValidator
import org.apache.tinkerpop.gremlin.structure.Vertex

class AccessGroupValidator: DefaultValidator() {

    override fun hasVertexTarget(target: VertexInfo): Vertex? {
        return try {
            val g = graph.traversal()
            when(target.label) {
                VertexLabel.ACCESS_GROUP.label -> g.V().hasLabel(VertexLabel.ACCESS_GROUP.label)
                        .has(PropertyLabel.CODE.label, target.code).next()
                VertexLabel.RULE.label -> g.V().hasLabel(VertexLabel.RULE.label)
                        .has(PropertyLabel.CODE.label, target.code).next()
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }

    override fun isCorrectVertexTarget(target: VertexInfo): Boolean {
         return when(target.label) {
            VertexLabel.ACCESS_GROUP.label -> target.label == VertexLabel.ACCESS_GROUP.label
            VertexLabel.RULE.label -> target.label == VertexLabel.RULE.label
            else -> false
        }
    }

    override fun hasProperty(code: String, property: Property): Boolean {
        val g = graph.traversal()
        return g.V().hasLabel(VertexLabel.ACCESS_GROUP.label).has(property.name, property.value) != null
    }

    override fun canUpdateVertexProperty(properties: List<Property>): Boolean {
        for (value in properties) {
            if (value.name != PropertyLabel.NAME.label
                    && value.name != PropertyLabel.DESCRIPTION.label
                    && value.name != PropertyLabel.ENABLE.label) return false
        }
        return true
    }
}