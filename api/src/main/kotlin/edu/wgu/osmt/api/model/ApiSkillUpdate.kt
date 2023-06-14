package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus

data class ApiSkillUpdate(
    @JsonProperty("skillName")
    override val skillName: String? = null,
    
    @JsonProperty("skillStatement")
    override val skillStatement: String? = null,
    
    @JsonProperty("status")
    override val publishStatus: PublishStatus? = null,
    
    @JsonProperty("collections")
    override val collections: ApiStringListUpdate? = null,
    
    @JsonProperty("authors")
    val authors: ApiStringListUpdate? = null,
    
    @JsonProperty("categories")
    val categories: ApiStringListUpdate? = null,
    
    @JsonProperty("keywords")
    override val keywords: ApiStringListUpdate? = null,
    
    @JsonProperty("certifications")
    override val certifications: ApiReferenceListUpdate? = null,
    
    @JsonProperty("standards")
    override val standards: ApiAlignmentListUpdate? = null,
    
    @JsonProperty("alignments")
    override val alignments: ApiAlignmentListUpdate? = null,
    
    @JsonProperty("employers")
    override val employers: ApiReferenceListUpdate? = null,
    
    @JsonProperty("occupations")
    override val occupations: ApiStringListUpdate? = null
) : SkillUpdate {
    
    fun validate(rowNumber: Number? = null): List<ApiFieldError>? {
        val errors = mutableListOf<ApiFieldError>()
        return if (errors.size > 0) errors else null
    }
    
    fun validateForCreation(rowNumber: Number? = null): List<ApiFieldError>? {
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

interface SkillUpdate {
    val skillName: String?
    val skillStatement: String?
    val publishStatus: PublishStatus?
    val collections: ApiStringListUpdate?
    val keywords: ApiStringListUpdate?
    val certifications: ApiReferenceListUpdate?
    val standards: ApiAlignmentListUpdate?
    val alignments: ApiAlignmentListUpdate?
    val employers: ApiReferenceListUpdate?
    val occupations: ApiStringListUpdate?
}