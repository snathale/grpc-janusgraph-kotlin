package br.com.ntopus.accesscontrol.vertex.proto

import br.com.ntopus.accesscontrol.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.VertexData


interface IProtoResponse {
    fun createErrorResponse(error: String): AccessControlServer.VertexResponse
    fun createSuccessResponse(data: VertexData): AccessControlServer.VertexResponse
}