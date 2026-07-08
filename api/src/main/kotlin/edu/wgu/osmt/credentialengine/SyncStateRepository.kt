package edu.wgu.osmt.credentialengine

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Repository
@Transactional
class SyncStateRepository {
    fun getWatermark(
        syncType: String,
        syncKey: String,
        recordType: String,
    ): LocalDateTime? =
        SyncStateTable
            .select {
                (SyncStateTable.syncType eq syncType) and
                    (SyncStateTable.syncKey eq syncKey) and
                    (SyncStateTable.recordType eq recordType)
            }.firstOrNull()
            ?.get(SyncStateTable.syncWatermark)

    fun getLastRecordId(
        syncType: String,
        syncKey: String,
        recordType: String,
    ): Long? =
        SyncStateTable
            .select {
                (SyncStateTable.syncType eq syncType) and
                    (SyncStateTable.syncKey eq syncKey) and
                    (SyncStateTable.recordType eq recordType)
            }.firstOrNull()
            ?.get(SyncStateTable.lastRecordId)

    /**
     * Raw JDBC write to preserve DATETIME(6) microsecond precision.
     * Exposed's datetime() column type truncates to milliseconds via setObject/setTimestamp,
     * but the actual MySQL column and the record updateDate columns store microseconds.
     */
    fun updateWatermark(
        syncType: String,
        syncKey: String,
        recordType: String,
        watermark: LocalDateTime,
        lastRecordId: Long? = null,
    ) {
        val sql =
            """
            UPDATE SyncState
            SET sync_watermark = ?, last_record_id = ?
            WHERE sync_type = ? AND sync_key = ? AND record_type = ?
            """.trimIndent()
        val formatted = watermark.format(DATETIME6_FORMATTER)
        val conn = TransactionManager.current().connection.connection as java.sql.Connection
        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, formatted)
            if (lastRecordId != null) ps.setLong(2, lastRecordId) else ps.setNull(2, java.sql.Types.BIGINT)
            ps.setString(3, syncType)
            ps.setString(4, syncKey)
            ps.setString(5, recordType)
            ps.executeUpdate()
        }
    }

    fun resetWatermark(
        syncType: String,
        syncKey: String,
        recordType: String,
    ) {
        SyncStateTable.update({
            (SyncStateTable.syncType eq syncType) and
                (SyncStateTable.syncKey eq syncKey) and
                (SyncStateTable.recordType eq recordType)
        }) {
            it[SyncStateTable.syncWatermark] = null
            it[SyncStateTable.lastRecordId] = null
        }
    }

    fun getStatusJson(
        syncType: String,
        syncKey: String,
        recordType: String,
    ): String? =
        SyncStateTable
            .select {
                (SyncStateTable.syncType eq syncType) and
                    (SyncStateTable.syncKey eq syncKey) and
                    (SyncStateTable.recordType eq recordType)
            }.firstOrNull()
            ?.get(SyncStateTable.statusJson)

    fun updateStatusJson(
        syncType: String,
        syncKey: String,
        recordType: String,
        statusJson: String,
    ) {
        SyncStateTable.update({
            (SyncStateTable.syncType eq syncType) and
                (SyncStateTable.syncKey eq syncKey) and
                (SyncStateTable.recordType eq recordType)
        }) {
            it[SyncStateTable.statusJson] = statusJson
        }
    }

    fun getOrCreateRow(
        syncType: String,
        syncKey: String,
        recordType: String,
    ): SyncState {
        val existing =
            SyncStateTable
                .select {
                    (SyncStateTable.syncType eq syncType) and
                        (SyncStateTable.syncKey eq syncKey) and
                        (SyncStateTable.recordType eq recordType)
                }.firstOrNull()

        return if (existing != null) {
            SyncState(
                syncType = existing[SyncStateTable.syncType],
                syncKey = existing[SyncStateTable.syncKey],
                recordType = existing[SyncStateTable.recordType],
                syncWatermark = existing[SyncStateTable.syncWatermark],
                statusJson = existing[SyncStateTable.statusJson],
                lastRecordId = existing[SyncStateTable.lastRecordId],
            )
        } else {
            SyncStateTable.insert {
                it[SyncStateTable.syncType] = syncType
                it[SyncStateTable.syncKey] = syncKey
                it[SyncStateTable.recordType] = recordType
                it[SyncStateTable.syncWatermark] = null
                it[SyncStateTable.lastRecordId] = null
                it[SyncStateTable.statusJson] = null
            }
            SyncState(syncType, syncKey, recordType, null, null, null)
        }
    }

    companion object {
        private val DATETIME6_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    }

    fun findAllBySyncKey(
        syncType: String,
        syncKey: String,
    ): List<SyncState> =
        SyncStateTable
            .select {
                (SyncStateTable.syncType eq syncType) and
                    (SyncStateTable.syncKey eq syncKey)
            }.map {
                SyncState(
                    syncType = it[SyncStateTable.syncType],
                    syncKey = it[SyncStateTable.syncKey],
                    recordType = it[SyncStateTable.recordType],
                    syncWatermark = it[SyncStateTable.syncWatermark],
                    statusJson = it[SyncStateTable.statusJson],
                    lastRecordId = it[SyncStateTable.lastRecordId],
                )
            }
}
