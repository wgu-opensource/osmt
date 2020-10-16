package edu.wgu.osmt.db

const val UNPUBLISHED = "Unpublished"
const val PUBLISHED = "Published"
const val ARCHIVED = "Archived"

enum class PublishStatus(val apiValue: String) {
    Unpublished(UNPUBLISHED),
    Published(PUBLISHED),
    Archived(ARCHIVED);

    companion object {
        const val DEFAULT_API_PUBLISH_STATUS_SET = "${UNPUBLISHED},${PUBLISHED}"
        val publishStatusSet = values().toSet()

        fun forApiValue(apiValue: String) = values().find { it.apiValue == apiValue }
    }
}
