package br.com.ntopus.accesscontrol.schema

import org.janusgraph.core.schema.JanusGraphManagement

class GraphSchema {
    val propertyKeys: MutableList<PropertyKeyBean> = mutableListOf()
    val vertexLabels: MutableList<VertexLabelBean> =  mutableListOf()
    val edgeLabels: MutableList<EdgeLabelBean> = mutableListOf()
    val vertexIndexes: MutableList<IndexBean> = mutableListOf()
    val edgeIndexes: MutableList<IndexBean> = mutableListOf()
    val vertexCentricIndexes: MutableList<VertexCentricIndexBean> = mutableListOf()

    /**
     * use the {@code mgmt} to create the schema
     * @param mgmt
     */
    fun make(mgmt: JanusGraphManagement) {
        //create properties
        for (property in propertyKeys) {
            property.make(mgmt)
        }

        //create vertex labels
        for (vertex in vertexLabels) {
            vertex.make(mgmt)
        }

        //create edge labels
        for (edge in edgeLabels) {
            edge.make(mgmt)
        }

        //create v indexes
        for (vindex in vertexIndexes) {
            vindex.make(mgmt, true)
        }

        //create e indexes
        for (eindex in edgeIndexes) {
            eindex.make(mgmt, false)
        }

        //create vc indexes
        for (vcindex in vertexCentricIndexes) {
            vcindex.make(mgmt)
        }

        mgmt.commit()
    }
}