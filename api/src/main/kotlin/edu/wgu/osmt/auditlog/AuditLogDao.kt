package edu.wgu.osmt.auditlog

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.wgu.osmt.db.OutputsModel
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AuditLogDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<AuditLog> {
    companion object : LongEntityClass<AuditLogDao>(AuditLogTable)

    var creationDate by AuditLogTable.creationDate
    var user by AuditLogTable.user
    var operationType by AuditLogTable.operationType
    var targetTableName by AuditLogTable.targetTableName
    var entityId by AuditLogTable.entityId
    var changedFields by AuditLogTable.changedFields

    override fun toModel(): AuditLog {
        val changeType = object : TypeToken<List<Change>>() {}.type
        val deserializedChanges = Gson().fromJson<List<Change>>(changedFields, changeType)
        return AuditLog(id.value, creationDate, operationType, targetTableName, entityId, user, deserializedChanges)
    }
}
