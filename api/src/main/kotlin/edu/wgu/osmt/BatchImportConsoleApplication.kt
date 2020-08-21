package edu.wgu.osmt

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader


class RichSkillRow {
    @CsvBindByName(column = "Skill Category")
    var skillCategory: String? = null

    @CsvBindByName(column = "Skill Name")
    var skillName: String? = null

    @CsvBindByName(column = "Contextualized Skill Statement")
    var skillStatement: String? = null

    @CsvBindByName(column = "Keywords")
    var keywords: String? = null

    @CsvBindByName(column = "Collection")
    var collections: String? = null

    @CsvBindByName(column = "Professional Standards")
    var standards: String? = null

    @CsvBindByName(column = "Certifications")
    var certifications: String? = null

    @CsvBindByName(column = "BLS Industry Major Group(s)")
    var blsMajors: String? = null

    @CsvBindByName(column = "BLS Industry(s) {Minor Group}")
    var blsMinors: String? = null

    @CsvBindByName(column = "BLS Occupation(s) {Broad Occupation}")
    var blsBroads: String? = null

    @CsvBindByName(column = "BLS Job Function(s) {Detailed Occupation}")
    var blsJobFunctions: String? = null

    @CsvBindByName(column = "O*NET Job Role(s)")
    var jobRoles: String? = null

    @CsvBindByName(column = "Author")
    var author: String? = null

    @CsvBindByName(column = "Employer")
    var employer: String? = null

    @CsvBindByName(column = "Alignment")
    var Alignment: String? = null
}

@SpringBootApplication
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
class BatchImportConsoleApplication : CommandLineRunner {
    val LOG: Logger = LoggerFactory.getLogger(BatchImportConsoleApplication::class.java)

    @Autowired
    private lateinit var applicationContext: ApplicationContext;

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository;

    fun handleRows(rows: List<RichSkillRow>) {
        LOG.info("got some rows: ${rows.size}")
        LOG.info("first statement: ${rows[0].skillStatement}")
    }

    fun processCsv(csv_path: String) {
        LOG.info("hello process csv: ${csv_path}")

        var fileReader: BufferedReader? = null

        try {
            fileReader = BufferedReader(FileReader(csv_path))
            val csvToBean = CsvToBeanBuilder<RichSkillRow>(fileReader)
                .withType(RichSkillRow::class.java)
                .withIgnoreLeadingWhiteSpace(true)
                .build()

            val rows = csvToBean.parse()
            handleRows(rows)
        } catch (e: FileNotFoundException) {
            LOG.error("Could not find file: ${csv_path}")
        } finally {
            fileReader!!.close()
        }

    }

    override fun run(vararg args: String?) {
        args[0]?.let { processCsv(it) }
        (applicationContext as ConfigurableApplicationContext).close()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(BatchImportConsoleApplication::class.java, *args)
}
