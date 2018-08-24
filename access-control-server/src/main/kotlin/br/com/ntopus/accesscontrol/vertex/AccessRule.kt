package br.com.ntopus.accesscontrol.model.vertex

import br.com.ntopus.accesscontrol.vertex.base.ICommon
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

class AccessRule(properties: Map<String, String>): ICommon(properties) {
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    @SerializedName("expirationDate")
    val expirationDate: Date? = if (!this.toString(properties["expirationDate"]).isEmpty()) format.parse(this.toString(properties["expirationDate"])) else null

    override fun formatDate(): String? {
        if (this.expirationDate == null) return null
        return format.format(this.expirationDate)
    }
//    companion object: ICommon {
//        override fun findByCode(code: String): ICommon {
//            val g = GraphFactory_1.open().traversal()
//            val values = g.V().hasLabel(VertexLabel.ACCESS_RULE.label)
//                    .has(PropertyLabel.CODE.label, code).valueMap<Vertex>()
//            val accessRule = AccessRule(hashMapOf())
//            for (item in values) {
//                accessRule.name = item.get(PropertyLabel.NAME.label).toString()
//                accessRule.code = item.get(PropertyLabel.CODE.label).toString()
//                accessRule.enable = item.get(PropertyLabel.ENABLE.label) as Boolean
//                accessRule.observation = item.get(PropertyLabel.OBSERVATION.label).toString()
//                accessRule.creationDate = item.get(PropertyLabel.CREATION_DATE.label) as Date
//            }
//            return accessRule
//        }
//    }


}