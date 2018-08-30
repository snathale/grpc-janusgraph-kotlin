package br.com.ntopus.accesscontrol.vertex

import br.com.ntopus.accesscontrol.vertex.base.ICommon
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
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

    override fun mapperToVertexData(label: String): VertexData {
        var list: List<Property> = listOf()
        list+= Property(PropertyLabel.ID.label, this.id.toString())
        list+= Property(PropertyLabel.CODE.label, this.code)
        if (this.formatDate() != null) {
            list+= Property(PropertyLabel.EXPIRATION_DATE.label, this.toString(this.formatDate()))
        }
        list+= Property(PropertyLabel.ENABLE.label, this.enable.toString())
        return VertexData(VertexLabel.ACCESS_RULE.label, list)
    }

}