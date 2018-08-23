package br.com.ntopus.accesscontrol.vertex

import br.com.ntopus.accesscontrol.vertex.base.IAgent

class User(properties: Map<String, String>): IAgent(properties) {

//    companion object {
//        override fun findByCode(code: String): ICommon {
//            val g = GraphFactory.open().traversal()
//            val values = g.V().hasLabel(VertexLabel.USER.label)
//                    .has(PropertyLabel.CODE.label, code).valueMap<Vertex>()
//            val user = User(hashMapOf())
//            for (item in values) {
//                user.name = item.get(PropertyLabel.NAME.label).toString()
//                user.code = item.get(PropertyLabel.CODE.label).toString()
//                user.enable = item.get(PropertyLabel.ENABLE.label) as Boolean
//                user.observation = item.get(PropertyLabel.OBSERVATION.label).toString()
//                user.creationDate = item.get(PropertyLabel.CREATION_DATE.label) as Date
//            }
//            return user
//        }
//    }

}