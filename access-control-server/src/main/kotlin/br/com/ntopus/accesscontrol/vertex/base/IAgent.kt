package br.com.ntopus.accesscontrol.vertex.base

abstract class IAgent(properties: Map<String, String>): IDefaultCommon(properties) {

    var observation: String = this.toString(properties["observation"])

}