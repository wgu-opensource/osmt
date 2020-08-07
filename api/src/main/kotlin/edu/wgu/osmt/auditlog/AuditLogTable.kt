package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.TableWithMappers
import org.jetbrains.exposed.sql.statements.InsertStatement

object AuditLogTable : TableWithMappers<AuditLog>("AuditLog") {
    val user = varchar("user", 256)
    val operationType = varchar("operationType", 128)
    val targetTableName = varchar("tableName", 128)
    val entityId = long("entityId")
    val changedFields = text("changedFields")

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: AuditLog) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[user] = t.user
        insertStatement[operationType] = t.operationType
        insertStatement[targetTableName] = t.tableName
        insertStatement[entityId] = t.entityId
        insertStatement[changedFields] = t.changedFields
    }
}
