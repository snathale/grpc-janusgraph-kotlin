package br.com.ntopus.accesscontrol.vertex.mapper.factory

import br.com.ntopus.accesscontrol.vertex.data.VertexInfo

interface IEdgeFactory {
    fun edgeForTarget(target: VertexInfo, edgeLabel: String? = ""): ICreateEdge?
}