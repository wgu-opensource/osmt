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
class ApiCollectionV2(
    private val collection: Collection,
    private val ss: List<RichSkillDescriptor>,
    private val appConfig: AppConfig
) : IApiCollection {
    @get:JsonProperty
    override val id: String
        get() = collection.canonicalUrl(appConfig.baseUrl)

    @JsonProperty("@context")
    val context = appConfig.rsdContextUrl

    @JsonProperty
    val `type` = "RichSkillCollection"

    @get:JsonProperty
    override val uuid: String
        get() = collection.uuid

    @get:JsonProperty
    override val name: String
        get() = collection.name

    @get:JsonProperty
    override val description: String?
        get() = collection.description

    @get:JsonProperty
    override val creator: String
        get() = appConfig.defaultCreatorUri

    @get:JsonProperty
    override val author: String?
        get() = collection.author?.let { it.value }

    @get:JsonProperty
    override val status: PublishStatus?
        get() = collection.status

    @get:JsonProperty
    override val creationDate: ZonedDateTime
        get() = collection.creationDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    override val updateDate: ZonedDateTime
        get() = collection.updateDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    override val publishDate: ZonedDateTime?
        get() = collection.publishDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    override val archiveDate: ZonedDateTime?
        get() = collection.archiveDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    override val skills: List<ApiSkillSummary>
        get() = ss.map { ApiSkillSummary.fromSkill(it, appConfig) }

    @get:JsonProperty
    override val owner: String?
        get() = collection.workspaceOwner

    companion object {
        fun fromDao(collectionDao: CollectionDao, appConfig: AppConfig): ApiCollectionV2 {
            return ApiCollectionV2(collectionDao.toModel(), collectionDao.skills.map{ it.toModel() }, appConfig)
        }
    }
}