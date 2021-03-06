package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.BaseTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object AuditLogTable : LongIdTable("AuditLog"), BaseTable {
    override val creationDate = datetime("creationDate")
    val user = varchar("user", 256)
    val operationType = varchar("operationType", 128)
    val targetTableName = varchar("tableName", 128)
    val entityId = long("entityId")
    val changedFields = text("changedFields")
}
