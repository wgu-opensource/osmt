package edu.wgu.osmt.auditlog

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
class AuditLogRepository {
    val table: AuditLogTable = AuditLogTable

    @Transactional
    fun insert(t: AuditLog): Long? = table.insert(t)
}
