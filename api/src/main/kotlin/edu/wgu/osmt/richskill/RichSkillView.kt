package edu.wgu.osmt.richskill

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.api.ApiFieldError
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordDTO
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import net.minidev.json.JSONObject
import java.time.LocalDateTime

class RichSkillView {
    interface PublicDetailView {}
    interface PrivateDetailView : PublicDetailView {}
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class RichSkillDTO(private val rsd: RichSkillDescriptor, private val baseUrl: String) {

    // TODO include view of collection

    @get:JsonView(RichSkillView.PublicDetailView::class)
    @JsonProperty("@context")
    val context = "https://rsd.osmt.dev/context-v1.json"

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val `type` = "RichSkillDescriptor"

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val author: KeywordDTO?
        get() = rsd.author?.let { KeywordDTO(it) }

    @get:JsonView(RichSkillView.PrivateDetailView::class)
    val creationDate: LocalDateTime
        get() = rsd.creationDate

    @get:JsonView(RichSkillView.PrivateDetailView::class)
    val updateDate: LocalDateTime
        get() = rsd.updateDate

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val skillName: String
        get() = rsd.name

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val skillStatement: String
        get() = rsd.statement

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val keywords: List<KeywordDTO>
        get() = rsd.searchingKeywords.map { kw -> KeywordDTO(kw) }

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val category: String?
        get() = rsd.category?.value

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val id: String
        @JsonProperty("id")
        get() = rsd.canonicalUrl(baseUrl)

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val uuid: String
        @JsonProperty("uuid")
        get() = rsd.uuid.toString()

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val certifications: List<KeywordDTO>
        get() = rsd.certifications.map { KeywordDTO(it) }

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val standards: List<KeywordDTO>
        get() = rsd.standards.map { KeywordDTO(it) }

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val alignments: List<KeywordDTO>
        get() = rsd.alignments.map { KeywordDTO(it) }

    @get:JsonView(RichSkillView.PublicDetailView::class)
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

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val employers: List<KeywordDTO>
        get() = rsd.employers.map { KeywordDTO(it) }
}


data class NamedReference(
  val id: String? = null,
  val name: String? = null
)

data class ListUpdate(
    val add: List<NamedReference>? = null,
    val remove: List<NamedReference>? = null
)

data class StringListUpdate(
    val add: List<String>? = null,
    val remove: List<String>? = null
)

data class RsdUpdateDTO(
    @JsonProperty("skillName")
    val skillName: String?,

    @JsonProperty("skillStatement")
    val skillStatement: String?,

    @JsonProperty("status")
    val publishStatus: PublishStatus?,

    @JsonProperty("category")
    val category: String?,

    @JsonProperty("author")
    val author: NamedReference?,

    @JsonProperty("keywords")
    val keywords: StringListUpdate?,

    @JsonProperty("certifications")
    val certifications: ListUpdate?,

    @JsonProperty("standards")
    val standards: ListUpdate?,

    @JsonProperty("alignments")
    val alignments: ListUpdate?,

    @JsonProperty("employers")
    val employers: ListUpdate?,

    @JsonProperty("occupations")
    val occupations: StringListUpdate?
) {

    fun validate(): List<ApiFieldError>? {
        val errors = mutableListOf<ApiFieldError>()
        return if (errors.size > 0) errors else null
    }

    fun validateForCreation(rowNumber:Number? = null): List<ApiFieldError>? {
        val errors = mutableListOf<ApiFieldError>()

        if (skillName.isNullOrBlank()) {
            errors.add(ApiFieldError(field="skillName", message="Name is required", rowNumber=rowNumber))
        }
        if (skillStatement.isNullOrBlank()) {
            errors.add(ApiFieldError(field="skillStatement", message="Statement is required", rowNumber=rowNumber))
        }

        validate()?.let { errors.addAll(it) }

        return if (errors.size > 0) errors else null
    }
}

