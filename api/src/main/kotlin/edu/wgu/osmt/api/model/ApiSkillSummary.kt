package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillDoc
import java.time.LocalDateTime

class ApiSkillSummary(
        @JsonProperty override val id: String,
        @JsonProperty override val uuid: String,
        @JsonProperty override val status: PublishStatus,
        @JsonProperty override val publishDate: LocalDateTime? = null,
        @JsonProperty override val archiveDate: LocalDateTime? = null,
        @JsonProperty override val skillName: String,
        @JsonProperty override val skillStatement: String,
        @JsonProperty val categories: List<String> = listOf(),
        @JsonProperty override val keywords: List<String> = listOf(),
        @JsonProperty override val occupations: List<ApiJobCode> = listOf()
): AbstractApiSkillSummary() {

    companion object {
        fun fromSkill(rsd: RichSkillDescriptor, appConfig: AppConfig): ApiSkillSummary {
            return ApiSkillSummary(
                id = rsd.canonicalUrl(appConfig.baseUrl),
                uuid = rsd.uuid,
                status = rsd.publishStatus(),
                publishDate = rsd.publishDate,
                archiveDate = rsd.archiveDate,
                skillName = rsd.name,
                skillStatement = rsd.statement,
                categories = rsd.categories.mapNotNull { it.value },
                keywords = rsd.keywords.mapNotNull { it.value },
                occupations = rsd.jobCodes.map { ApiJobCode.fromJobCode(it) }
            )
        }

        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkillSummary {
            return fromSkill(rsdDao.toModel(), appConfig)
        }

        fun fromDoc(rsDoc: RichSkillDoc): ApiSkillSummary {
            return with(rsDoc) {
                ApiSkillSummary(
                    uri,
                    uuid,
                    publishStatus,
                    publishDate,
                    archiveDate,
                    name,
                    statement,
                    categories,
                    searchingKeywords,
                    jobCodes.map { ApiJobCode.fromJobCode(it) })
            }
        }
    }
}

