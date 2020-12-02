package edu.wgu.osmt.jobcode

import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomJobCodeRepository{
    val elasticSearchTemplate: ElasticsearchRestTemplate
    fun typeAheadSearch(query: String): SearchHits<JobCode>
}

class CustomJobCodeRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate): CustomJobCodeRepository{
    override fun typeAheadSearch(query: String): SearchHits<JobCode> {
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

        return elasticSearchTemplate.search(nsq.build(), JobCode::class.java)
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.jobcode")
class JobCodeEsRepoConfig


interface EsJobCodeRepository : ElasticsearchRepository<JobCode, Int>, CustomJobCodeRepository
