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

    val major: String? = null,             // bls major category name
    val minor: String? = null,             // bls minor category name
    val broad: String? = null,             // bls broad category name
    val detailed: String? = null,          // bls detailed (for o*net level codes -- blank for bls detailed)

    val code: String,                           // bls detailed code or a o*net code: XX-XXXX or XX-XXXX.XX
    val name: String? = null,                   // human readable label
    val description: String? = null,
    val framework: String? = null               // e.g.: "bls" or "o*net"
) : DatabaseData, HasUpdateDate {

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
    val major: NullableFieldUpdate<String>?,
    val minor: NullableFieldUpdate<String>?,
    val broad: NullableFieldUpdate<String>?,
    val detailed: NullableFieldUpdate<String>?,
    val code: String?,
    val name: NullableFieldUpdate<String>?,
    val description: NullableFieldUpdate<String>?,
    val framework: NullableFieldUpdate<String>?
) : UpdateObject<JobCode> {

    fun compareMajor(that: JobCode): JSONObject? {
        return major?.let {
            compare(that::major, it::t, stringOutput)
        }
    }

    fun compareMinor(that: JobCode): JSONObject? {
        return minor?.let {
            compare(that::minor, it::t, stringOutput)
        }
    }

    fun compareBroad(that: JobCode): JSONObject? {
        return broad?.let {
            compare(that::broad, it::t, stringOutput)
        }
    }

    fun compareDetailed(that: JobCode): JSONObject? {
        return detailed?.let {
            compare(that::detailed, it::t, stringOutput)
        }
    }

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

    fun compareFramework(that: JobCode): JSONObject? {
        return framework?.let {
            compare(that::framework, it::t, stringOutput)
        }
    }

    override val comparisonList: List<(t: JobCode) -> JSONObject?> =
        listOf(
            ::compareMajor,
            ::compareMinor,
            ::compareBroad,
            ::compareDetailed,
            ::compareCode,
            ::compareName,
            ::compareDescription,
            ::compareFramework)
}
