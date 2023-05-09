package edu.wgu.osmt.keyword

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiKeywordUpdate(
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("value")
    val value: String?,
    @JsonProperty("type")
    val type: KeywordTypeEnum,
    @JsonProperty("framework")
    val framework: String?
) {
}

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