//package br.com.ntopus.accesscontrol.model.vertex.mapper.graph
//
//import br.com.ntopus.accesscontrol.model.GraphFactory_1
//import br.com.ntopus.accesscontrol.vertex.base.JSONResponse
//import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
//import org.apache.tinkerpop.gremlin.structure.Vertex
//
//abstract class ICreateEdge {
//    val graph = GraphFactory_1.open()
//    abstract fun createEdge(vSource: Vertex, vTarget: Vertex, target: VertexInfo, sourceCode: String): JSONResponse
//}