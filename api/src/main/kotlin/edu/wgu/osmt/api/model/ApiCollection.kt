package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.KeywordCount
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor


@JsonInclude(JsonInclude.Include.ALWAYS)
open class ApiCollection(
    collection: Collection,
    ss: List<RichSkillDescriptor>,
    keywords: Map<KeywordTypeEnum, List<KeywordCount>>,
    appConfig: AppConfig
): AbstractApiCollection(collection, ss, keywords, appConfig) {

    @get:JsonProperty
    val skillKeywords: Map<KeywordTypeEnum, List<KeywordCount>>
        get() = keywords

    companion object {
        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollection {
            val skills = collectionDao.skills.map{ it.toModel() }

            return ApiCollection(
                collectionDao.toModel(),
                skills,
                RichSkillDescriptor.getKeywordsFromSkills(skills),
                appConfig
            )
        }
    }
}