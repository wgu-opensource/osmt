package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.keyword.KeywordCount
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.time.ZoneId
import java.time.ZonedDateTime


@JsonInclude(JsonInclude.Include.ALWAYS)
open class ApiCollection(
        @JsonIgnore open val collection: Collection,
        @JsonIgnore open val ss: List<RichSkillDescriptor>,
        @JsonIgnore open val keywords: Map<KeywordTypeEnum, List<KeywordCount>>,
        private val appConfig: AppConfig
) {
    @get:JsonProperty
    val id: String
        get() = collection.canonicalUrl(appConfig.baseUrl)

    @JsonProperty("@context")
    val context = appConfig.rsdContextUrl

    @JsonProperty
    val `type` = "RichSkillCollection"

    @get:JsonProperty
    val uuid: String
        get() = collection.uuid.toString()

    @get:JsonProperty
    val name: String
        get() = collection.name

    @get:JsonProperty
    val description: String?
        get() = collection.description

    @get:JsonProperty
    val creator: String
        get() = appConfig.defaultCreatorUri

    @get:JsonProperty
    val author: String?
        get() = collection.author?.value

    @get:JsonProperty
    val status: PublishStatus?
        get() = collection.status

    @get:JsonProperty
    val creationDate: ZonedDateTime
        get() = collection.creationDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val updateDate: ZonedDateTime
        get() = collection.updateDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val publishDate: ZonedDateTime?
        get() = collection.publishDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val archiveDate: ZonedDateTime?
        get() = collection.archiveDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    open val skills: List<ApiSkillSummary>
        get() = ss.map { ApiSkillSummary.fromSkill(it, appConfig) }

    @get:JsonProperty
    open val skillKeywords: Map<KeywordTypeEnum, List<KeywordCount>>
        get() = keywords

    @get:JsonProperty
    val owner: String?
        get() = collection.workspaceOwner

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