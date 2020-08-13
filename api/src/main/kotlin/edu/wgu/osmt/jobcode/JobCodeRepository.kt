package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.DslCrudRepository
import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

interface JobCodeRepository : DslCrudRepository<JobCode, JobCodeUpdate> {
    fun findAll(): List<JobCode>
    fun findById(id: Long): JobCode?
}

@Repository
class JobCodeRepositoryImpl : JobCodeRepository {
    val dao = JobCodeDao.Companion
    override val table: TableWithUpdateMapper<JobCode, JobCodeUpdate> = JobCodeTable

    override fun findAll() = transaction {
        dao.all().map { it.toModel() }
    }

    override fun findById(id: Long): JobCode? = transaction {
        dao.findById(id)?.toModel()
    }
}
