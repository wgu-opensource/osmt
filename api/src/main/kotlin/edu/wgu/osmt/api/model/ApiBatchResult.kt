package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiBatchResult(
    @JsonProperty("success")
    val success: Boolean = false,

    @JsonProperty("message")
    val message: String? = null,

    @JsonProperty("modifiedCount")
    val modifiedCount: Number? = null,

    @JsonProperty("totalCount")
    val totalCount: Number? = null
)
