package edu.wgu.osmt.jobcode

import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service

@Service
class JobCodeSearchService  @Autowired constructor(
    val elasticsearchRestTemplate: ElasticsearchRestTemplate
){

    fun jobCodeTypeAheadSearch(query: String): SearchHits<JobCode> {
        val limitedPageable = OffsetPageable(0, 10, null)
        val bq = QueryBuilders.boolQuery()

        val nsq: NativeSearchQueryBuilder =
            NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(bq).withSort(SortBuilders.scoreSort())

        bq.must(
            QueryBuilders.boolQuery()
                .should(
                    QueryBuilders.matchBoolPrefixQuery(
                        JobCode::code.name,
                        query
                    )
                )
                .should(
                    QueryBuilders.matchPhraseQuery(
                        JobCode::code.name,
                        query
                    ).boost(5f)
                )
        ).minimumShouldMatch(1)

        return elasticsearchRestTemplate.search(nsq.build(), JobCode::class.java)
    }
}
