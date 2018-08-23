package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.data.VertexData
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import net.badata.protobuf.converter.Converter

fun main(args: Array<String>) {
    println("Running the test client.")

    val channel = ManagedChannelBuilder.forAddress("localhost", 5000).usePlaintext().build()

    val stub = AccessControlServiceGrpc.newBlockingStub(channel)

    val properties:List<Property> = listOf(Property("code", "2"), Property("name", "test"))
    val user = VertexData("user", properties)

    try {
        val vertex = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, user)
        val put = AccessControlServer.AddVertexRequest.newBuilder().setVertex(vertex).build()

        println("Putting ${vertex.label} $properties")
        val response = try {
            stub.addVertex(put)
        } catch (e: StatusRuntimeException) {
            println("EXCEPTION------->${e.message}")
        }
        println("Done putting, $response")
    }catch (e: Exception) {
        println("EXCEPTION 1------->${e.message}")
    }




//    val get = AccessControlServer.GetVertexByCodeRequest.newBuilder().setCode("2").build()
//    val response2 = stub.getVertexByCode(get)
//
//    println("Get(code=2): $response2")
}

