package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.importer.JanusGraphSchemaImporter
import org.janusgraph.core.ConfiguredGraphFactory
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.JanusGraphFactory

class GraphFactory(val config: String) {

    private var graph: JanusGraph? = this.open()

    fun open(): JanusGraph {
        if (this.graph != null){
            return this.getInstance()
        }
        return JanusGraphFactory.open(config)
    }

    companion object {
        fun loadSchema(graph: JanusGraph, schema: String): JanusGraph {
            JanusGraphSchemaImporter().writeGraphSONSchema(graph, schema)
            return graph
        }

    }

    fun getInstance(): JanusGraph {
        return this.graph!!
    }

}