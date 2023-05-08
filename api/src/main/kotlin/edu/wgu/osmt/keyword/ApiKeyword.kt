package edu.wgu.osmt.keyword

import edu.wgu.osmt.api.model.ApiNamedReference

data class ApiKeyword(
    val id: Long?,
    val name: String?,
    val value: String?,
    val type: KeywordTypeEnum,
    val framework: String?
) {

    companion object factory {
        fun fromKeyword(keyword: Keyword): ApiKeyword {
            return ApiKeyword(keyword.id, keyword.value, keyword.value, keyword.type, keyword.framework)
        }
    }

}