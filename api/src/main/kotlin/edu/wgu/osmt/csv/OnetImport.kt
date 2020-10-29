package edu.wgu.osmt.csv

import com.opencsv.bean.CsvBindByName
import edu.wgu.osmt.jobcode.JobCodeRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

class OnetJobCode : CsvRow, HasCodeHierarchy {
    @CsvBindByName(column = "O*NET-SOC Code")
    override var code: String? = null

    @CsvBindByName(column = "Title")
    var title: String? = null

    @CsvBindByName(column = "Description")
    var description: String? = null
}

@Component
class OnetImport : CsvImport<OnetJobCode> {
    override val log: Logger = LoggerFactory.getLogger(OnetImport::class.java)

    override val csvRowClass = OnetJobCode::class.java

    @Autowired
    private lateinit var jobCodeRepository: JobCodeRepository

    override fun handleRows(rows: List<OnetJobCode>) {
        log.info("Processing ${rows.size} rows...")
        for (row in rows) transaction {
            val jobCode = row.code?.let { jobCodeRepository.findByCodeOrCreate(it, JobCodeRepository.`O*NET_FRAMEWORK`) }
            jobCode?.let{
                it.name = row.title
                it.description = row.description
                it.detailed = row.detailed()
                it.framework = JobCodeRepository.`O*NET_FRAMEWORK`
            }
        }
    }
}
