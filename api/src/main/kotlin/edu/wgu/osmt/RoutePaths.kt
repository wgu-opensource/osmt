package edu.wgu.osmt

object RoutePaths {
    const val API = "/api"
    const val SEARCH_PATH = "$API/search"
    const val EXPORT = "$API/export"
    const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
    const val EXPORT_LIBRARY = "$EXPORT/library"
    const val SEARCH_SIMILAR_SKILLS = "$SEARCH_SKILLS/similarity"
    const val SEARCH_SIMILARITIES = "$SEARCH_SKILLS/similarities"
    const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

    const val SKILLS_PATH = "$API/skills"
    const val SKILLS_LIST = SKILLS_PATH
    const val SKILLS_CREATE = SKILLS_PATH
    const val SKILLS_FILTER = "$SKILLS_PATH/filter"
    const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
    const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
    const val SKILL_UPDATE = "$SKILL_DETAIL/update"
    const val SKILL_AUDIT_LOG = "$SKILL_DETAIL/log"
    const val EXPORT_SKILLS = "$EXPORT/skills"


    const val COLLECTIONS_PATH = "$API/collections"
    const val COLLECTIONS_LIST = COLLECTIONS_PATH
    const val COLLECTION_CREATE = COLLECTIONS_PATH
    const val COLLECTION_PUBLISH = "$COLLECTIONS_PATH/publish"
    const val COLLECTION_DETAIL = "$COLLECTIONS_PATH/{uuid}"
    const val COLLECTION_UPDATE = "$COLLECTION_DETAIL/update"
    const val COLLECTION_SKILLS_UPDATE = "$COLLECTION_DETAIL/updateSkills"
    const val COLLECTION_SKILLS = "$COLLECTION_DETAIL/skills"
    const val COLLECTION_AUDIT_LOG = "$COLLECTION_DETAIL/log"
    const val COLLECTION_CSV = "$COLLECTION_DETAIL/csv"
    const val COLLECTION_XLSX = "$COLLECTION_DETAIL/xlsx"
    const val COLLECTION_REMOVE = "$COLLECTION_DETAIL/remove"

    const val WORKSPACE_PATH = "$API/workspace"

    const val TASKS_PATH = "$API/results"
    const val TASK_DETAIL_TEXT = "$TASKS_PATH/text/{uuid}"
    const val TASK_DETAIL_MEDIA = "$TASKS_PATH/media/{uuid}"
    const val TASK_DETAIL_BATCH = "$TASKS_PATH/batch/{uuid}"
    const val TASK_DETAIL_SKILLS = "$TASKS_PATH/skills/{uuid}"

    const val SEARCH_JOBCODES_PATH = "$SEARCH_PATH/jobcodes"
    const val SEARCH_KEYWORDS_PATH = "$SEARCH_PATH/keywords"

    const val ES_ADMIN = "$API/es-admin"
    const val ES_ADMIN_DELETE_INDICES = "$ES_ADMIN/delete-indices"
    const val ES_ADMIN_REINDEX = "$ES_ADMIN/reindex"

    object QueryParams {
        const val FROM = "from"
        const val SIZE = "size"
        const val STATUS = "status"
        const val SORT = "sort"
        const val COLLECTION_ID = "collectionId"
    }
}
