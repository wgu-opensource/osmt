package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.db.HasUpdateDate
import edu.wgu.osmt.db.UpdateObject
import edu.wgu.osmt.jobcode.JobCode
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.springframework.data.elasticsearch.annotations.Document
import java.time.LocalDateTime
import java.time.ZoneOffset

@Document(indexName = "richskillrepository", createIndex = true)
data class RichSkillDescriptor(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val title: String,
    val description: String,
    val jobCodes: List<JobCode> = listOf()
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
) : UpdateObject


