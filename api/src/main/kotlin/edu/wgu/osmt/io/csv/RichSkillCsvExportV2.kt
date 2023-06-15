package edu.wgu.osmt.io.csv

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeBreakout
import edu.wgu.osmt.richskill.RichSkillAndCollections

class RichSkillCsvExportV2(
    private val appConfig: AppConfig
) : CsvResource<RichSkillAndCollections>("RichSkillCsvExport") {
    val listDelimiter = "; "

    override fun columnTranslations(data: List<RichSkillAndCollections>): Array<CsvColumn<RichSkillAndCollections>> {
        val columns = arrayOf(
            CsvColumn<RichSkillAndCollections>("Canonical URL") { it.rs.canonicalUrl(appConfig.baseUrl) },
            CsvColumn("RSD Name") { it.rs.name },
            CsvColumn("Author") { it.rs.authors.joinToString(listDelimiter) { author -> author.value ?: "" } },
            CsvColumn("Skill Statement") { it.rs.statement },
            CsvColumn("Category") { it.rs.categories.joinToString(listDelimiter) { category -> category.value ?: "" } },
            CsvColumn("Keywords") { it.rs.searchingKeywords.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } },
            CsvColumn("Standards") { it.rs.standards.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } },
            CsvColumn("Certifications") { it.rs.certifications.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } },
            CsvColumn("Occupation Major Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::majorCode) },
            CsvColumn("Occupation Minor Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::minorCode) },
            CsvColumn("Broad Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::broadCode) },
            CsvColumn("Detailed Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::detailedCode) },
            CsvColumn("O*Net Job Codes") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::jobRoleCode) },
            CsvColumn("Employers") { it.rs.employers.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } }
        )
        val alignmentCount = data.map { s -> s.rs.alignments.size }.maxOrNull() ?: 0
        val alignmentColumns = (0 until alignmentCount).flatMap { i ->
            val label = if (i > 0) " ${i + 1}" else ""
            listOf(
                CsvColumn<RichSkillAndCollections>("Alignment${label} Name") { it.rs.alignments.getOrNull(i)?.value ?: "" },
                CsvColumn<RichSkillAndCollections>("Alignment${label} URL") { it.rs.alignments.getOrNull(i)?.uri ?: "" },
                CsvColumn<RichSkillAndCollections>("Alignment${label} Framework") { it.rs.alignments.getOrNull(i)?.framework ?: "" }
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
