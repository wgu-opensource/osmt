package edu.wgu.osmt.jobcode

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneOffset

interface JobCodeRepository {
    val table: Table
    fun findAll(): List<JobCode>
    fun findById(id: Long): JobCode?
    fun findByCode(code: String): JobCode?
    fun create(code: String, framework: String? = null): JobCodeDao
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

    override fun findByCode(code: String): JobCode? {
        return transaction {
            table.select { table.code eq code }.singleOrNull()?.let { dao.wrapRow(it).toModel() }
        }
    }

    override fun create(code: String, framework: String?): JobCodeDao {
        return transaction {
            dao.new {
                updateDate = LocalDateTime.now(ZoneOffset.UTC)
                creationDate = LocalDateTime.now(ZoneOffset.UTC)
                this.code = code
                this.framework = framework
            }
        }
    }
}
