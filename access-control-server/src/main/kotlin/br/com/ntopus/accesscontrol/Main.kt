package br.com.ntopus.accesscontrol

import io.grpc.Server as GServer
import io.grpc.ServerBuilder

fun main(args: Array<String>) {
    println("Starting this awful piece of garbage.")

    val port = 5000

    val server = Server()
    val s = ServerBuilder.forPort(port).addService(server).build()
    val tmp = s.start()
    println("Server started, listening on ${port}")
    tmp.awaitTermination()
}

