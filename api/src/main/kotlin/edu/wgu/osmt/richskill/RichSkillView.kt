package edu.wgu.osmt.richskill

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.api.model.ApiNamedReference
import net.minidev.json.JSONObject
import java.time.LocalDateTime

class RichSkillView {
    interface PublicDetailView {}
    interface PrivateDetailView : PublicDetailView {}
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ApiSkill(private val rsd: RichSkillDescriptor, private val baseUrl: String) {

    // TODO include view of collection

    @get:JsonView(RichSkillView.PublicDetailView::class)
    @JsonProperty("@context")
    val context = "https://rsd.osmt.dev/context-v1.json"

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val `type` = "RichSkillDescriptor"

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val author: ApiNamedReference?
        get() = rsd.author?.let { ApiNamedReference.fromKeyword(it) }

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
    val keywords: List<ApiNamedReference>
        get() = rsd.searchingKeywords.map { ApiNamedReference.fromKeyword(it) }

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
    val certifications: List<ApiNamedReference>
        get() = rsd.certifications.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val standards: List<ApiNamedReference>
        get() = rsd.standards.map { ApiNamedReference.fromKeyword(it) }

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val alignments: List<ApiNamedReference>
        get() = rsd.alignments.map { ApiNamedReference.fromKeyword(it) }

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
    val employers: List<ApiNamedReference>
        get() = rsd.employers.map { ApiNamedReference.fromKeyword(it) }
}



