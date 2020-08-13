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
    val category: Keyword? = null
) : DatabaseData<RichSkillDescriptor>, HasUpdateDate {

    override fun withId(id: Long): RichSkillDescriptor {
        return copy(id = id)
    }

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
                updateDate = now
            )
        }
    }
}

data class RsdUpdateObject(
    override val id: Long,
    val name: String?,
    val statement: String? = null,
    val author: String? = null,
    val category: NullableFieldUpdate<Keyword>? = null
) : UpdateObject<RichSkillDescriptor> {

    init {
        validate(this) {
            validate(RsdUpdateObject::category).validate {
                validate(NullableFieldUpdate<Keyword>::t).validate {
                    validate(Keyword::type).isEqualTo(KeywordTypeEnum.Category)
                }
            }
        }
    }

    fun compareName(that: RichSkillDescriptor): JSONObject? {
        return name?.let {
            compare(that::name, this::name, stringOutput)
        }
    }

    fun compareStatement(that: RichSkillDescriptor): JSONObject? {
        return statement?.let {
            compare(that::statement, this::statement, stringOutput)
        }
    }

    fun compareCategory(that: RichSkillDescriptor): JSONObject? {
        return category?.let {
            compare(that::category, it::t, keywordOutput)
        }
    }

    fun compareAuthor(that: RichSkillDescriptor): JSONObject? {
        return author?.let {
            compare(that::author, this::author, stringOutput)
        }
    }

    override val comparisonList: List<(t: RichSkillDescriptor) -> JSONObject?> =
        listOf(::compareName, ::compareStatement, ::compareCategory, ::compareAuthor)
}


