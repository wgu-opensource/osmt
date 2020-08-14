package edu.wgu.osmt.keyword

/**
 * Note, this enum backs a custom Mysql enum type
 * Changes to this class must be reflected in [[KeywordTable.keyword_type_enum]]
 */
enum class KeywordTypeEnum {
    Category,
    Certifications,
    Other,
    ProfessionalStandards {
        override val displayName = "Professional Standards"
    },
    Sel,
    Tools,
    Keyword,
    Alignment,
    Occupation,
    Employers,
    ;


    open val displayName: String = this.name
}
