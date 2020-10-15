package edu.wgu.osmt.elasticsearch

import org.springframework.data.domain.Sort

enum class SortEnum(val apiValue: String) {
    CategoryAsc("category.asc") {
        override val sort = Sort.by("category.keyword").ascending()
    },
    CategoryDesc("category.desc") {
        override val sort = Sort.by("category.keyword").descending()
    },
    SkillNameAsc("name.asc") {
        override val sort = Sort.by("name.keyword").ascending()
    },
    SkillNameDesc("name.desc") {
        override val sort = Sort.by("name.keyword").descending()
    },
    Unsorted("unsorted") {
        override val sort = Sort.unsorted()
    };

    abstract val sort: Sort

    companion object {
        fun forApiValue(apiValue: String): SortEnum = values().find { it.apiValue == apiValue } ?: Unsorted
    }
}
