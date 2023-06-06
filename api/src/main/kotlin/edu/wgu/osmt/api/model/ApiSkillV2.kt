package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.util.OsmtUtil

@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiSkillV2(
        private val rsd: RichSkillDescriptor,
        val cs: Set<Collection>,
        val appConfig: AppConfig
): ApiSkill(rsd, cs, appConfig) {

    @get:JsonIgnore
    override val authors: List<String>
        get() = rsd.authors.mapNotNull { it.value }

    @get:JsonIgnore
    override val categories: List<String>
        get() = rsd.categories.mapNotNull { it.value }

    @get:JsonProperty
    val author: String
        get() = OsmtUtil.parseMultiValueStringFieldToSingleStringField(rsd.authors.map { "${it.value}," }.toString())


    @get:JsonProperty
    val category: String
        get() = OsmtUtil.parseMultiValueStringFieldToSingleStringField(rsd.categories.map { "${it.value}," }.toString())

    companion object {
        fun fromDao(rsdDao: RichSkillDescriptorDao, appConfig: AppConfig): ApiSkillV2{
            return ApiSkillV2(rsdDao.toModel(), rsdDao.collections.map{ it.toModel() }.filter { !it.isWorkspace() }.toSet(), appConfig)
        }
    }
}



