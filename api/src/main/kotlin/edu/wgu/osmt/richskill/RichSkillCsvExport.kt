package edu.wgu.osmt.richskill

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.csv.CsvColumn
import edu.wgu.osmt.csv.CsvResource
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeBreakout

class RichSkillCsvExport(
    private val appConfig: AppConfig
): CsvResource<RichSkillAndCollections>("RichSkillCsvExport") {
    val listDelimiter = "; "

    override fun columnTranslations(data: List<RichSkillAndCollections>): Array<CsvColumn<RichSkillAndCollections>> {
        val columns = arrayOf(
            CsvColumn<RichSkillAndCollections>("Canonical URL") { it.rs.canonicalUrl(appConfig.baseUrl) },
            CsvColumn("RSD Name") { it.rs.name },
            CsvColumn("Author") { it.rs.author?.value ?: "" },
            CsvColumn("Skill Statement") { it.rs.statement },
            CsvColumn("Category") { it.rs.category?.value ?: "" },
            CsvColumn("Keywords") { it.rs.searchingKeywords.map { keyword -> keyword.value ?: "" }.joinToString(listDelimiter) },
            CsvColumn("Standards") { it.rs.standards.map { keyword -> keyword.value ?: "" }.joinToString(listDelimiter) },
            CsvColumn("Certifications") { it.rs.certifications.map { keyword -> keyword.value ?: "" }.joinToString(listDelimiter) },
            CsvColumn("Occupation Major Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::majorCode) },
            CsvColumn("Occupation Minor Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::minorCode) },
            CsvColumn("Broad Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::broadCode) },
            CsvColumn("Detailed Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::detailedCode) },
            CsvColumn("O*Net Job Codes") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::jobRoleCode) },
            CsvColumn("Employers") { it.rs.employers.map {keyword -> keyword.value ?: ""}.joinToString(listDelimiter) }
        )
        val alignmentCount = data.map { s -> s.rs.alignments.size }.maxOrNull() ?: 0
        val alignmentColumns = (0 until alignmentCount).flatMap { i ->
            val label = if (i > 0) " ${i+1}" else ""
            listOf(
                CsvColumn<RichSkillAndCollections>("Alignment${label} Name") { it.rs.alignments[i].value ?: "" },
                CsvColumn<RichSkillAndCollections>("Alignment${label} URL") { it.rs.alignments[i].uri ?: "" },
                CsvColumn<RichSkillAndCollections>("Alignment${label} Framework") { it.rs.alignments[i].framework ?: "" }
            )
        }
        return columns + alignmentColumns
    }

    private fun prepareJobCodePart(
            codes: List<JobCode>,
            partTransformation: (String) -> String?
    ): String = codes
            .asSequence()
            .map { it.code }
            .map(partTransformation)
            .filterNotNull()
            .distinct()
            .joinToString(listDelimiter)

    }
