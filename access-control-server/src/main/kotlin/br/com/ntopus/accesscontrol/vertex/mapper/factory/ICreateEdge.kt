package br.com.ntopus.accesscontrol.vertex.mapper.factory

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.VertexInfo
import org.apache.tinkerpop.gremlin.structure.Vertex

abstract class ICreateEdge {
    val graph = GraphFactory.open()
    abstract fun createEdge(vSource: Vertex, vTarget: Vertex, target: VertexInfo): AccessControlServer.EdgeResponse
}