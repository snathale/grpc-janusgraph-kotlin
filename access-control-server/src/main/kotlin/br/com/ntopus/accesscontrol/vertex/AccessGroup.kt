//package br.com.ntopus.accesscontrol.vertex
//
//import br.com.ntopus.accesscontrol.model.GraphFactory
//import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
//import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
//import br.com.ntopus.accesscontrol.model.vertex.base.*
//import br.com.ntopus.accesscontrol.vertex.base.ICommon
//import br.com.ntopus.accesscontrol.vertex.base.IPermission
//import org.apache.tinkerpop.gremlin.structure.Vertex
//import java.util.*
//
//class AccessGroup(properties: Map<String, String>): IPermission(properties) {
//
//    companion object {
//        fun findByCode(code: String): ICommon {
//            val g = GraphFactory.open().traversal()
//            val values = g.V().hasLabel(VertexLabel.ACCESS_GROUP.label)
//                    .has(PropertyLabel.CODE.label, code).valueMap<Vertex>()
//            val acce = AccessGroup(hashMapOf())
//            for (item in values) {
//                acce.name = item.get(PropertyLabel.NAME.label).toString()
//                acce.code = item.get(PropertyLabel.CODE.label).toString()
//                acce.enable = item.get(PropertyLabel.ENABLE.label) as Boolean
//                acce.description = item.get(PropertyLabel.DESCRIPTION.label).toString()
//                acce.creationDate = item.get(PropertyLabel.CREATION_DATE.label) as Date
//            }
//            return acce
//        }
//    }
//
//}