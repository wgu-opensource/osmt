package edu.wgu.osmt.jobcode

import edu.wgu.osmt.api.model.JobCodeUpdate
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
    fun createFromApi(jobCodeUpdate: JobCodeUpdate): JobCodeDao
    fun onetsByDetailCode(detailedCode: String): SizedIterable<JobCodeDao>

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
        if (code.isBlank()) return null
        return table.select { table.code eq code }.firstOrNull()?.let { dao.wrapRow(it) }
    }

    override fun findBlsCode(code: String): JobCodeDao? {
        return table.select { table.code eq code and (table.framework eq JobCodeRepository.BLS_FRAMEWORK) }
            .firstOrNull()?.let { dao.wrapRow(it) }
    }

    override fun createFromApi(jobCodeUpdate: JobCodeUpdate): JobCodeDao {
        return dao.new {
            this.code = jobCodeUpdate.code
            this.framework = jobCodeUpdate.framework
            this.name = jobCodeUpdate.targetNodeName
            this.description = jobCodeUpdate.description
            this.name = "my name"
            this.major = "my major"
        }
    }

    override fun findByCodeOrCreate(code: String, framework: String?): JobCodeDao {
        val existing = findByCode(code)
        return existing ?: create(code, framework)
    }

    override fun create(code: String, framework: String?): JobCodeDao {
        val maybeDetailed = JobCodeBreakout.detailedCode(code).let{ findBlsCode(code) }
        return dao.new {
            creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.code = code
            this.framework = framework
            maybeDetailed?.let { detailed ->
                {
                    this.detailed = detailed.detailed
                    this.broad = detailed.broad
                    this.major = detailed.major
                    this.minor = detailed.minor
                }
            }
        }.also { jobCodeEsRepo.save(it.toModel()) }
    }

    override fun onetsByDetailCode(detailedCode: String): SizedIterable<JobCodeDao> {
        return table.select {table.code regexp "${detailedCode}.[0-90-9]"}.let{dao.wrapRows(it)}
    }
}
