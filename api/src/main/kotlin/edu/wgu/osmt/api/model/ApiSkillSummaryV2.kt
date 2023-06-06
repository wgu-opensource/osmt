package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.util.OsmtUtil
import java.time.LocalDateTime

@JsonIgnoreProperties("categories")
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
                category = OsmtUtil.parseMultiValueStringFieldToSingleStringField(rsd.categories.mapNotNull { it.value }.toString()),
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
                    categories,
                    category = OsmtUtil.parseMultiValueStringFieldToSingleStringField(categories.toString()),
                    searchingKeywords,
                    jobCodes.map { ApiJobCode.fromJobCode(it) })
            }
        }
    }
}

