package edu.wgu.osmt.richskill

import edu.wgu.osmt.jobcode.JobCodeDao
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface RichSkillJobCodeRepository {
    val table: Table

    fun hasRSDs(jobCodeDao: JobCodeDao): Boolean
}

@Repository
@Transactional
class RichSkillJobCodeRepositoryImpl: RichSkillJobCodeRepository {
    override val table = RichSkillJobCodes

    override fun hasRSDs(jobCodeDao: JobCodeDao): Boolean {
        return !table.select { table.jobCodeId eq jobCodeDao.id }.empty()
    }
}
