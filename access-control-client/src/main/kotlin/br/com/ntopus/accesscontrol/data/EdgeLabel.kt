package br.com.ntopus.accesscontrol.vertex.data

enum class EdgeLabel(val label: String) {
    HAS("has"),
    PROVIDE("provide"),
    OWN("own"),
    ADD("add"),
    REMOVE("remove"),
    ASSOCIATED("associated"),
    INHERIT("inherit")
}