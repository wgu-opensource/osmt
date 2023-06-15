package edu.wgu.osmt.api.model

import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillRepository

class ApiSkillUpdateMapper {
    
    companion object {
        fun mapApiSkillUpdateV2ToApiSkillUpdate(
                apiSkillUpdateV2: ApiSkillUpdateV2,
                skillUUID: String,
                richSkillRepository: RichSkillRepository
        ): ApiSkillUpdate {
            return ApiSkillUpdate(
                    skillName = apiSkillUpdateV2.skillName,
                    skillStatement = apiSkillUpdateV2.skillStatement,
                    publishStatus = apiSkillUpdateV2.publishStatus,
                    collections = apiSkillUpdateV2.collections,
                    authors = getDiff(apiSkillUpdateV2.author, skillUUID, richSkillRepository, KeywordTypeEnum.Author),
                    categories = getDiff(apiSkillUpdateV2.category, skillUUID, richSkillRepository, KeywordTypeEnum.Category)
            )
        }

        private fun getDiff(
            newFieldValue: String?,
            skillUUID: String,
            richSkillRepository: RichSkillRepository,
            type: KeywordTypeEnum
        ): ApiStringListUpdate {
            val storedFieldValues = richSkillRepository.findByUUID(skillUUID)?.keywords?.filter { it.type == type }?.mapNotNull { it.value }
            
            return if (newFieldValue != null && storedFieldValues != null) {
                if(storedFieldValues.contains(newFieldValue)) {
                    val sum = storedFieldValues + listOf(newFieldValue)
                    val toBeRemoved = sum.groupBy { it }
                        .filter { it.value.size == 1 }
                        .flatMap { it.value }
                    ApiStringListUpdate(listOf(newFieldValue), toBeRemoved)
                }else {
                    ApiStringListUpdate(listOf(newFieldValue), storedFieldValues)
                }
            } else {
                ApiStringListUpdate(listOf(), listOf())
            }
        }
    }

}