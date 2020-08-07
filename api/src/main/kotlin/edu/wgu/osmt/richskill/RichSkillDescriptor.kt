package edu.wgu.osmt.richskill

import com.google.gson.JsonObject
import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.db.HasUpdateDate
import edu.wgu.osmt.db.UpdateObject
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime
import java.time.ZoneOffset

@Document(indexName = "richskillrepository", createIndex = true)
data class RichSkillDescriptor(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val title: String,
    val description: String,
    val jobCodes: List<JobCode> = listOf(),
    val keywords: List<Keyword> = listOf()
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
    val description: String?
) : UpdateObject {

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

    fun diff(that: RichSkillDescriptor): JSONArray {
        val l = listOfNotNull(compareTitle(that), compareDescription(that))
        val jsonObj = JSONArray()
        l.forEach { jsonObj.add(it) }
        return jsonObj
    }
}


