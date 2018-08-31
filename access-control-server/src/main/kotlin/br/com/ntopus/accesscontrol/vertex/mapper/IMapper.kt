package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.Property

data class EdgeCreated(
        val source: VertexInfo,
        val target: VertexInfo,
        val edgeLabel: String
)

data class VertexInfo(
        val label: String,
        val code: String
)

interface VertexResponse {
    val id: Long
    val code: String
}
interface IMapper {
    fun insert(): AccessControlServer.VertexResponse
    fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse
//    fun createEdge(target: VertexInfo, edgeTarget: String = ""): JSONResponse
    fun delete(): AccessControlServer.VertexResponse
}