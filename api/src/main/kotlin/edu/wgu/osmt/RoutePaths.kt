package edu.wgu.osmt

object RoutePaths {
    const val SUPPORTED = "/v2"
    const val VERSION3 = "/v3"
    const val LATEST = VERSION3


    const val API = "/api"
    private const val SEARCH_PATH = "$API/search"

    //export
    private const val EXPORT = "$API/export"
    const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
    private const val EXPORT_LIBRARY = "$EXPORT/library"
    const val EXPORT_LIBRARY_CSV = "$EXPORT_LIBRARY/csv"
    const val EXPORT_LIBRARY_XLSX = "$EXPORT_LIBRARY/xlsx"
    private const val EXPORT_SKILLS = "$EXPORT/skills"
    const val EXPORT_SKILLS_CSV = "$EXPORT_SKILLS/csv"
    const val EXPORT_SKILLS_XLSX = "$EXPORT_SKILLS/xlsx"
    const val SEARCH_SIMILAR_SKILLS = "$SEARCH_SKILLS/similarity"
    const val SEARCH_SIMILARITIES = "$SEARCH_SKILLS/similarities"
    const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

    //skills
    private const val SKILLS_PATH = "$API/skills"
    const val SKILLS_LIST = SKILLS_PATH
    const val SKILLS_CREATE = SKILLS_PATH
    const val SKILLS_FILTER = "$SKILLS_PATH/filter"
    const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
    const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
    const val SKILL_UPDATE = "$SKILL_DETAIL/update"
    const val SKILL_AUDIT_LOG = "$SKILL_DETAIL/log"

    //categories
    private const val CATEGORY_PATH = "$API/categories"
    const val CATEGORY_LIST = CATEGORY_PATH
    const val CATEGORY_DETAIL = "$CATEGORY_PATH/{identifier}"
    const val CATEGORY_SKILLS = "$CATEGORY_DETAIL/skills"

    //collections
    private const val COLLECTIONS_PATH = "/collections"
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

    private const val TASKS_PATH = "$API/results"
    const val TASK_DETAIL_TEXT = "$TASKS_PATH/text/{uuid}"
    const val TASK_DETAIL_MEDIA = "$TASKS_PATH/media/{uuid}"
    const val TASK_DETAIL_BATCH = "$TASKS_PATH/batch/{uuid}"
    const val TASK_DETAIL_SKILLS = "$TASKS_PATH/skills/{uuid}"

    const val SEARCH_JOBCODES_PATH = "$SEARCH_PATH/jobcodes"
    const val SEARCH_KEYWORDS_PATH = "$SEARCH_PATH/keywords"

    const val ES_ADMIN = "$API/es-admin"
    const val ES_ADMIN_DELETE_INDICES = "$ES_ADMIN/delete-indices"
    const val ES_ADMIN_REINDEX = "$ES_ADMIN/reindex"

    object Latest {
        const val API = "/api$LATEST"
        private const val SEARCH_PATH = "$API/search"

        //export
        private const val EXPORT = "$API/export"
        const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
        private const val EXPORT_LIBRARY = "$EXPORT/library"
        const val EXPORT_LIBRARY_CSV = "$EXPORT_LIBRARY/csv"
        const val EXPORT_LIBRARY_XLSX = "$EXPORT_LIBRARY/xlsx"
        private const val EXPORT_SKILLS = "$EXPORT/skills"
        const val EXPORT_SKILLS_CSV = "$EXPORT_SKILLS/csv"
        const val EXPORT_SKILLS_XLSX = "$EXPORT_SKILLS/xlsx"
        const val SEARCH_SIMILAR_SKILLS = "$SEARCH_SKILLS/similarity"
        const val SEARCH_SIMILARITIES = "$SEARCH_SKILLS/similarities"
        const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

        //skills
        private const val SKILLS_PATH = "$API/skills"
        const val SKILLS_LIST = SKILLS_PATH
        const val SKILLS_CREATE = SKILLS_PATH
        const val SKILLS_FILTER = "$SKILLS_PATH/filter"
        const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
        const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
        const val SKILL_UPDATE = "$SKILL_DETAIL/update"
        const val SKILL_AUDIT_LOG = "$SKILL_DETAIL/log"

        //categories
        private const val CATEGORY_PATH = "$API/categories"
        const val CATEGORY_LIST = CATEGORY_PATH
        const val CATEGORY_DETAIL = "$CATEGORY_PATH/{identifier}"
        const val CATEGORY_SKILLS = "$CATEGORY_DETAIL/skills"

        //collections
        private const val COLLECTIONS_PATH = "$API/collections"
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

        private const val TASKS_PATH = "$API/results"
        const val TASK_DETAIL_TEXT = "$TASKS_PATH/text/{uuid}"
        const val TASK_DETAIL_MEDIA = "$TASKS_PATH/media/{uuid}"
        const val TASK_DETAIL_BATCH = "$TASKS_PATH/batch/{uuid}"
        const val TASK_DETAIL_SKILLS = "$TASKS_PATH/skills/{uuid}"

        const val SEARCH_JOBCODES_PATH = "$SEARCH_PATH/jobcodes"
        const val SEARCH_KEYWORDS_PATH = "$SEARCH_PATH/keywords"

        const val ES_ADMIN = "$API/es-admin"
        const val ES_ADMIN_DELETE_INDICES = "$ES_ADMIN/delete-indices"
        const val ES_ADMIN_REINDEX = "$ES_ADMIN/reindex"
    }

