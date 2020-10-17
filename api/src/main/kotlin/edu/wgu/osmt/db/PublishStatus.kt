package edu.wgu.osmt.db

const val UNPUBLISHED = "unpublished"
const val PUBLISHED = "published"
const val ARCHIVED = "archived"

enum class PublishStatus(val apiValue: String) {
    Unpublished(UNPUBLISHED),
    Published(PUBLISHED),
    Archived(ARCHIVED);

    companion object {
        const val DEFAULT_API_PUBLISH_STATUS_SET = "${UNPUBLISHED},${PUBLISHED}"
        val publishStatusSet = values().toSet()

        fun forApiValue(apiValue: String) = values().find { it.apiValue == apiValue.toLowerCase() }
    }
}
