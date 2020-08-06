package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.TableWithMappers
import org.jetbrains.exposed.sql.statements.InsertStatement

object AuditLogTable : TableWithMappers<AuditLog>("AuditLog") {
    val user = text("user")
    val operationType = text("operationType")
    val entityType = text("entityType")
    val entityId = long("entityId")
    val changedFields = text("changedFields")
    
    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: AuditLog) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[user] = t.user
        insertStatement[operationType] = t.operationType
        insertStatement[entityType] = t.entityType
        insertStatement[entityId] = t.entityId
        insertStatement[changedFields] = t.changedFields
    }
}
