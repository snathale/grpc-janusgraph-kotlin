package br.com.ntopus.accesscontrol

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.factory.MapperFactory
import br.com.ntopus.accesscontrol.vertex.mapper.AbstractMapper
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.proto.AccessControlServiceGrpc
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse
import io.grpc.stub.StreamObserver
import net.badata.protobuf.converter.Configuration
import net.badata.protobuf.converter.Converter
import net.badata.protobuf.converter.FieldsIgnore
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.`is`
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.both
import org.apache.tinkerpop.gremlin.structure.Vertex


@ProtoClass(AccessControlServer.GetVertexByCodeRequest::class)
data class VertexByCode(@ProtoField var label: String = "", @ProtoField var code: String = "")

class AccessControlService : AccessControlServiceGrpc.AccessControlServiceImplBase() {

    override fun addVertex(request: AccessControlServer.AddVertexRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        try {
            val vertex = Converter.create().toDomain(VertexData::class.java, request!!.vertex)
            responseObserver?.onNext(MapperFactory.createFactory(vertex).insert())

        } catch (e: Exception) {
            responseObserver?.onNext(ProtoResponse
                    .createVertexErrorResponse("@AVE-001 Impossible create a Vertex ${e.message}"))
        }
        responseObserver?.onCompleted()
    }

    override fun getVertexById(request: AccessControlServer.GetVertexByIdRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        try {
            val g = GraphFactory.open().traversal()
            val vertex = g.V(request!!.id).next()
            val response = ProtoResponse
                    .createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(vertex))
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            val response = ProtoResponse
                    .createVertexErrorResponse("@GVIE-001 Vertex not found")
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

    override fun getVertexByCode(request: AccessControlServer.GetVertexByCodeRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        try {
            val vertexByCode = Converter.create().toDomain(VertexByCode::class.java, request)
            val g = GraphFactory.open().traversal()
            val vertex = g.V().hasLabel(vertexByCode.label).has(PropertyLabel.CODE.label, vertexByCode.code).next()
            val response = ProtoResponse
                    .createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(vertex))
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            val response = ProtoResponse
                    .createVertexErrorResponse("@GVCE-001 Vertex not found")
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

    override fun deleteVertex(request: AccessControlServer.DeleteVertexRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        try {
            val vertexData = VertexData(request!!.label, listOf(Property(PropertyLabel.ID.label, request.id.toString())))
            val response = MapperFactory.createFactory(vertexData).delete()
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            val response = ProtoResponse
                    .createVertexErrorResponse("@ACDV-001 Impossible delete Vertex ${e.message}")
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

    override fun updateVertexProperty(request: AccessControlServer.UpdateVertexPropertyRequest?, responseObserver: StreamObserver<AccessControlServer.VertexResponse>?) {
        val vertex = VertexData(request!!.label, listOf(Property(PropertyLabel.ID.label, request.id.toString())))
        try {
            val properties = Converter.create().toDomain(Property::class.java, request.propertyList)
            val response = MapperFactory.createFactory(vertex).updateProperty(properties)
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            val response = ProtoResponse.createVertexErrorResponse("@UPVE-001 Impossible update Vertex Property ${e.message}")
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

    override fun addEdge(request: AccessControlServer.AddEdgeRequest?, responseObserver: StreamObserver<AccessControlServer.EdgeResponse>?) {
        val ignore = FieldsIgnore().add(EdgeData::class.java, "properties")
        if (request!!.edge.edgeLabel.isEmpty()) {
            ignore.add(EdgeData::class.java, "edgeLabel")
        }
        try {
            val config = Configuration.builder().addIgnoredFields(ignore).build()
            val edge = Converter.create(config).toDomain(EdgeData::class.java, request.edge)
            val vertex = VertexData(edge.source.label, listOf(Property(PropertyLabel.ID.label, edge.source.id.toString())))
            responseObserver?.onNext(MapperFactory.createFactory(vertex).createEdge(edge.target, request.edge.edgeLabel))
        } catch (e: Exception) {
            responseObserver?.onNext(ProtoResponse
                    .createEdgeErrorResponse("@AEE-001 Impossible create a Edge ${e.message}"))
        }
        responseObserver?.onCompleted()
    }

    override fun getEdgeById(request: AccessControlServer.GetEdgeRequestById?, responseObserver: StreamObserver<AccessControlServer.EdgeResponse>?) {
        try {
            val g = GraphFactory.open().traversal()
            val edge = g.E(request!!.id).next()
            val response = ProtoResponse
                    .createEdgeSuccessResponse(AbstractMapper.parseEdgeToEdgeData(edge))
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            val response = ProtoResponse
                    .createEdgeErrorResponse("@GEE-001 Edge not found")
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

    override fun hasPermission(request: AccessControlServer.HasPermissionRequest?, responseObserver: StreamObserver<AccessControlServer.HasPermissionResponse>?) {
        try {
            val g = GraphFactory.open().traversal()
            val agent = Converter.create().toDomain(VertexInfo::class.java, request!!.agent)
            val rule = Converter.create().toDomain(VertexInfo::class.java, request.rule)
            val vAgent = g.V(agent.id).hasLabel(agent.label).next()
            //            if (agent.label == VertexLabel.USER.label) {
            //                val accessRule = vAgent.edges(Direction.OUT, EdgeLabel.ASSOCIATED.label)
            //            }
            val vRule = g.V(rule.id).hasLabel(VertexLabel.RULE.label).next()
            val path = g.V(vAgent).repeat(both().simplePath().until(`is`<Vertex>(vRule))).limit(1).next()
            println("--------->$path")
            val response = AccessControlServer.HasPermissionResponse.newBuilder()
                    .setPermission(true).build()
            responseObserver?.onNext(response)
        } catch (e: Exception) {
            println(e.message)
            val response = AccessControlServer.HasPermissionResponse.newBuilder()
                    .setPermission(false).build()
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }
}