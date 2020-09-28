package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.DatabaseData
import org.jetbrains.exposed.sql.Table
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime
import java.time.ZoneOffset

enum class AuditOperationType {
    Insert,
    Update,
    Delete
}

data class AuditLog(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    val operationType: String,
    val tableName: String,
    val entityId: Long,
    val user: String,
    val changedFields: String
) : DatabaseData {

    companion object {

        fun fromAtomicOp(
            table: Table,
            entityId: Long,
            changes: String,
            user: String,
            opType: AuditOperationType
        ): AuditLog {
            return AuditLog(
                id = null,
                creationDate = LocalDateTime.now(ZoneOffset.UTC),
                operationType = opType.name,
                tableName = table.tableName,
                entityId = entityId,
                user = user,
                changedFields = changes
            )
        }

        fun fromAtomicOp(
            table: Table,
            entityId: Long,
            changes: String,
            user: OAuth2User,
            opType: AuditOperationType
        ): AuditLog {
            return fromAtomicOp(table,entityId,changes,user.name.toString(),opType)
        }
    }
}
