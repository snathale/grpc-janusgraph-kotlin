package br.com.ntopus.accesscontrol.schema

import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.janusgraph.core.PropertyKey
import org.janusgraph.core.schema.JanusGraphManagement
import org.apache.tinkerpop.gremlin.structure.Direction

class VertexCentricIndexBean {
    val name: String? = null
    val edge: String? = null
    val propertyKeys: MutableList<String>? = mutableListOf()
    val order: String = "incr"
    val direction: String = "BOTH"

    fun make(mgmt: JanusGraphManagement) {
        if (name == null) {
            println ("[SCHEMA] Missing 'name' property, not able to create a vertex-centric index")
            return
        }

        if (edge == null) {
            println ("[SCHEMA] Vertex-centric index needs 'edge' property to specify a edge label")
            return
        }

        val elabel = mgmt.getEdgeLabel(edge)
        if (elabel == null) {
            println ("[SCHEMA] Edge: ${edge} doesn't exist")
            return
        }

        if (mgmt.containsRelationIndex(elabel, name)) {
            println ("[SCHEMA] Vertex-centric index: ${name} exists")
            return
        }

        if (propertyKeys == null || propertyKeys.size == 0) {
            println ("[SCHEMA] Missing 'propertyKeys property, not able to create an index")
            return
        }

        val keys: Array<PropertyKey?> = arrayOfNulls(propertyKeys.size)
        var counter = 0
        for (property in propertyKeys) {
            val key = mgmt.getPropertyKey(property)
            if (key == null) {
                println ("[SCHEMA] PropertyKey:${property} doesn't exist, can't create ${name} vertex-centric index")
                return
            }
            keys[counter++] = mgmt.getPropertyKey(property)
        }

        mgmt.buildEdgeIndex(elabel, name, Direction.valueOf(direction), Order.valueOf(order), *keys)
        println ("[SCHEMA] Vertex-centric index: ${name} creation is done")
    }

}