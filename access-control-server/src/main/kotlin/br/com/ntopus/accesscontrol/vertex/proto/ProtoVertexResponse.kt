package br.com.ntopus.accesscontrol.vertex.proto

import br.com.ntopus.accesscontrol.StatusResponse
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import net.badata.protobuf.converter.Converter

object ProtoVertexResponse: IProtoResponse{
    override fun createErrorResponse(error: String): AccessControlServer.VertexResponse {
        return AccessControlServer.VertexResponse.newBuilder()
                .setMessage(error).setStatus(StatusResponse.ERROR.label).build()
    }

    override fun createSuccessResponse(data: VertexData?): AccessControlServer.VertexResponse {
        if (data == null) {
            return AccessControlServer.VertexResponse.newBuilder().setStatus(StatusResponse.SUCCESS.label).build()
        }
        val vertex = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, data)
        return AccessControlServer.VertexResponse.newBuilder()
                .setData(vertex).setStatus(StatusResponse.SUCCESS.label).build()
    }

}