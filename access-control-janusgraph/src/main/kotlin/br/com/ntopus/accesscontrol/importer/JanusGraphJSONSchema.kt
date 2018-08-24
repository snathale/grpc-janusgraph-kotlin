package br.com.ntopus.accesscontrol.importer

import groovy.lang.Closure
import org.apache.tinkerpop.shaded.jackson.databind.node.ObjectNode
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.JanusGraphTransaction
import org.janusgraph.core.schema.JanusGraphManagement
import org.janusgraph.graphdb.database.StandardJanusGraph
import br.com.ntopus.accesscontrol.GraphSchema
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper
import java.io.File


class JanusGraphJSONSchema(var graph: StandardJanusGraph) {

    fun readFile(schemaFile: String) {
        val mgmt: JanusGraphManagement = graph.openManagement()

        try {
            parse(schemaFile)
                    .make(mgmt)
        } catch (e: Exception) {
            rollbackTxs(graph)
            print ("[SCHEMA] Parse GSON failed: ${e.message}")
        }
    }

    fun commitTxs(graph: JanusGraph): Boolean {
        val sgraph: StandardJanusGraph = graph as StandardJanusGraph
            try {
                val txs: Set<JanusGraphTransaction> = sgraph.openTransactions
                //commit all running transactions
                val iter:  Iterator<JanusGraphTransaction>  = txs.iterator()
                while (iter.hasNext()) {
                    iter.next().commit()
                }
            } catch (e: Exception) {
                //ignore
                return false
            }
        return true
    }

    public fun rollbackTxs(graph: JanusGraph): Boolean {
        val sgraph: StandardJanusGraph = graph as StandardJanusGraph
                try {
                    val txs: Set<JanusGraphTransaction> = sgraph.openTransactions
                    //commit all running transactions
                    val iter: Iterator<JanusGraphTransaction> = txs.iterator()
                    while (iter.hasNext()) {
                        iter.next().rollback()
                    }
                } catch (e: Exception) {
                    //ignore
                    return false
                }
        return true
    }

    private fun parse(schemaFile: String): GraphSchema {
        val gsonFile: File = File(schemaFile)

        if (!gsonFile.exists()) {
            throw Exception("[SCHEMA] File not found:" + schemaFile)
        }

        val mapper = ObjectMapper()
        return mapper.readValue(gsonFile, GraphSchema::class.java) as GraphSchema
    }

    fun make(nodes: MutableList<ObjectNode>, name: String, check: Closure<Boolean>, exist: Closure<Void>, create: Closure<Void>) {
        for (node in nodes) {
            val nameStr: String = node.get(name).asText()
            if (check.call(nameStr)) {
                exist.call(nameStr)
            } else {
                create.call(nameStr, node)
            }
        }
    }

}