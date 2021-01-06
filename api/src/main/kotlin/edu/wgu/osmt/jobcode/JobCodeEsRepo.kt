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
        val lowerCaseQuery = query.toLowerCase()
        val codeRegex = "[0-9]+(-)?[0-9]+(.[0-9]*)?".toRegex()

        val queries = if (codeRegex.matches(query)){
            listOf(
                prefixQuery(
                    "${path}${JobCode::code.name}.keyword",
                    lowerCaseQuery
                ).boost(2.0f),
                prefixQuery(
                    "${path}${JobCode::minorCode.name}.keyword",
                    lowerCaseQuery
                ),
                prefixQuery(
                    "${path}${JobCode::detailedCode.name}.keyword",
                    lowerCaseQuery
                ),
                prefixQuery(
                    "${path}${JobCode::majorCode.name}.keyword",
                    lowerCaseQuery
                ),
                prefixQuery(
                    "${path}${JobCode::broadCode.name}.keyword",
                    lowerCaseQuery
                )
            )
        } else {
            listOf(
                matchPhrasePrefixQuery(
                    "${path}${JobCode::name.name}",
                    lowerCaseQuery
                ).boost(2.0f),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::minor.name}",
                    lowerCaseQuery
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::detailed.name}",
                    lowerCaseQuery
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::major.name}",
                    lowerCaseQuery
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::broad.name}",
                    lowerCaseQuery
                ),
                matchPhrasePrefixQuery(
                    "${path}${JobCode::description.name}",
                    lowerCaseQuery
                )
            )
        }
        disjunctionQuery.innerQueries().addAll(queries)

        return boolQuery().must(existsQuery("${path}${JobCode::name.name}")).must(disjunctionQuery)
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.jobcode")
class JobCodeEsRepoConfig


interface JobCodeEsRepo : ElasticsearchRepository<JobCode, Int>, CustomJobCodeRepository
