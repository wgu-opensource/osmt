package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDoc
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.multiMatchQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

interface EsRichSkillRepository : ElasticsearchRepository<RichSkillDoc, Int> {
    @Query("{\"match\": {\"uuid\": \"?0\"}}")
    fun findByUUID(uuid: String, pageable: Pageable): Page<RichSkillDoc>

    //TODO add query
    @Query()
    fun findQueryByMultipleFields(q: String, pageable: Pageable = PageRequest.of(0, 50)): Page<RichSkillDoc>
}

interface EsKeywordRespository : ElasticsearchRepository<Keyword, Int> {
    @Query("{\"match\": {\"db_id\": \"?0\"}}")
    fun findByUUID(id: Long, pageable: Pageable): Page<Keyword>
}

