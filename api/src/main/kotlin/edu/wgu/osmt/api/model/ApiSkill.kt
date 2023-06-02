package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao

@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiSkill(
        private val rsd: RichSkillDescriptor,
        private val cs: Set<Collection>,
        private val appConfig: AppConfig
): AbstractApiSkill(rsd, cs, appConfig) {

    @get:JsonProperty
    val authors: List<String>
        get() = rsd.authors.mapNotNull { it.value }


    @get:JsonProperty
    val categories: List<String>
        get() = rsd.categories.mapNotNull { it.value }

    companion object {
        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkill{
            return ApiSkill(rsdDao.toModel(), rsdDao.collections.map{ it.toModel() }.filter { !it.isWorkspace() }.toSet(), appConfig)
        }
    }
}



