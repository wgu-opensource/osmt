package edu.wgu.osmt

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.richskill.RichSkillKeywords
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader


class RichSkillRow {
    @CsvBindByName(column = "Collection")
    var collections: String? = null

    @CsvBindByName(column = "Skill Name")
    var skillName: String? = null

    @CsvBindByName(column = "Skill Category")
    var skillCategory: String? = null

    @CsvBindByName(column = "Contextualized Skill Statement")
    var skillStatement: String? = null

    @CsvBindByName(column = "Keywords")
    var keywords: String? = null

    @CsvBindByName(column = "Professional Standards")
    var standards: String? = null

    @CsvBindByName(column = "Certifications")
    var certifications: String? = null

    @CsvBindByName(column = "BLS Major Group")
    var blsMajors: String? = null

    @CsvBindByName(column = "BLS Minor Group")
    var blsMinors: String? = null

    @CsvBindByName(column = "BLS Broad Occupation")
    var blsBroads: String? = null

    @CsvBindByName(column = "BLS Detailed Occupation")
    var blsDetaileds: String? = null

    @CsvBindByName(column = "O*NET Job Role")
    var jobRoles: String? = null

    @CsvBindByName(column = "Author")
    var author: String? = null

    @CsvBindByName(column = "Employer")
    var employer: String? = null

    @CsvBindByName(column = "Alignment Title")
    var alignmentTitle: String? = null

    @CsvBindByName(column = "Alignment")
    var alignmentUri: String? = null
}

@Component
@Profile("import")
class BatchImportConsoleApplication : CommandLineRunner {
    val LOG: Logger = LoggerFactory.getLogger(BatchImportConsoleApplication::class.java)

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    private lateinit var keywordRepository: KeywordRepository

    @Autowired
    private lateinit var jobCodeRepository: JobCodeRepository

    @Autowired
    private lateinit var collectionRepository: CollectionRepository;

    val collectionSkillsTable = CollectionSkills

    fun split_field(value: String?, delimiters: String = ";"): List<String>? {
        return value?.let { it.split(delimiters).map { it.trim() } }?.distinct()
    }

    fun parse_keywords(keywordType: KeywordTypeEnum, rowValue: String?, useUri: Boolean = false): List<Keyword>? {
        return split_field(rowValue)?.map {
            if (useUri)
                keywordRepository.findOrCreate(keywordType, uri = it)
            else
                keywordRepository.findOrCreate(keywordType, value = it)
        }
    }

    fun parse_jobcodes(rowValue: String?): List<JobCode>? {
        return split_field(rowValue)?.map { code ->
            val jobCode = jobCodeRepository.findByCode(code)
            jobCode ?: jobCodeRepository.create(code).toModel()
        }
    }

    fun parse_collections(rowValue: String?): List<Collection>? {
        return split_field(rowValue)?.filter { it.isNotBlank() }?.map { collectionName ->
            val collection = collectionRepository.findByName(collectionName)
            collection ?: transaction { collectionRepository.create(collectionName).toModel() }
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
        LOG.info("Processing ${rows.size} rows...")

        for (row in rows) {
            val user = null
            var category: Keyword? = null
            var keywords: List<Keyword>? = null
            var standards: List<Keyword>? = null
            var certifications: List<Keyword>? = null
            var employers: List<Keyword>? = null
            var alignments: List<Keyword>? = null
            var occupations: List<JobCode>? = null
            var collections: List<Collection>? = null

            category = row.skillCategory?.let { keywordRepository.findOrCreate(KeywordTypeEnum.Category, value = it) }

            keywords = parse_keywords(KeywordTypeEnum.Keyword, row.keywords)
            standards = parse_keywords(KeywordTypeEnum.Standard, row.standards)
            certifications = parse_keywords(KeywordTypeEnum.Certification, row.certifications)
            employers = parse_keywords(KeywordTypeEnum.Employer, row.employer)
            occupations = parse_jobcodes(row.jobRoles)
            collections = parse_collections(row.collections)

            if (row.alignmentTitle != null || row.alignmentUri != null) {
                alignments = listOf( keywordRepository.findOrCreate(KeywordTypeEnum.Alignment, value = row.alignmentTitle, uri = row.alignmentUri) )
            }

            val all_keywords = concatenate(keywords, standards, certifications, employers, alignments)

            val author:Keyword? = row.author?.let { keywordRepository.findOrCreate(KeywordTypeEnum.Author, value = it) }

            if (row.skillName != null && row.skillStatement != null) {
                val newSkill = richSkillRepository.create(
                    name = row.skillName!!,
                    statement = row.skillStatement!!,
                    author = author,
                    user = user
                )

                richSkillRepository.update(RsdUpdateObject(
                    id = newSkill.id.value,
                    category = NullableFieldUpdate(category),
                    keywords = all_keywords?.let { ListFieldUpdate(add = it) },
                    jobCodes = occupations?.let { ListFieldUpdate(add = it) },
                    collections = collections?.let {ListFieldUpdate(add = it)}
                ), user)
                LOG.info("created skill '${row.skillName!!}'")
            }
        }

    }

    fun processCsv(csv_path: String) {
        LOG.info("Starting to process csv: ${csv_path}")

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
            fileReader?.close()
        }

    }

    override fun run(vararg args: String?) {
        // --csv=path/to/csv
        val arguments = args.filterNotNull().flatMap { it.split(",") }

        val csvPath = arguments.find { it.contains("--csv") }?.split("=")?.last()
        if (csvPath != null) {
            processCsv(csvPath)
        } else {
            LOG.error("Missing --csv=path/to/csv argument")
        }
        (applicationContext as ConfigurableApplicationContext).close()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(BatchImportConsoleApplication::class.java, *args)
}
