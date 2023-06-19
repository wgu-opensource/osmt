package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum

@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiKeyword(
    private val keyword: Keyword
) {
    @get:JsonProperty
    val id: Long?
        get() = keyword.id

    @get:JsonProperty
    val name: String?
        get() = keyword.value
    
    @get:JsonProperty
    val framework: String?
        get() = keyword.framework

    @get:JsonProperty
    val type: KeywordTypeEnum
        get() = keyword.type

    @get:JsonProperty
    val url: String?
        get() = keyword.uri

    companion object {
        fun fromDao(
            keywordDao: KeywordDao,
        ): ApiKeyword {
            return ApiKeyword(
                keyword = keywordDao.toModel(),
            )
        }
    }
}
