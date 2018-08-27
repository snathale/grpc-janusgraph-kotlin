package br.com.ntopus.accesscontrol

import org.janusgraph.core.JanusGraph

interface IGraphFactory {
    fun open(config: String): GraphFactory
    fun loadSchema(graph: JanusGraph, schema: String): GraphFactory
    fun getInstance (): JanusGraph
}