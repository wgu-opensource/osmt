package edu.wgu.osmt.richskill

import org.elasticsearch.common.Nullable
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import edu.wgu.osmt.collection.Collection
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
    val alignments: List<String> = listOf()
) {
    companion object {
        fun fromRsd(rs: RichSkillDescriptor): RichSkillDoc {
            return RichSkillDoc(
                id = rs.id!!,
                uuid = rs.uuid,
                name = rs.name,
                statement = rs.statement,
                category = rs.category?.value,
                author = rs.author?.value,
                publishStatus = rs.publishStatus(),
                searchingKeywords = rs.searchingKeywords.mapNotNull{it.value},
                jobCodes = rs.jobCodes.map{it.code},
                standards = rs.standards.mapNotNull{it.value},
                certifications = rs.certifications.mapNotNull { it.value },
                employers = rs.employers.mapNotNull{it.value},
                alignments = rs.alignments.mapNotNull { it.value }
                )
        }
    }
}
