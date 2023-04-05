package edu.wgu.osmt.auditlog

import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionTable
import edu.wgu.osmt.collection.diff
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.io.csv.BatchImportRichSkill
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.diff
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Provides support to re-generate the audit log from current state of database
 *  - Audit log table must be emptied first
 */
@Component
class AuditLogUtils {
    val logger: Logger = LoggerFactory.getLogger(AuditLogUtils::class.java)

    @Autowired
    private lateinit var auditLogRepository: AuditLogRepository

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    private lateinit var appConfig: AppConfig

    fun generateSkillLogs() {
        val skills = richSkillRepository.dao.all()
        skills.map { skill ->
            val changes = skill.toModel().let { it.diff(null) }
            auditLogRepository.create(
                AuditLog.fromAtomicOp(
                    RichSkillDescriptorTable,
                    skill.id.value,
                    changes,
                    BatchImportRichSkill.user,
                    AuditOperationType.Insert
                )
            )
        }
    }

    fun generateCollectionLogs() {
        val collections = collectionRepository.dao.all()
        collections.map { collection ->
            val changes = collection.toModel().let { it.diff(null) }
            auditLogRepository.create(
                AuditLog.fromAtomicOp(
                    CollectionTable,
                    collection.id.value,
                    changes,
                    BatchImportRichSkill.user,
                    AuditOperationType.Insert
                )
            )
        }
    }

    fun baseLineIfEmpty() {
        if (appConfig.baseLineAuditLogIfEmpty) {
            val entryCount = transaction { auditLogRepository.dao.all().count() }
            if (entryCount == 0L) {
                logger.info("Audit log appears empty, generating baseline from current state...")
                transaction { generateSkillLogs() }
                transaction { generateCollectionLogs() }
                logger.info("Audit log baseline complete.")
            }
        }
    }
}
