package br.com.ntopus.accesscontrol.vertex.proto

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.EdgeData
import br.com.ntopus.accesscontrol.vertex.data.VertexData


interface IProtoResponse {
    fun createVertexErrorResponse(error: String): AccessControlServer.VertexResponse
    fun createVertexSuccessResponse(data: VertexData? = null): AccessControlServer.VertexResponse
    fun createEdgeSuccessResponse(data: EdgeData? =  null): AccessControlServer.EdgeResponse
    fun createEdgeErrorResponse(error :String): AccessControlServer.EdgeResponse
}