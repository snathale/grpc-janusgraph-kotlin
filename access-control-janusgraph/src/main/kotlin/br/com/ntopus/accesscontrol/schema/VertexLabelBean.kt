package br.com.ntopus.accesscontrol.schema

import org.janusgraph.core.schema.JanusGraphManagement
import org.janusgraph.core.schema.VertexLabelMaker

class VertexLabelBean {
    val name: String? = null
    val partition: Boolean = false
    val useStatic: Boolean = false

    fun make(mgmt: JanusGraphManagement) {
        if (name == null) {
            println("[SCHEMA] Need \"name\" property to define a vertex")
        } else if (mgmt.containsVertexLabel(name)) {
            println("[SCHEMA] Vertex: ${name} exists")
        } else {
            try {
                val maker: VertexLabelMaker = mgmt.makeVertexLabel(name)
                if (partition) maker.partition()
                if (useStatic) maker.setStatic()
                maker.make()
                println ("[SCHEMA] Vertex:${name} creation is done")
            } catch (e: Exception) {
                println("[SCHEMA] Can't create vertex: ${name}, ${e.message}")
            }
        }
    }
}