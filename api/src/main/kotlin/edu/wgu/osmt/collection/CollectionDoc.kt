package edu.wgu.osmt.collection

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus
import org.elasticsearch.core.Nullable
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import org.springframework.data.elasticsearch.annotations.FieldType.*
import java.time.LocalDateTime

/**
 * Elasticsearch representation of a Collection.
 * Also corresponds to `CollectionSummary` API response object
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Document(indexName = "collection_v1", createIndex = true)
@Setting(settingPath = "/elasticsearch/settings.json")
data class CollectionDoc(
    @Field(name = "db_id")
    @get:JsonIgnore
    val id: Long,

    @Id
    @Field(type = Keyword)
    val uuid: String,

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type, analyzer = "english_stemmer"),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    val name: String,

    @Field(type = Keyword)
    @get:JsonProperty("status")
    val publishStatus: PublishStatus,

    @Field(type = Keyword)
    @Nullable
    @get:JsonIgnore
    val skillIds: List<String>?,

    @Field(type = Integer)
    @Nullable
    val skillCount: Int?,

    @MultiField(
        mainField = Field(type = Text, analyzer = "english_stemmer"),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
            InnerField(suffix = "raw", analyzer = "whitespace_exact", type = Text),
            InnerField(suffix = "keyword", type = Keyword)
        ]
    )
    @Nullable
    @get:JsonIgnore
    val author: String?,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    @get:JsonProperty("archiveDate")
    val archiveDate: LocalDateTime? = null,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second])
    @get:JsonProperty("publishDate")
    val publishDate: LocalDateTime? = null
)
