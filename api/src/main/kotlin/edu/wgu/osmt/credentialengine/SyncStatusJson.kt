package edu.wgu.osmt.credentialengine

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Job-level sync status for debugging. Stored in SyncState.status_json.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SyncStatusJson(
    val lastRecordUuid: String? = null,
    val lastRecordName: String? = null,
    val batchIndex: Int? = null,
    val batchesCompleted: Int? = null,
    val lastUpdatedAt: String? = null,
    val sessionCorrelationId: String? = null,
    val inProgress: Boolean? = null,
    val error: SyncStatusError? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SyncStatusError(
    val message: String,
    val correlationId: String,
    val recordUuid: String? = null,
    val recordName: String? = null,
    val occurredAt: String,
)

private val mapper: ObjectMapper =
    jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

fun SyncStatusJson.toJsonString(): String = mapper.writeValueAsString(this)

fun parseSyncStatusJson(json: String?): SyncStatusJson? =
    if (json.isNullOrBlank()) {
        null
    } else {
        try {
            mapper.readValue<SyncStatusJson>(json)
        } catch (e: Exception) {
            null
        }
    }
