package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.data.VertexData
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.proto.AccessControlServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import mu.KotlinLogging
import net.badata.protobuf.converter.Converter
import java.util.concurrent.TimeUnit

class AccessControlClient(host: String = "localhost", port: Int = 5000) {

    private val logger = KotlinLogging.logger {}
    private var channel: ManagedChannel? = null
    private var stub: AccessControlServiceGrpc.AccessControlServiceBlockingStub? = null

    init {
        this.setChannel(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build())
    }

    fun shoutdown(): Boolean {
        return channel!!.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun setChannel(channel: ManagedChannel): AccessControlClient {
        this.channel = channel
        this.stub = AccessControlServiceGrpc.newBlockingStub(channel)
        return this
    }

    fun addVertex(vertex: VertexData) {
        logger.info{"Will try to add vertex $vertex"}
        val converter = Converter.create().toProtobuf(AccessControlServer.Vertex::class.java, vertex)
        val put = AccessControlServer.AddVertexRequest.newBuilder().setVertex(converter).build()
        println("----------->${channel}")
        var response : AccessControlServer.VertexResponse? = null
        try {
            response = this.stub!!.addVertex(put)
        } catch (e: Exception) {
            logger.warn{ "RPC failed: ${e.message}"}
        }
        logger.info{"Adding vertex: $response"}
    }
}