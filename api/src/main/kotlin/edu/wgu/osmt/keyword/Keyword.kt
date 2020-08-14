package edu.wgu.osmt.keyword

import edu.wgu.osmt.db.*
import net.minidev.json.JSONObject
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime

data class Keyword(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val value: String,
    val type: KeywordTypeEnum,
    val uri: String? = null
) : DatabaseData, HasUpdateDate {
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

object KeywordTable : TableWithUpdateMapper<KeywordUpdateObj>, LongIdTable("Keyword") {
    override val table: LongIdTable = this
    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    val value: Column<String> = varchar("value", 1024)
    val uri = text("uri").nullable()
    val keyword_type_enum =
        customEnumeration(
            "keyword_type_enum",
            "Enum('Category', 'Certifications', 'Keyword', 'Other', 'ProfessionalStandards', 'Sel', 'Tools')",
            { value -> KeywordTypeEnum.valueOf(value as String) }, { it.name })
}
