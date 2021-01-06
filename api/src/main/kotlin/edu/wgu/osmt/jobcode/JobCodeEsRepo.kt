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

        val queries =
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
                ),
                simpleQueryStringQuery(
                    lowerCaseQuery
                ).field("${path}${JobCode::name.name}").boost(2.0f),
                simpleQueryStringQuery(lowerCaseQuery
                ).field("${path}${JobCode::minor.name}"),
                simpleQueryStringQuery(lowerCaseQuery).field("${path}${JobCode::detailed.name}"),
                simpleQueryStringQuery(lowerCaseQuery).field("${path}${JobCode::major.name}"),
                simpleQueryStringQuery(lowerCaseQuery).field("${path}${JobCode::broad.name}")
            )
        disjunctionQuery.innerQueries().addAll(queries)

        return boolQuery().must(existsQuery("${path}${JobCode::name.name}")).must(disjunctionQuery)
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.jobcode")
class JobCodeEsRepoConfig


interface JobCodeEsRepo : ElasticsearchRepository<JobCode, Int>, CustomJobCodeRepository
