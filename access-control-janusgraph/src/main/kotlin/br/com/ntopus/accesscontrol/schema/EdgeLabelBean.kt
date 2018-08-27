package br.com.ntopus.accesscontrol.schema

import org.janusgraph.core.Multiplicity
import org.janusgraph.core.PropertyKey
import org.janusgraph.core.schema.EdgeLabelMaker
import org.janusgraph.core.schema.JanusGraphManagement

class EdgeLabelBean {
    val name:String? = null
    val multiplicity: String = "MULTI"
    val signatures: MutableList<String> = mutableListOf()
    val unidirected: Boolean = false

    fun make(mgmt: JanusGraphManagement) {
        if (name == null) {
            println ("[SCHEMA] Need \"name\" property to define a label")
        } else if (mgmt.containsEdgeLabel(this.name)) {
            println("[SCHEMA] Edge: ${name} exists")
        } else {
            try {
                val maker: EdgeLabelMaker = mgmt.makeEdgeLabel(name).multiplicity(Multiplicity.valueOf(this.multiplicity))
                if (this.signatures.size > 0) {
                    for (key in signatures) {
                        val property: PropertyKey = mgmt.getPropertyKey(key)
                        maker.signature(property)
                    }
                }
                if (unidirected) {
                    maker.unidirected()
                }
                maker.make()
                println("[SCHEMA] Edge: ${name} creation is done")
            } catch (e: Exception) {
                println("[SCHEMA] Cant't create edge: ${name}, ${e.message}")
            }
        }
    }
}