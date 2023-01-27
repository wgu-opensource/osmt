package edu.wgu.osmt.db

const val WORKSPACE = "workspace"

enum class CollectionStatus(val apiValue: String) {
    Published(PUBLISHED),
    Archived(ARCHIVED),
    Draft(DRAFT),
    Workspace(WORKSPACE);
}
