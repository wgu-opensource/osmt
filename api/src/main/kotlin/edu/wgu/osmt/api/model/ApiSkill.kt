package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import net.minidev.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiSkill(private val rsd: RichSkillDescriptor, private val appConfig: AppConfig) {

    // TODO include view of collection

    @JsonProperty("@context")
    val context = "https://rsd.osmt.dev/context-v1.json"

    @JsonProperty
    val `type` = "RichSkillDescriptor"

    @get:JsonProperty
    val creator: String
        get() = appConfig.defaultCreatorUri

    @get:JsonProperty
    val author: ApiNamedReference?
        get() = rsd.author?.let { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val status: PublishStatus
        get() = rsd.publishStatus()

    @get:JsonProperty
    val creationDate: ZonedDateTime
        get() = rsd.creationDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val updateDate: ZonedDateTime
        get() = rsd.updateDate.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val publishDate: ZonedDateTime?
        get() = rsd.publishDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val archiveDate: ZonedDateTime?
        get() = rsd.archiveDate?.atZone(ZoneId.of("UTC"))

    @get:JsonProperty
    val skillName: String
        get() = rsd.name

    @get:JsonProperty
    val skillStatement: String
        get() = rsd.statement

    @get:JsonProperty
    val keywords: List<String>
        get() = rsd.searchingKeywords.mapNotNull { it.value }

    @get:JsonProperty
    val category: String?
        get() = rsd.category?.value

    @get:JsonProperty
    val id: String
        get() = rsd.canonicalUrl(appConfig.baseUrl)

    @get:JsonProperty
    val uuid: String
        get() = rsd.uuid.toString()

    @get:JsonProperty
    val certifications: List<ApiNamedReference>
        get() = rsd.certifications.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val standards: List<ApiNamedReference>
        get() = rsd.standards.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val alignments: List<ApiNamedReference>
        get() = rsd.alignments.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val occupations: List<ApiJobCode>
        get() = rsd.jobCodes.map { jobCode ->
            ApiJobCode.fromJobCode(jobCode)
        }

    @get:JsonProperty
    val employers: List<ApiNamedReference>
        get() = rsd.employers.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val collections: List<String>
        get() = rsd.collections.map { it.name }
}



