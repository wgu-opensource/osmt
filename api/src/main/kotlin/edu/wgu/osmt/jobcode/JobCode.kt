package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.DatabaseData
import edu.wgu.osmt.db.HasUpdateDate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.UpdateObject
import net.minidev.json.JSONObject
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
) : UpdateObject<JobCode> {

    fun compareCode(that: JobCode): JSONObject? {
        return code?.let {
            compare(that::code, this::code)
        }
    }

    fun compareName(that: JobCode): JSONObject? {
        return name?.let {
            compare(that::name, it::t, stringOutput)
        }
    }

    fun compareDescription(that: JobCode): JSONObject? {
        return description?.let {
            compare(that::description, it::t, stringOutput)
        }
    }

    fun compareSource(that: JobCode): JSONObject? {
        return source?.let {
            compare(that::source, it::t, stringOutput)
        }
    }

    override val comparisonList: List<(t: JobCode) -> JSONObject?> =
        listOf(::compareCode, ::compareName, ::compareDescription, ::compareSource)
}
