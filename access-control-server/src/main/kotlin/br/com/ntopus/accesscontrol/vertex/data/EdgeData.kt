package br.com.ntopus.accesscontrol.vertex.data

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField

@ProtoClass(AccessControlServer.Edge::class)
data class EdgeData (
        @ProtoField var source: VertexInfo? = null,
        @ProtoField var target: VertexInfo? = null,
        @ProtoField(name="property") var properties: List<Property> = listOf()
)