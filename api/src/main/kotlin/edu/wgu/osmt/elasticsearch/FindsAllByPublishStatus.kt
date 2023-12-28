package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.matchAll
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OsmtQueryHelper.createMatchPhrasePrefixDslQuery
import edu.wgu.osmt.elasticsearch.OsmtQueryHelper.createNativeQuery
import edu.wgu.osmt.elasticsearch.OsmtQueryHelper.createSimpleQueryDslQuery
import edu.wgu.osmt.elasticsearch.OsmtQueryHelper.createTermsDslQuery
import edu.wgu.osmt.richskill.RichSkillDoc
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.SearchHits
import java.util.stream.Collectors

interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nativeQuery = createMatchAllRichSkillNativeQuery(pageable, publishStatus)
        return elasticSearchTemplate.search(nativeQuery, javaClass)
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
        val nativeQuery = createMatchAllRichSkillNativeQuery(pageable, publishStatus)
        return elasticSearchTemplate.count(nativeQuery, javaClass)
    }

    fun createMatchAllRichSkillNativeQuery(pageable: Pageable, publishStatus: Set<PublishStatus>): NativeQuery {
        val MATCH_ALL = matchAll().build()._toQuery()
        var filterValues = publishStatus
                            .stream()
                            .map { ps -> ps.name}
                            .collect(Collectors.toList())
        var filter = createTermsDslQuery( RichSkillDoc::publishStatus.name, filterValues, false)

        return createNativeQuery(pageable, filter, MATCH_ALL)
    }

    fun getCollectionUuidsFromComplexName(pageable: Pageable, dslFilter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, collectionName: String, msgPrefix: String, log: Logger) : List<String> {
        var dslQuery = createSimpleQueryDslQuery("${CollectionDoc::name.name}.raw", collectionName)
        var nativeQuery = createNativeQuery(pageable, dslFilter, dslQuery, msgPrefix, log)

        return elasticSearchTemplate
                            .search( nativeQuery, CollectionDoc::class.java )
                            .searchHits
                            .map { it.content.uuid }
    }

    fun getCollectionUuidsFromName(pageable: Pageable, dslFilter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, collectionName: String, msgPrefix: String, log: Logger) : List<String> {
        var dslQuery = createMatchPhrasePrefixDslQuery(CollectionDoc::name.name, collectionName)
        var nativeQuery = createNativeQuery(pageable, dslFilter, dslQuery, msgPrefix, log)

        return elasticSearchTemplate
                            .search( nativeQuery, CollectionDoc::class.java )
                            .searchHits
                            .map { it.content.uuid }
    }

    fun getCollectionFromUuids(pageable: Pageable, dslFilter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, uuids: List<String>, msgPrefix: String, log: Logger ): SearchHits<CollectionDoc> {
        var dslQuery = createTermsDslQuery("_id", uuids)
        var nativeQuery = createNativeQuery(pageable, dslFilter, dslQuery, msgPrefix, log)

        return elasticSearchTemplate.search(nativeQuery, CollectionDoc::class.java)
    }
}
