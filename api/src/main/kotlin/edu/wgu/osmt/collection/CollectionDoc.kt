package edu.wgu.osmt.collection

import edu.wgu.osmt.db.PublishStatus
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType.*
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField


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
    val skillIds: List<String>,

    @Field
    val skillCount: Int
)
