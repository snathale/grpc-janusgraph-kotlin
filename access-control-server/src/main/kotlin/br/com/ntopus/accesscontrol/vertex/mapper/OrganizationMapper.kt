package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.Organization
import br.com.ntopus.accesscontrol.vertex.validator.OrganizationValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.data.*
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse
import org.apache.tinkerpop.gremlin.structure.Edge

class OrganizationMapper (val properties: Map<String, String>): IMapper {
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

    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.EdgeResponse {
        if (!OrganizationValidator().isCorrectVertexTarget(target)) {
            return ProtoResponse.createEdgeErrorResponse(
                    "@OCEE-001 Impossible create this edge with target id ${target.id}"
            )
        }
        val organization = OrganizationValidator()
                .hasVertex(this.organization.id)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@OCEE-002 Impossible find Organization with id ${this.organization.id}"
                )

        val unitOrganization = OrganizationValidator().hasVertexTarget(target)
                ?: return ProtoResponse.createEdgeErrorResponse(
                        "@OCEE-003 Impossible find Unit Organization with id ${target.id}"
                )
        val edge: Edge
        try {
            edge = organization.addEdge(EdgeLabel.HAS.label, unitOrganization)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createEdgeErrorResponse("@OCEE-004 ${e.message.toString()}")
        }
        val property = listOf(Property(PropertyLabel.ID.label, edge.id().toString()))
        val response = EdgeData(
                VertexInfo(this.organization.id, VertexLabel.ORGANIZATION.label),
                target, EdgeLabel.HAS.label, property
        )
        return ProtoResponse.createEdgeSuccessResponse(response)
    }

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