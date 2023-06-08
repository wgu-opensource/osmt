package edu.wgu.osmt.jobcode

import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.JobCodeUpdate
import edu.wgu.osmt.db.JobCodeLevel
import edu.wgu.osmt.richskill.RichSkillJobCodeRepository
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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
    fun hasChildren(jobCodeDao: JobCodeDao): Boolean
    fun findBlsCode(code: String): JobCodeDao?
    fun create(code: String, framework: String? = null): JobCodeDao
    fun createFromApi(jobCodes: List<JobCodeUpdate>): List<JobCodeDao>
    fun onetsByDetailCode(detailedCode: String): SizedIterable<JobCodeDao>
    fun remove(jobCodeId: Long): ApiBatchResult

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

    @Autowired
    @Lazy
    lateinit var richSkillJobCodeRepository: RichSkillJobCodeRepository

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

    override fun createFromApi(jobCodes: List<JobCodeUpdate>): List<JobCodeDao> {
        return jobCodes.map { jobCodeUpdate ->
            dao.new {
                this.code = jobCodeUpdate.code
                this.framework = jobCodeUpdate.framework
                this.name = jobCodeUpdate.targetNodeName
                this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
                this.major = "my major"
            }.also { jobCodeEsRepo.save(it.toModel()) }
        }
    }

    override fun findByCodeOrCreate(code: String, framework: String?): JobCodeDao {
        val existing = findByCode(code)
        return existing ?: create(code, framework)
    }

    override fun hasChildren(jobCodeDao: JobCodeDao): Boolean {
        return findAll().any { jobCode ->
            jobCode.code != jobCodeDao.code &&
            (jobCodeDao.code == JobCodeBreakout.majorCode(jobCode.code) ||
                    jobCodeDao.code == JobCodeBreakout.minorCode(jobCode.code) ||
                    jobCodeDao.code == JobCodeBreakout.broadCode(jobCode.code) ||
                    jobCodeDao.code == JobCodeBreakout.detailedCode(jobCode.code))
        }
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

    override fun remove(jobCodeId: Long): ApiBatchResult {
        val jobCodeFound = findById(jobCodeId)
        val jobCodeEsFound = jobCodeEsRepo.findById(jobCodeId.toInt())
        var hasChildren = false
        var hasRSDs = false
        if (jobCodeFound != null && jobCodeEsFound.isPresent) {
            hasChildren = hasChildren(jobCodeFound)
            hasRSDs = richSkillJobCodeRepository.hasRSDs(jobCodeFound)
            if (!hasChildren && !hasRSDs) {
                transaction {
                    table.deleteWhere{ table.id eq jobCodeFound.id }
                    jobCodeEsRepo.delete(jobCodeEsFound.get())
                }
                return ApiBatchResult(
                    success = true,
                    modifiedCount = 1,
                    totalCount = 1
                )
            }
        }
        return ApiBatchResult(
            success = false,
            modifiedCount = 0,
            totalCount = 0,
            message = JobCodeErrorMessages.forDeleteError(hasChildren, hasRSDs)
        )
    }
}

enum class JobCodeErrorMessages(val apiValue: String) {
    JobCodeNotExist("You cannot delete this occupation because you doesn't exist"),
    JobCodeHasChildren("You cannot delete this occupation because has children"),
    JobCodeHasRSD("You cannot delete this occupation because is used in one or more RSDs");

    companion object {
        fun forDeleteError(hasChildren: Boolean, hasRSDs: Boolean): String {
            return if (hasChildren) {
                JobCodeHasChildren.apiValue
            } else if (hasRSDs) {
                JobCodeHasRSD.apiValue
            } else {
                JobCodeNotExist.apiValue
            }
        }
    }
}
