package br.com.ntopus.accesscontrol.vertex.base

import java.text.SimpleDateFormat
import java.util.*

abstract class IDefaultCommon(properties: Map<String, String>): ICommon(properties) {

    var creationDate: Date = Date()

    var name: String = this.toString(properties["name"])

    override fun formatDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        return format.format(this.creationDate)
    }

}