package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.ALWAYS)
class ExportRichSkill {
    @JsonProperty("query")
    val query: String? = null
    @JsonProperty("uuids")
    val uuids: List<String>? = null
}