package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.factory.MapperFactory
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.proto.ProtoVertexResponse
import io.grpc.stub.StreamObserver
import net.badata.protobuf.converter.Converter


class AccessControlService: AccessControlServiceGrpc.AccessControlServiceImplBase(){
    override fun addVertex(request: AccessControlServer.AddVertexRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        val converter = Converter.create()
        try {
            val vertex = converter.toDomain(VertexData::class.java, request!!.vertex)
            responseObserver?.onNext(MapperFactory.createFactory(vertex).insert())

        } catch (e: Exception) {
            responseObserver?.onNext(ProtoVertexResponse.createErrorResponse("@ACIE-001 Impossible create a Vertex ${e.message}"))
        }
        responseObserver?.onCompleted()
    }

//    override fun putValue(request: AccessControlServer.PutRequest?, responseObserver: StreamObserver<AccessControlServer.PutResponse>?) {
//
//        var status = false
//
//        if (request!!.key != null && request.value != null) {
//            db.put(request.key, request.value)
//            status = true
//        }
//
//        val response = AccessControlServer.PutResponse.newBuilder().setSuccess(status).build()
//        responseObserver?.onNext(response)
//        responseObserver?.onCompleted()
//    }
//
//    override fun getValue(request: AccessControlServer.GetRequest?, responseObserver: StreamObserver<AccessControlServer.GetResponse>?) {
//        val response = AccessControlServer.GetResponse.newBuilder()
//
//        val key = request?.key
//
//        if(key != null) {
//            val value = db.get(key)
//
//            if(value != null) {
//                println("data found")
//                response.value = value
//            } else {
//            }
//            responseObserver?.onNext(response.build())
//        }
//        responseObserver?.onCompleted()
//    }


}