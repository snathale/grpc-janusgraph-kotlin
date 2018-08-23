package br.com.ntopus.accesscontrol.vertex.base

import br.com.ntopus.accesscontrol.StatusResponse
import br.com.ntopus.accesscontrol.vertex.data.VertexData

interface ProtoResponse {
    val status: String
    val message: String?
}
data class ProtoVertexResponseSuccess(
        override val status: String = StatusResponse.SUCCESS.label,
        override val message: String? = "",
        val data: VertexData
): ProtoResponse
data class ProtoVertexResponseError(
        override val status: String = StatusResponse.ERROR.label,
        override val message: String
): ProtoResponse
abstract class ICommon(properties: Map<String, String>) {

    var code: String = this.toString(properties["code"])

    var enable: Boolean = if (!this.toString(properties["enable"]).isEmpty()) properties["enable"]!!.toBoolean() else true

    var id: Long? = null

    fun toString(value: Any?): String {
        if (value.toString() == "null") {
            return ""
        }
        return value.toString()
    }

    abstract fun formatDate(): String?
}