package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.Property

data class VertexInfo(
        val id: Long,
        val label: String
)

interface IMapper {
    fun insert(): AccessControlServer.VertexResponse
    fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse
    fun createEdge(target: VertexInfo, edgeTarget: String = ""): AccessControlServer.EdgeResponse
    fun delete(): AccessControlServer.VertexResponse
}