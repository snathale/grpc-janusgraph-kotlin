package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.PropertyLabel
import br.com.ntopus.accesscontrol.vertex.validator.GroupValidator
import br.com.ntopus.accesscontrol.proto.AccessControlServer
import br.com.ntopus.accesscontrol.vertex.Group
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.data.VertexLabel
import br.com.ntopus.accesscontrol.vertex.proto.ProtoResponse

class GroupMapper (val properties: Map<String, String>): IMapper {
    override fun createEdge(target: VertexInfo, edgeTarget: String): AccessControlServer.VertexResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val group = Group(properties)
    private val graph = GraphFactory.open()

    override fun insert(): AccessControlServer.VertexResponse {
        if (!GroupValidator().canInsertVertex(this.group)) {
            return ProtoResponse.createVertexErrorResponse("@GCVE-001 Empty Group properties")
        }
        try {
            val group = graph.addVertex(VertexLabel.GROUP.label)
            group.property(PropertyLabel.NAME.label, this.group.name)
            group.property(PropertyLabel.CODE.label, this.group.code)
            if (!this.group.observation.isEmpty()) {
                group.property(PropertyLabel.OBSERVATION.label, this.group.observation)
            }
            group.property(PropertyLabel.CREATION_DATE.label, this.group.creationDate)
            group.property(PropertyLabel.ENABLE.label, this.group.enable)
            graph.tx().commit()
            this.group.id = group.longId()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse("@GCVE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(this.group.mapperToVertexData(VertexLabel.GROUP.label))
    }

    override fun updateProperty(properties: List<Property>): AccessControlServer.VertexResponse {
        val group = GroupValidator().hasVertex(this.group.id)
                ?: return ProtoResponse.createVertexErrorResponse("@GUPE-001 Impossible find Group with id ${this.group.id}")

        if (!GroupValidator().canUpdateVertexProperty(properties)) {
            return ProtoResponse.createVertexErrorResponse( "@GUPE-002 Group property can be updated")
        }
        try {
            for (property in properties) {
                group.property(property.name, property.value)
            }
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            return ProtoResponse.createVertexErrorResponse( "@GUPE-003 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse(AbstractMapper.parseVertexToVertexData(group))
    }

    override fun delete(): AccessControlServer.VertexResponse {
        val group = GroupValidator().hasVertex(this.group.id)
                ?: return ProtoResponse.createVertexErrorResponse("@GDE-001 Impossible find Group with id ${this.group.id}")
        try {
            group.property(PropertyLabel.ENABLE.label, false)
            graph.tx().commit()
        } catch (e: Exception) {
            graph.tx().rollback()
            ProtoResponse.createVertexErrorResponse("@GDE-002 ${e.message.toString()}")
        }
        return ProtoResponse.createVertexSuccessResponse()
    }
//
//    override fun createEdge(target: VertexInfo, edgeTarget: String): JSONResponse {
//        if (!GroupValidator().isCorrectVertexTarget(target)) {
//            return FAILResponse(data = "@GCEE-001 Impossible create edge with target code ${target.code}")
//        }
//        val vSource = GroupValidator().hasVertex(this.group.code)
//                ?: return FAILResponse(data = "@GCEE-002 Impossible find Group with code ${this.group.code}")
//
//        val vTarget = GroupValidator().hasVertexTarget(target)
//                ?: return FAILResponse(data ="@GCEE-003 Impossible find Group with code ${target.code}")
//        try {
//            vSource.addEdge(EdgeLabel.HAS.label, vTarget)
//            graph.tx().commit()
//        } catch (e: Exception) {
//            graph.tx().rollback()
//            return FAILResponse(data = "@GCEE-004 ${e.message.toString()}")
//        }
//        val response = EdgeCreated(VertexInfo(VertexLabel.GROUP.label, this.group.code), target, EdgeLabel.HAS.label)
//        return SUCCESSResponse(data = response)
//    }
}