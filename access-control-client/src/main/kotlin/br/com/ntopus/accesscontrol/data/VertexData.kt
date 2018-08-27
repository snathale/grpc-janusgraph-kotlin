package br.com.ntopus.accesscontrol.data

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField

@ProtoClass(AccessControlServer.Vertex::class)
data class VertexData (@ProtoField var label: String = "", @ProtoField(name="property") var properties: List<Property> = listOf())
