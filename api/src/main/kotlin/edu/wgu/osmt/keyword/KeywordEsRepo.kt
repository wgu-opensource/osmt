package edu.wgu.osmt.keyword

import edu.wgu.osmt.config.INDEX_KEYWORD_DOC
import edu.wgu.osmt.config.SORT_INSENSITIVE
import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomKeywordRepository {
    val elasticSearchTemplate: ElasticsearchTemplate
    fun typeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword>

    fun deleteIndex() {
        elasticSearchTemplate.indexOps(IndexCoordinates.of(INDEX_KEYWORD_DOC)).delete()
    }
}

class CustomKeywordRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchTemplate) :
    CustomKeywordRepository {
    override fun typeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword> {
        val limitedPageable: OffsetPageable
        val bq = QueryBuilders.boolQuery()
        val nsq: NativeSearchQueryBuilder

        if(query.isEmpty()){ //retrieve all
            limitedPageable = OffsetPageable(0, 10000, null)
            nsq = NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(bq)
                .withSort(SortBuilders.fieldSort("${Keyword::value.name}$SORT_INSENSITIVE").order(SortOrder.ASC))
            bq
                .must(QueryBuilders.termQuery(Keyword::type.name, type.name))
                .should(
                    QueryBuilders.matchAllQuery()
                )
        }
        else {
            limitedPageable  = OffsetPageable(0, 20, null)
            nsq = NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(bq)
                .withSort(SortBuilders.fieldSort("${Keyword::value.name}$SORT_INSENSITIVE").order(SortOrder.ASC))
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
        }

        return elasticSearchTemplate.search(nsq.build(), Keyword::class.java)
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.keyword")
class KeywordEsRepoConfig

interface KeywordEsRepo : ElasticsearchRepository<Keyword, Int>, CustomKeywordRepository
