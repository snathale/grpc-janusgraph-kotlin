package br.com.ntopus.accesscontrol

fun main(args: Array<String>) {
    println("Importer Janusgraph Schema")
    val graph = GraphFactory(args[0]).getInstance()
    GraphFactory.loadSchema(graph, args[1])
    graph.close()
}