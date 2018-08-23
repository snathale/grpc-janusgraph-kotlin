package br.com.ntopus.accesscontrol.graph

import org.janusgraph.core.JanusGraph
import org.janusgraph.core.JanusGraphFactory
import java.io.File

object GraphFactory {

    private lateinit var graph: JanusGraph

    init {
        try {
//            val a = javaClass.classLoader.getResource("janusgraph-cql-es.properties").readText()
            val a = javaClass.classLoader.getResource("janusgraph-cql-es.properties").file
            this.graph = JanusGraphFactory.open(javaClass.classLoader.getResource("janusgraph-cql-es.properties").file)
        } catch (e: Exception) {
            JanusGraphFactory.open("")
        }
    }

    fun open(): JanusGraph {
        return this.graph
    }
    fun setInstance (config: String): GraphFactory {
        this.graph = JanusGraphFactory.open(GraphFactory::class.java.getResource(config).readText())
        return this
    }
}