package edu.wgu.osmt.api.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort

enum class JobCodeSortEnum(override val apiValue: String): SortOrder {
    NameAsc("name.asc") {
        override val sort = Sort.by("name.sort_insensitive").ascending()
    },
    NameDesc("name.desc") {
        override val sort = Sort.by("name.sort_insensitive").descending()
    },
    CodeAsc("code.asc") {
        override val sort = Sort.by("code.keyword").ascending()
    },
    CodeDesc("code.desc") {
        override val sort = Sort.by("code.keyword").descending()
    };

    companion object : SortOrderCompanion<JobCodeSortEnum> {
        override val logger: Logger = LoggerFactory.getLogger(JobCodeSortEnum::class.java)

        override val defaultSort = NameAsc

        override fun forApiValue(apiValue: String): JobCodeSortEnum {
            return values().find { it.apiValue == apiValue } ?: NameAsc.also {
                logger.warn("Sort with value $apiValue could not be found; using default ${NameAsc.apiValue} sort")
            }
        }
    }
}
