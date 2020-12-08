package edu.wgu.osmt.auditlog

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.collection.CollectionTable
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.SizedIterable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class AuditLogRepositoryTest @Autowired constructor(val auditLogRepository: AuditLogRepository): SpringTest(), BaseDockerizedTest, HasDatabaseReset {

    @Test
    fun `can insert a collection audit log`(){
        val auditLog = AuditLog.Companion.fromAtomicOp(CollectionTable, 1L, "{}", "test user", AuditOperationType.Insert)
        auditLogRepository.create(auditLog)
        val result: SizedIterable<AuditLogDao> = auditLogRepository.findByTableAndId(auditLog.tableName, auditLog.entityId)
        assertThat(result.first().entityId).isEqualTo(auditLog.entityId)
    }

    @Test
    fun `can insert a skill audit log`(){
        val auditLog = AuditLog.Companion.fromAtomicOp(RichSkillDescriptorTable, 1L, "{}", "test user", AuditOperationType.Insert)
        auditLogRepository.create(auditLog)
        val result: SizedIterable<AuditLogDao> = auditLogRepository.findByTableAndId(auditLog.tableName, auditLog.entityId)
        assertThat(result.first().entityId).isEqualTo(auditLog.entityId)
    }
}
