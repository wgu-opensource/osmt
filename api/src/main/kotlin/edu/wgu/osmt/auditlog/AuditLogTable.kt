package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.TableWithMappers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.springframework.stereotype.Service

@Service
class AuditLogTable : TableWithMappers<AuditLog>("AuditLog") {
    val user = text("user")
    val operationType = text("operationType")
    val entityType = text("entityType")
    val entityId = long("entityId")
    val changedFields = text("changedFields")

    override fun fromRow(t: ResultRow): AuditLog = AuditLog(
        creationDate = t[creationDate],
        id = t[id],
        user = t[user],
        operationType = t[operationType],
        entityType = t[entityType],
        entityId = t[entityId],
        changedFields = t[changedFields]
    )

    override fun insertStatementApplyFromT(insertStatement: InsertStatement<Number>, t: AuditLog) {
        super.insertStatementApplyFromT(insertStatement, t)
        insertStatement[user] = t.user
        insertStatement[operationType] = t.operationType
        insertStatement[entityType] = t.entityType
        insertStatement[entityId] = t.entityId
        insertStatement[changedFields] = t.changedFields
    }
}
