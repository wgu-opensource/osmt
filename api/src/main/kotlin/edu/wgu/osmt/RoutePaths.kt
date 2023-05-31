package edu.wgu.osmt

object RoutePaths {
    const val API = "/api"
    const val SEARCH_PATH = "$API/search"
    const val EXPORT = "$API/export"
    const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
    const val EXPORT_LIBRARY = "$EXPORT/library"
    const val EXPORT_LIBRARY_CSV = "$EXPORT_LIBRARY/csv"
    const val EXPORT_LIBRARY_XLSX = "$EXPORT_LIBRARY/xlsx"
    const val SEARCH_SIMILAR_SKILLS = "$SEARCH_SKILLS/similarity"
    const val SEARCH_SIMILARITIES = "$SEARCH_SKILLS/similarities"
    const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

    const val SKILLS_PATH = "$API/skills"
    const val SKILLS_LIST = SKILLS_PATH
    const val SKILLS_CREATE = SKILLS_PATH
    const val SKILLS_FILTER = "$SKILLS_PATH/filter"
    const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
    const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
    const val SKILL_DETAIL_XLSX = "$SKILLS_PATH/{uuid}/xlsx"
    const val SKILL_UPDATE = "$SKILL_DETAIL/update"
    const val SKILL_AUDIT_LOG = "$SKILL_DETAIL/log"
    const val EXPORT_SKILLS = "$EXPORT/skills"
    const val EXPORT_SKILLS_CSV = "$EXPORT_SKILLS/csv"
    const val EXPORT_SKILLS_XLSX = "$EXPORT_SKILLS/xlsx"

    const val CATEGORY_PATH = "$API/categories"
    const val CATEGORY_LIST = CATEGORY_PATH
    const val CATEGORY_DETAIL = "$CATEGORY_PATH/{identifier}"
    const val CATEGORY_SKILLS = "$CATEGORY_DETAIL/skills"

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

    const val METADATA_PATH = "$API/metadata"
    const val JOB_CODE_PATH = "$METADATA_PATH/jobcodes"
    const val JOB_CODE_CREATE = JOB_CODE_PATH
    const val JOB_CODE_LIST = JOB_CODE_PATH
    const val JOB_CODE_DETAIL = "$JOB_CODE_PATH/{id}"
    const val JOB_CODE_UPDATE = "$JOB_CODE_DETAIL/update"
    const val JOB_CODE_REMOVE = "$JOB_CODE_DETAIL/remove"

    const val NAMED_REFERENCES_PATH = "$METADATA_PATH/named-references"
    const val NAMED_REFERENCES_CREATE = NAMED_REFERENCES_PATH
    const val NAMED_REFERENCES_LIST = NAMED_REFERENCES_PATH
    const val NAMED_REFERENCES_DETAIL = "$NAMED_REFERENCES_PATH/{id}"
    const val NAMED_REFERENCES_UPDATE = "$NAMED_REFERENCES_DETAIL/update"
    const val NAMED_REFERENCES_REMOVE = "$NAMED_REFERENCES_DETAIL/remove"

    object QueryParams {
        const val FROM = "from"
        const val SIZE = "size"
        const val STATUS = "status"
        const val SORT = "sort"
        const val COLLECTION_ID = "collectionId"
    }
}
