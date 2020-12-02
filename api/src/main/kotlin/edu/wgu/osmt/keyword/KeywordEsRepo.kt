package edu.wgu.osmt.keyword

import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomKeywordRepository {
    val elasticSearchTemplate: ElasticsearchRestTemplate
    fun typeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword>
}

class CustomKeywordRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomKeywordRepository {
    override fun typeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword> {
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

        return elasticSearchTemplate.search(nsq.build(), Keyword::class.java)
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.keyword")
class KeywordEsRepoConfig

interface EsKeywordRepository : ElasticsearchRepository<Keyword, Int>, CustomKeywordRepository
