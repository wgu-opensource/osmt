package edu.wgu.osmt.io.xlsx

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeBreakout
import edu.wgu.osmt.richskill.RichSkillAndCollections

class RichSkillXlsxExport(
    private val appConfig: AppConfig
) : XlsxResource<RichSkillAndCollections>("RichSkillXlsxExport") {
    private val listDelimiter = "; "

    override fun columnTranslations(data: List<RichSkillAndCollections>): Array<XlsxColumn<RichSkillAndCollections>> {
        val columns = arrayOf(
            XlsxColumn<RichSkillAndCollections>("Canonical URL") { it.rs.canonicalUrl(appConfig.baseUrl) },
            XlsxColumn("RSD Name") { it.rs.name },
            XlsxColumn("Authors") { it.rs.authors.joinToString(listDelimiter) { author -> author.value ?: "" } },
            XlsxColumn("Skill Statement") { it.rs.statement },
            XlsxColumn("Categories") { it.rs.categories.joinToString(listDelimiter) { category -> category.value ?: "" } },
            XlsxColumn("Keywords") { it.rs.searchingKeywords.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } },
            XlsxColumn("Standards") { it.rs.standards.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } },
            XlsxColumn("Certifications") { it.rs.certifications.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } },
            XlsxColumn("Occupation Major Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::majorCode) },
            XlsxColumn("Occupation Minor Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::minorCode) },
            XlsxColumn("Broad Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::broadCode) },
            XlsxColumn("Detailed Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::detailedCode) },
            XlsxColumn("O*Net Job Codes") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::jobRoleCode) },
            XlsxColumn("Employers") { it.rs.employers.joinToString(listDelimiter) { keyword -> keyword.value ?: "" } }
        )
        val alignmentCount = data.map { s -> s.rs.alignments.size }.maxOrNull() ?: 0
        val alignmentColumns = (0 until alignmentCount).flatMap { i ->
            val label = if (i > 0) " ${i + 1}" else ""
            listOf(
                XlsxColumn<RichSkillAndCollections>("Alignment${label} Name") { it.rs.alignments.getOrNull(i)?.value ?: "" },
                XlsxColumn<RichSkillAndCollections>("Alignment${label} URL") { it.rs.alignments.getOrNull(i)?.uri ?: "" },
                XlsxColumn<RichSkillAndCollections>("Alignment${label} Framework") { it.rs.alignments.getOrNull(i)?.framework ?: "" }
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
