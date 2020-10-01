package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.richskill.RichSkillDescriptor
import net.minidev.json.JSONObject
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ApiSkill(private val rsd: RichSkillDescriptor, private val baseUrl: String) {

    // TODO include view of collection

    @JsonProperty("@context")
    val context = "https://rsd.osmt.dev/context-v1.json"

    @JsonProperty
    val `type` = "RichSkillDescriptor"

    @get:JsonProperty
    val author: ApiNamedReference?
        get() = rsd.author?.let { ApiNamedReference.fromKeyword(it) }

    @get:JsonProperty
    val creationDate: LocalDateTime
        get() = rsd.creationDate

    @get:JsonProperty
    val updateDate: LocalDateTime
        get() = rsd.updateDate

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
        get() = rsd.canonicalUrl(baseUrl)

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
    val occupations: List<JSONObject>
        get() = rsd.jobCodes.map { jobCode ->
            JSONObject(
                mutableMapOf(
                    "code" to jobCode.code,
                    "id" to jobCode.url,
                    "name" to jobCode.name,
                    "framework" to jobCode.framework
                )
            )
        }

    @get:JsonProperty
    val employers: List<ApiNamedReference>
        get() = rsd.employers.map { ApiNamedReference.fromKeyword(it) }
}



