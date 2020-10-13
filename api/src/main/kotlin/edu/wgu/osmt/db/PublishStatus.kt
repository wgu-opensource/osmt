package edu.wgu.osmt.db

enum class PublishStatus(val apiValue: String) {
    Unpublished("unpublished"),
    Published("published"),
    Archived("archived");

    companion object {
        fun forApiValue(apiValue: String) = values().find { it.apiValue == apiValue }
    }
}
