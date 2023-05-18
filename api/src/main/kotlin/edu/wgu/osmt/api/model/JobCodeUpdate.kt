package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.JobCodeLevel

data class JobCodeUpdate(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("targetNode")
    val targetNode: String? = null,
    @JsonProperty("targetNodeName")
    val targetNodeName: String? = null,
    @JsonProperty("frameworkName")
    val framework: String? = null,
    @JsonProperty("level")
    val level: JobCodeLevel? = null,
    @JsonProperty("parents")
    val parents: List<JobCodeUpdate>? = null
) {
}