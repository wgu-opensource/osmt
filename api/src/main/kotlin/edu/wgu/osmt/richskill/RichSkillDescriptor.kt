package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.db.HasUpdateDate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.UpdateObject
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import net.minidev.json.JSONArray
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
    val name: String? = null,
    val statement: String? = null,
    val author: String? = null,
    val category: NullableFieldUpdate<Keyword>? = null
) : UpdateObject {

    init {
        validate(this) {
            validate(RsdUpdateObject::category).validate {
                validate(NullableFieldUpdate<Keyword>::t).validate {
                    validate(Keyword::type).isEqualTo(KeywordTypeEnum.Category)
                }
            }
        }
    }

    fun compareTitle(that: RichSkillDescriptor): JSONObject? {
        return name?.let {
            if (name != that.name) JSONObject(
                mutableMapOf(
                    that::name.name to mutableMapOf(
                        "old" to that.name,
                        "new" to name
                    )
                )
            ) else null
        }
    }

    fun compareDescription(that: RichSkillDescriptor): JSONObject? {
        return statement?.let {
            if (statement != that.statement) JSONObject(
                mutableMapOf(
                    that::statement.name to mutableMapOf(
                        "original" to that.statement,
                        "new" to statement
                    )
                )
            ) else null
        }
    }

    fun compareCategory(that: RichSkillDescriptor): JSONObject? {
        return category?.let {
            if (category.t != that.category) {
                JSONObject(
                    mutableMapOf(
                        that::category.name to mutableMapOf(
                            "original" to that.category?.value,
                            "new" to category.t?.value
                        )
                    )
                )
            } else null
        }
    }

    fun compareAuthor(that: RichSkillDescriptor): JSONObject? {
        return author?.let {
            if (author != that.author) JSONObject(
                mutableMapOf(
                    that::author.name to mutableMapOf(
                        "original" to that.author,
                        "new" to author
                    )
                )
            ) else null
        }
    }

    fun diff(that: RichSkillDescriptor): JSONArray {
        val l = listOfNotNull(compareTitle(that), compareDescription(that), compareCategory(that), compareAuthor(that))
        val jsonObj = JSONArray()
        l.forEach { jsonObj.add(it) }
        return jsonObj
    }
}


