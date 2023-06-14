package edu.wgu.osmt.api.model

import edu.wgu.osmt.config.SEMICOLON
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillRepository

class ApiSkillUpdateMapper {

    companion object {
        //TODO: test Coverage
        fun mapApiSkillUpdateV2ToApiSkillUpdate(
            apiSkillUpdateV2: ApiSkillUpdateV2,
            skillUUID: String,
            richSkillRepository: RichSkillRepository
        ) : ApiSkillUpdate {
            return ApiSkillUpdate(
                skillName = apiSkillUpdateV2.skillName,
                skillStatement = apiSkillUpdateV2.skillStatement,
                publishStatus = apiSkillUpdateV2.publishStatus,
                collections = apiSkillUpdateV2.collections,
                authors = getDiff(apiSkillUpdateV2.author?.split(SEMICOLON), skillUUID, richSkillRepository, KeywordTypeEnum.Author),
                categories = getDiff(apiSkillUpdateV2.author?.split(SEMICOLON), skillUUID, richSkillRepository, KeywordTypeEnum.Category)
            )
        }
        
        private fun getDiff(
            split: List<String>?,
            skillUUID: String,
            richSkillRepository: RichSkillRepository,
            type: KeywordTypeEnum
        ): ApiStringListUpdate? {
            var add: List<String>? = null
            var remove: List<String>? = null
            val storedList = richSkillRepository.findByUUID(skillUUID)?.keywords?.filter { it.type==type }?.mapNotNull { it.value }
            
            if(split!=null && storedList!=null){
                if(split.filterNot { storedList.contains(it) }.isNotEmpty()) {
                    add = split.filterNot { storedList.contains(it) }
                }
                if(storedList.filterNot { split.contains(it) }.isNotEmpty()) {
                    remove = storedList.filterNot { split.contains(it) }
                }
            }
            
            return ApiStringListUpdate(add, remove)
        }
    }
}