    object OldStillSupported {
        private const val API = "/api$SUPPORTED"
        private const val SEARCH_PATH = "$API/search"

        //export
        private const val EXPORT = "$API/export"
        private const val EXPORT_LIBRARY = "$EXPORT/library"
        const val EXPORT_LIBRARY_CSV = "$EXPORT_LIBRARY/csv"
        const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
        const val EXPORT_SKILLS = "$EXPORT/skills"
        const val SEARCH_SIMILAR_SKILLS = "$SEARCH_SKILLS/similarity"
        const val SEARCH_SIMILARITIES = "$SEARCH_SKILLS/similarities"
        const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

        //skills
        private const val SKILLS_PATH = "$API/skills"
        const val SKILLS_LIST = SKILLS_PATH
        const val SKILLS_CREATE = SKILLS_PATH
        const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
        const val SKILL_UPDATE = "$SKILL_DETAIL/update"
        const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
        const val SKILL_AUDIT_LOG = "$SKILL_DETAIL/log"

        //collections
        private const val COLLECTIONS_PATH = "$API/collections"
        const val COLLECTIONS_LIST = COLLECTIONS_PATH
        const val COLLECTION_CREATE = COLLECTIONS_PATH
        const val COLLECTION_PUBLISH = "$COLLECTIONS_PATH/publish"
        const val COLLECTION_DETAIL = "$COLLECTIONS_PATH/{uuid}"
        const val COLLECTION_UPDATE = "$COLLECTION_DETAIL/update"
        const val COLLECTION_SKILLS_UPDATE = "$COLLECTION_DETAIL/updateSkills"
        const val COLLECTION_SKILLS = "$COLLECTION_DETAIL/skills"
        const val COLLECTION_AUDIT_LOG = "$COLLECTION_DETAIL/log"
        const val COLLECTION_CSV = "$COLLECTION_DETAIL/csv"
        const val COLLECTION_REMOVE = "$COLLECTION_DETAIL/remove"

        const val WORKSPACE_PATH = "${API}/workspace"

        private const val TASKS_PATH = "${Unversioned.API}/results"
        const val TASK_DETAIL_TEXT = "$TASKS_PATH/text/{uuid}"
        const val TASK_DETAIL_BATCH = "$TASKS_PATH/batch/{uuid}"
        const val TASK_DETAIL_SKILLS = "$TASKS_PATH/skills/{uuid}"

        const val SEARCH_JOBCODES_PATH = "${Unversioned.SEARCH_PATH}/jobcodes"
        const val SEARCH_KEYWORDS_PATH = "${Unversioned.SEARCH_PATH}/keywords"

        const val ES_ADMIN = "${Unversioned.API}/es-admin"
        const val ES_ADMIN_DELETE_INDICES = "$ES_ADMIN/delete-indices"
        const val ES_ADMIN_REINDEX = "$ES_ADMIN/reindex"
    }

    object Unversioned {
        const val API = "/api"
        const val SEARCH_PATH = "$API/search"

        //export
        private const val EXPORT = "$API/export"
        const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
        private const val EXPORT_LIBRARY = "$EXPORT/library"
        const val EXPORT_LIBRARY_CSV = "$EXPORT_LIBRARY/csv"
        const val EXPORT_SKILLS = "$EXPORT/skills"
        const val SEARCH_SIMILAR_SKILLS = "$SEARCH_SKILLS/similarity"
        const val SEARCH_SIMILARITIES = "$SEARCH_SKILLS/similarities"
        const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

        //skills
        private const val SKILLS_PATH = "$API/skills"
        const val SKILLS_LIST = SKILLS_PATH
        const val SKILLS_CREATE = SKILLS_PATH
        const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
        const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
        const val SKILL_UPDATE = "$SKILL_DETAIL/update"
        const val SKILL_AUDIT_LOG = "$SKILL_DETAIL/log"

        //categories
        private const val CATEGORY_PATH = "$API/categories"
        const val CATEGORY_LIST = CATEGORY_PATH
        const val CATEGORY_DETAIL = "$CATEGORY_PATH/{identifier}"

        //collections
        private const val COLLECTIONS_PATH = "$API/collections"
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

        private const val TASKS_PATH = "$API/results"
        const val TASK_DETAIL_TEXT = "$TASKS_PATH/text/{uuid}"
        const val TASK_DETAIL_MEDIA = "$TASKS_PATH/media/{uuid}"
        const val TASK_DETAIL_BATCH = "$TASKS_PATH/batch/{uuid}"
        const val TASK_DETAIL_SKILLS = "$TASKS_PATH/skills/{uuid}"

        const val SEARCH_JOBCODES_PATH = "$SEARCH_PATH/jobcodes"
        const val SEARCH_KEYWORDS_PATH = "$SEARCH_PATH/keywords"

        private const val ES_ADMIN = "$API/es-admin"
        const val ES_ADMIN_DELETE_INDICES = "$ES_ADMIN/delete-indices"
        const val ES_ADMIN_REINDEX = "$ES_ADMIN/reindex"

    }

    object QueryParams {
        const val FROM = "from"
        const val SIZE = "size"
        const val STATUS = "status"
        const val SORT = "sort"
        const val COLLECTION_ID = "collectionId"
    }
}
