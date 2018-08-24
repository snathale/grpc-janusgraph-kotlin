package br.com.ntopus.accesscontrol.importer

import org.janusgraph.core.JanusGraph
import org.janusgraph.core.schema.JanusGraphManagement
import org.janusgraph.core.schema.SchemaAction
import org.janusgraph.graphdb.database.StandardJanusGraph

class JanusGraphSchemaImporter {

    fun writeGraphSONSchema(graph: JanusGraph, schema: String) {
        val importer = JanusGraphJSONSchema(graph as StandardJanusGraph)
        importer.readFile(schema)
    }

    fun updateCompositeIndexState(graph: JanusGraph, name: String, newState: SchemaAction) {
        val mgmt: JanusGraphManagement = graph.openManagement()
        val index = mgmt.getGraphIndex(name)
        if (index == null) {
            print ("[SCHEMA] ${name} index doesn't exist")
            return
        }
        mgmt.updateIndex(index, newState)
        mgmt.commit()
        JanusGraphJSONSchema(graph as StandardJanusGraph).rollbackTxs(graph)
    }
}