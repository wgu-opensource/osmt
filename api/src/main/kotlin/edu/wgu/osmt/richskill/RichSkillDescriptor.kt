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

@Document(indexName = "richskillrepository", createIndex = true)
data class RichSkillDescriptor(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val title: String,
    val description: String,
    val jobCodes: List<JobCode> = listOf(),
    val keywords: List<Keyword> = listOf(),
    val category: Keyword? = null
) : DatabaseData<RichSkillDescriptor>, HasUpdateDate {

    override fun withId(id: Long): RichSkillDescriptor {
        return copy(id = id)
    }

    companion object {
        fun create(title: String, description: String): RichSkillDescriptor {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            return RichSkillDescriptor(
                id = null,
                title = title,
                description = description,
                creationDate = now,
                updateDate = now
            )
        }
    }
}

data class RsdUpdateObject(
    override val id: Long,
    val title: String?,
    val description: String?,
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
        return title?.let {
            if (title != that.title) JSONObject(
                mutableMapOf(
                    that::title.name to mutableMapOf(
                        "old" to that.title,
                        "new" to title
                    )
                )
            ) else null
        }
    }

    fun compareDescription(that: RichSkillDescriptor): JSONObject? {
        return description?.let {
            if (description != that.description) JSONObject(
                mutableMapOf(
                    that::description.name to mutableMapOf(
                        "original" to that.description,
                        "new" to description
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

    fun diff(that: RichSkillDescriptor): JSONArray {
        val l = listOfNotNull(compareTitle(that), compareDescription(that), compareCategory(that))
        val jsonObj = JSONArray()
        l.forEach { jsonObj.add(it) }
        return jsonObj
    }
}


