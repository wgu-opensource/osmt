package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.keyword.KeywordTypeEnum
import javax.validation.constraints.NotBlank

data class ApiKeywordUpdate (
    @NotBlank
    @JsonProperty("name")
    val name: String,
    @JsonProperty("uri")
    val uri: String? = null,
    @NotBlank
    @JsonProperty("type")
    val type: KeywordTypeEnum,
    @JsonProperty("framework")
    val framework: String? = null
)
