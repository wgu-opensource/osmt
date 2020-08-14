package edu.wgu.osmt.richskill

import com.google.gson.Gson
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.db.PublishStatusDao
import edu.wgu.osmt.db.PublishStatusTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

interface RichSkillRepository {
    val table: RichSkillDescriptorTable
    val dao: RichSkillDescriptorDao.Companion
    fun update(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor?
    fun findAll(): List<RichSkillDescriptor>
    fun findById(id: Long): RichSkillDescriptor?
    fun findByUUID(uuid: String): RichSkillDescriptor?
    fun create(
        name: String,
        statement: String,
        author: String,
        user: OAuth2User?
    ): RichSkillDescriptorDao
}

@Repository
class RichSkillRepositoryImpl @Autowired constructor(val auditLogRepository: AuditLogRepository) :
    RichSkillRepository {
    override val dao = RichSkillDescriptorDao.Companion
    override val table = RichSkillDescriptorTable

    override fun findAll() = transaction {
        dao.all().map { it.toModel() }
    }

    override fun findById(id: Long) = transaction {
        dao.findById(id)?.toModel()
    }

    @Transactional
    override fun update(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor? {
        transaction {
            val original = dao.findById(updateObject.id)
            val changes = original?.let { updateObject.diff(it) }
            changes?.let { it ->
                auditLogRepository.insert(
                    AuditLog.fromAtomicOp(
                        table,
                        updateObject.id,
                        it.toString(),
                        user!!,
                        AuditOperationType.Update
                    )
                )
            }
            table.update({ table.id eq updateObject.id }) {
                updateBuilderApplyFromUpdateObject(it, updateObject)
            }
        }
        return transaction { dao.findById(updateObject.id)?.toModel() }
    }

    override fun findByUUID(uuid: String): RichSkillDescriptor? = transaction {
        val query = table.select { table.uuid eq uuid }.singleOrNull()
        query?.let { dao.wrapRow(it).toModel() }
    }

    override fun create(
        name: String,
        statement: String,
        author: String,
        user: OAuth2User?
    ): RichSkillDescriptorDao {
        val newRichSkill = transaction {
            dao.new {
                this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
                this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
                this.uuid = UUID.randomUUID().toString()
                this.name = name
                this.statement = statement
                this.author = author
                this.publishStatus =
                    PublishStatusDao[EntityID(PublishStatus.Unpublished.ordinal.toLong(), PublishStatusTable)]
            }
        }
        transaction {
            auditLogRepository.insert(
                AuditLog.fromAtomicOp(
                    table,
                    newRichSkill.id.value,
                    Gson().toJson(newRichSkill.toModel()),
                    user!!,
                    AuditOperationType.Insert
                )
            )
        }
        return newRichSkill
    }
}
