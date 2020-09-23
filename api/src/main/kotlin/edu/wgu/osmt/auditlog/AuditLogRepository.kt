package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.insertObject
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
class AuditLogRepository {
    val table: AuditLogTable = AuditLogTable

    @Transactional
    fun insert(t: AuditLog): Long? = table.insertObject(t)
}
