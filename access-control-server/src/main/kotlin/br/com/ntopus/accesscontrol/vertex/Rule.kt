//package br.com.ntopus.accesscontrol.model.vertex
//
//import br.com.ntopus.accesscontrol.model.GraphFactory_1
//import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
//import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
//import br.com.ntopus.accesscontrol.model.vertex.base.*
//import br.com.ntopus.accesscontrol.vertex.base.ICommon
//import br.com.ntopus.accesscontrol.vertex.base.IPermission
//import org.apache.tinkerpop.gremlin.structure.Vertex
//import java.util.*
//
//class Rule(properties: Map<String, String>): IPermission(properties) {
//
//    companion object {
//        fun findByCode(code: String): ICommon {
//            val g = GraphFactory_1.open().traversal()
//            val values = g.V().hasLabel(VertexLabel.RULE.label)
//                    .has(PropertyLabel.CODE.label, code).valueMap<Vertex>()
//            val rule = Rule(hashMapOf())
//            for (item in values) {
//                rule.name = item.get(PropertyLabel.NAME.label).toString()
//                rule.code = item.get(PropertyLabel.CODE.label).toString()
//                rule.enable = item.get(PropertyLabel.ENABLE.label) as Boolean
//                rule.description = item.get(PropertyLabel.DESCRIPTION.label).toString()
//                rule.creationDate = item.get(PropertyLabel.CREATION_DATE.label) as Date
//            }
//            return rule
//        }
//    }
//
//}