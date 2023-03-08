package edu.wgu.osmt.richskill

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.config.INDEX_RICHSKILL_DOC
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.KeywordTypeEnum
import org.elasticsearch.core.Nullable
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import org.springframework.data.elasticsearch.annotations.FieldType.*
import java.time.LocalDateTime


/**
 * Elasticsearch representation of a Rich Skill.
 * Also corresponds to `SkillSummary` API response object
 */
@Document(indexName = INDEX_RICHSKILL_DOC, createIndex = true, versionType = Document.VersionType.EXTERNAL)
@Setting(settingPath = "/elasticsearch/settings.json")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class RichSkillDoc(
    @Field(name = "db_id")
    @Nullable
    @get:JsonIgnore
    @get:JsonProperty("db_id")
    val id: Long,

    @Id
    @Field(type = Keyword)
    @get:JsonProperty
    val uuid: String,

    @Field(type = Keyword)
    @get:JsonProperty("id")
    val uri: String,

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword),
            InnerField(suffix = "sort_insensitive", type = Keyword, normalizer = "lowercase_normalizer")
        ]
    )
    @get:JsonProperty("skillName")
    val name: String,

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword),
            InnerField(suffix = "sort_insensitive", type = Keyword, normalizer = "lowercase_normalizer")
        ]
    )
    @get:JsonProperty("skillStatement")
    val statement: String,

    @Nullable
    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword),
            InnerField(suffix = "sort_insensitive", type = Keyword, normalizer = "lowercase_normalizer")
        ]
    )
    @get:JsonProperty
    val category: String? = null,

    @Nullable
    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    @get:JsonProperty("author")
    val author: String? = null,

    @Field(type = Keyword)
    @get:JsonProperty("status")
    val publishStatus: PublishStatus,

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    @get:JsonProperty("keywords")
    val searchingKeywords: List<String> = listOf(),

    @Field(type = Nested)
    @get:JsonProperty("occupations")
    val jobCodes: List<JobCode> = listOf(),

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    @get:JsonProperty("standards")
    val standards: List<String> = listOf(),

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    @get:JsonIgnore
    val certifications: List<String> = listOf(),

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    @get:JsonIgnore
    val employers: List<String> = listOf(),

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    @get:JsonIgnore
    val alignments: List<String> = listOf(),

    @Field(type = Nested)
    @get:JsonIgnore
    val collections: List<CollectionDoc> = listOf(),

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    @get:JsonProperty("publishDate")
    val publishDate: LocalDateTime? = null,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    @get:JsonProperty("archiveDate")
    val archiveDate: LocalDateTime? = null
) {
    companion object {
        fun fromDao(dao: RichSkillDescriptorDao, appConfig: AppConfig): RichSkillDoc {
            return RichSkillDoc(
                id = dao.id.value,
                uuid = dao.uuid,
                uri = "${appConfig.baseUrl}/api/skills/${dao.uuid}",
                name = dao.name,
                statement = dao.statement,
                category = dao.category?.value,
                author = dao.author?.value,
                publishStatus = dao.publishStatus(),
                searchingKeywords = dao.keywords.filter { it.type == KeywordTypeEnum.Keyword }.mapNotNull { it.value },
                jobCodes = dao.jobCodes.map { it.toModel() },
                standards = dao.keywords.filter { it.type == KeywordTypeEnum.Standard }.mapNotNull { it.value },
                certifications = dao.keywords.filter { it.type == KeywordTypeEnum.Certification }
                    .mapNotNull { it.value },
                employers = dao.keywords.filter { it.type == KeywordTypeEnum.Employer }.mapNotNull { it.value },
                alignments = dao.keywords.filter { it.type == KeywordTypeEnum.Alignment }.mapNotNull { it.value },
                collections = dao.collections.map { it.toDoc(embedded = true) },
                publishDate = dao.publishDate,
                archiveDate = dao.archiveDate
            )
        }
    }
}
