package edu.wgu.osmt.richskill

import edu.wgu.osmt.collection.CollectionDoc
import org.elasticsearch.common.Nullable
import edu.wgu.osmt.db.PublishStatus
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import org.springframework.data.elasticsearch.annotations.FieldType.*

@Document(indexName = "richskill", createIndex = true)
data class RichSkillDoc(
    @Field(name = "db_id")
    @Nullable
    val id: Long?,

    @Id
    @Field(type = Text)
    val uuid: String,

    @MultiField(
        mainField = Field(type = Text),
        otherFields = [InnerField(suffix = "keyword", type = Keyword)]
    )
    val name: String,

    @Field
    val statement: String,

    @Nullable
    @MultiField(
        mainField = Field(type = Text),
        otherFields = [InnerField(suffix = "keyword", type = Keyword)]
    )
    val category: String? = null,

    @Nullable
    @MultiField(
        mainField = Field(type = Text),
        otherFields = [InnerField(suffix = "keyword", type = Keyword)]
    )
    val author: String? = null,

    @Field(type = Keyword)
    val publishStatus: PublishStatus,

    @Field(type = Text)
    val searchingKeywords: List<String>,

    @Field(type = Text)
    val jobCodes: List<String> = listOf(),

    @Field(type = Text)
    val standards: List<String> = listOf(),

    @Field(type = Text)
    val certifications: List<String> = listOf(),

    @Field(type = Text)
    val employers: List<String> = listOf(),

    @Field(type = Text)
    val alignments: List<String> = listOf(),

    @Field(type = Object)
    val collections: List<CollectionDoc>
)
