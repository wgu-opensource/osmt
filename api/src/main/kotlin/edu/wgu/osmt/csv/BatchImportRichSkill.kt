package edu.wgu.osmt.csv

import com.opencsv.bean.CsvBindByName
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionUpdateObject
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.jobcode.JobCodeBreakout
import edu.wgu.osmt.jobcode.JobCodeDao
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


class RichSkillRow: CsvRow {
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
class BatchImportRichSkill: CsvImport<RichSkillRow> {

    companion object {
        const val user = "Batch Import"
    }

    override val csvRowClass: Class<RichSkillRow> = RichSkillRow::class.java

    override val log: Logger = LoggerFactory.getLogger(BatchImportRichSkill::class.java)

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    private lateinit var keywordRepository: KeywordRepository

    @Autowired
    private lateinit var jobCodeRepository: JobCodeRepository

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    fun split_field(value: String?, delimiters: String = ";"): List<String>? {
        val strings = value?.let { it.split(delimiters).map { it.trim() } }?.distinct()
        return strings?.map{if (it.isBlank()) null else it}?.filterNotNull()
    }

    fun parse_keywords(keywordType: KeywordTypeEnum, rowValue: String?, useUri: Boolean = false): List<KeywordDao>? {
        return split_field(rowValue)?.map {
            if (useUri)
                keywordRepository.findOrCreate(keywordType, uri = it)
            else
                keywordRepository.findOrCreate(keywordType, value = it)
        }?.filterNotNull()
    }

    fun parse_jobcodes(rowValue: String?, parserFunction:
        (codeParam: String) -> String?): List<JobCodeDao>? {
        return split_field(rowValue)?.map {
                code -> jobCodeRepository.findByCodeOrCreate(parserFunction(code)!!)
        }
    }

    fun parse_collections(rowValue: String?): List<CollectionDao>? {
        return split_field(rowValue)?.filter { it.isNotBlank() }?.mapNotNull { collectionName ->
            val collection = collectionRepository.findByName(collectionName)
            collection ?:  collectionRepository.create(CollectionUpdateObject(name = collectionName, author = NullableFieldUpdate(keywordRepository.getDefaultAuthor())), user)
        }
    }

    fun <T> concatenate(vararg lists: List<T>?): List<T>? {
        val flat = lists.filterNotNull().flatten()
        return when {
            flat.isNotEmpty() -> flat
            else -> null
        }
    }

    override fun handleRows(rows: List<RichSkillRow>) {
        log.info("Processing ${rows.size} rows...")

        for (row in rows) transaction {
            var category: KeywordDao? = null
            var keywords: List<KeywordDao>? = null
            var standards: List<KeywordDao>? = null
            var certifications: List<KeywordDao>? = null
            var employers: List<KeywordDao>? = null
            var alignments: List<KeywordDao>? = null
            var blsMajor: List<JobCodeDao>? = null
            var blsMinor: List<JobCodeDao>? = null
            var blsBroad: List<JobCodeDao>? = null
            var blsDetailed: List<JobCodeDao>? = null
            var occupations: List<JobCodeDao>? = null
            var collections: List<CollectionDao>? = null

            category = row.skillCategory?.let { keywordRepository.findOrCreate(KeywordTypeEnum.Category, value = it) }

            keywords = parse_keywords(KeywordTypeEnum.Keyword, row.keywords)
            standards = parse_keywords(KeywordTypeEnum.Standard, row.standards)
            certifications = parse_keywords(KeywordTypeEnum.Certification, row.certifications)
            employers = parse_keywords(KeywordTypeEnum.Employer, row.employer)
            blsMajor = parse_jobcodes(row.blsMajors, JobCodeBreakout::majorCode)
            blsMinor = parse_jobcodes(row.blsMinors, JobCodeBreakout::minorCode)
            blsBroad = parse_jobcodes(row.blsBroads, JobCodeBreakout::broadCode)
            blsDetailed = parse_jobcodes(row.blsDetaileds, JobCodeBreakout::detailedCode)
            occupations = parse_jobcodes(row.jobRoles, JobCodeBreakout::jobRoleCode)
            collections = parse_collections(row.collections)

            if (row.alignmentTitle != null || row.alignmentUri != null) {
                alignments = listOf( keywordRepository.findOrCreate(KeywordTypeEnum.Alignment, value = row.alignmentTitle, uri = row.alignmentUri) ).filterNotNull()
            }

            val all_keywords = concatenate(keywords, standards, certifications, employers, alignments)
            val allJobcodes = concatenate(blsMajor,blsMinor,blsBroad,blsDetailed,occupations)

            if (row.skillName != null && row.skillStatement != null) {
                richSkillRepository.create(RsdUpdateObject(
                    name = row.skillName!!,
                    statement = row.skillStatement!!,
                    category = NullableFieldUpdate(category),
                    keywords = all_keywords?.let { ListFieldUpdate(add = it) },
                    collections = collections?.let {ListFieldUpdate(add = it)},
                    jobCodes = allJobcodes?.let { ListFieldUpdate(add = it) },
                    author = NullableFieldUpdate(keywordRepository.getDefaultAuthor())
                ), user)
                log.info("created skill '${row.skillName!!}'")
            }
        }

    }
}
