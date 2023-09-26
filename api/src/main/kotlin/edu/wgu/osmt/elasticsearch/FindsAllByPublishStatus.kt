package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.db.PublishStatus
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery

import org.springframework.data.elasticsearch.client.elc.*
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import java.util.stream.Collectors

interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nqb = buildQuery(pageable, publishStatus)
        val query = createStringQuery(
            "FindsAllByPublishStatus.findAllFilteredByPublishStatus()",
            nqb,
            LoggerFactory.getLogger(FindsAllByPublishStatus::class.java)
        )
        return elasticSearchTemplate.search(query, javaClass)

        /* TODO: Uncomment to Upgrade to ElasticSearch 8.x apis
        val nqb = createQueryBuilder(pageable, publishStatus)
        val query = createQuery("FindsAllByPublishStatus.countAllFilteredByPublishStatus()", nqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.search(query, javaClass)
         */
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
        val nqb = buildQuery(pageable, publishStatus)
        val query = createStringQuery(
            "FindsAllByPublishStatus.countAllFilteredByPublishStatus()",
            nqb,
            LoggerFactory.getLogger(FindsAllByPublishStatus::class.java)
        )
        return elasticSearchTemplate.count(query, javaClass)

        /* TODO: Uncomment to Upgrade to ElasticSearch 8.x apis
        val nqb = createQueryBuilder(pageable, publishStatus)
        val query = createQuery("FindsAllByPublishStatus.countAllFilteredByPublishStatus()", nqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.count(query, javaClass)
         */
    }

    @Deprecated("Upgrade to ES v8.x queries", ReplaceWith("createQueryBuilder"), DeprecationLevel.WARNING)
    fun buildQuery(
        pageable: Pageable,
        publishStatus: Set<PublishStatus>
    ): NativeSearchQueryBuilder {
        val nsqb = NativeSearchQueryBuilder().withPageable(pageable)
        nsqb.withQuery(QueryBuilders.matchAllQuery())
        nsqb.withFilter(
            BoolQueryBuilder().should(
                QueryBuilders.termsQuery(
                    "publishStatus",
                    publishStatus.map { ps -> ps.toString() }
                )
            )
        )
        return nsqb
    }

    @Deprecated("", ReplaceWith("createQuery"), DeprecationLevel.WARNING)
    fun createStringQuery(msgPrefix: String, nqb: NativeSearchQueryBuilder, log: Logger): Query {
        val queryStr = nqb.build().query.toString()
        log.debug(String.Companion.format("%s:\n%s", msgPrefix, queryStr))
        return StringQuery(queryStr)
    }

    fun createQuery(msgPrefix: String, nqb: NativeQueryBuilder, log: Logger): Query {
        val nq = nqb.build()
        log.debug(String.Companion.format("%s:\n%s", msgPrefix, nq.query.toString()))
        return nq;
    }

    fun createQueryBuilder(pageable: Pageable, publishStatus: Set<PublishStatus>): NativeQueryBuilder {
        val MATCH_ALL = matchAll().build()._toQuery()
        val fieldValues = publishStatus
            .stream()
            .map { ps -> FieldValue.of(ps.name) }
            .collect(Collectors.toList())
        var tqf: TermsQueryField = TermsQueryField.Builder().value(fieldValues).build()
        var terms = terms().field("publishStatus").terms(tqf).build()._toQuery()
        var filter = bool().should(terms).build()._toQuery()

        return NativeQueryBuilder()
            .withPageable(pageable)
            .withQuery(MATCH_ALL)
            .withFilter(filter)
    }
}

