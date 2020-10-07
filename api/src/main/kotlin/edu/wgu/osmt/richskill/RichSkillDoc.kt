package edu.wgu.osmt.richskill

import edu.wgu.osmt.collection.CollectionDoc
import org.elasticsearch.common.Nullable
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import edu.wgu.osmt.db.PublishStatus
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "richskill", createIndex = true)
data class RichSkillDoc(
    @Field(name = "db_id")
    @Nullable
    val id: Long?,

    @Id
    val uuid: String,

    @Field
    val name: String,

    @Field
    val statement: String,

    @Nullable
    val category: String? = null,

    @Nullable
    val author: String? = null,

    @Field
    val publishStatus: PublishStatus,

    @Field
    val searchingKeywords: List<String>,

    @Field
    val jobCodes: List<String> = listOf(),

    @Field
    val standards: List<String> = listOf(),

    @Field
    val certifications: List<String> = listOf(),

    @Field
    val employers: List<String> = listOf(),

    @Field
    val alignments: List<String> = listOf(),

    @Field(type = FieldType.Nested)
    val collections: List<CollectionDoc>
) {
    companion object {

        val defaultSearchFields = setOf(
            RichSkillDoc::name.name,
            RichSkillDoc::statement.name,
            RichSkillDoc::category.name,
            RichSkillDoc::searchingKeywords.name,
            RichSkillDoc::jobCodes.name,
            RichSkillDoc::standards.name,
            RichSkillDoc::certifications.name,
            RichSkillDoc::employers.name,
            RichSkillDoc::alignments.name
        )
    }
}
