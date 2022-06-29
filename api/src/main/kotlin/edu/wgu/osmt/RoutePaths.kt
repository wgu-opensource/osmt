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
    const val SKILL_UPDATE = "$SKILL_DETAIL/update"
    const val SKILL_AUDIT_LOG = "${SKILL_DETAIL}/log"


    const val COLLECTIONS_PATH = "/api/collections"
    const val COLLECTIONS_LIST = COLLECTIONS_PATH
    const val COLLECTION_CREATE = COLLECTIONS_PATH
    const val COLLECTION_PUBLISH = "$COLLECTIONS_PATH/publish"
    const val COLLECTION_DETAIL = "${COLLECTIONS_PATH}/{uuid}"
    const val COLLECTION_UPDATE = "${COLLECTION_DETAIL}/update"
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

    object QueryParams {
        const val FROM = "from"
        const val SIZE = "size"
        const val STATUS = "status"
        const val SORT = "sort"
        const val COLLECTION_ID = "collectionId"
    }

    fun createRegex(routePath: String): String {
        return routePath.replace("{uuid}", "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    }
}
