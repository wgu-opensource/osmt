package edu.wgu.osmt.richskill

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.csv.CsvColumn
import edu.wgu.osmt.csv.CsvResource
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeBreakout

class RichSkillCsvExport(
    private val appConfig: AppConfig
): CsvResource<RichSkillDescriptor>("RichSkillCsvExport") {
    val listDelimeter = "; "

    override fun columnTranslations(): Array<CsvColumn<RichSkillDescriptor>> {
        return arrayOf(
            return arrayOf(
                CsvColumn("Canonical URL") { it.canonicalUrl(appConfig.baseUrl) },
                CsvColumn("Collection") { it.collections.map {it.name}.joinToString(listDelimeter) },
                CsvColumn("Skill Name") { it.name },
                CsvColumn("Skill Category") { it.category?.value ?: "" },
                CsvColumn("Contextualized Skill Statement") { it.statement },
                CsvColumn("Keywords") { it.searchingKeywords.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("BLS Major Group") { prepareJobCodePart(it.jobCodes, JobCodeBreakout::majorCode) },
                CsvColumn("BLS Minor Group") { prepareJobCodePart(it.jobCodes, JobCodeBreakout::minorCode) },
                CsvColumn("BLS Broad Occupation") { prepareJobCodePart(it.jobCodes, JobCodeBreakout::broadCode) },
                CsvColumn("BLS Detailed Occupation") { prepareJobCodePart(it.jobCodes, JobCodeBreakout::detailedCode) },
                CsvColumn("O*NET Job Role") { prepareJobCodePart(it.jobCodes, JobCodeBreakout::jobRoleCode) },
                CsvColumn("Professional Standards") { it.standards.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Certifications") { it.certifications.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Alignment Title") { it.alignments.map { keyword -> keyword.value ?: "" }.joinToString(listDelimeter) },
                CsvColumn("Alignment") { it.alignments.map { keyword -> keyword.uri ?: "" }.joinToString(listDelimeter) }
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
