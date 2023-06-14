package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.config.SEMICOLON
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



