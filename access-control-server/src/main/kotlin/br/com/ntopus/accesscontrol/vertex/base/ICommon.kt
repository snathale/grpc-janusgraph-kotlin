package br.com.ntopus.accesscontrol.vertex.base

import br.com.ntopus.accesscontrol.vertex.data.VertexData

abstract class ICommon(properties: Map<String, String>) {

    var code: String = this.toString(properties["code"])

    var enable: Boolean = if (!this.toString(properties["enable"]).isEmpty()) properties["enable"]!!.toBoolean() else true

    var id: Long = properties["id"]!!.toLong()

    fun toString(value: Any?): String {
        if (value.toString() == "null") {
            return ""
        }
        return value.toString()
    }

    abstract fun formatDate(): String?

    abstract fun mapperToVertexData(label: String = ""): VertexData
}