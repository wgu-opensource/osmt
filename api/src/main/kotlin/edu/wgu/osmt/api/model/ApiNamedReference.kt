package edu.wgu.osmt.api.model


data class ApiNamedReference(
    val id: String? = null,
    val name: String? = null
)

data class ApiReferenceListUpdate(
    val add: List<ApiNamedReference>? = null,
    val remove: List<ApiNamedReference>? = null
)

data class ApiStringListUpdate(
    val add: List<String>? = null,
    val remove: List<String>? = null
)

