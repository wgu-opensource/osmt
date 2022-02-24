package edu.wgu.osmt

object RoutePaths {
    const val SEARCH_PATH = "/api/search"
    const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
    const val SEARCH_SIMILAR_SKILLS = "$SEARCH_SKILLS/similarity"
    const val SEARCH_SIMILARITIES = "$SEARCH_SKILLS/similarities"
    const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

    const val SKILLS_PATH = "/api/skills"
    const val SKILLS_LIST = SKILLS_PATH
    const val SKILLS_CREATE = SKILLS_PATH
    const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
    const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
    const val SKILL_IMPORT = "$SKILLS_PATH/import"
    const val SKILL_REMOVE = "$SKILL_DETAIL/remove"
    const val SKILL_UPDATE = "$SKILL_DETAIL/update"
    const val SKILL_SHARE_EXTERNALLY = "$SKILL_DETAIL/share"
    const val SKILL_UNSHARE_EXTERNALLY = "$SKILL_DETAIL/unshare"
    const val SKILL_AUDIT_LOG = "${SKILL_DETAIL}/log"


    const val COLLECTIONS_PATH = "/api/collections"
    const val COLLECTIONS_LIST = COLLECTIONS_PATH
    const val COLLECTION_CREATE = COLLECTIONS_PATH
    const val COLLECTION_PUBLISH = "$COLLECTIONS_PATH/publish"
    const val COLLECTION_DETAIL = "${COLLECTIONS_PATH}/{uuid}"
    const val COLLECTION_IMPORT = "$COLLECTIONS_PATH/import"
    const val COLLECTION_REMOVE = "${COLLECTION_DETAIL}/remove"
    const val COLLECTION_UPDATE = "${COLLECTION_DETAIL}/update"
    const val COLLECTION_SHARE_EXTERNALLY = "$COLLECTION_DETAIL/share"
    const val COLLECTION_UNSHARE_EXTERNALLY = "$COLLECTION_DETAIL/unshare"
    const val COLLECTION_SKILLS_UPDATE = "${COLLECTION_DETAIL}/updateSkills"
    const val COLLECTION_SKILLS = "${COLLECTION_DETAIL}/skills"
    const val COLLECTION_AUDIT_LOG = "${COLLECTION_DETAIL}/log"
    const val COLLECTION_CSV = "${COLLECTION_DETAIL}/csv"


    const val TASKS_PATH = "/api/results"
    const val TASK_DETAIL_TEXT = "${TASKS_PATH}/text/{uuid}"
    const val TASK_DETAIL_BATCH = "${TASKS_PATH}/batch/{uuid}"
    const val TASK_DETAIL_SKILLS = "${TASKS_PATH}/skills/{uuid}"

    const val SEARCH_JOBCODES_PATH = "$SEARCH_PATH/jobcodes"

    const val SEARCH_KEYWORDS_PATH = "$SEARCH_PATH/keywords"

    const val SEARCH_HUB_PATH = "/api/external"
    const val SEARCH_HUB_LIBRARIES = "$SEARCH_HUB_PATH/libraries"
    const val SEARCH_HUB_SEARCH_COLLECTIONS = "$SEARCH_HUB_PATH/search/collections"
    const val SEARCH_HUB_SEARCH_SKILLS = "$SEARCH_HUB_PATH/search/skills"

    object QueryParams {
        const val FROM = "from"
        const val SIZE = "size"
        const val STATUS = "status"
        const val SORT = "sort"
        const val COLLECTION_ID = "collectionId"
    }

    fun scrubForConfigure(routePath: String): String {
        return routePath.replace("{uuid}", "*")
    }
}
