package edu.wgu.osmt.credentialengine

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object SyncStateTable : LongIdTable("SyncState") {
    val syncType = varchar("sync_type", 64)
    val syncKey = varchar("sync_key", 64)
    val recordType = varchar("record_type", 64)
    val syncWatermark = datetime("sync_watermark").nullable()
    val lastRecordId = long("last_record_id").nullable()
    val statusJson = text("status_json").nullable()

    init {
        uniqueIndex("uk_sync_state", syncType, syncKey, recordType)
    }
}
