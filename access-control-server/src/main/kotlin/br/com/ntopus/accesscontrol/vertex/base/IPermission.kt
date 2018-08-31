package br.com.ntopus.accesscontrol.vertex.base

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel

abstract class IPermission(properties: Map<String, String>): IDefaultCommon(properties) {

    var description: String = this.toString(properties["description"])

    override fun mapperToVertexData(label: String): VertexData {
        var list: List<Property> = listOf()
        list+= Property(PropertyLabel.ID.label, this.id.toString())
        list+= Property(PropertyLabel.NAME.label, this.name)
        list+= Property(PropertyLabel.CODE.label, this.code)
        list+= Property(PropertyLabel.CREATION_DATE.label, this.formatDate())
        if (!this.description.isEmpty()) {
            list+= Property(PropertyLabel.DESCRIPTION.label, this.description)
        }
        list+= Property(PropertyLabel.ENABLE.label, this.enable.toString())
        return VertexData(label, list)
    }

}