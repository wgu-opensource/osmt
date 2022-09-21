package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.db.PublishStatus
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder


interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchRestTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nsq: NativeSearchQueryBuilder = buildQuery(pageable, publishStatus)
        return elasticSearchTemplate.search(nsq.build(), javaClass)
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
        val nsq: NativeSearchQueryBuilder = buildQuery(pageable, publishStatus)
        return elasticSearchTemplate.count(nsq.build(), javaClass)
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

