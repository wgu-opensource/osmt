package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.TableWithMappers
import edu.wgu.osmt.db.UpdateObject
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.springframework.stereotype.Service

@Service
class AuditLogTable: TableWithMappers<AuditLog, UpdateObject>("AuditLog") {
    val user = text("user")
    val operationType = text("operationType")
    val entityType = text("entityType")
    val entityId = long("entityId")
    val changedFields = text("changedFields")

    override fun toRowFromUpdateObject(updateBuilder: UpdateBuilder<Number>, updateObject: UpdateObject){}

    override fun fromRow(t: ResultRow): AuditLog  = AuditLog(
            operationType = t[operationType],
            entityType = t[entityType],
            entityId = t[entityId],
            user = t[user],
            changedFields = t[changedFields],
            creationDate = t[creationDate],
            id = t[id]
    )

    override fun toRowFromT(updateBuilder: UpdateBuilder<Number>, t: AuditLog) {
        super.toRowFromT(updateBuilder, t)
        updateBuilder[operationType] = t.operationType
        updateBuilder[user] = t.user
        updateBuilder[entityType] = t.entityType
        updateBuilder[entityId] = t.entityId
        updateBuilder[changedFields] = t.changedFields
    }
}
