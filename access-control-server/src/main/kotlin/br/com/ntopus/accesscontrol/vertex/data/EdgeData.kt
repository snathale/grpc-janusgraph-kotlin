package br.com.ntopus.accesscontrol.vertex.data

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField

@ProtoClass(AccessControlServer.Edge::class)
data class EdgeData (
        @ProtoField var source: VertexInfo = VertexInfo(),
        @ProtoField var target: VertexInfo = VertexInfo(),
        @ProtoField var edgeLabel: String? = "",
        @ProtoField(name="property") var properties: List<Property>? = listOf()
)