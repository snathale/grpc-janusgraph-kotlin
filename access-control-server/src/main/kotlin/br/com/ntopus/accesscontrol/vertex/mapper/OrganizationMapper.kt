package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.Organization
import br.com.ntopus.accesscontrol.model.vertex.validator.OrganizationValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.proto.ProtoVertexResponse

class OrganizationMapper (val properties: Map<String, String>): IMapper {

    private val organization = Organization(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        try {
            if (!OrganizationValidator().canInsertVertex(this.organization)) {
                return ProtoVertexResponse.createErrorResponse("@OCVE-001 Empty Organization properties")
            }
            val organization = graph.addVertex(VertexLabel.ORGANIZATION.label)
            organization.property(PropertyLabel.NAME.label, this.organization.name)
            organization.property(PropertyLabel.CODE.label, this.organization.code)
            if (!this.organization.observation.isEmpty()) {
                organization.property(PropertyLabel.OBSERVATION.label, this.organization.observation)
            }
            organization.property(PropertyLabel.CREATION_DATE.label, this.organization.creationDate)
            organization.property(PropertyLabel.ENABLE.label, this.organization.enable)
            graph.tx().commit()
            this.organization.id = organization.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoVertexResponse.createErrorResponse("@OCVE-002 ${e.message.toString()}")
        }
        return ProtoVertexResponse.createSuccessResponse(this.organization.mapperToVertexData())
    }


//    override fun createEdge(target: VertexInfo, edgeTarget: String): JSONResponse {
//        if (!OrganizationValidator().isCorrectVertexTarget(target)) {
//            return FAILResponse(data = "@OCEE-001 Impossible create this edge with target code ${target.code}")
//        }
//        val organization = OrganizationValidator()
//                .hasVertex(this.organization.code)
//                ?: return FAILResponse(data = "@OCEE-002 Impossible find Organization with code ${this.organization.code}")
//
//        val unitOrganization = OrganizationValidator().hasVertexTarget(target)
//                ?: return FAILResponse(data = "@OCEE-003 Impossible find Unit Organization with code ${target.code}")
//
//        try {
//            organization.addEdge(EdgeLabel.HAS.label, unitOrganization)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@OCEE-004 ${e.message.toString()}")
//        }
//        val response = EdgeCreated(VertexInfo(VertexLabel.ORGANIZATION.label, this.organization.code), target, EdgeLabel.HAS.label)
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun updateProperty(properties: List<Property>): JSONResponse {
//        val organization = OrganizationValidator()
//                .hasVertex(this.organization.code)
//                ?: return FAILResponse(data = "@OUPE-001 Impossible find Organization with code ${this.organization.code}")
//
//        if (!OrganizationValidator().canUpdateVertexProperty(properties)) {
//            return FAILResponse(data = "@OUPE-002 Organization property can be updated")
//        }
//        try {
//            for (property in properties) {
//                organization.property(property.name, property.value)
//            }
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@OUPE-003 ${e.message.toString()}")
//        }
//        val traversal = graph.traversal().V().hasLabel(VertexLabel.ORGANIZATION.label)
//                .has(PropertyLabel.CODE.label, this.organization.code).next()
//        val values = AbstractMapper.parseMapVertex(traversal)
//        val response = AgentResponse(
//                organization.id() as Long,
//                this.organization.code,
//                AbstractMapper.parseMapValue(values[PropertyLabel.NAME.label].toString()),
//                AbstractMapper.parseMapValueDate(values[PropertyLabel.CREATION_DATE.label].toString())!!,
//                AbstractMapper.parseMapValue(values[PropertyLabel.ENABLE.label].toString()).toBoolean(),
//                AbstractMapper.parseMapValue((values[PropertyLabel.OBSERVATION.label].toString()))
//        )
//        return SUCCESSResponse(data = response)
//    }
//
//    override fun delete(): JSONResponse {
//        val organization = OrganizationValidator()
//                .hasVertex(this.organization.code)
//                ?: return FAILResponse(data = "@ODE-001 Impossible find Organization with code ${this.organization.code}")
//        try {
//            organization.property(PropertyLabel.ENABLE.label, false)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@ODE-002 ${e.message.toString()}")
//        }
//        return SUCCESSResponse(data = null)
//    }
}