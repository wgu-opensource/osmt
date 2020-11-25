package edu.wgu.osmt.keyword

import edu.wgu.osmt.db.*
import net.minidev.json.JSONObject
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime
import org.elasticsearch.common.Nullable
import org.springframework.data.annotation.Id

@Document(indexName = "keyword", createIndex = true)
data class Keyword(
    @Id
    @Nullable
    override val id: Long?,

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    override val creationDate: LocalDateTime,

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    override val updateDate: LocalDateTime,

    @Field(type = FieldType.Text)
    val type: KeywordTypeEnum,

    @Nullable
    val value: String? = null,

    @Nullable
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

object KeywordTable : LongIdTable("Keyword"), TableWithUpdate<KeywordUpdateObj> {
    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    val value: Column<String?> = varchar("value", 768).nullable()
    val uri: Column<String?> = varchar("uri", 768).nullable()

    val keyword_type_enum =
        customEnumeration(
            "keyword_type_enum",
            fromDb = { value -> KeywordTypeEnum.valueOf(value as String) }, toDb = { it.name })

    init {
        index(true, keyword_type_enum, value)
        index(true, keyword_type_enum, uri)
    }
}
