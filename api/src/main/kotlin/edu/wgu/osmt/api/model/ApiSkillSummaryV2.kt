package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.config.SEMICOLON
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import java.time.LocalDateTime

class ApiSkillSummaryV2(
        @JsonProperty override val id: String,
        @JsonProperty override val uuid: String,
        @JsonProperty override val status: PublishStatus,
        @JsonProperty override val publishDate: LocalDateTime? = null,
        @JsonProperty override val archiveDate: LocalDateTime? = null,
        @JsonProperty override val skillName: String,
        @JsonProperty override val skillStatement: String,
        @JsonIgnore override val categories: List<String> = listOf(),
        @JsonProperty val category: String? = null,
        @JsonProperty override val keywords: List<String> = listOf(),
        @JsonProperty override val occupations: List<ApiJobCode> = listOf()
): ApiSkillSummary(id, uuid, status, publishDate, archiveDate, skillName, skillStatement, categories) {

    companion object {

        fun fromSkill(rsd: RichSkillDescriptor, appConfig: AppConfig): ApiSkillSummaryV2 {
            return ApiSkillSummaryV2(
                    id = rsd.canonicalUrl(appConfig.baseUrl),
                    uuid = rsd.uuid,
                    status = rsd.publishStatus(),
                    publishDate = rsd.publishDate,
                    archiveDate = rsd.archiveDate,
                    skillName = rsd.name,
                    skillStatement = rsd.statement,
                    categories = rsd.categories.mapNotNull { it.value },
                    category = rsd.categories.mapNotNull { it.value }.sorted().joinToString(SEMICOLON),
                    keywords = rsd.keywords.mapNotNull { it.value },
                    occupations = rsd.jobCodes.map { ApiJobCode.fromJobCode(it) }
            )
        }

        fun fromLatest(apiSkillSummary: ApiSkillSummary): ApiSkillSummaryV2{
            return ApiSkillSummaryV2(
                    id = apiSkillSummary.id,
                    uuid = apiSkillSummary.uuid,
                    status = apiSkillSummary.status,
                    publishDate = apiSkillSummary.publishDate,
                    archiveDate = apiSkillSummary.archiveDate,
                    skillName = apiSkillSummary.skillName,
                    skillStatement = apiSkillSummary.skillStatement,
                    categories = apiSkillSummary.categories,
                    category = apiSkillSummary.categories.sorted().joinToString(SEMICOLON),
                    keywords = apiSkillSummary.keywords,
                    occupations = apiSkillSummary.occupations
            )

        }
    }
}

