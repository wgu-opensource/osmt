package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.DslCrudRepository
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface RichSkillRepository : DslCrudRepository<RichSkillDescriptor, RsdUpdateObject> {
    val dao: RichSkillDescriptorDao.Companion
    suspend fun update(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor?
    suspend fun findAll(): List<RichSkillDescriptor>
    suspend fun findById(id: Long): RichSkillDescriptor?
}

@Repository
class RichSkillRepositoryImpl :
    RichSkillRepository {
    override val dao = RichSkillDescriptorDao.Companion
    override val table = RichSkillDescriptorTable

    override suspend fun findAll() = newSuspendedTransaction {
        dao.all().map { it.toModel() }
    }

    override suspend fun findById(id: Long) = newSuspendedTransaction {
        dao.findById(id)?.toModel()
    }

    @Transactional
    override suspend fun update(updateObject: RsdUpdateObject, user: OAuth2User?): RichSkillDescriptor? {
        newSuspendedTransaction {
            val original = dao.findById(updateObject.id)?.toModel()
            original?.let { updateObject.diff(it) }
            table.update({ table.id eq updateObject.id }) {
                updateBuilderApplyFromUpdateObject(it, updateObject)
            }
        }
        return newSuspendedTransaction { dao.findById(updateObject.id)?.toModel() }
    }
}
