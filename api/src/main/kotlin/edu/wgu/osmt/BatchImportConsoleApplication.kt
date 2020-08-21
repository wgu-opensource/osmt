package edu.wgu.osmt

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
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
    var alignment: String? = null
}

@SpringBootApplication
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
class BatchImportConsoleApplication : CommandLineRunner {
    val LOG: Logger = LoggerFactory.getLogger(BatchImportConsoleApplication::class.java)

    @Autowired
    private lateinit var applicationContext: ApplicationContext;

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository;

    @Autowired
    private lateinit var keywordRepository: KeywordRepository;

    val defaultAuthor = "Western Governors University";

    fun parse_keywords(keywordType: KeywordTypeEnum, rowValue: String?, useUri: Boolean = false):List<Keyword>? {
        val splitValues = rowValue?.let { it.split(';').map { it.trim() } }
        return splitValues?.map {
            if (useUri)
                keywordRepository.findOrCreate(keywordType, uri=it)
            else
                keywordRepository.findOrCreate(keywordType, value=it)
        }
    }

    fun <T> concatenate(vararg lists: List<T>?): List<T>? {
        val flat = lists.filterNotNull().flatten()
        return when {
            flat.isNotEmpty() -> flat
            else -> null
        }
    }

    fun handleRows(rows: List<RichSkillRow>) {
        LOG.debug("got ${rows.size} rows.")

        for (row in rows) {
            val user = null
            var category: Keyword? = null
            var keywords: List<Keyword>? = null
            var standards: List<Keyword>? = null
            var certifications: List<Keyword>? = null
            var employers: List<Keyword>? = null
            var occupations: List<Keyword>? = null
            var alignments: List<Keyword>? = null

            category = row.skillCategory?.let { keywordRepository.findOrCreate(KeywordTypeEnum.Category, value = it) }

            keywords = parse_keywords(KeywordTypeEnum.Keyword, row.keywords)
            standards = parse_keywords(KeywordTypeEnum.ProfessionalStandards, row.standards)
            certifications = parse_keywords(KeywordTypeEnum.Certifications, row.certifications)
            employers = parse_keywords(KeywordTypeEnum.Employers, row.employer)
            occupations = parse_keywords(KeywordTypeEnum.Occupation, row.jobRoles)
            alignments = parse_keywords(KeywordTypeEnum.Alignment, row.alignment, useUri = true)

            val all_keywords = concatenate(keywords, standards, certifications, employers, occupations, alignments)

            if (row.skillName != null && row.skillStatement != null) {
                val newSkill = richSkillRepository.create(
                    row.skillName!!,
                    row.skillStatement!!,
                   row.author ?: defaultAuthor,
                    user)
                richSkillRepository.update(RsdUpdateObject(
                   id = newSkill.id.value,
                   category = NullableFieldUpdate(category),
                   keywords = all_keywords?.let { ListFieldUpdate(add=it) }
                ), user)
            }
        }

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
