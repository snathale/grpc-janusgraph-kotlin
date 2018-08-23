package br.com.ntopus.accesscontrol.data

import br.com.ntopus.accesscontrol.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.Property
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField

@ProtoClass(AccessControlServer.Vertex::class)
data class VertexData (@ProtoField var label: String = "", @ProtoField var properties: List<Property> = listOf())