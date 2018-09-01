package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.Organization
import br.com.ntopus.accesscontrol.vertex.validator.OrganizationValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse

class OrganizationMapper (val properties: Map<String, String>): IMapper {
    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.VertexResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val organization = Organization(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        try {
            if (!OrganizationValidator().canInsertVertex(this.organization)) {
                return ProtoResponse.createVertexErrorResponse("@OCVE-001 Empty Organization properties")
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
            return ProtoResponse.createVertexErrorResponse("@OCVE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(this.organization.mapperToVertexData(VertexLabel.ORGANIZATION.label))
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

    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val organization = OrganizationValidator()
                .hasVertex(this.organization.id)
                ?: return ProtoResponse.createVertexErrorResponse("@OUPE-001 Impossible find Organization with id ${this.organization.id}")

        if (!OrganizationValidator().canUpdateVertexProperty(properties)) {
            return ProtoResponse.createVertexErrorResponse("@OUPE-002 Organization property can be updated")
        }
        try {
            for (property in properties) {
                organization.property(property.name, property.value)
            }
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@OUPE-003 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(organization))
    }

    override fun delete(): AccessControlServer.VertexResponse {
        val organization = OrganizationValidator()
                .hasVertex(this.organization.id)
                ?: return ProtoResponse.createVertexErrorResponse(
                        "@ODE-001 Impossible find Organization with id ${this.organization.id}")
        try {
            organization.property(PropertyLabel.ENABLE.label, false)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@UDE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse()
    }
}