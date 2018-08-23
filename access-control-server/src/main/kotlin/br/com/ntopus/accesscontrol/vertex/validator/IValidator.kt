package br.com.ntopus.accesscontrol.model.vertex.validator

import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.base.ICommon
import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
import org.apache.tinkerpop.gremlin.structure.Vertex

interface IValidator {
    fun canInsertVertex(vertex: ICommon): Boolean
    fun canUpdateVertexProperty(properties: List<Property>): Boolean
    fun hasVertexTarget(target: VertexInfo): Vertex?
    fun hasVertex(code: String): Vertex?
    fun isCorrectVertexTarget(target: VertexInfo): Boolean
    fun hasProperty(code: String, property: Property): Boolean
}