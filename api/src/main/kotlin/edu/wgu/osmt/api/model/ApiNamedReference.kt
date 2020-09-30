package edu.wgu.osmt.api.model

import edu.wgu.osmt.keyword.Keyword


data class ApiNamedReference(
    val id: String? = null,
    val name: String? = null
) {
    companion object factory {
        fun fromKeyword(keyword: Keyword): ApiNamedReference {
            return ApiNamedReference(id=keyword.uri, name=keyword.value)
        }
    }
}

data class ApiReferenceListUpdate(
    val add: List<ApiNamedReference>? = null,
    val remove: List<ApiNamedReference>? = null
)

data class ApiStringListUpdate(
    val add: List<String>? = null,
    val remove: List<String>? = null
)

