package edu.wgu.osmt.api.model

import edu.wgu.osmt.config.CATEGORY_ASC
import edu.wgu.osmt.config.CATEGORY_DESC
import edu.wgu.osmt.config.NAME_ASC
import edu.wgu.osmt.config.NAME_DESC
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort

const val nameKeyword: String = "name.keyword";

interface SortOrder {
    val apiValue: String

    // defines sorting on elasticsearch properties
    val sort: Sort
}

interface SortOrderCompanion<T> where T: SortOrder{
    val logger: Logger

    val defaultSort: T

    val defaultSortValue: String
        get() = defaultSort.apiValue

    fun forValueOrDefault(apiValue: String?, sort: T = defaultSort): T {
        return if (!apiValue.isNullOrEmpty()){
            forApiValue(apiValue)
        } else {
            sort
        }
    }

    fun forApiValue(apiValue: String): T
}



/**
 * Provides an enum for Rich skills that defines elasticsearch sorting
 */
enum class SkillSortEnum(override val apiValue: String) : SortOrder {
    CategoryAsc(CATEGORY_ASC) {
        override val sort = Sort.by(
                Sort.Order.asc("category.sort_insensitive"),
                Sort.Order.asc("name.sort_insensitive"))
    },
    CategoryDesc(CATEGORY_DESC) {
        override val sort = Sort.by(
                Sort.Order.desc("category.sort_insensitive"),
                Sort.Order.asc("name.sort_insensitive"))
    },
    NameAsc(NAME_ASC) {
        override val sort = Sort.by(nameKeyword).ascending()
    },
    NameDesc(NAME_DESC) {
        override val sort = Sort.by(nameKeyword).descending()
    };

    companion object : SortOrderCompanion<SkillSortEnum> {
        override val logger: Logger = LoggerFactory.getLogger(SkillSortEnum::class.java)

        override val defaultSort = CategoryAsc

        override fun forApiValue(apiValue: String): SkillSortEnum {
            return values().find { it.apiValue == apiValue } ?: NameAsc.also {
                logger.warn("Sort with value ${apiValue} could not be found; using default ${NameAsc.apiValue} sort")
            }
        }
    }
}

/**
 * Provides an enum for Collections that defines elasticsearch sorting
 */
enum class CollectionSortEnum(override val apiValue: String) : SortOrder {
    SkillCountAsc("skill.asc") {
        override val sort = Sort.by("skillCount").ascending()
    },
    SkillCountDesc("skill.desc") {
        override val sort = Sort.by("skillCount").descending()
    },
    CollectionNameAsc(NAME_ASC) {
        override val sort = Sort.by(nameKeyword).ascending()
    },
    CollectionNameDesc("name.desc") {
        override val sort = Sort.by(nameKeyword).descending()
    };

    companion object : SortOrderCompanion<CollectionSortEnum> {
        override val logger: Logger = LoggerFactory.getLogger(CollectionSortEnum::class.java)

        override val defaultSort = CollectionNameAsc

        override fun forApiValue(apiValue: String): CollectionSortEnum {
            return values().find { it.apiValue == apiValue } ?: CollectionNameAsc.also {
                logger.warn("Sort with value ${apiValue} could not be found; using default ${CollectionNameAsc.apiValue} sort")
            }
        }
    }
}
