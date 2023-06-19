package edu.wgu.osmt.keyword

import edu.wgu.osmt.config.INDEX_KEYWORD_DOC
import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.db.HasUpdateDate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.TableWithUpdate
import edu.wgu.osmt.db.UpdateObject
import org.elasticsearch.core.Nullable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField
import org.springframework.data.elasticsearch.annotations.Setting
import java.time.LocalDateTime

@Document(indexName = INDEX_KEYWORD_DOC, createIndex = true)
@Setting(settingPath = "/elasticsearch/settings.json")
data class Keyword(
    @Id
    @Nullable
    override val id: Long?,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    override val creationDate: LocalDateTime,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    override val updateDate: LocalDateTime,

    @Field(type = FieldType.Keyword)
    val type: KeywordTypeEnum,

    @Nullable
    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword),
            InnerField(suffix = "sort_insensitive", type = FieldType.Keyword, normalizer = "lowercase_normalizer")
        ]
    )
    val value: String? = null,

    @Nullable
    val uri: String? = null,

    @Nullable
    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword),
            InnerField(suffix = "sort_insensitive", type = FieldType.Keyword, normalizer = "lowercase_normalizer")
        ]
    )
    val framework: String? = null
) : DatabaseData, HasUpdateDate {
}

data class KeywordUpdateObj(
        override val id: Long,
        val value: String?,
        val uri: NullableFieldUpdate<String>?,
        val framework: NullableFieldUpdate<String>?
) : UpdateObject<KeywordDao> {

    override fun applyToDao(dao: KeywordDao) {
        value?.let{dao.value = it}
        uri?.let{dao.uri = it.t}
        framework?.let{dao.framework = it.t}
    }
}

data class KeywordCount(
    val keyword: Any,
    val count: Int
)

object KeywordTable : LongIdTable("Keyword"), TableWithUpdate<KeywordUpdateObj> {
    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    val value: Column<String?> = varchar("value", 768).nullable()
    val uri: Column<String?> = varchar("uri", 768).nullable()
    val framework: Column<String?> = varchar("framework", 768).nullable()

    val keyword_type_enum =
        customEnumeration(
            "keyword_type_enum",
            fromDb = { value -> KeywordTypeEnum.valueOf(value as String) }, toDb = { it.name })

    init {
        index(true, keyword_type_enum, value)
        index(true, keyword_type_enum, uri)
    }
}
