package br.com.ntopus.accesscontrol.factory

import br.com.ntopus.accesscontrol.GraphFactory
import org.janusgraph.core.JanusGraph

object GraphFactory {
    private val graph = GraphFactory(javaClass.classLoader.getResource("janusgraph-cql-es.properties").file).open()

    fun open(): JanusGraph {
        return this.graph
    }
}