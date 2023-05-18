package edu.wgu.osmt.jobcode

import com.fasterxml.jackson.annotation.JsonIgnore
import edu.wgu.osmt.config.INDEX_JOBCODE_DOC
import edu.wgu.osmt.db.DatabaseData
import org.elasticsearch.core.Nullable
import org.springframework.data.elasticsearch.annotations.*
import java.time.LocalDateTime
import java.time.ZoneOffset

@Document(indexName = INDEX_JOBCODE_DOC, createIndex = true)
@Setting(settingPath = "/elasticsearch/settings.json")
data class JobCode(
    @Field
    @Nullable
    @JsonIgnore
    override val id: Long?,

    @JsonIgnore
    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    override val creationDate: LocalDateTime,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val major: String? = null,             // bls major category name

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val minor: String? = null,             // bls minor category name

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val broad: String? = null,             // bls broad category name

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val detailed: String? = null,          // bls detailed (for o*net level codes -- blank for bls detailed)

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val code: String,                           // bls detailed code or a o*net code: XX-XXXX or XX-XXXX.XX

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword),
            // InnerField(suffix = "sort_insensitive", type = FieldType.Keyword, normalizer = "lowercase_normalizer")
        ]
    )
    val name: String? = null,                   // human readable label

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val description: String? = null,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = FieldType.Text),
            InnerField(suffix = "keyword", type = FieldType.Keyword)
        ]
    )
    val framework: String? = null,               // e.g.: "bls" or "o*net"

    @Field
    val url: String? = null,                     // e.g.: "http://onetonline/an/example/of/a/jobcode/canonicalUri"

    @Field
    @Nullable
    val majorCode: String? = JobCodeBreakout.majorCode(code),

    @Field
    @Nullable
    val minorCode: String? = JobCodeBreakout.minorCode(code),

    @Field
    @Nullable
    val broadCode: String? = JobCodeBreakout.broadCode(code),

    @Field
    @Nullable
    val detailedCode: String? = JobCodeBreakout.detailedCode(code),

    @Field
    @Nullable
    val jobRoleCode: String? = JobCodeBreakout.jobRoleCode(code)
) : DatabaseData {

    companion object {
        fun create(code: String): JobCode {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            return JobCode(
                id = null,
                creationDate = now,
                code = code
            )
        }
    }
}
