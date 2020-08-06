package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.DslCrudRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Repository

interface RichSkillRepository : DslCrudRepository<RichSkillDescriptor, RsdUpdateObject> {
    val dao: RichSkillDescriptorDao.Companion
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
}
