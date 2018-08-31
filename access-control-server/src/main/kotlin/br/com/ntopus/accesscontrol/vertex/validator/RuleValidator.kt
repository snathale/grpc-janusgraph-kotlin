package br.com.ntopus.accesscontrol.vertex.validator

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.validator.DefaultValidator
import org.apache.tinkerpop.gremlin.structure.Vertex

class RuleValidator: DefaultValidator() {
    override fun hasProperty(code: String, property: Property): Boolean {
        val g = graph.traversal()
        return g.V().hasLabel(VertexLabel.RULE.label).has(PropertyLabel.CODE.label, code)
                .has(property.name, property.value).next() != null
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