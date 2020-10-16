package edu.wgu.osmt.jobcode

import edu.wgu.osmt.elasticsearch.EsJobCodeRepository
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

interface JobCodeRepository {
    val table: Table
    fun findAll(): List<JobCode>
    fun findById(id: Long): JobCodeDao?
    fun findByCode(code: String): JobCodeDao?
    fun findByCodeOrCreate(code: String, framework: String? = null): JobCodeDao
    fun create(code: String, framework: String? = null): JobCodeDao
}

@Repository
@Transactional
class JobCodeRepositoryImpl @Autowired constructor(val esJobCodeRepository: EsJobCodeRepository) : JobCodeRepository {
    val dao = JobCodeDao.Companion
    override val table = JobCodeTable

    override fun findAll() = dao.all().map {
        it.toModel()
    }

    override fun findById(id: Long): JobCodeDao? = dao.findById(id)

    override fun findByCode(code: String): JobCodeDao? {
        return table.select { table.code eq code }.singleOrNull()?.let { dao.wrapRow(it) }
    }

    override fun findByCodeOrCreate(code: String, framework: String?): JobCodeDao {
        val existing = findByCode(code)
        return existing ?: create(code, framework)
    }

    override fun create(code: String, framework: String?): JobCodeDao {
        return dao.new {
            creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.code = code
            this.framework = framework
        }.also { esJobCodeRepository.save(it.toModel()) }
    }
}
