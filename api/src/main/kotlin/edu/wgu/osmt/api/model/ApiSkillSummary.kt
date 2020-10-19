package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao

class ApiSkillSummary(
    @JsonProperty val id: String,
    @JsonProperty val uuid: String,
    @JsonProperty val status: PublishStatus,
    @JsonProperty val skillName: String,
    @JsonProperty val skillStatement: String,
    @JsonProperty val category: String? = null,
    @JsonProperty val keywords: List<String> = listOf(),
    @JsonProperty val occupations: List<ApiJobCode> = listOf()
) {

    companion object {
        fun fromSkill(rsd: RichSkillDescriptor, appConfig: AppConfig): ApiSkillSummary {
            return ApiSkillSummary(
                id=rsd.canonicalUrl(appConfig.baseUrl),
                uuid=rsd.uuid,
                status=rsd.publishStatus(),
                skillName=rsd.name,
                skillStatement=rsd.statement,
                category=rsd.category?.value,
                keywords=rsd.keywords.mapNotNull { it.value },
                occupations=rsd.jobCodes.map { ApiJobCode.fromJobCode(it) }
            )
        }
        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkillSummary {
            return fromSkill(rsdDao.toModel(), appConfig)
        }
    }
}

