package edu.wgu.osmt.keyword

import edu.wgu.osmt.db.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime

data class Keyword(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val value: String,
    val type: KeywordTypeEnum
) : DatabaseData<Keyword>, HasUpdateDate {
    override fun withId(id: Long): Keyword = copy(id = id)
}

data class KeywordUpdateObj(override val id: Long, val value: String?) : UpdateObject

object KeywordTable : TableWithUpdateMapper<Keyword, KeywordUpdateObj>("Keyword") {
    val value: Column<String> = varchar("value", 1024)
    val keyword_type_enum =
        customEnumeration(
            "keyword_type_enum",
            "Enum('Category', 'ProfessionalStandards', 'Sel', 'Tools', 'Certifications', 'Other')",
            { value -> KeywordTypeEnum.valueOf(value as String) }, { it.name })

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: Keyword) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[value] = t.value
    }
}
