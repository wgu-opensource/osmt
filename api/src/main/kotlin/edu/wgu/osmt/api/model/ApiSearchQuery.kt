package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.db.PublishStatus

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ApiSearchQuery(
    @JsonProperty("query")
    val query: String?,

    @JsonProperty("skillName")
    val skillName: String?,

    @JsonProperty("collectionName")
    val collectionName: String?,

    @JsonProperty("category")
    val category: String?,

    @JsonProperty("skillStatement")
    val skillStatement: String?,

    @JsonProperty("keywords")
    val keywords: List<String>?,

    @JsonProperty("occupations")
    val occupations: List<ApiNamedReference>?,

    @JsonProperty("standards")
    val standards: List<ApiNamedReference>?,

    @JsonProperty("certifications")
    val certifications: List<ApiNamedReference>?,

    @JsonProperty("employers")
    val employers: List<ApiNamedReference>?,

    @JsonProperty("alignments")
    val alignments: List<ApiNamedReference>?
)
