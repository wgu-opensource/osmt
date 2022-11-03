package edu.wgu.osmt.richskill

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.csv.CsvColumn
import edu.wgu.osmt.csv.CsvResource
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeBreakout

class RichSkillDocCsvExport(
    private val appConfig: AppConfig
) : CsvResource<RichSkillDoc>("RichSkillCsvExport") {
    val listDelimiter = "; "

    override fun columnTranslations(data: List<RichSkillDoc>): Array<CsvColumn<RichSkillDoc>> {
        val columns = arrayOf(
            CsvColumn<RichSkillDoc>("RSD Name") { it.name },
            CsvColumn("Skill Statement") { it.statement },
            CsvColumn("Category") { it.category.toString() },
            CsvColumn("Publish Status") { it.publishStatus.toString() },
            CsvColumn("Keywords") { it.searchingKeywords.joinToString(listDelimiter) },
            CsvColumn("Job Codes") { it.jobCodes.map{jobCode ->  jobCode.toString()}.joinToString { listDelimiter }},
            CsvColumn("Standards") { it.standards.joinToString(listDelimiter) },
            CsvColumn("Certifications") { it.certifications.joinToString(listDelimiter) },
            CsvColumn("Employers") { it.employers.joinToString(listDelimiter) },
            CsvColumn("Alignments") { it.alignments.joinToString(listDelimiter) },
            CsvColumn("Publish Date") { it.publishDate.toString()},
            CsvColumn("Archive Date") { it.archiveDate.toString()},
        )

        return columns
    }

}
