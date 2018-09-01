package br.com.ntopus.accesscontrol.vertex.validator

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.Property
import br.com.ntopus.accesscontrol.vertex.base.ICommon
import br.com.ntopus.accesscontrol.vertex.base.IDefaultCommon
import br.com.ntopus.accesscontrol.vertex.mapper.VertexInfo
import org.apache.tinkerpop.gremlin.structure.Vertex

abstract class DefaultValidator: IValidator {
    val graph = GraphFactory.open()

    override fun hasVertex(id: Long): Vertex? {
        return try {
            graph.traversal().V(id).next()
        } catch (e: Exception) {
            null
        }
    }

    override fun canInsertVertex(vertex: ICommon): Boolean {
        if ((vertex as IDefaultCommon).name.isEmpty() || vertex.code.isEmpty()) {
            return false
        }
        return true
    }

    override fun hasVertexTarget(target: VertexInfo): Vertex? {
        return null
    }

    override fun isCorrectVertexTarget(target: VertexInfo): Boolean {
        return false
    }

    override fun canUpdateVertexProperty(properties: List<Property>): Boolean {
        return false
    }

    override fun hasProperty(code: String, property: Property): Boolean {
        return false
    }
}