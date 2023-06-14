package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus

data class ApiSkillUpdateV2(
    @JsonProperty("skillName")
    override val skillName: String? = null,
    
    @JsonProperty("skillStatement")
    override val skillStatement: String? = null,
    
    @JsonProperty("status")
    override val publishStatus: PublishStatus? = null,
    
    @JsonProperty("collections")
    override val collections: ApiStringListUpdate? = null,
    
    @JsonProperty("author")
    val author: String? = null,
    
    @JsonProperty("category")
    val category: String? = null,
    
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
}
