package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.importer.JanusGraphSchemaImporter
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.JanusGraphFactory

object GraphFactory {
    private var graph: JanusGraph = JanusGraphFactory.open(javaClass.classLoader.getResource("janusgraph-inmemory.properties").file)

    fun open(): JanusGraph {
        return graph
    }

    fun loadSchema() {
        JanusGraphSchemaImporter().writeGraphSONSchema(open(), javaClass.classLoader.getResource("schema.json").file)
    }

    fun setInstance (config: String): GraphFactory {
        graph = JanusGraphFactory.open(javaClass.classLoader.getResource(config).file)
        return this
    }
}

fun main(args: Array<String>) {
    println("Importer Janusgraph Schema")
    GraphFactory.loadSchema()
    GraphFactory.open().close()
}