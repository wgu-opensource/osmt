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

@Document(indexName = "richskillrepository", createIndex = true)
data class RichSkillDescriptor(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val uuid: UUID,
    val name: String,
    val statement: String,
    val author: String,
    val jobCodes: List<JobCode> = listOf(),
    val keywords: List<Keyword> = listOf(),
    val category: Keyword? = null,
    val publishStatus: PublishStatus
) : DatabaseData, HasUpdateDate {

    companion object {
        fun create(name: String, statement: String, author: String): RichSkillDescriptor {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            return RichSkillDescriptor(
                id = null,
                uuid = UUID.randomUUID(),
                name = name,
                statement = statement,
                author = author,
                creationDate = now,
                updateDate = now,
                publishStatus = PublishStatus.Unpublished
            )
        }
    }
}

data class RsdUpdateObject(
    override val id: Long,
    val name: String? = null,
    val statement: String? = null,
    val author: String? = null,
    val category: NullableFieldUpdate<Keyword>? = null,
    val keywords: ListFieldUpdate<Keyword>? = null,
    val jobCodes: ListFieldUpdate<JobCode>? = null
) : UpdateObject<RichSkillDescriptorDao> {

    init {
        validate(this) {
            validate(RsdUpdateObject::category).validate {
                validate(NullableFieldUpdate<Keyword>::t).validate {
                    validate(Keyword::type).isEqualTo(KeywordTypeEnum.Category)
                }
            }
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
            if (that.category?.let { id } != it.t?.id) {
                jsonUpdateStatement(that.name, that.category?.let { it.value }, it.t?.value)
            } else null
        }
    }

    fun compareAuthor(that: RichSkillDescriptorDao): JSONObject? {
        return author?.let {
            compare(that::author, this::author, stringOutput)
        }
    }

    override val comparisonList: List<(t: RichSkillDescriptorDao) -> JSONObject?> =
        listOf(::compareName, ::compareStatement, ::compareCategory, ::compareAuthor)
}


