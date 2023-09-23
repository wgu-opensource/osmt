package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import edu.wgu.osmt.db.PublishStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import java.util.stream.Collectors


interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
//        val nsqb: NativeSearchQuery = buildQuery(pageable, publishStatus).build()
//        val searchQuery: Query = StringQuery(nsqb.getQuery().toString())
//        return elasticSearchTemplate.search(searchQuery, javaClass)

        val nsq: NativeQueryBuilder = buildQuery(pageable, publishStatus)
        return elasticSearchTemplate.search(nsq.build(), javaClass)
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
//        val nsqb: NativeSearchQuery = buildQuery(pageable, publishStatus).build()
//        val searchQuery: Query = StringQuery(nsqb.getQuery().toString())
//        return elasticSearchTemplate.count(searchQuery, javaClass)

        val nsq: NativeQueryBuilder = buildQuery(pageable, publishStatus)
        return elasticSearchTemplate.count(nsq.build(), javaClass)
    }

    /*
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
*/
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
}

