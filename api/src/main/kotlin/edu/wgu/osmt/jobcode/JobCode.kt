package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.db.HasUpdateDate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.UpdateObject
import org.springframework.data.elasticsearch.annotations.Document
import java.time.LocalDateTime
import java.time.ZoneOffset

@Document(indexName = "jobCode", createIndex = true)
data class JobCode(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    override val updateDate: LocalDateTime,
    val code: String,
    val name: String? = null,
    val description: String? = null,
    val source: String? = null
) : DatabaseData<JobCode>, HasUpdateDate {

    override fun withId(id: Long): JobCode {
        return copy(id = id)
    }

    companion object {
        fun create(code: String): JobCode {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            return JobCode(
                id = null,
                creationDate = now,
                updateDate = now,
                code = code
            )
        }
    }
}

data class JobCodeUpdate(
    override val id: Long,
    val code: String?,
    val name: NullableFieldUpdate<String>?,
    val description: NullableFieldUpdate<String>?,
    val source: NullableFieldUpdate<String>?
) : UpdateObject
