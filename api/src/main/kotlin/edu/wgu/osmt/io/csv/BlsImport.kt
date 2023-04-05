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
import org.springframework.stereotype.Component

/**
 * Import BLS codes in CSV format
 *  - @see <a href="https://www.bls.gov/soc/2018/#materials"></a>
 *  - download `2018 SOC Definitions` in Excel format
 *  - convert Excel -> csv
 *  - see `Imports` instructions in api/README.md
 */
@Component
class BlsImport : CsvImport<BlsJobCode> {
    override val log: Logger = LoggerFactory.getLogger(BlsImport::class.java)

    override val csvRowClass = BlsJobCode::class.java

    private lateinit var jobCodeRepository: JobCodeRepository
    private lateinit var jobCodeEsRepo: JobCodeEsRepo
    private lateinit var richSkillRepository: RichSkillRepository
    private lateinit var richSkillEsRepo: RichSkillEsRepo
    private lateinit var appConfig: AppConfig

    constructor(
        jobCodeRepository: JobCodeRepository,
        jobCodeEsRepo: JobCodeEsRepo,
        richSkillRepository: RichSkillRepository,
        richSkillEsRepo: RichSkillEsRepo,
        appConfig: AppConfig
    ) {
        this.jobCodeRepository = jobCodeRepository
        this.jobCodeEsRepo = jobCodeEsRepo
        this.richSkillRepository = richSkillRepository
        this.richSkillEsRepo = richSkillEsRepo
        this.appConfig = appConfig
    }

    override fun handleRows(rows: List<BlsJobCode>) {
        val broad = rows.filter { it.socGroup == "Broad" }
        val minor = rows.filter { it.socGroup == "Minor" }
        val major = rows.filter { it.socGroup == "Major" }

        log.info("Processing ${rows.size} rows...")
        for (row in rows) transaction {
            log.info("Importing ${row.socTitle} - ${row.code}")
            val broadTitle = broad.find { it.code == row.broad() }?.socTitle
            val minorTitle = minor.find { it.code == row.minor() }?.socTitle
            val majorTitle = major.find { it.code == row.major() }?.socTitle

            val existingOnetCodes = row.code?.let{jobCodeRepository.onetsByDetailCode(it).toList()} ?: listOf()
            val jobCode = row.code?.let { jobCodeRepository.findByCodeOrCreate(it, JobCodeRepository.BLS_FRAMEWORK) }

            jobCode?.let {
                it.name = row.socTitle
                it.description = row.socDefinition
                it.detailed = row.socTitle
                broadTitle.let { broad -> it.broad = broad }
                minorTitle.let { minor -> it.minor = minor }
                majorTitle.let { major -> it.major = major }
            }.also {
                jobCode?.let { jobCodeEsRepo.save(it.toModel()) }
                jobCode?.let {
                    richSkillRepository.containingJobCode(it.code)
                        .map { dao -> richSkillEsRepo.save(RichSkillDoc.fromDao(dao, appConfig)) }
                }
            }

            existingOnetCodes.map{
                if (it.code.endsWith("00")){
                    row.socTitle.let {name -> it.name = name}
                    row.socDefinition.let{ description -> it.description = description}
                    it.framework = JobCodeRepository.`O*NET_FRAMEWORK`
                }
                it.detailed = row.socTitle
                broadTitle.let { broad -> it.broad = broad }
                minorTitle.let { minor -> it.minor = minor }
                majorTitle.let { major -> it.major = major }
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

class BlsJobCode : CsvRow, HasCodeHierarchy {
    @CsvBindByName(column = "SOC Group")
    var socGroup: String? = null

    @CsvBindByName(column = "SOC Code")
    override var code: String? = null

    @CsvBindByName(column = "SOC Title")
    var socTitle: String? = null

    @CsvBindByName(column = "SOC Definition")
    var socDefinition: String? = null
}
