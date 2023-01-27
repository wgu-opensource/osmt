package edu.wgu.osmt.db

const val WORKSPACE = "workspace"

enum class CollectionStatusEnum(val apiValue: String) {
    Published(PUBLISHED),
    Archived(ARCHIVED),
    Draft(DRAFT),
    Workspace(WORKSPACE);

    open val displayName: String = this.name

    companion object {
        fun forApiValue(apiValue: String) = CollectionStatusEnum.values().find { it.name.toLowerCase() == apiValue.toLowerCase() }
    }
}
