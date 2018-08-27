package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.base.ProtoResponse
import br.com.ntopus.accesscontrol.vertex.proto.IProtoResponse

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

data class AgentResponse(
        override val id: Long,
        override val code: String,
        val name: String,
        val creationDate: String,
        val enable: Boolean,
        val observation: String
): VertexResponse

data class PermissionResponse(
        override val id: Long,
        override val code: String,
        val name: String,
        val creationDate: String,
        val description: String,
        val enable: Boolean
): VertexResponse

data class AssociationResponse(
        override val id: Long,
        override val code: String,
        val expirationDate: String?,
        val enable: Boolean
): VertexResponse

interface IMapper {
    fun insert(): AccessControlServer.VertexResponse
//    fun updateProperty(properties: List<Property>): JSONResponse
//    fun createEdge(target: VertexInfo, edgeTarget: String = ""): JSONResponse
//    fun delete(): JSONResponse
}