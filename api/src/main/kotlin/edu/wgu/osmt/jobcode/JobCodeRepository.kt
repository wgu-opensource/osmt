package edu.wgu.osmt.jobcode

import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

interface JobCodeRepository {
    val table: Table
    fun findAll(): SizedIterable<JobCodeDao>
    fun findById(id: Long): JobCodeDao?
    fun findByCode(code: String): JobCodeDao?
    fun findByCodeOrCreate(code: String, framework: String? = null): JobCodeDao
    fun findBlsCode(code: String): JobCodeDao?
    fun create(code: String, framework: String? = null): JobCodeDao

    companion object {
        const val BLS_FRAMEWORK = "bls"
        const val `O*NET_FRAMEWORK` = "o*net"
    }
}

@Repository
@Transactional
class JobCodeRepositoryImpl: JobCodeRepository {

    @Autowired
    @Lazy
    lateinit var jobCodeEsRepo: JobCodeEsRepo

    val dao = JobCodeDao.Companion
    override val table = JobCodeTable

    override fun findAll() = dao.all()

    override fun findById(id: Long): JobCodeDao? = dao.findById(id)

    override fun findByCode(code: String): JobCodeDao? {
        return table.select { table.code eq code }.firstOrNull()?.let { dao.wrapRow(it) }
    }

    override fun findBlsCode(code: String): JobCodeDao? {
        return table.select { table.code eq code and (table.framework eq JobCodeRepository.BLS_FRAMEWORK) }
            .firstOrNull()?.let { dao.wrapRow(it) }
    }

    override fun findByCodeOrCreate(code: String, framework: String?): JobCodeDao {
        val existing = findByCode(code)
        return existing ?: create(code, framework)
    }

    override fun create(code: String, framework: String?): JobCodeDao {
        return dao.new({
            creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.code = code
            this.framework = framework
        }).also { jobCodeEsRepo.save(it.toModel()) }
    }
}
