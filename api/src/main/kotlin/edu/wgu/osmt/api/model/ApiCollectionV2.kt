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
        @JsonIgnore override val ss: List<RichSkillDescriptor>,
        @JsonIgnore override val keywords: Map<KeywordTypeEnum, List<KeywordCount>>,
        @JsonIgnore private val appConfig: AppConfig
) : ApiCollection(collection, ss, keywords, appConfig) {

    @get:JsonIgnore
    override val skillKeywords: Map<KeywordTypeEnum, List<KeywordCount>>
        get() = keywords

    @get:JsonIgnore
    override val skills: List<ApiSkillSummary>
        get() = ss.map { ApiSkillSummary.fromSkill(it, appConfig) }

    @get:JsonProperty("skills")
    val skillsV2: List<ApiSkillSummaryV2>
        get() = ss.map { ApiSkillSummaryV2.fromSkill(it, appConfig) }

    companion object {
        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollectionV2 {
            val skills = collectionDao.skills.map{ it.toModel() }

            return ApiCollectionV2(
                    collectionDao.toModel(),
                    collectionDao.skills.map{ it.toModel() },
                    RichSkillDescriptor.getKeywordsFromSkills(skills),
                    appConfig)
        }

        fun fromLatest(apiCollection: ApiCollection, appConfig: AppConfig) : ApiCollectionV2{

            val result = ApiCollectionV2(
                    collection = apiCollection.collection,
                    ss = apiCollection.ss,
                    keywords = apiCollection.keywords,
                    appConfig
            )

            return result
        }


    }
}