package edu.wgu.osmt

class RoutePaths {
    companion object {
        const val SEARCH_PATH = "/api/search"
        const val SEARCH_SKILLS = "$SEARCH_PATH/skills"
        const val SEARCH_COLLECTIONS = "$SEARCH_PATH/collections"

        const val SKILLS_PATH = "/api/skills"
        const val SKILL_LIST = SKILLS_PATH
        const val SKILL_PUBLISH = "$SKILLS_PATH/publish"
        const val SKILL_DETAIL = "$SKILLS_PATH/{uuid}"
        const val SKILL_UPDATE = "$SKILL_DETAIL/update"

        const val COLLECTIONS_PATH = "/api/collections"
        const val COLLECTION_LIST = COLLECTIONS_PATH
        const val COLLECTION_DETAIL = "${COLLECTIONS_PATH}/{uuid}"
        const val COLLECTION_UPDATE = "${COLLECTION_DETAIL}/update"
    }
    object QueryParams {
        const val FROM = "from"
        const val SIZE = "size"
        const val STATUS = "status"
        const val SORT = "sort"
    }
}
