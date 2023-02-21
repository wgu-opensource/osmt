package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ApiSearch(
    @JsonProperty("query")
    val query: String? = null,

    @JsonProperty("advanced")
    val advanced: ApiAdvancedSearch? = null,

    @JsonProperty("filtered")
    val filtered: ApiAdvancedFilteredSearch? = null,

    @JsonProperty("uuids")
    val uuids: List<String>? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ApiAdvancedSearch(
    @JsonProperty("skillName")
    val skillName: String? = null,

    @JsonProperty("collectionName")
    val collectionName: String? = null,

    @JsonProperty("category")
    val category: String? = null,

    @JsonProperty("skillStatement")
    val skillStatement: String? = null,

    @JsonProperty("author")
    val author: String? = null,

    @JsonProperty("keywords")
    val keywords: List<String>? = null,

    @JsonProperty("occupations")
    val occupations: List<String>? = null,

    @JsonProperty("standards")
    val standards: List<ApiNamedReference>? = null,

    @JsonProperty("certifications")
    val certifications: List<ApiNamedReference>? = null,

    @JsonProperty("employers")
    val employers: List<ApiNamedReference>? = null,

    @JsonProperty("alignments")
    val alignments: List<ApiNamedReference>? = null
)

data class ApiSkillListUpdate(
    @JsonProperty("add")
    val add: ApiSearch? = null,

    @JsonProperty("remove")
    val remove: ApiSearch? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ApiAdvancedFilteredSearch(
    @JsonProperty("statuses")
    val statuses: Array<String>? = null,

    @JsonProperty("statement")
    val skillStatement: String? = null,

    @JsonProperty("categories")
    val categories: Array<String>? = null,

    @JsonProperty("keywords")
    val keywords: Array<String>? = null,

    @JsonProperty("standards")
    val standards: Array<String>? = null,

    @JsonProperty("certifications")
    val certifications: List<ApiNamedReference>? = null,

    @JsonProperty("alignments")
    val alignments: List<ApiNamedReference>? = null,

    @JsonProperty("jobcodes")
    val jobCodes: List<String>? = null

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApiAdvancedFilteredSearch

        if (statuses != null) {
            if (other.statuses == null) return false
            if (!statuses.contentEquals(other.statuses)) return false
        } else if (other.statuses != null) return false

        return true
    }

    override fun hashCode(): Int {
        return statuses?.contentHashCode() ?: 0
    }
}