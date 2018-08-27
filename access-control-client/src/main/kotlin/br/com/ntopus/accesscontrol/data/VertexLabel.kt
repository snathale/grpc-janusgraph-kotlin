package br.com.ntopus.accesscontrol.data

enum class VertexLabel(val label: String) {
    ORGANIZATION("organization"),
    UNIT_ORGANIZATION("unitOrganization"),
    GROUP("group"),
    USER("user"),
    ACCESS_GROUP("accessGroup"),
    RULE("rule"),
    ACCESS_RULE("accessRule")
}