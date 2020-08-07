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
    val type: KeywordType
) : DatabaseData<Keyword>, HasUpdateDate {
    override fun withId(id: Long): Keyword = copy(id = id)
}

data class KeywordType(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val type: String
) : DatabaseData<KeywordType>, HasUpdateDate {

    override fun withId(id: Long): KeywordType = copy(id = id)
}

data class KeywordUpdateObj(override val id: Long, val value: String?) : UpdateObject
data class KeywordTypeUpdateObj(override val id: Long, val value: String?) : UpdateObject

object KeywordTypeTable : TableWithUpdateMapper<KeywordType, KeywordTypeUpdateObj>("KeywordType") {
    val type: Column<String> = varchar("keyword_type", 128)

    init {
        index(true, type) // ensure unique values for type
    }

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: KeywordType) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[type] = t.type
    }
}

object KeywordTable : TableWithUpdateMapper<Keyword, KeywordUpdateObj>("Keyword") {
    val value: Column<String> = varchar("value", 1024)
    val keyword_type = reference("keyword_type_id", KeywordTypeTable, ReferenceOption.CASCADE)

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: Keyword) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[value] = t.value
    }
}
