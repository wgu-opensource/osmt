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
                CsvColumn("Collection") { it.collections.map {it.name}.joinToString(listDelimeter) },
                CsvColumn("Skill Name") { it.rs.name },
                CsvColumn("Skill Category") { it.rs.category?.value ?: "" },
                CsvColumn("Contextualized Skill Statement") { it.rs.statement },
                CsvColumn("Keywords") { it.rs.searchingKeywords.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("BLS Major Group") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::majorCode) },
                CsvColumn("BLS Minor Group") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::minorCode) },
                CsvColumn("BLS Broad Occupation") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::broadCode) },
                CsvColumn("BLS Detailed Occupation") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::detailedCode) },
                CsvColumn("O*NET Job Role") { prepareJobCodePart(it.rs.jobCodes, JobCodeBreakout::jobRoleCode) },
                CsvColumn("Professional Standards") { it.rs.standards.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Certifications") { it.rs.certifications.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Alignment Title") { it.rs.alignments.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Alignment") { it.rs.alignments.map { keyword -> keyword.uri ?: "" }.joinToString(listDelimeter) }
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
