package br.com.ntopus.accesscontrol.schema

import org.janusgraph.core.Cardinality
import org.janusgraph.core.schema.JanusGraphManagement

class PropertyKeyBean {
    val name: String? = null
    val dataType: String = ""
    val cardinality: String = ""

    fun make(mgmt: JanusGraphManagement) {
        if (this.name == null) {
            println("[SCHEMA] Need \"name\" property to define a propertyKey")
        } else if (mgmt.containsPropertyKey(name)) {
            println("[SCHEMA] Property: ${name} exists")
        } else {
            try {
                mgmt.makePropertyKey(name)
                        .dataType(TypeMap.MAP.get(dataType))
                        .cardinality(Cardinality.valueOf(this.cardinality)).make()
                println("[SCHEMA] PropertyKey:${name} creation is done")
            } catch (e: Exception) {
                println("[SCHEMA] Can't create property:${name}, ${e.message}")
            }
        }
    }
}