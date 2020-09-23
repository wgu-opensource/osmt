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

    val major: String? = null,             // bls major category name
    val minor: String? = null,             // bls minor category name
    val broad: String? = null,             // bls broad category name
    val detailed: String? = null,          // bls detailed (for o*net level codes -- blank for bls detailed)

    val code: String,                           // bls detailed code or a o*net code: XX-XXXX or XX-XXXX.XX
    val name: String? = null,                   // human readable label
    val description: String? = null,
    val framework: String? = null,               // e.g.: "bls" or "o*net"
    val url: String? = null                     // e.g.: "http://onetonline/an/example/of/a/jobcode/canonicalUri"
) : DatabaseData {

    companion object {
        fun create(code: String): JobCode {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            return JobCode(
                id = null,
                creationDate = now,
                code = code
            )
        }
    }
}
