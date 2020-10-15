package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.time.ZoneId
import java.time.ZonedDateTime


@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiCollection(private val collection: Collection, private val ss: List<RichSkillDescriptor>, private val appConfig: AppConfig) {
    @get:JsonProperty
    val id: String
        get() = collection.canonicalUrl(appConfig.baseUrl)

    @JsonProperty("@context")
    val context = "https://rsd.osmt.dev/context-v1.json"

    @JsonProperty
    val `type` = "RichSkillCollection"

    @get:JsonProperty
    val uuid: String
        get() = collection.uuid.toString()

    @get:JsonProperty
    val name: String
        get() = collection.name

    @get:JsonProperty
    val creator: String
        get() = appConfig.defaultCreatorUri

    @get:JsonProperty
    val author: ApiNamedReference?
        get() = collection.author?.let { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val status: PublishStatus
        get() = collection.publishStatus()

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
    val skills: List<ApiSkillSummary>
        get() = ss.map { ApiSkillSummary.fromSkill(it, appConfig) }

    companion object {
        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollection {
            return ApiCollection(collectionDao.toModel(), collectionDao.skills.map{ it.toModel() }, appConfig)
        }
    }
}