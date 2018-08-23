package br.com.ntopus.accesscontrol.vertex.base

abstract class IPermission(properties: Map<String, String>): IDefaultCommon(properties) {

    var description: String = this.toString(properties["description"])

}