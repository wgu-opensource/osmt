package edu.wgu.osmt.jobcode

import com.fasterxml.jackson.annotation.JsonIgnore
import edu.wgu.osmt.csv.HasCodeHierarchy
import edu.wgu.osmt.db.DatabaseData
import org.elasticsearch.common.Nullable
import org.springframework.data.elasticsearch.annotations.*
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

    @MultiField(
        mainField = Field(type = FieldType.Text),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val major: String? = null,             // bls major category name

    @MultiField(
        mainField = Field(type = FieldType.Text),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val minor: String? = null,             // bls minor category name

    @MultiField(
        mainField = Field(type = FieldType.Text),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val broad: String? = null,             // bls broad category name

    @MultiField(
        mainField = Field(type = FieldType.Text),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val detailed: String? = null,          // bls detailed (for o*net level codes -- blank for bls detailed)

    @MultiField(
        mainField = Field(type = FieldType.Text),
        otherFields = [
            InnerField(suffix = "", type = FieldType.Search_As_You_Type),
            InnerField(suffix = "keyword", type = FieldType.Keyword)]
    )
    val code: String,                           // bls detailed code or a o*net code: XX-XXXX or XX-XXXX.XX

    @Field(type = FieldType.Search_As_You_Type)
    val name: String? = null,                   // human readable label

    @Field(type = FieldType.Search_As_You_Type)
    val description: String? = null,

    @Field(type = FieldType.Search_As_You_Type)
    val framework: String? = null,               // e.g.: "bls" or "o*net"

    @Field
    val url: String? = null                     // e.g.: "http://onetonline/an/example/of/a/jobcode/canonicalUri"
) : DatabaseData {

    @Field
    @Nullable
    val majorCode: String? = JobCodeBreakout.majorCode(code)

    @Field
    @Nullable
    val minorCode: String? = JobCodeBreakout.minorCode(code)

    @Field
    @Nullable
    val broadCode: String? = JobCodeBreakout.broadCode(code)

    @Field
    @Nullable
    val detailedCode: String? = JobCodeBreakout.detailedCode(code)

    @Field
    @Nullable
    val jobRoleCode: String? = JobCodeBreakout.jobRoleCode(code)

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
