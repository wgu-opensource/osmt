package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.keyword.KeywordCount
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.time.ZoneId
import java.time.ZonedDateTime

abstract class AbstractApiCollection(
        private val collection: Collection,
        private val ss: List<RichSkillDescriptor>,
        val keywords: Map<KeywordTypeEnum, List<KeywordCount>>,
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
        get() = collection.uuid

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
        get() = collection.author?.let { it.value }

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
    val skills: List<ApiSkillSummary>
        get() = ss.map { ApiSkillSummary.fromSkill(it, appConfig) }

    @get:JsonProperty
    val owner: String?
        get() = collection.workspaceOwner
}
