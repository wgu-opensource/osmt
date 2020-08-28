package edu.wgu.osmt.richskill

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import net.minidev.json.JSONObject
import java.time.LocalDateTime
import java.util.*


class RichSkillView {
    interface PublicDetailView {}
    interface PrivateDetailView : PublicDetailView {}
}

class RichSkillDTO(rsd: RichSkillDescriptor, baseDomain: String) {

    // TODO don't include uri if null
    fun keywordJsonOutput(kw: Keyword): JSONObject = JSONObject(mutableMapOf("name" to kw.value, "uri" to kw.uri))

    @field:JsonView(RichSkillView.PublicDetailView::class)
    @JsonProperty("@context")
    val context = "https://rsd.osmt.dev/context-v1.json"

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val `type` = "RichSkillDescriptor"

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val author: String = rsd.author

    @field:JsonView(RichSkillView.PrivateDetailView::class)
    val creationDate: LocalDateTime = rsd.creationDate

    @field:JsonView(RichSkillView.PrivateDetailView::class)
    val updateDate: LocalDateTime = rsd.updateDate

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val skillName: String = rsd.name

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val skillStatement: String = rsd.statement

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val jobCodes: List<JobCode> = rsd.jobCodes

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val keywords: List<String> = rsd.searchingKeywords.map { it.value }

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val category: String? = rsd.category?.value

    @field:JsonView(RichSkillView.PublicDetailView::class)
    @JsonProperty("id")
    val canonicalUri: String = "$baseDomain/skill/${rsd.uuid}"

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val certifications: List<JSONObject> = rsd.certifications.map { keywordJsonOutput(it) }

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val standards: List<JSONObject> = rsd.profStds.map { keywordJsonOutput(it) }

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val alignments: List<Keyword> = rsd.alignments

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val occupations: List<JobCode> = rsd.jobCodes

    @field:JsonView(RichSkillView.PublicDetailView::class)
    val employers: List<JSONObject> = rsd.employers.map { keywordJsonOutput(it) }
}


