package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty

data class JobCodeUpdate(
    @JsonProperty("code")
    val code: String = "",
    @JsonProperty("frameworkName")
    val framework: String = "",
    @JsonProperty("targetNodeName")
    val targetNodeName: String = "",
    @JsonProperty("level")
    val level: String = "",
    @JsonProperty("description")
    val description: String = ""
) {
}