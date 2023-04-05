package edu.wgu.osmt.io.csv

import com.opencsv.bean.CsvBindByName
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Imports O*NET codes in CSV format
 *  - @see <a href="https://www.onetcenter.org/database.html#occ"></a>
 *  - download `Occupation Data` in Excel format
 *  - convert Excel -> csv
 *  - import _after_ running [[BlsImport]]
 *  - see `Imports` instructions in api/README.md
 */
@Component
class OnetImport : CsvImport<OnetJobCode> {
    override val log: Logger = LoggerFactory.getLogger(OnetImport::class.java)

    override val csvRowClass = OnetJobCode::class.java

    @Autowired
    private lateinit var jobCodeRepository: JobCodeRepository

    @Autowired
    private lateinit var jobCodeEsRepo: JobCodeEsRepo

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    private lateinit var richSkillEsRepo: RichSkillEsRepo

    @Autowired
    private lateinit var appConfig: AppConfig

    override fun handleRows(rows: List<OnetJobCode>) {
        log.info("Processing ${rows.size} rows...")
        for (row in rows) transaction {
            log.info("Importing ${row.title} - ${row.code}")
            val jobCode =
                row.code?.let { jobCodeRepository.findByCodeOrCreate(it, JobCodeRepository.`O*NET_FRAMEWORK`) }

            val detailed = row.detailed()?.let { jobCodeRepository.findBlsCode(it) }

            // Optimization, only fetch these if detailed failed
            val broad = detailed?.broad ?: row.broad()?.let { jobCodeRepository.findBlsCode(it) }?.broad
            val major = detailed?.major ?: row.major()?.let { jobCodeRepository.findBlsCode(it) }?.major
            val minor = detailed?.minor ?: row.minor()?.let { jobCodeRepository.findBlsCode(it) }?.minor

            jobCode?.let {
                it.name = row.title
                it.description = row.description
                it.detailed = detailed?.name
                it.broad = broad
                it.minor = minor
                it.major = major
                it.framework = JobCodeRepository.`O*NET_FRAMEWORK`
            }.also {
                jobCode?.let { jobCodeEsRepo.save(it.toModel()) }
                jobCode?.let {
                    richSkillRepository.containingJobCode(it.code)
                        .map { dao -> richSkillEsRepo.save(RichSkillDoc.fromDao(dao, appConfig)) }
                }
            }
        }
    }
}

class OnetJobCode : CsvRow, HasCodeHierarchy {
    @CsvBindByName(column = "O*NET-SOC Code")
    override var code: String? = null

    @CsvBindByName(column = "Title")
    var title: String? = null

    @CsvBindByName(column = "Description")
    var description: String? = null
}
