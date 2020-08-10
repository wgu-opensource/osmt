package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.DslCrudRepository
import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Repository

interface JobCodeRepository : DslCrudRepository<JobCode, JobCodeUpdate> {
    suspend fun findAll(): List<JobCode>
    suspend fun findById(id: Long): JobCode?
}

@Repository
class JobCodeRepositoryImpl : JobCodeRepository {
    val dao = JobCodeDao.Companion
    override val table: TableWithUpdateMapper<JobCode, JobCodeUpdate> = JobCodeTable

    override suspend fun findAll() = newSuspendedTransaction {
        dao.all().map { it.toModel() }
    }

    override suspend fun findById(id: Long): JobCode? = newSuspendedTransaction {
        dao.findById(id)?.toModel()
    }
}
