package edu.wgu.osmt

import edu.wgu.osmt.csv.BatchImportRichSkill
import edu.wgu.osmt.csv.BlsImport
import edu.wgu.osmt.csv.OnetImport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("import")
class ImportCommandRunner : CommandLineRunner {
    val LOG: Logger = LoggerFactory.getLogger(ImportCommandRunner::class.java)

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var batchImportRichSkill: BatchImportRichSkill

    @Autowired
    private lateinit var blsImport: BlsImport

    @Autowired
    private lateinit var onetImport: OnetImport

    override fun run(vararg args: String?) {
        val arguments = args.filterNotNull().flatMap { it.split(",") }

        /**
         * --csv=path/to/csv
         */
        val csvPath = arguments.find { it.contains("--csv") }?.split("=")?.last()

        /**
         * --import-type=
         * must match an entry in [[ImportType]]
         */
        val importType = arguments.find { it.contains("--import-type") }?.split("=")?.last()
            ?.let { ImportType.valueOf(it.toLowerCase().capitalize()) } ?: ImportType.Batchskill

        if (csvPath != null) {
            LOG.info("running import command for ${importType}")
            when (importType) {
                ImportType.Batchskill -> batchImportRichSkill.processCsv(csvPath)
                ImportType.Bls -> blsImport.processCsv(csvPath)
                ImportType.Onet -> onetImport.processCsv(csvPath)
            }

        } else {
            LOG.error("Missing --csv=path/to/csv argument")
        }
        (applicationContext as ConfigurableApplicationContext).close()
    }
}

enum class ImportType {
    Batchskill,
    Onet,
    Bls
}
