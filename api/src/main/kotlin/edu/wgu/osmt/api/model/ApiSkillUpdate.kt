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

    @JsonProperty("collections")
    val collections: ApiStringListUpdate? = null,

    @JsonProperty("authors")
    val authors: ApiStringListUpdate? = null,

    @JsonProperty("categories")
    val categories: ApiStringListUpdate? = null,

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
    val occupations: ApiStringListUpdate? = null
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
}