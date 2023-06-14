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
        ): ApiSkillUpdate {
            return ApiSkillUpdate(
                    skillName = apiSkillUpdateV2.skillName,
                    skillStatement = apiSkillUpdateV2.skillStatement,
                    publishStatus = apiSkillUpdateV2.publishStatus,
                    collections = apiSkillUpdateV2.collections,
                    authors = getDiff(apiSkillUpdateV2.author?.split(SEMICOLON), skillUUID, richSkillRepository, KeywordTypeEnum.Author),
                    categories = getDiff(apiSkillUpdateV2.category?.split(SEMICOLON), skillUUID, richSkillRepository, KeywordTypeEnum.Category)
            )
        }

        private fun getDiff(
                split: List<String>?,
                skillUUID: String,
                richSkillRepository: RichSkillRepository,
                type: KeywordTypeEnum
        ): ApiStringListUpdate {
            val storedList = richSkillRepository.findByUUID(skillUUID)?.keywords?.filter { it.type == type }?.mapNotNull { it.value }

            return if (split != null && storedList != null) {
                getUncommon(split, storedList)
            } else {
                ApiStringListUpdate(listOf(), listOf())
            }
        }

        private fun getUncommon(split: List<String>, stored: List<String>): ApiStringListUpdate {
            val sum = split + stored
            var add: List<String> = listOf()
            var remove: List<String> = listOf()
            if((sum.size != split.size + stored.size) && (split.size >= stored.size)){
                add = sum.groupBy { it }
                        .filter { it.value.size == 1 }
                        .flatMap { it.value }
            }else if(split.size >= stored.size){
                add = split
            } else if(sum.size != split.size + stored.size){
                remove = sum.groupBy { it }
                    .filter { it.value.size == 1 }
                    .flatMap { it.value }
            }

            return ApiStringListUpdate(add, remove)
        }
    }

}