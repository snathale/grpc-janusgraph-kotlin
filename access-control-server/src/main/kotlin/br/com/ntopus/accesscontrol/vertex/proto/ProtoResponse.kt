package br.com.ntopus.accesscontrol.vertex.proto

import br.com.ntopus.accesscontrol.StatusResponse
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.EdgeData
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import net.badata.protobuf.converter.Converter

object ProtoResponse: IProtoResponse{

    override fun createVertexErrorResponse(error: String): AccessControlServer.VertexResponse {
        return AccessControlServer.VertexResponse.newBuilder()
                .setMessage(error).setStatus(StatusResponse.ERROR.label).build()
    }

    override fun createVertexSuccessResponse(data: VertexData?): AccessControlServer.VertexResponse {
        if (data == null) {
            return AccessControlServer.VertexResponse.newBuilder().setStatus(StatusResponse.SUCCESS.label).build()
        }
        val vertex = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, data)
        return AccessControlServer.VertexResponse.newBuilder()
                .setData(vertex).setStatus(StatusResponse.SUCCESS.label).build()
    }

    override fun createEdgeSuccessResponse(data: EdgeData?): AccessControlServer.EdgeResponse {
        if (data == null) {
            return AccessControlServer.EdgeResponse.newBuilder().setStatus(StatusResponse.SUCCESS.label).build()
        }
        val edge = Converter.create().toProtobuf(AccessControlServer.Edge::class.java, data)
        return AccessControlServer.EdgeResponse.newBuilder()
                .setData(edge).setStatus(StatusResponse.SUCCESS.label).build()
    }

    override fun createEdgeErrorResponse(error: String): AccessControlServer.EdgeResponse {
        return AccessControlServer.EdgeResponse.newBuilder()
                .setMessage(error).setStatus(StatusResponse.ERROR.label).build()
    }

}