package edu.wgu.osmt.auditlog

import edu.wgu.osmt.db.BaseRepository
import edu.wgu.osmt.db.HasInsert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Repository


@Repository
class AuditLogRepository @Autowired constructor(override val table: AuditLogTable) : BaseRepository<AuditLog>,
    HasInsert<AuditLog> {}
