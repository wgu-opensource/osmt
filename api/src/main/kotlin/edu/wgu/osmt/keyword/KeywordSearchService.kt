package edu.wgu.osmt.keyword

import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service

@Service
class KeywordSearchService @Autowired constructor(
    val elasticsearchRestTemplate: ElasticsearchRestTemplate
) {

    fun keywordTypeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword> {
        val limitedPageable = OffsetPageable(0, 10, null)
        val bq = QueryBuilders.boolQuery()

        val nsq: NativeSearchQueryBuilder =
            NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(bq).withSort(SortBuilders.scoreSort())

        bq
            .must(QueryBuilders.termQuery(Keyword::type.name, type.name))
            .should(
                QueryBuilders.matchBoolPrefixQuery(
                    Keyword::value.name,
                    query
                )
            )
            .should(
                QueryBuilders.matchPhraseQuery(
                    Keyword::value.name,
                    query
                ).boost(5f)
            ).minimumShouldMatch(1)

        return elasticsearchRestTemplate.search(nsq.build(), Keyword::class.java)
    }
}
