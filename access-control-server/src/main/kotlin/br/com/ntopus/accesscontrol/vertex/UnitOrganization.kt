//package br.com.ntopus.accesscontrol.model.vertex
//
//import br.com.ntopus.accesscontrol.model.GraphFactory_1
//import br.com.ntopus.accesscontrol.model.data.*
//import br.com.ntopus.accesscontrol.vertex.base.IAgent
//import br.com.ntopus.accesscontrol.vertex.base.ICommon
//import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
//import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
//import sun.security.provider.certpath.Vertex
//import java.util.*
//
//class UnitOrganization(properties: Map<String, String>): IAgent(properties) {
//    companion object {
//        fun findByCode(code: String): ICommon {
//            val g = GraphFactory_1.open().traversal()
//            val values = g.V().hasLabel(VertexLabel.UNIT_ORGANIZATION.label)
//                    .has(PropertyLabel.CODE.label, code).valueMap<Vertex>()
//            val unitOrganization = UnitOrganization(hashMapOf())
//            for (item in values) {
//                unitOrganization.name = item.get("name").toString()
//                unitOrganization.code = item.get("code").toString()
//                unitOrganization.enable = item.get("enable") as Boolean
//                unitOrganization.observation = item.get("observation").toString()
//                unitOrganization.creationDate = item.get("creationDate") as Date
//            }
//            return unitOrganization
//        }
//    }
//
//}