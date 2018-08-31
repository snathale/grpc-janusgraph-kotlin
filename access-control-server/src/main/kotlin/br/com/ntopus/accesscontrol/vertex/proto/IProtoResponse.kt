package br.com.ntopus.accesscontrol.vertex.proto

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.VertexData


interface IProtoResponse {
    fun createErrorResponse(error: String): AccessControlServer.VertexResponse
    fun createSuccessResponse(data: VertexData? = null): AccessControlServer.VertexResponse
}