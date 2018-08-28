package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.factory.MapperFactory
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.proto.AccessControlServiceGrpc
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.proto.ProtoVertexResponse
import io.grpc.stub.StreamObserver
import net.badata.protobuf.converter.Converter
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField


@ProtoClass(AccessControlServer.GetVertexByCodeRequest::class)
data class VertexByCode(@ProtoField var label: String = "", @ProtoField var code: String = "")

class AccessControlService: AccessControlServiceGrpc.AccessControlServiceImplBase() {

    override fun addVertex(request: AccessControlServer.AddVertexRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        val converter = Converter.create()
        try {
            val vertex = converter.toDomain(VertexData::class.java, request!!.vertex)
            responseObserver?.onNext(MapperFactory.createFactory(vertex).insert())

        } catch (e: Exception) {
            responseObserver?.onNext(ProtoVertexResponse
                    .createErrorResponse("@AVE-001 Impossible create a Vertex ${e.message}"))
        }
        responseObserver?.onCompleted()
    }

    override fun getVertexById(request: AccessControlServer.GetVertexByIdRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        try {
            val g = GraphFactory.open().traversal()
            val vertex = g.V(request!!.id).next()
            val response = ProtoVertexResponse
                    .createSuccessResponse(AbstractMapper.parseVertexToVertexData(vertex))
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            val response =  ProtoVertexResponse
                    .createErrorResponse("@GVIE-001 Vertex not found")
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

    override fun getVertexByCode(request: AccessControlServer.GetVertexByCodeRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        try {
            val vertexByCode = Converter.create().toDomain(VertexByCode::class.java, request)
            val g = GraphFactory.open().traversal()
            val vertex = g.V().hasLabel(vertexByCode.label).has(PropertyLabel.CODE.label, vertexByCode.code).next()
            val response = ProtoVertexResponse
                    .createSuccessResponse(AbstractMapper.parseVertexToVertexData(vertex))
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            val response =  ProtoVertexResponse
                    .createErrorResponse("@GVCE-001 Vertex not found")
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

}