package edu.wgu.osmt.jobcode

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

interface JobCodeRepository {
    val table: Table
    fun findAll(): List<JobCode>
    fun findById(id: Long): JobCodeDao?
    fun findByCode(code: String): JobCodeDao?
    fun create(code: String, framework: String? = null): JobCodeDao
}

@Repository
@Transactional
class JobCodeRepositoryImpl : JobCodeRepository {
    val dao = JobCodeDao.Companion
    override val table = JobCodeTable

    override fun findAll() = transaction {
        dao.all().map { it.toModel() }
    }

    override fun findById(id: Long): JobCodeDao? {
        return dao.findById(id)
    }

    override fun findByCode(code: String): JobCodeDao? {
        return table.select { table.code eq code }.singleOrNull()?.let { dao.wrapRow(it) }
    }

    override fun create(code: String, framework: String?): JobCodeDao {
        return transaction {
            dao.new {
                creationDate = LocalDateTime.now(ZoneOffset.UTC)
                this.code = code
                this.framework = framework
            }
        }
    }
}
