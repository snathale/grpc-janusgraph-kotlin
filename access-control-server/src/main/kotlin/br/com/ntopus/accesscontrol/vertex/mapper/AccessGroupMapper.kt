//package br.com.ntopus.accesscontrol.model.vertex.mapper
//
//import br.com.ntopus.accesscontrol.model.GraphFactory_1
//import br.com.ntopus.accesscontrol.vertex.data.Property
//import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
//import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
//import br.com.ntopus.accesscontrol.vertex.AccessGroup
//import br.com.ntopus.accesscontrol.vertex.base.FAILResponse
//import br.com.ntopus.accesscontrol.vertex.base.JSONResponse
//import br.com.ntopus.accesscontrol.vertex.base.SUCCESSResponse
//import br.com.ntopus.accesscontrol.model.vertex.mapper.graph.AccessGroupEdgeFactory
//import br.com.ntopus.accesscontrol.model.vertex.validator.AccessGroupValidator
//import br.com.ntopus.accesscontrol.vertex.mapper.IMapper
//import br.com.ntopus.accesscontrol.vertex.mapper.PermissionResponse
//import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
//
//class AccessGroupMapper(val properties: Map<String, String>) : IMapper {
//    private val graph = GraphFactory_1.open()
//    private val accessGroup = AccessGroup(properties)
//
//    override fun insert(): JSONResponse {
//        if (!AccessGroupValidator().canInsertVertex(this.accessGroup)) {
//            return FAILResponse(data = "@AGCVE-001 Empty Access Group properties")
//        }
//        try {
//            val accessGroup = graph.addVertex(VertexLabel.ACCESS_GROUP.label)
//            accessGroup.property(PropertyLabel.NAME.label, this.accessGroup.name)
//            accessGroup.property(PropertyLabel.CODE.label, this.accessGroup.code)
//            accessGroup.property(PropertyLabel.CREATION_DATE.label, this.accessGroup.creationDate)
//            accessGroup.property(PropertyLabel.ENABLE.label, this.accessGroup.enable)
//            if (!this.accessGroup.description.isEmpty()) {
//                accessGroup.property(PropertyLabel.DESCRIPTION.label, this.accessGroup.description)
//            }
//            graph.tx().commit()
//            this.accessGroup.id = accessGroup.longId()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@AGCVE-002 ${e.message.toString()}")
//        }
//        val response = PermissionResponse(
//                this.accessGroup.id!!,
//                this.accessGroup.code,
//                this.accessGroup.name,
//                this.accessGroup.formatDate(),
//                this.accessGroup.description,
//                this.accessGroup.enable
//        )
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun createEdge(target: VertexInfo, edgeTarget: String): JSONResponse {
//        if (!AccessGroupValidator().isCorrectVertexTarget(target)) {
//            return FAILResponse(data = "@AGCEE-001 Impossible create this edge with target code ${target.code}")
//        }
//        val vSource = AccessGroupValidator().hasVertex(this.accessGroup.code)
//                ?: return FAILResponse(
//                        data = "@AGCEE-002 Impossible find Access Group with code ${this.accessGroup.code}"
//                )
//        val vTarget = AccessGroupValidator().hasVertexTarget(target)
//                ?: return FAILResponse(
//                        data = "@AGCEE-003 Impossible find ${target.label.capitalize()} with code ${target.code}"
//                )
//        val edgeForTarget = AccessGroupEdgeFactory().edgeForTarget(target, edgeTarget)
//                ?: return FAILResponse(
//                        data = "@AGCEE-004 Impossible create a edge from Access Group with code ${this.accessGroup.code}"
//                )
//        return edgeForTarget.createEdge(vSource, vTarget, target, this.accessGroup.code)
//    }
//
//    override fun updateProperty(properties: List<Property>): JSONResponse {
//        val accessGroup = AccessGroupValidator().hasVertex(this.accessGroup.code)
//                ?: return FAILResponse(data = "AGUPE-001 Impossible find Access Group with code ${this.accessGroup.code}")
//        if (!AccessGroupValidator().canUpdateVertexProperty(properties)) {
//            return FAILResponse(data = "@AGUPE-002 Access Group property can be updated")
//        }
//
//        try {
//            for (property in properties) {
//                accessGroup.property(property.name, property.value)
//            }
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@AGUPE-003 ${e.message.toString()}")
//        }
//        val traversal = graph.traversal().V().hasLabel(VertexLabel.ACCESS_GROUP.label)
//                .has(PropertyLabel.CODE.label, this.accessGroup.code).next()
//        val values = AbstractMapper.parseMapVertex(traversal)
//        val response = PermissionResponse(
//                accessGroup.id() as Long,
//                this.accessGroup.code,
//                AbstractMapper.parseMapValue(values[PropertyLabel.NAME.label].toString()),
//                AbstractMapper.parseMapValueDate(values[PropertyLabel.CREATION_DATE.label].toString())!!,
//                AbstractMapper.parseMapValue((values[PropertyLabel.DESCRIPTION.label].toString())),
//                AbstractMapper.parseMapValue(values[PropertyLabel.ENABLE.label].toString()).toBoolean()
//        )
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun delete(): JSONResponse {
//        val user = AccessGroupValidator().hasVertex(this.accessGroup.code)
//                ?: return FAILResponse(data = "@AGDE-001 Impossible find Access Group with code ${this.accessGroup.code}")
//        try {
//            user.property(PropertyLabel.ENABLE.label, false)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@AGDE-002 ${e.message.toString()}")
//        }
//        return SUCCESSResponse(data = null)
//    }
//}