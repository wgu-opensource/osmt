package edu.wgu.osmt.jobcode

import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.DisMaxQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.*
import org.elasticsearch.search.sort.SortBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomJobCodeRepository {
    val elasticSearchTemplate: ElasticsearchRestTemplate
    fun typeAheadSearch(query: String): SearchHits<JobCode>
}

class CustomJobCodeRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomJobCodeRepository {

    override fun typeAheadSearch(query: String): SearchHits<JobCode> {
        val limitedPageable = OffsetPageable(0, 10, null)
        val disjunctionQuery = JobCodeQueries.multiPropertySearch(query)

        val nsq: NativeSearchQueryBuilder =
            NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(disjunctionQuery)
                .withSort(SortBuilders.scoreSort())

        return elasticSearchTemplate.search(nsq.build(), JobCode::class.java)
    }
}

object JobCodeQueries {
    fun multiPropertySearch(query: String, parentDocPath: String? = null): BoolQueryBuilder {
        val disjunctionQuery = disMaxQuery()
        val path = parentDocPath?.let { "${it}." } ?: ""
        disjunctionQuery.innerQueries().addAll(
            listOf(
                matchPhrasePrefixQuery(
                    "${path}${JobCode::code.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::minor.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::minorCode.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::detailed.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::detailedCode.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::major.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::majorCode.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::broad.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::broadCode.name}",
                    query
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::description.name}",
                    query
                )
            )
        )
        return boolQuery().must(existsQuery("${path}${JobCode::name.name}")).must(disjunctionQuery)
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.jobcode")
class JobCodeEsRepoConfig


interface JobCodeEsRepo : ElasticsearchRepository<JobCode, Int>, CustomJobCodeRepository
