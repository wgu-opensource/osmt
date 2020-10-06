package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.*
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import net.minidev.json.JSONObject
import org.springframework.data.elasticsearch.annotations.Document
import org.valiktor.functions.isEqualTo
import org.valiktor.validate
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.valiktor.functions.validate
import java.util.*
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.keyword.KeywordDao
import org.elasticsearch.common.Nullable
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.valiktor.functions.isNotEqualTo

@Document(indexName = "richskill", createIndex = true)
data class RichSkillDescriptor(

    @Field(name = "db_id")
    @Nullable
    override val id: Long?,

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    override val creationDate: LocalDateTime,

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    override val updateDate: LocalDateTime,

    @Id
    val uuid: String,

    val name: String,

    val statement: String,

    @Field(type = FieldType.Nested)
    val jobCodes: List<JobCode> = listOf(),

    val keywords: List<Keyword> = listOf(),

    @Nullable
    val category: Keyword? = null,

    @Nullable
    val author: Keyword? = null,

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    @Nullable
    override val archiveDate: LocalDateTime? = null,

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    @Nullable
    override val publishDate: LocalDateTime? = null

) : DatabaseData, HasUpdateDate, PublishStatusDetails {

    var collections: List<Collection> = listOf()

    // Keyword collections
    val certifications: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Certification }

    val standards: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Standard }

    val searchingKeywords: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Keyword }

    val alignments: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Alignment }

    val employers: List<Keyword>
        get() = this.keywords.filter { it.type == KeywordTypeEnum.Employer }

    fun canonicalUrl(baseUrl: String): String = "$baseUrl/api/skills/${uuid}"

    companion object {
        fun create(name: String, statement: String): RichSkillDescriptor {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            return RichSkillDescriptor(
                id = null,
                uuid = UUID.randomUUID().toString(),
                name = name,
                statement = statement,
                creationDate = now,
                updateDate = now
            )
        }
    }
}

data class RsdUpdateObject(
    override val id: Long? = null,
    val name: String? = null,
    val statement: String? = null,
    val author: NullableFieldUpdate<KeywordDao>? = null,
    val category: NullableFieldUpdate<KeywordDao>? = null,
    val keywords: ListFieldUpdate<KeywordDao>? = null,
    val jobCodes: ListFieldUpdate<JobCodeDao>? = null,
    override val publishStatus: PublishStatus? = null
) : UpdateObject<RichSkillDescriptorDao>, HasPublishStatus {

    init {
        validate(this) {
            validate(RsdUpdateObject::category).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Category)
                }
            }
            validate(RsdUpdateObject::author).validate {
                validate(NullableFieldUpdate<KeywordDao>::t).validate {
                    validate(KeywordDao::type).isEqualTo(KeywordTypeEnum.Author)
                }
            }
            validate(RsdUpdateObject::publishStatus).isNotEqualTo(PublishStatus.Unpublished)
        }
    }

    fun compareName(that: RichSkillDescriptorDao): JSONObject? {
        return name?.let {
            compare(that::name, this::name, stringOutput)
        }
    }

    fun compareStatement(that: RichSkillDescriptorDao): JSONObject? {
        return statement?.let {
            compare(that::statement, this::statement, stringOutput)
        }
    }

    fun compareCategory(that: RichSkillDescriptorDao): JSONObject? {
        return category?.let {
            if (that.category?.value?.let { id } != it.t?.id?.value) {
                jsonUpdateStatement(that::category.name, that.category?.let { it.value }, it.t?.value)
            } else null
        }
    }

    fun compareAuthor(that: RichSkillDescriptorDao): JSONObject? {
        return author?.let {
            if (that.author?.value?.let { id } != it.t?.id?.value) {
                jsonUpdateStatement(that::author.name, that.author?.value?.let { it }, it.t?.value)
            } else null
        }
    }

    fun comparePublishStatus(that: RichSkillDescriptorDao): JSONObject? {
        return publishStatus?.let {
            jsonUpdateStatement(that::publishStatus.name, that.publishStatus().name, it.name)
        }
    }

    fun compareKeywords(that: RichSkillDescriptorDao): JSONObject? {
        val added = keywords?.add?.map { mutableMapOf("id" to it.id.value, "value" to it.value, "type" to it.type) }
        val removed =
            keywords?.remove?.map { mutableMapOf("id" to it.id.value, "value" to it.value, "type" to it.type) }
        val addedPair = added?.let { "added" to it }
        val removedPair = removed?.let { "removed" to it }
        val operationsList = listOfNotNull(addedPair, removedPair).toTypedArray()

        return if (added?.isNotEmpty() == true or (removed?.isNotEmpty() == true)) {
            JSONObject(mutableMapOf(that::keywords.name to mutableMapOf(*operationsList)))
        } else {
            null
        }
    }

    override val comparisonList: List<(t: RichSkillDescriptorDao) -> JSONObject?> =
        listOf(
            ::compareName,
            ::compareStatement,
            ::compareCategory,
            ::compareAuthor,
            ::comparePublishStatus,
            ::compareKeywords
        )
}


