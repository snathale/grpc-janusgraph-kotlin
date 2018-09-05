package br.com.ntopus.accesscontrol.vertex.mapper

import br.com.ntopus.accesscontrol.factory.GraphFactory
import br.com.ntopus.accesscontrol.vertex.data.*
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex
import java.text.SimpleDateFormat
import java.util.*

object AbstractMapper {
    fun parseMapValue (value: String): String {
        if (value == "null") return ""
        return value.replace("[","").replace("]","")
    }

    fun parseMapVertex(vertex: Vertex): Map<String, String> {
        val values: MutableMap<String, String> = mutableMapOf("id" to vertex.id().toString())
        for (item in vertex.properties<Vertex>()) {
            values[item.key()] = toString(item.value())
        }
        return values
    }

    fun parseMapValueDate(date: String): String? {
        if (date.isEmpty()) return null
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val defaultFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
        return format.format(defaultFormat.parse(parseMapValue(date)))
    }

    fun parseEdgeToEdgeData(edge: Edge): EdgeData {
        var list: List<Property> = listOf()
        list += Property(PropertyLabel.ID.label, toString(edge.id()))
        for (item in edge.properties<Edge>()) {
            list+= Property(item.key(), toString(item.value()))
        }
        val source = VertexInfo(edge.outVertex().id() as Long, edge.outVertex().label())
        val target = VertexInfo(edge.inVertex().id() as Long, edge.inVertex().label())
        return EdgeData(source, target, edge.label(), list)
    }

    fun parseVertexToVertexData(vertex: Vertex): VertexData {
        var list: List<Property> = listOf()
        list += Property(PropertyLabel.ID.label, toString(vertex.id()))
        for (item in vertex.properties<Vertex>()) {
            if (item.key() == PropertyLabel.EXPIRATION_DATE.label || item.key() == PropertyLabel.CREATION_DATE.label ){
                list+= Property(item.key(), parseMapValueDate(item.value().toString())!!)
                continue
            }
            list+= Property(item.key(), toString(item.value()))
        }
        return VertexData(vertex.label(), list)
    }

    fun toString(value: Any?): String {
        if (value.toString() == "null") {
            return ""
        }
        return value.toString()
    }

}