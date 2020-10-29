package edu.wgu.osmt.csv

import com.opencsv.bean.CsvBindByName
import edu.wgu.osmt.jobcode.JobCodeRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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

@Component
class BlsImport : CsvImport<BlsJobCode> {
    override val log: Logger = LoggerFactory.getLogger(BlsImport::class.java)

    override val csvRowClass = BlsJobCode::class.java

    @Autowired
    private lateinit var jobCodeRepository: JobCodeRepository

    override fun handleRows(rows: List<BlsJobCode>) {
        val detailed = rows.filter { it.socGroup == "Detailed" }
        val broad = rows.filter { it.socGroup == "Broad" }
        val minor = rows.filter { it.socGroup == "Minor" }
        val major = rows.filter { it.socGroup == "Major" }

        log.info("Processing ${detailed.size} detailed rows...")

        for (row in detailed) transaction {
            log.info("handling ${row.socTitle} - ${row.code}")
            val broadTitle = broad.find { it.code == row.broad() }?.socTitle
            val minorTitle = minor.find { it.code == row.minor() }?.socTitle
            val majorTitle = major.find { it.code == row.major() }?.socTitle

            val jobCode = row.code?.let { jobCodeRepository.findByCodeOrCreate(it, JobCodeRepository.BLS_FRAMEWORK) }
            jobCode?.let {
                log.info("jobcode - ${it.code} - ${it.name}")
                it.name = row.socTitle
                it.description = row.socDefinition
                broadTitle.let { broad -> it.broad = broad }
                minorTitle.let { minor -> it.minor = minor }
                majorTitle.let { major -> it.major = major }
            }
        }
    }
}
