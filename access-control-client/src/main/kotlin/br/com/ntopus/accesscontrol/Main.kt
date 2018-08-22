package br.com.ntopus.accesscontrol

import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import java.nio.charset.Charset

fun main(args: Array<String>) {
    println("Running the test client.")

    val channel = ManagedChannelBuilder.forAddress("localhost", 5000).usePlaintext().build()

    val stub = AccessControlServiceGrpc.newBlockingStub(channel)

    val key = ByteString.copyFrom("test", Charset.defaultCharset())
    val value = ByteString.copyFrom("value", Charset.defaultCharset())

    val put = AccessControlServer.PutRequest.newBuilder().setKey(key).setValue(value).build()

    println("Putting $key, $value")
    val response = try {
        stub.putValue(put)
    } catch (e: StatusRuntimeException) {
        println("EXCEPTION------->${e.message}")

    }


    println("Done putting, $response")

    val get = AccessControlServer.GetRequest.newBuilder().setKey(key).build()
    val response2 = stub.getValue(get)

    println("Get($key): $response2")
}

