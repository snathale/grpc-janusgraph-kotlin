package br.com.ntopus.accesscontrol.vertex.validator

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
import org.apache.tinkerpop.gremlin.structure.Vertex

class UnitOrganizationValidator : DefaultValidator() {

    override fun hasVertex(id: Long): Vertex? {
        return try {
            graph.traversal().V(id).hasLabel(VertexLabel.UNIT_ORGANIZATION.label).next()
        } catch (e: Exception) {
            null
        }
    }

    override fun hasVertexTarget(target: VertexInfo): Vertex? {
        return try {
            graph.traversal().V().hasLabel(VertexLabel.GROUP.label).has(PropertyLabel.CODE.label, target.code).next()
        } catch (e: Exception) {
            null
        }
    }

    override fun isCorrectVertexTarget(target: VertexInfo): Boolean {
        return target.label == VertexLabel.GROUP.label
    }

    override fun hasProperty(code: String, property: Property): Boolean {
        val g = graph.traversal()
        return g.V().hasLabel(VertexLabel.UNIT_ORGANIZATION.label).has(property.name, property.value) != null

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