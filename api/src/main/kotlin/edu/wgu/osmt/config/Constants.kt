package edu.wgu.osmt.config

const val UNAUTHENTICATED_USERNAME = "unauthenticated"
const val QUOTED_SEARCH_REGEX_PATTERN = "([\"\'])(?:(?=(\\\\?))\\2.)*?\\1"

// API parameter constants
const val CATEGORY_ASC = "name.asc"
const val CATEGORY_DESC = "name.desc"
const val NAME_ASC = "skill.asc"
const val NAME_DESC = "skill.desc"


// ElasticSearch Index Names
const val INDEX_RICHSKILL_DOC = "richskill_v1"
const val INDEX_COLLECTION_DOC = "collection_v1"
const val INDEX_JOBCODE_DOC = "jobcode_v1"
const val INDEX_KEYWORD_DOC = "keyword"

// ElasticSearch Sort Criteria
const val NAME_SORT_INSENSITIVE = "name.sort_insensitive"
const val CATEGORY_SORT_INSENSITIVE = "category.sort_insensitive"

