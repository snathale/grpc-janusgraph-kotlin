package br.com.ntopus.accesscontrol.vertex.data

import br.com.ntopus.accesscontrol.proto.AccessControlServer
import net.badata.protobuf.converter.annotation.ProtoClass
import net.badata.protobuf.converter.annotation.ProtoField

@ProtoClass(AccessControlServer.Property::class)
data class Property(@ProtoField var name: String = "", @ProtoField var value: String = "")