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

    fun asRsdUpdateObject(keywordRepository: KeywordRepository, jobcodeRepository: JobCodeRepository): RsdUpdateObject {
        val authorKeyword = author?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Author, value=it.name, uri=it.id)
        }

        val categoryKeyword = category?.let {
            keywordRepository.findOrCreate(KeywordTypeEnum.Category, value=it)
        }

        val addingKeywords = mutableListOf<Keyword>()
        val removingKeywords = mutableListOf<Keyword>()
        val jobsToAdd = mutableListOf<JobCode>()
        val jobsToRemove = mutableListOf<JobCode>()

        fun lookup_references(lud: ListUpdate, keywordType: KeywordTypeEnum) {
            lud.add?.map {
                keywordRepository.findOrCreate(keywordType, value=it.name, uri=it.id)
            }?.let {
                addingKeywords.addAll(it)
            }

            lud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value=it.name, uri=it.id)
            }?.let {
                removingKeywords.addAll(it.map { it?.toModel() }.filterNotNull())
            }
        }

        fun lookup_keywords(slud: StringListUpdate, keywordType: KeywordTypeEnum) {
            slud.add?.map {
                keywordRepository.findOrCreate(keywordType, value=it)
            }?.let {
                addingKeywords.addAll(it)
            }

            slud.remove?.map {
                keywordRepository.findByValueOrUri(keywordType, value=it)
            }?.let {
                removingKeywords.addAll(it.map { it?.toModel() }.filterNotNull())
            }
        }

        occupations?.let {
            it.add?.map {
                jobcodeRepository.findByCodeOrCreate(code=it)
            }?.let { jobsToAdd.addAll(it.filterNotNull()) }

            it.remove?.map {
                jobcodeRepository.findByCode(it)
            }?.let { jobsToRemove.addAll(it.filterNotNull()) }
        }

        keywords?.let { lookup_keywords(it, KeywordTypeEnum.Keyword) }
        certifications?.let { lookup_references(it, KeywordTypeEnum.Certification) }
        standards?.let { lookup_references(it, KeywordTypeEnum.Standard) }
        alignments?.let { lookup_references(it, KeywordTypeEnum.Alignment) }
        employers?.let { lookup_references(it, KeywordTypeEnum.Employer) }

        val allKeywordsUpdate = if (addingKeywords.size > 0 || removingKeywords.size > 0) {
            ListFieldUpdate<Keyword>(
                add=if (addingKeywords.size > 0) addingKeywords else listOf(),
                remove=if (removingKeywords.size > 0) removingKeywords else listOf()
            )
        } else {
            null
        }

        val jobCodesUpdate = if (jobsToAdd.size > 0 || jobsToRemove.size > 0) {
            ListFieldUpdate<JobCode>(
                add=if (jobsToAdd.size > 0) jobsToAdd else listOf(),
                remove=if (jobsToRemove.size > 0) jobsToRemove else listOf()
            )
        } else {
            null
        }

        return RsdUpdateObject(
            name = skillName,
            statement = skillStatement,
            publishStatus = publishStatus,
            author = authorKeyword?.let { NullableFieldUpdate(it) },
            category = categoryKeyword?.let { NullableFieldUpdate(it) },
            keywords = allKeywordsUpdate,
            jobCodes = jobCodesUpdate
        )
    }

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

