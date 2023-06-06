package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.KeywordCount
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.util.*


@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiCollectionV2(
        collection: Collection,
        private val ss: List<RichSkillDescriptor>,
        private val appConfig: AppConfig
) : ApiCollection(collection, ss, EnumMap(KeywordTypeEnum::class.java), appConfig) {

    @get:JsonIgnore
    override val skillKeywords: Map<KeywordTypeEnum, List<KeywordCount>>
        get() = keywords

    @get:JsonIgnore
    override val skills: List<ApiSkillSummary>
        get() = ss.map { ApiSkillSummary.fromSkill(it, appConfig) }

    @get:JsonProperty
    val skillsV2: List<ApiSkillSummaryV2>
        get() = ss.map { ApiSkillSummaryV2.fromSkill(it, appConfig) }

    companion object {
        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollectionV2 {
            return ApiCollectionV2(collectionDao.toModel(), collectionDao.skills.map{ it.toModel() }, appConfig)
        }
    }
}