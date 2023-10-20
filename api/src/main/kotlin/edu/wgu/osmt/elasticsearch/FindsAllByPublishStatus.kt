package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.matchAll
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.WguQueryHelper.createMatchPhrasePrefixDslQuery
import edu.wgu.osmt.elasticsearch.WguQueryHelper.createQuery
import edu.wgu.osmt.elasticsearch.WguQueryHelper.createSimpleQueryDslQuery
import edu.wgu.osmt.elasticsearch.WguQueryHelper.createTermsDslQuery
import edu.wgu.osmt.richskill.RichSkillDoc
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import java.util.stream.Collectors

/**
 * This have been partially converted to use the ElasticSearch 8.7.X apis. Need to do full conversion to use
 * the v8.7.x ES Java API client, https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.10/searching.html
 */
interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nqb = createQueryBuilder(pageable, publishStatus)
        val query = createQuery("FindsAllByPublishStatus.findAllFilteredByPublishStatus()", nqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.search(query, javaClass)
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
        val nqb = createQueryBuilder(pageable, publishStatus)
        val query = createQuery("FindsAllByPublishStatus.countAllFilteredByPublishStatus()", nqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.count(query, javaClass)
    }

    fun createQueryBuilder(pageable: Pageable, publishStatus: Set<PublishStatus>): NativeQueryBuilder {
        val MATCH_ALL = matchAll().build()._toQuery()
        var filterValues = publishStatus
                            .stream()
                            .map { ps -> ps.name}
                            .collect(Collectors.toList())
        var filter = createTermsDslQuery( RichSkillDoc::publishStatus.name, filterValues, false)
        return NativeQueryBuilder()
                    .withPageable(pageable)
                    .withQuery(MATCH_ALL)
                    .withFilter(filter)
    }

    fun getCollectionUuidsFromComplexName(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, collectionName: String) : List<String> {
        val query = NativeQuery
            .builder()
            .withFilter(filter)
            .withQuery(createSimpleQueryDslQuery("${CollectionDoc::name.name}.raw", collectionName))
            .withPageable(pageable)
            .build()
        return elasticSearchTemplate
            .search( query, CollectionDoc::class.java )
            .searchHits
            .map { it.content.uuid }
    }

    fun getCollectionUuidsFromName(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, collectionName: String) : List<String> {
        val query = NativeQuery
                            .builder()
                            .withFilter(filter)
                            .withQuery(createMatchPhrasePrefixDslQuery(CollectionDoc::name.name, collectionName))
                            .withPageable(pageable)
                            .build()
        return elasticSearchTemplate
                            .search( query, CollectionDoc::class.java )
                            .searchHits
                            .map { it.content.uuid }
    }

    fun getCollectionFromUuids(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, uuids: List<String> ): SearchHits<CollectionDoc> {
        val query = NativeQuery
            .builder()
            .withFilter(filter)
            .withQuery(createTermsDslQuery("_id", uuids))
            .withPageable(pageable)
            .build()
        return elasticSearchTemplate.search(query, CollectionDoc::class.java)
    }
}
