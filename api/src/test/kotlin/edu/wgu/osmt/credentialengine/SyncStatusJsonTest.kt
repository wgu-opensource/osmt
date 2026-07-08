package edu.wgu.osmt.credentialengine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SyncStatusJsonTest {
    @Test
    fun `serialize and deserialize`() {
        val status =
            SyncStatusJson(
                lastRecordUuid = "uuid-1",
                lastRecordName = "Skill A",
                batchIndex = 2,
                batchesCompleted = 2,
                lastUpdatedAt = "2026-03-04T12:00:00Z",
            )
        val json = status.toJsonString()
        val parsed = parseSyncStatusJson(json)
        assertThat(parsed).isNotNull
        assertThat(parsed!!.lastRecordUuid).isEqualTo("uuid-1")
        assertThat(parsed.lastRecordName).isEqualTo("Skill A")
        assertThat(parsed.batchIndex).isEqualTo(2)
    }

    @Test
    fun `parse null returns null`() {
        assertThat(parseSyncStatusJson(null)).isNull()
        assertThat(parseSyncStatusJson("")).isNull()
    }

    @Test
    fun `parse invalid returns null`() {
        assertThat(parseSyncStatusJson("not json")).isNull()
    }

    @Test
    fun `serialize with error`() {
        val status =
            SyncStatusJson(
                error =
                    SyncStatusError(
                        message = "CE publish failed",
                        correlationId = "abc12def34",
                        recordUuid = "uuid-1",
                        recordName = "Skill A",
                        occurredAt = "2026-03-04T12:05:00Z",
                    ),
            )
        val json = status.toJsonString()
        assertThat(json).contains("abc12def34")
        assertThat(json).contains("CE publish failed")
    }

    @Test
    fun `serialize with sessionCorrelationId`() {
        val status =
            SyncStatusJson(
                batchesCompleted = 3,
                sessionCorrelationId = "xyz99abc12",
                lastUpdatedAt = "2026-03-04T12:00:00",
            )
        val json = status.toJsonString()
        assertThat(json).contains("xyz99abc12")
        val parsed = parseSyncStatusJson(json)
        assertThat(parsed!!.sessionCorrelationId).isEqualTo("xyz99abc12")
    }

    @Test
    fun `serialize and parse inProgress`() {
        val inProgress = SyncStatusJson(inProgress = true, sessionCorrelationId = "abc")
        val json = inProgress.toJsonString()
        assertThat(json).contains("inProgress")
        val parsed = parseSyncStatusJson(json)
        assertThat(parsed!!.inProgress).isTrue()

        val done = SyncStatusJson(inProgress = false, batchesCompleted = 1)
        val doneJson = done.toJsonString()
        val doneParsed = parseSyncStatusJson(doneJson)
        assertThat(doneParsed!!.inProgress).isFalse()
    }
}
