package edu.wgu.osmt.db

import com.fasterxml.jackson.annotation.JsonProperty

const val UNARCHIVED = "unarchived"
const val DELETED = "deleted"
const val WORKSPACE = "workspace"
const val PUBLISHED = "published"
const val ARCHIVED = "archived"
const val DRAFT = "draft"

enum class PublishStatus(val apiValue: String) {
    @JsonProperty("unarchived")
    Unarchived(UNARCHIVED),
    @JsonProperty("deleted")
    Deleted(DELETED),
    @JsonProperty("workspace")
    Workspace(WORKSPACE),
    @JsonProperty("published")
    Published(PUBLISHED),
    @JsonProperty("archived")
    Archived(ARCHIVED),
    @JsonProperty("draft")
    Draft(DRAFT);

    companion object {
        const val DEFAULT_API_PUBLISH_STATUS_SET = "${DRAFT},${PUBLISHED}"
        val publishStatusSet = values().toSet()

        fun forApiValue(apiValue: String) = values().find { it.apiValue.toLowerCase() == apiValue.toLowerCase() }
    }
}
