package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.db.PublishStatus
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.StringQuery

/** TODO: Upgrade to ElasticSearch 8.x apis
import org.springframework.data.elasticsearch.client.elc.*
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import java.util.stream.Collectors
 */

interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nsqb: NativeSearchQuery = buildQuery(pageable, publishStatus).build()
        val query = createStringQuery("FindsAllByPublishStatus.findAllFilteredByPublishStatus()", nsqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.search(query, javaClass)

        /* TODO: Upgrade to ElasticSearch 8.x apis
        val searchQuery: NativeQuery = buildQuery(pageable, publishStatus).build()
        return elasticSearchTemplate.search(query, javaClass)
         */
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
        val nsqb: NativeSearchQuery = buildQuery(pageable, publishStatus).build()
        val query = createStringQuery("FindsAllByPublishStatus.countAllFilteredByPublishStatus()", nsqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.count(query, javaClass)

        /* TODO: Upgrade to ElasticSearch 8.x apis
        val searchQuery: NativeQuery = buildQuery(pageable, publishStatus).build()
        return elasticSearchTemplate.count(query, javaClass)
        */
    }

    fun buildQuery(
        pageable: Pageable,
        publishStatus: Set<PublishStatus>
    ): NativeSearchQueryBuilder {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        nsq.withQuery(QueryBuilders.matchAllQuery())
        nsq.withFilter(
            BoolQueryBuilder().should(
                QueryBuilders.termsQuery(
                    "publishStatus",
                    publishStatus.map { ps -> ps.toString() }
                )
            )
        )
        return nsq
    }

    fun createStringQuery (msgPrefix: String, nsq: NativeSearchQuery, log: Logger): StringQuery {
        val queryStr = nsq.query.toString()
        log.info(String.Companion.format("%s:\n%s", msgPrefix, queryStr))
        return StringQuery(queryStr)
    }

    /* TODO: Upgrade to ElasticSearch v8.X
    fun buildQuery(
        pageable: Pageable,
        publishStatus: Set<PublishStatus>
    ): NativeQueryBuilder {

        val MATCH_ALL =  matchAll().build()._toQuery()
        val fieldValues = publishStatus
            .stream()
            .map{ ps -> FieldValue.of(ps.name) }
            .collect(Collectors.toList())
        var tqf: TermsQueryField = TermsQueryField.Builder().value(fieldValues).build()
        var terms = terms().field("publishStatus").terms(tqf).build()._toQuery()
        var filter = bool().should(terms).build()._toQuery()

        return NativeQueryBuilder()
            .withPageable(pageable)
            .withQuery(MATCH_ALL)
            .withFilter(filter)
    }
     */
}

