package edu.wgu.osmt.db

const val UNARCHIVED = "unarchived"
const val DELETED = "deleted"

const val PUBLISHED = "published"
const val ARCHIVED = "archived"
const val DRAFT = "draft"

enum class PublishStatus(val apiValue: String) {
    Unarchived(UNARCHIVED),
    Deleted(DELETED),

    Published(PUBLISHED),
    Archived(ARCHIVED),
    Draft(DRAFT);

    companion object {
        const val DEFAULT_API_PUBLISH_STATUS_SET = "${DRAFT},${PUBLISHED}"
        val publishStatusSet = values().toSet()

        fun forApiValue(apiValue: String) = values().find { it.apiValue.toLowerCase() == apiValue.toLowerCase() }

        fun isCurrentlyPublished(status: PublishStatus): Boolean {
            return when(status) {
                Published, Unarchived -> true
                else -> false
            }
        }
    }
}
