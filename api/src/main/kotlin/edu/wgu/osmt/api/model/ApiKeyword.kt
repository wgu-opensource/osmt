package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordTypeEnum
import org.springframework.beans.factory.annotation.Autowired

@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiKeyword(
    private val keyword: Keyword,
    private val totalSkills: Long?,
    private val appConfig: AppConfig
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

    @get:JsonProperty("url")
    val url: String?
        get() = keyword.uri

    @get:JsonProperty
    val skillCount: Long?
        get() = totalSkills

    @get:JsonProperty
    val publicUrl: String
        get() = "${appConfig.baseUrl}/api/metadata/keywords/${id}"

    companion object {
        fun fromDao(
            keywordDao: KeywordDao,
            appConfig: AppConfig
        ): ApiKeyword {
            return ApiKeyword(
                keyword = keywordDao.toModel(),
                totalSkills = keywordDao.skills.count(),
                appConfig = appConfig
            )
        }

        fun fromModel(
            keyword: Keyword,
            appConfig: AppConfig
        ): ApiKeyword {
            return ApiKeyword(
                totalSkills = keyword.id?.let { KeywordDao.findById(it)?.skills?.count() ?: 0 },
                keyword = keyword,
                appConfig = appConfig
            )
        }
    }
}
