package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

interface EsRichSkillRepository : CrudRepository<RichSkillDoc, Int> {
    @Query("{\"match\": {\"uuid\": \"?0\"}}")
    fun findByUUID(uuid: String, pageable: Pageable): Page<RichSkillDoc>
}

interface EsKeywordRespository: CrudRepository<Keyword,  Int> {
    @Query("{\"match\": {\"db_id\": \"?0\"}}")
    fun findByUUID(id: Long, pageable: Pageable): Page<Keyword>

}

interface EsCollectionRepository: CrudRepository<CollectionDoc, Int>

//interface EsJobCodeRepository: CrudRepository<Keyword,  Int> {
//    @Query("{\"match\": {\"uuid\": \"?0\"}}")
//    fun findByUUID(uuid: String, pageable: Pageable): Page<Keyword>
//}
