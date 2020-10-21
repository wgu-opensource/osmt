package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.SearchService.Companion.DEFAULT_PAGESIZE
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchRestTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        nsq.withQuery(QueryBuilders.matchAllQuery())
        nsq.withFilter(
            BoolQueryBuilder().should(
                QueryBuilders.termsQuery(
                    "publishStatus",
                    publishStatus
                )
            )
        )
        return elasticSearchTemplate.search(nsq.build(), javaClass)
    }
}

interface CustomRichSkillQueries : FindsAllByPublishStatus<RichSkillDoc>

class CustomRichSkillQueriesImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomRichSkillQueries {
    override val javaClass = RichSkillDoc::class.java
}

interface CustomCollectionQueries: FindsAllByPublishStatus<CollectionDoc>

class CustomCollectionQueriesImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomCollectionQueries {

    override val javaClass = CollectionDoc::class.java
}

interface EsRichSkillRepository : ElasticsearchRepository<RichSkillDoc, Int>, CustomRichSkillQueries {
    fun findByUuid(
        uuid: String,
        pageable: Pageable = PageRequest.of(0, DEFAULT_PAGESIZE, Sort.by("name.keyword").descending())
    ): Page<RichSkillDoc>
}

interface EsJobCodeRepository : ElasticsearchRepository<JobCode, Int>

interface EsKeywordRepository : ElasticsearchRepository<Keyword, Int> {
    fun findByValueAndType(value: String, type: KeywordTypeEnum): Keyword
}

interface EsCollectionRepository : ElasticsearchRepository<CollectionDoc, Int>, CustomCollectionQueries {
    fun findByUuid(uuid: String, pageable: Pageable): Page<CollectionDoc>

    fun findAllByUuidIn(
        uuids: List<String>,
        pageable: Pageable
    ): Page<CollectionDoc>

    fun findByName(q: String, pageable: Pageable = PageRequest.of(0, DEFAULT_PAGESIZE)): Page<CollectionDoc>
}

