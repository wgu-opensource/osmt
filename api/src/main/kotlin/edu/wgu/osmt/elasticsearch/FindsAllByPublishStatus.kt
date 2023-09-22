package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.db.PublishStatus
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery


interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nsqb: NativeSearchQuery = buildQuery(pageable, publishStatus).build()
        val searchQuery: Query = StringQuery(nsqb.getQuery().toString())
        return elasticSearchTemplate.search(searchQuery, javaClass)
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
        val nsqb: NativeSearchQuery = buildQuery(pageable, publishStatus).build()
        val searchQuery: Query = StringQuery(nsqb.getQuery().toString())
        return elasticSearchTemplate.count(searchQuery, javaClass)
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
}

