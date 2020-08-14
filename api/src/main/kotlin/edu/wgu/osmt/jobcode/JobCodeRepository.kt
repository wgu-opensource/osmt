package edu.wgu.osmt.jobcode

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

interface JobCodeRepository {
    val table: Table
    fun findAll(): List<JobCode>
    fun findById(id: Long): JobCode?
}

@Repository
class JobCodeRepositoryImpl : JobCodeRepository {
    val dao = JobCodeDao.Companion
    override val table = JobCodeTable

    override fun findAll() = transaction {
        dao.all().map { it.toModel() }
    }

    override fun findById(id: Long): JobCode? = transaction {
        dao.findById(id)?.toModel()
    }
}
