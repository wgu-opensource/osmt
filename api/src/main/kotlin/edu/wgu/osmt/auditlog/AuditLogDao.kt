package edu.wgu.osmt.auditlog

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AuditLogDao(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<AuditLogDao>(AuditLogTable)

    var creationDate by AuditLogTable.creationDate
    var user by AuditLogTable.user
    var operationType by AuditLogTable.operationType
    var targetTableName by AuditLogTable.targetTableName
    var entityId by AuditLogTable.entityId
    var changedFields by AuditLogTable.changedFields
}
