package edu.wgu.osmt.api.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort


/**
 * Provides an enum that defines both elasticsearch and mysql sorting
 */
enum class ApiSortEnum(val apiValue: String) {
    CategoryAsc("category.asc") {
        override val esSort = Sort.by("category.keyword").ascending()
        override val sqlSort = Sort.by("category").ascending()
    },
    CategoryDesc("category.desc") {
        override val esSort = Sort.by("category.keyword").descending()
        override val sqlSort = Sort.by("category").descending()
    },
    SkillNameAsc("name.asc") {
        override val esSort = Sort.by("name.keyword").ascending()
        override val sqlSort = Sort.by("name").ascending()
    },
    SkillNameDesc("name.desc") {
        override val esSort = Sort.by("name.keyword").descending()
        override val sqlSort = Sort.by("name").descending()
    };

    // defines sorting on elasticsearch properties
    abstract val esSort: Sort

    // defines sorting on mysql properties
    abstract val sqlSort: Sort

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ApiSortEnum::class.java)

        fun forApiValue(apiValue: String): ApiSortEnum {
            return values().find { it.apiValue == apiValue } ?: SkillNameAsc.also {
                logger.warn("Sort with value ${apiValue} could not be found; using default ${SkillNameAsc.apiValue} sort")
            }
        }
    }
}
