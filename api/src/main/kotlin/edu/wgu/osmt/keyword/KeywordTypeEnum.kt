package edu.wgu.osmt.keyword

/**
 * Note, this enum backs a custom Mysql enum type
 * Changes to this class must be reflected in a migration to add the enum values
 * to the database column where it is used (Currently just [[KeywordTable.keyword_type_enum]])
 */
enum class KeywordTypeEnum {
    Category,
    Keyword,
    Standard,
    Certification,
    Alignment,
    Employer,
    Author
    ;


    open val displayName: String = this.name

    companion object {
        fun forApiValue(apiValue: String) = KeywordTypeEnum.values().find { it.name.toLowerCase() == apiValue.toLowerCase() }
    }
}
