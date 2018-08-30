package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.UnitOrganization
import br.com.ntopus.accesscontrol.model.vertex.validator.UnitOrganizationValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.proto.ProtoVertexResponse

class UnitOrganizationMapper (val properties: Map<String, String>): IMapper {
    private val unitOrganization = UnitOrganization(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        try {
            if (!UnitOrganizationValidator().canInsertVertex(this.unitOrganization)) {
                return ProtoVertexResponse.createErrorResponse("@UOCVE-001 Empty Unit Organization properties")
            }
            val unitOrganization = graph.addVertex(VertexLabel.UNIT_ORGANIZATION.label)
            unitOrganization.property(PropertyLabel.NAME.label, this.unitOrganization.name)
            unitOrganization.property(PropertyLabel.CODE.label, this.unitOrganization.code)
            unitOrganization.property(PropertyLabel.CREATION_DATE.label, this.unitOrganization.creationDate)
            unitOrganization.property(PropertyLabel.ENABLE.label, this.unitOrganization.enable)
            if (!this.unitOrganization.observation.isEmpty()) {
                unitOrganization.property(PropertyLabel.OBSERVATION.label, this.unitOrganization.observation)
            }
            graph.tx().commit()
            this.unitOrganization.id = unitOrganization.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoVertexResponse.createErrorResponse("@UOCVE-002 ${e.message.toString()}")
        }
        return ProtoVertexResponse.createSuccessResponse(this.unitOrganization.mapperToVertexData(VertexLabel.UNIT_ORGANIZATION.label))
    }

//    override fun createEdge(target: VertexInfo, edgeTarget: String): JSONResponse {
//        if (!UnitOrganizationValidator().isCorrectVertexTarget(target)) {
//            return FAILResponse(data = "@UOCEE-001 Impossible create edge with target code ${target.code}")
//        }
//        val unitOrganization = UnitOrganizationValidator()
//                .hasVertex(this.unitOrganization.code)
//                ?: return FAILResponse(
//                        data = "@UOCEE-002 Impossible find Unit Organization with code ${this.unitOrganization.code}"
//                )
//
//        val group = UnitOrganizationValidator().hasVertexTarget(target)
//                ?: return FAILResponse(data = "@UOCEE-003 Impossible find Group with code ${target.code}")
//        try {
//            unitOrganization.addEdge(EdgeLabel.HAS.label, group)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@UOCEE-004 ${e.message.toString()}")
//        }
//        val response = EdgeCreated(
//                VertexInfo(VertexLabel.UNIT_ORGANIZATION.label, this.unitOrganization.code),
//                target, EdgeLabel.HAS.label
//        )
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun updateProperty(properties: List<Property>): JSONResponse {
//        val unitOrganization = UnitOrganizationValidator()
//                .hasVertex(this.unitOrganization.code)
//                ?: return FAILResponse(
//                        data = "@UOCEE-001 Impossible find Unit Organization with code ${this.unitOrganization.code}"
//                )
//        if (!UnitOrganizationValidator().canUpdateVertexProperty(properties)) {
//            return FAILResponse(data = "@UOUPE-002 Unit Organization property can be updated")
//        }
//        try {
//            for (property in properties) {
//                unitOrganization.property(property.name, property.value)
//            }
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@UOUPE-003 ${e.message.toString()}")
//        }
//        val traversal = graph.traversal().V().hasLabel(VertexLabel.UNIT_ORGANIZATION.label)
//                .has(PropertyLabel.CODE.label, this.unitOrganization.code).next()
//        val values = AbstractMapper.parseMapVertex(traversal)
//        val response = AgentResponse(
//                unitOrganization.id() as Long,
//                this.unitOrganization.code,
//                AbstractMapper.parseMapValue(values[PropertyLabel.NAME.label].toString()),
//                AbstractMapper.parseMapValueDate(values[PropertyLabel.CREATION_DATE.label].toString())!!,
//                AbstractMapper.parseMapValue(values[PropertyLabel.ENABLE.label].toString()).toBoolean(),
//                AbstractMapper.parseMapValue((values[PropertyLabel.OBSERVATION.label].toString()))
//        )
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun delete(): JSONResponse {
//        val unitOrganization = UnitOrganizationValidator()
//                .hasVertex(this.unitOrganization.code)
//                ?: return FAILResponse(data = "@UODE-001 Impossible find Unit Organization with code ${this.unitOrganization.code}")
//        try {
//            unitOrganization.property(PropertyLabel.ENABLE.label, false)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@UODE-002 ${e.message.toString()}")
//        }
//        return SUCCESSResponse(data = null)
//    }
}