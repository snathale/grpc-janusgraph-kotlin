package br.com.ntopus.accesscontrol.vertex.base

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexData
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel

abstract class IAgent(properties: Map<String, String>): IDefaultCommon(properties) {

    var observation: String = this.toString(properties["observation"])

    override fun mapperToVertexData(): VertexData {
        var list: List<Property> = listOf()
        list+= Property(PropertyLabel.ID.label, this.id.toString())
        list+= Property(PropertyLabel.CODE.label, this.code)
        list+= Property(PropertyLabel.CREATION_DATE.label, this.formatDate())
        if (!this.observation.isEmpty()) {
            list+= Property(PropertyLabel.OBSERVATION.label, this.observation)
        }
        list+= Property(PropertyLabel.ENABLE.label, this.enable.toString())
        return VertexData(VertexLabel.USER.label, list)
    }

}