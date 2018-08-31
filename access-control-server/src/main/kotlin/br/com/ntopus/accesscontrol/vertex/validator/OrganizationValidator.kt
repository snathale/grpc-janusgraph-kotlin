package br.com.ntopus.accesscontrol.vertex.validator

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
import br.com.ntopus.accesscontrol.vertex.validator.DefaultValidator
import org.apache.tinkerpop.gremlin.structure.Vertex

class OrganizationValidator: DefaultValidator() {

    override fun hasVertexTarget(target: VertexInfo): Vertex? {
        return try {
            graph.traversal().V().hasLabel(VertexLabel.UNIT_ORGANIZATION.label).has(PropertyLabel.CODE.label, target.code).next()
        } catch (e: Exception) {
            null
        }
    }

    override fun isCorrectVertexTarget(target: VertexInfo): Boolean {
        return target.label.equals(VertexLabel.UNIT_ORGANIZATION.label)
    }

    override fun hasProperty(code: String, property: Property): Boolean {
        val g = graph.traversal()
        return g.V().hasLabel(VertexLabel.ORGANIZATION.label).has(PropertyLabel.CODE.label, code)
                .has(property.name, property.value) != null
    }

    override fun canUpdateVertexProperty(properties: List<Property>): Boolean {
        for (value in properties) {
            if (value.name != PropertyLabel.NAME.label
                    && value.name != PropertyLabel.OBSERVATION.label
                    && value.name != PropertyLabel.ENABLE.label) return false
        }
        return true
    }

}