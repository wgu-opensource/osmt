package edu.wgu.osmt.richskill

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.csv.CsvColumn
import edu.wgu.osmt.csv.CsvResource
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeBreakout

class RichSkillCsvExport(
    private val appConfig: AppConfig
): CsvResource<RichSkillAndCollections>("RichSkillCsvExport") {
    val listDelimeter = "; "

    override fun columnTranslations(): Array<CsvColumn<RichSkillAndCollections>> {
        return arrayOf(
            return arrayOf(
                CsvColumn("Canonical URL") { it.rs.canonicalUrl(appConfig.baseUrl) },
                CsvColumn("RSD Name") { it.rs.name },
                CsvColumn("Author") { it.rs.author?.value ?: "" },
                CsvColumn("Skill Statement") { it.rs.statement },
                CsvColumn("Category") { it.rs.category?.value ?: "" },
                CsvColumn("Keywords") { it.rs.searchingKeywords.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Standards") { it.rs.standards.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Certifications") { it.rs.certifications.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Occupation Major Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::majorCode) },
                CsvColumn("Occupation Minor Groups") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::minorCode) },
                CsvColumn("Broad Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::broadCode) },
                CsvColumn("Detailed Occupations") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::detailedCode) },
                CsvColumn("O*Net Job Codes") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::jobRoleCode) },
                CsvColumn("Employers") { it.rs.employers.map {keyword -> keyword.value ?: ""}.joinToString(listDelimeter) },
                CsvColumn("Alignment Name") { it.rs.alignments.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Alignment URL") { it.rs.alignments.map { keyword -> keyword.uri ?: "" }.joinToString(listDelimeter) }
            )
        )
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
            .joinToString(listDelimeter)

    }
