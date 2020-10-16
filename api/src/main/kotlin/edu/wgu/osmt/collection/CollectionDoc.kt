package edu.wgu.osmt.collection

import com.fasterxml.jackson.annotation.JsonInclude
import edu.wgu.osmt.db.PublishStatus
import org.elasticsearch.common.Nullable
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType.*
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Document(indexName = "collection", createIndex = true)
data class CollectionDoc(
    @Field(name = "db_id")
    val id: Long,

    @Id
    @Field(type = Keyword)
    val uuid: String,

    @MultiField(
        mainField = Field(type = Text),
        otherFields = [InnerField(suffix = "keyword", type = Keyword)]
    )
    val name: String,

    @Field(type = Keyword)
    val publishStatus: PublishStatus,

    @Field(type = Keyword)
    @Nullable
    val skillIds: List<String>?,

    @Field
    @Nullable
    val skillCount: Int?,

    @Field
    @Nullable
    val author: String?
)
