package edu.wgu.osmt.db

const val UNARCHIVED = "unarchived"
const val PUBLISHED = "published"
const val ARCHIVED = "archived"

enum class PublishStatus(val apiValue: String) {
    Unarchived(UNARCHIVED),
    Published(PUBLISHED),
    Archived(ARCHIVED);

    companion object {
        const val DEFAULT_API_PUBLISH_STATUS_SET = "${UNARCHIVED},${PUBLISHED}"
        val publishStatusSet = values().toSet()

        fun forApiValue(apiValue: String) = values().find { it.apiValue.toLowerCase() == apiValue.toLowerCase() }
    }
}
