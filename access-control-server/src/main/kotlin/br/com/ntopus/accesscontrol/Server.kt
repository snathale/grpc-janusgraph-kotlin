package br.com.ntopus.accesscontrol

import io.grpc.stub.StreamObserver

class Server: AccessControlServiceGrpc.AccessControlServiceImplBase(){
    private var db = DB()

    override fun putValue(request: AccessControlServer.PutRequest?, responseObserver: StreamObserver<AccessControlServer.PutResponse>?) {

        var success = false

        if (request!!.key != null && request.value != null) {
            db.put(request.key, request.value)
            success = true
        }

        val response = AccessControlServer.PutResponse.newBuilder().setSuccess(success).build()
        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

    override fun getValue(request: AccessControlServer.GetRequest?, responseObserver: StreamObserver<AccessControlServer.GetResponse>?) {
        val response = AccessControlServer.GetResponse.newBuilder()

        val key = request?.key

        if(key != null) {
            val value = db.get(key)

            if(value != null) {
                println("data found")
                response.value = value
            } else {
            }
            responseObserver?.onNext(response.build())
        }
        responseObserver?.onCompleted()
    }


}