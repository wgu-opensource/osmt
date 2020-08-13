package edu.wgu.osmt.keyword

import edu.wgu.osmt.db.*
import edu.wgu.osmt.richskill.RichSkillDescriptor
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime

data class Keyword(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val value: String,
    val type: KeywordTypeEnum,
    val uri: String? = null
) : DatabaseData<Keyword>, HasUpdateDate {
    override fun withId(id: Long): Keyword = copy(id = id)
}

data class KeywordUpdateObj(override val id: Long, val value: String?, val uri: NullableFieldUpdate<String>?) :
    UpdateObject<Keyword> {

    fun compareValue(that: Keyword): JSONObject? = compare(that::value, this::value, stringOutput)

    fun compareUri(that: Keyword): JSONObject? {
        return uri?.let {
            compare(that::uri, it::t, stringOutput)
        }
    }

    override val comparisonList: List<(t: Keyword) -> JSONObject?> = listOf(::compareValue, ::compareUri)
}

object KeywordTable : TableWithUpdateMapper<Keyword, KeywordUpdateObj>("Keyword") {
    val value: Column<String> = varchar("value", 1024)
    val uri = text("uri").nullable()
    val keyword_type_enum =
        customEnumeration(
            "keyword_type_enum",
            "Enum('Category', 'Certifications', 'Keyword', 'Other', 'ProfessionalStandards', 'Sel', 'Tools')",
            { value -> KeywordTypeEnum.valueOf(value as String) }, { it.name })

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: Keyword) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[value] = t.value
    }
}
