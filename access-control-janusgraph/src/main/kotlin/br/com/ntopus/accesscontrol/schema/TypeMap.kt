package br.com.ntopus.accesscontrol.schema

import org.janusgraph.core.attribute.Geoshape
import java.util.*
import kotlin.collections.HashMap

class TypeMap {
    companion object {
        val MAP: HashMap<String, Class<*>> = hashMapOf(
                "String" to String::class.javaObjectType,
                "Character" to Character::class.javaObjectType,
                "Boolean" to Boolean::class.javaObjectType,
                "Byte" to Byte::class.javaObjectType,
                "Short" to Short::class.javaObjectType,
                "Integer" to Int::class.javaObjectType,
                "Long" to Long::class.javaObjectType,
                "Float" to Float::class.java,
                "Geoshape" to Geoshape::class.javaObjectType,
                "UUID" to UUID::class.javaObjectType,
                "Date" to Date::class.javaObjectType
        )
    }
}