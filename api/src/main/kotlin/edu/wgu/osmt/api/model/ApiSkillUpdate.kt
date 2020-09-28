package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus

data class ApiSkillUpdate(
    @JsonProperty("skillName")
    val skillName: String?,

    @JsonProperty("skillStatement")
    val skillStatement: String?,

    @JsonProperty("status")
    val publishStatus: PublishStatus?,

    @JsonProperty("category")
    val category: String?,

    @JsonProperty("author")
    val author: ApiNamedReference?,

    @JsonProperty("keywords")
    val keywords: ApiStringListUpdate?,

    @JsonProperty("certifications")
    val certifications: ApiReferenceListUpdate?,

    @JsonProperty("standards")
    val standards: ApiReferenceListUpdate?,

    @JsonProperty("alignments")
    val alignments: ApiReferenceListUpdate?,

    @JsonProperty("employers")
    val employers: ApiReferenceListUpdate?,

    @JsonProperty("occupations")
    val occupations: ApiStringListUpdate?
) {

    fun validate(): List<ApiFieldError>? {
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