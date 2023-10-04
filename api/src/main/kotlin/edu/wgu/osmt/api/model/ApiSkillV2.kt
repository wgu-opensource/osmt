package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.config.SEMICOLON
import edu.wgu.osmt.db.JobCodeLevel
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao

class ApiSkillV2(
        @JsonIgnore override val rsd: RichSkillDescriptor,
        @JsonIgnore override val cs: Set<Collection>,
        private val appConfig: AppConfig
): ApiSkill(rsd, cs, appConfig) {

    @get:JsonIgnore
    override val authors: List<String>
        get() = rsd.authors.mapNotNull { it.value }

    @get:JsonIgnore
    override val categories: List<String>
        get() = rsd.categories.mapNotNull { it.value }

    @get:JsonProperty
    val author: String
        get() = rsd.authors.mapNotNull { it.value }.sorted().joinToString(SEMICOLON)
    
    @get:JsonProperty
    val category: String
        get() = rsd.categories.mapNotNull { it.value }.sorted().joinToString(SEMICOLON)

    @get:JsonProperty
    override val occupations: List<ApiJobCode>
        get() {
            return rsd.jobCodes.filter { it.code.isNotBlank() }.map { jobCode ->
                val parents = listOfNotNull(
                    jobCode.major.let {jobCode.majorCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.major, level= JobCodeLevel.Major) }},
                    jobCode.minor.let{jobCode.minorCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.minor, level= JobCodeLevel.Minor) }},
                    jobCode.broad?.let {jobCode.broadCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.broad, level= JobCodeLevel.Broad) }},
                    jobCode.detailed?.let {jobCode.detailedCode?.let { ApiJobCode(code=it, targetNodeName=jobCode.detailed, level= JobCodeLevel.Detailed) }}
                ).distinct()

                ApiJobCode.fromJobCodeV2(jobCode, parents=parents)
            }
        }

    companion object {
        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkillV2 {
            return ApiSkillV2(rsdDao.toModel(), rsdDao.collections.map{ it.toModel() }.filter { !it.isWorkspace() }.toSet(), appConfig)
        }

        fun fromLatest(apiSkill: ApiSkill, appConfig: AppConfig): ApiSkillV2 {
            return ApiSkillV2(
                    rsd = apiSkill.rsd,
                    cs = apiSkill.cs,
                    appConfig
            )
        }
    }
}



