package edu.wgu.osmt.jobcode

import com.fasterxml.jackson.annotation.JsonIgnore
import edu.wgu.osmt.db.DatabaseData
import org.elasticsearch.common.Nullable
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime
import java.time.ZoneOffset

@Document(indexName = "jobcode_v1", createIndex = true)
data class JobCode(
    @Field
    @Nullable
    @JsonIgnore
    override val id: Long?,

    @JsonIgnore
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    override val creationDate: LocalDateTime,

    @Field(type = FieldType.Keyword)
    val major: String? = null,             // bls major category name

    @Field(type = FieldType.Keyword)
    val minor: String? = null,             // bls minor category name

    @Field(type = FieldType.Keyword)
    val broad: String? = null,             // bls broad category name

    @Field(type = FieldType.Keyword)
    val detailed: String? = null,          // bls detailed (for o*net level codes -- blank for bls detailed)

    @Field(type = FieldType.Keyword)
    val code: String,                           // bls detailed code or a o*net code: XX-XXXX or XX-XXXX.XX

    @Field
    val name: String? = null,                   // human readable label

    @Field
    val description: String? = null,

    @Field
    val framework: String? = null,               // e.g.: "bls" or "o*net"

    @Field
    val url: String? = null                     // e.g.: "http://onetonline/an/example/of/a/jobcode/canonicalUri"
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
