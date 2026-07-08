package edu.wgu.osmt.credentialengine

import java.time.LocalDateTime

data class SyncState(
    val syncType: String,
    val syncKey: String,
    val recordType: String,
    val syncWatermark: LocalDateTime?,
    val statusJson: String? = null,
    val lastRecordId: Long? = null,
)
