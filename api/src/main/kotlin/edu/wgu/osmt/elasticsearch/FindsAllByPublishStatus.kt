package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
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

