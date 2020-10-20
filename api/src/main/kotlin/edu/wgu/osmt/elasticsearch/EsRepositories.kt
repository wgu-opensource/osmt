package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.elasticsearch.SearchService.Companion.DEFAULT_PAGESIZE
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface EsRichSkillRepository : ElasticsearchRepository<RichSkillDoc, Int> {
    fun findByUuid(
        uuid: String,
        pageable: Pageable = PageRequest.of(0, DEFAULT_PAGESIZE, Sort.by("name.keyword").descending())
    ): Page<RichSkillDoc>
}

interface EsJobCodeRepository : ElasticsearchRepository<JobCode, Int>

interface EsKeywordRepository : ElasticsearchRepository<Keyword, Int> {
    fun findByValueAndType(value: String, type: KeywordTypeEnum): Keyword
}

interface EsCollectionRepository : ElasticsearchRepository<CollectionDoc, Int> {
    fun findByUuid(uuid: String, pageable: Pageable): Page<CollectionDoc>

    fun findAllByUuidIn(
        uuids: List<String>,
        pageable: Pageable
    ): Page<CollectionDoc>

    fun findByName(q: String, pageable: Pageable = PageRequest.of(0, DEFAULT_PAGESIZE)): Page<CollectionDoc>
}

