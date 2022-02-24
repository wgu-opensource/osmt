package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus

data class ApiSkillUpdate(
    @JsonProperty("skillName")
    val skillName: String? = null,

    @JsonProperty("skillStatement")
    val skillStatement: String? = null,

    @JsonProperty("status")
    val publishStatus: PublishStatus? = null,

    @JsonProperty("category")
    val category: String? = null,

    @JsonProperty("collections")
    val collections: ApiStringListUpdate? = null,

    @JsonProperty("author")
    val author: String? = null,

    @JsonProperty("keywords")
    val keywords: ApiStringListUpdate? = null,

    @JsonProperty("certifications")
    val certifications: ApiReferenceListUpdate? = null,

    @JsonProperty("standards")
    val standards: ApiAlignmentListUpdate? = null,

    @JsonProperty("alignments")
    val alignments: ApiAlignmentListUpdate? = null,

    @JsonProperty("employers")
    val employers: ApiReferenceListUpdate? = null,

    @JsonProperty("occupations")
    val occupations: ApiStringListUpdate? = null,

    @JsonProperty("clonedFrom")
    val clonedFrom: String? = null,
) {

    fun validate(rowNumber:Number? = null): List<ApiFieldError>? {
        val errors = mutableListOf<ApiFieldError>()
        return if (errors.size > 0) errors else null
    }

    fun validateForCreation(rowNumber:Number? = null): List<ApiFieldError>? {
        val errors = mutableListOf<ApiFieldError>()

        if (skillName.isNullOrBlank()) {
            errors.add(ApiFieldError(field = "skillName", message = "Name is required", rowNumber = rowNumber))
        }
        if (skillStatement.isNullOrBlank()) {
            errors.add(
                ApiFieldError(
                    field = "skillStatement",
                    message = "Statement is required",
                    rowNumber = rowNumber
                )
            )
        }

        validate()?.let { errors.addAll(it) }

        return if (errors.size > 0) errors else null
    }

    companion object {

        fun fromApiSkill(apiSkill: ApiSkill): ApiSkillUpdate {
            return ApiSkillUpdate(
                    skillName=apiSkill.skillName,
                    skillStatement= apiSkill.skillStatement,
                    publishStatus= if (apiSkill.archiveDate != null) PublishStatus.Archived else PublishStatus.Published,
                    category= apiSkill.category,
                    author= apiSkill.author,
                    keywords= ApiStringListUpdate(add=apiSkill.keywords),
                    certifications= ApiReferenceListUpdate(add=apiSkill.certifications),
                    standards= ApiAlignmentListUpdate(add=apiSkill.standards),
                    alignments= ApiAlignmentListUpdate(add=apiSkill.alignments),
                    employers= ApiReferenceListUpdate(add=apiSkill.employers),
                    occupations= ApiStringListUpdate(add=apiSkill.occupations.map { it.code }),
            )
        }
    }
}