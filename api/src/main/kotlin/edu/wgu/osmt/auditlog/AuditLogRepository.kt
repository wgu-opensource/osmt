package edu.wgu.osmt.auditlog

import edu.wgu.osmt.config.AppConfig
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface AuditLogRepository {
    val table: AuditLogTable
    val dao: AuditLogDao.Companion

    fun create(auditLog: AuditLog): AuditLogDao?
    fun findByTableAndId(tableName: String, entityId: Long): AuditLogDao?
}

@Repository
@Transactional
class AuditLogRepositoryImpl @Autowired constructor(appConfig: AppConfig) : AuditLogRepository {
    override val table = AuditLogTable
    override val dao = AuditLogDao.Companion

    override fun create(auditLog: AuditLog): AuditLogDao? {
        if (auditLog.id != null) {
            return null
        }

        val auditLog = dao.new {
            this.entityId = auditLog.entityId
            this.creationDate = auditLog.creationDate
            this.changedFields = auditLog.changedFields
            this.operationType = auditLog.operationType
            this.targetTableName = auditLog.tableName
            this.user = auditLog.user
        }
        return auditLog
    }

    override fun findByTableAndId(tableName: String, entityId: Long): AuditLogDao? {
        return table.select {
            table.targetTableName eq tableName and (table.entityId eq entityId)
        }.firstOrNull()?.let { dao.wrapRow(it) }
    }
}
