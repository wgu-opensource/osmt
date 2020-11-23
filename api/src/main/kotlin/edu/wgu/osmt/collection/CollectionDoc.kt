package edu.wgu.osmt.collection

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus
import org.elasticsearch.common.Nullable
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
data class CollectionDoc(
    @Field(name = "db_id")
    @get:JsonIgnore
    val id: Long,

    @Id
    @Field(type = Keyword)
    val uuid: String,

    @MultiField(
        mainField = Field(type = Text),
        otherFields = [
            InnerField(suffix = "", type = Search_As_You_Type),
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

    @Field
    @Nullable
    val skillCount: Int?,

    @Field(type = Search_As_You_Type)
    @Nullable
    @get:JsonIgnore
    val author: String?,

    @Field(type = Date, format = DateFormat.date_time)
    @get:JsonProperty("publishDate")
    val publishDate: LocalDateTime? = null,

    @Field(type = Date, format = DateFormat.date_time)
    @get:JsonProperty("archiveDate")
    val archiveDate: LocalDateTime? = null
)
