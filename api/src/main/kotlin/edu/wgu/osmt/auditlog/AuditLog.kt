package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.DatabaseData
import org.jetbrains.exposed.sql.Table
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime
import java.time.ZoneOffset


fun <T> List<T>.nullIfEmpty(): List<T>? {
    return if (this.isEmpty()) null else this
}

enum class AuditOperationType {
    Insert,
    Update,
    Delete,
    PublishStatusChange
}

data class Change(
    val fieldName: String,
    val old: String?,
    val new: String?
) {
    companion object {
        fun maybeChange(
            fieldName: String,
            old: String?,
            new: String?
        ): Change? {
            return if (old != new) {
                Change(fieldName, old, new)
            } else null
        }
    }
}

data class AuditLog(
    override val id: Long?,
    override val creationDate: LocalDateTime,
    val operationType: String,
    val tableName: String,
    val entityId: Long,
    val user: String,
    val changedFields: List<Change>
) : DatabaseData {

    companion object {

        fun fromAtomicOp(
            table: Table,
            entityId: Long,
            changes: List<Change>,
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
            changes: List<Change>,
            user: OAuth2User,
            opType: AuditOperationType
        ): AuditLog {
            return fromAtomicOp(table, entityId, changes, user.name.toString(), opType)
        }
    }
}
