package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.util.OsmtUtil.Companion.parseMultiValueToSingleValue
import java.time.LocalDateTime

class ApiSkillSummaryV2(
    @JsonProperty val id: String,
    @JsonProperty val uuid: String,
    @JsonProperty val status: PublishStatus,
    @JsonProperty val publishDate: LocalDateTime? = null,
    @JsonProperty val archiveDate: LocalDateTime? = null,
    @JsonProperty val skillName: String,
    @JsonProperty val skillStatement: String,
    @JsonProperty val category: String? = null,
    @JsonProperty val keywords: List<String> = listOf(),
    @JsonProperty val occupations: List<ApiJobCode> = listOf()
) {

    companion object {
        private fun fromSkill(rsd: RichSkillDescriptor, appConfig: AppConfig): ApiSkillSummaryV2 {
            return ApiSkillSummaryV2(
                id = rsd.canonicalUrl(appConfig.baseUrl),
                uuid = rsd.uuid,
                status = rsd.publishStatus(),
                publishDate = rsd.publishDate,
                archiveDate = rsd.archiveDate,
                skillName = rsd.name,
                skillStatement = rsd.statement,
                category = parseMultiValueToSingleValue(rsd.categories.mapNotNull { it.value }.toString()),
                keywords = rsd.keywords.mapNotNull { it.value },
                occupations = rsd.jobCodes.map { ApiJobCode.fromJobCode(it) }
            )
        }

        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkillSummaryV2 {
            return fromSkill(rsdDao.toModel(), appConfig)
        }

        fun fromDoc(rsDoc: RichSkillDoc): ApiSkillSummaryV2 {
            return with(rsDoc) {
                ApiSkillSummaryV2(
                    uri,
                    uuid,
                    publishStatus,
                    publishDate,
                    archiveDate,
                    name,
                    statement,
                    parseMultiValueToSingleValue(categories.toString()),
                    searchingKeywords,
                    jobCodes.map { ApiJobCode.fromJobCode(it) })
            }
        }
    }
}

