package edu.wgu.osmt.richskill

import com.google.gson.Gson
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditOperationType
import edu.wgu.osmt.db.DslCrudRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Repository

interface RichSkillRepository : DslCrudRepository<RichSkillDescriptor, RsdUpdateObject> {
    suspend fun insert(t: RichSkillDescriptor, user: OAuth2User): RichSkillDescriptor
    suspend fun update(updateObject: RsdUpdateObject, user: OAuth2User): RichSkillDescriptor?
    suspend fun findAll(): List<RichSkillDescriptor>
    suspend fun findById(id: Long): RichSkillDescriptor?
}

@Repository
class RichSkillRepositoryImpl @Autowired constructor(val auditLogRepository: AuditLogRepository) :
    RichSkillRepository {
    val dao = RichSkillDescriptorDao.Companion
    override val table = RichSkillDescriptorTable

    override suspend fun insert(t: RichSkillDescriptor, user: OAuth2User): RichSkillDescriptor {
        val resultId = table.insert(t)
        val fetched = findById(resultId)!!
        auditLogRepository.insert(
            AuditLog.fromAtomicOp(
                table,
                resultId,
                Gson().toJson(fetched),
                user,
                AuditOperationType.Insert
            )
        )
        return fetched
    }

    override suspend fun findAll() = newSuspendedTransaction {
        dao.all().map { it.toModel() }
    }

    override suspend fun findById(id: Long) = newSuspendedTransaction {
        dao.findById(id)?.toModel()
    }

    override suspend fun update(updateObject: RsdUpdateObject, user: OAuth2User): RichSkillDescriptor? {
        newSuspendedTransaction {
            val original = dao.findById(updateObject.id)?.toModel()
            val changes = original?.let { updateObject.diff(it) }

            table.update(updateObject)

            changes?.let {
                auditLogRepository.insert(
                    AuditLog.fromAtomicOp(
                        table,
                        updateObject.id,
                        it.toString(),
                        user,
                        AuditOperationType.Update
                    )
                )
            }
        }
        return newSuspendedTransaction { dao.findById(updateObject.id)?.toModel() }
    }
}
