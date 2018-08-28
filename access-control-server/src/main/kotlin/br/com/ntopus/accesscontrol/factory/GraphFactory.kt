package br.com.ntopus.accesscontrol.factory

import br.com.ntopus.accesscontrol.GraphFactory
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.JanusGraphFactory

object GraphFactory {
    private var graph: JanusGraph = GraphFactory(javaClass.classLoader.getResource("janusgraph-cql-es.properties").file).open()

    fun open(): JanusGraph {
        return this.graph
    }

    fun setInstance (config: String): br.com.ntopus.accesscontrol.factory.GraphFactory {
        this.graph = JanusGraphFactory.open(javaClass.classLoader.getResource(config).file)
        return this
    }
}