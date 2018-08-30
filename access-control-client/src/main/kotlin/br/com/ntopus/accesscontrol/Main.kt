package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.data.Property
import br.com.ntopus.accesscontrol.data.VertexData
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.proto.AccessControlServiceGrpc
import io.grpc.ManagedChannelBuilder
import net.badata.protobuf.converter.Converter
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField

@ProtoClass(AccessControlServer.VertexResponse::class)
data class VertexResponse(
        @ProtoField val status: String = "",
        @ProtoField val data: VertexData = VertexData(),
        @ProtoField val message: String = ""
)

fun main(args: Array<String>) {
    println("Running the test client.")

    val channel = ManagedChannelBuilder.forAddress("localhost", 5000).usePlaintext().build()

    val stub = AccessControlServiceGrpc.newBlockingStub(channel)

    val properties: List<Property> = listOf(Property("code", "2"), Property("name", "test2"))
    val user = VertexData("user", properties)
    val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, user)
    print(converter
    )
    val put = AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
    val response = stub.addVertex(put)

    println("Done putting, $response")

    val get = AccessControlServer.GetVertexByCodeRequest.newBuilder()
            .setCode("2").setLabel("user").build()
    val response2 = stub.getVertexByCode(get)
    println("Get(code=2): $response2")

    val responseParser = Converter.create().toDomain(VertexData::class.java, response.data)
    val id = responseParser.properties[0].value.toLong()
    val getById = AccessControlServer.GetVertexByIdRequest.newBuilder()
            .setId(id).build()
    val response3 = stub.getVertexById(getById)
    println("GetBydId ($id): $response3")
}

