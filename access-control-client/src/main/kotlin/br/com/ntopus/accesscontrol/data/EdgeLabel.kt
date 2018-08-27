package br.com.ntopus.accesscontrol.data

enum class EdgeLabel(val label: String) {
    HAS("has"),
    PROVIDE("provide"),
    OWN("own"),
    ADD("add"),
    REMOVE("remove"),
    ASSOCIATED("associated"),
    INHERIT("inherit")
}