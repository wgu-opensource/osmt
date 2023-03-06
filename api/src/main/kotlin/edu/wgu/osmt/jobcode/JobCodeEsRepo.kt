package edu.wgu.osmt.jobcode

import edu.wgu.osmt.config.INDEX_JOBCODE_DOC
import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders.*
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomJobCodeRepository {
    val elasticSearchTemplate: ElasticsearchRestTemplate
    fun typeAheadSearch(query: String): SearchHits<JobCode>

    fun deleteIndex() {
        elasticSearchTemplate.indexOps(IndexCoordinates.of(INDEX_JOBCODE_DOC)).delete()
    }
}

class CustomJobCodeRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomJobCodeRepository {

    override fun typeAheadSearch(query: String): SearchHits<JobCode> {
        val nsq: NativeSearchQueryBuilder

        val limitedPageable: OffsetPageable = if (query.isEmpty()) {
            OffsetPageable(0, 10000, null)
        } else {
            OffsetPageable(0, 20, null)

        }
        val disjunctionQuery = JobCodeQueries.multiPropertySearch(query)
        nsq =
            NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(disjunctionQuery)
                .withSort(SortBuilders.fieldSort("${JobCode::code.name}.keyword").order(SortOrder.ASC))
        return elasticSearchTemplate.search(nsq.build(), JobCode::class.java)
    }
}

object JobCodeQueries {
    fun multiPropertySearch(query: String, parentDocPath: String? = null): BoolQueryBuilder {
        val disjunctionQuery = disMaxQuery()
        val path = parentDocPath?.let { "${it}." } ?: ""
        val isComplex = query.contains("\"")

        val queries =
            listOf(
                prefixQuery(
                    "${path}${JobCode::code.name}.keyword",
                    query
                ).boost(2.0f),
                prefixQuery(
                    "${path}${JobCode::minorCode.name}.keyword",
                    query
                ),
                prefixQuery(
                    "${path}${JobCode::detailedCode.name}.keyword",
                    query
                ),
                prefixQuery(
                    "${path}${JobCode::majorCode.name}.keyword",
                    query
                ),
                prefixQuery(
                    "${path}${JobCode::broadCode.name}.keyword",
                    query
                )
            )

        disjunctionQuery.innerQueries().addAll(queries)

        if (isComplex) {
            disjunctionQuery.innerQueries().addAll(
                listOf(
                    simpleQueryStringQuery(
                        query
                    ).field("${path}${JobCode::name.name}.raw").boost(2.0f).defaultOperator(Operator.AND),
                    simpleQueryStringQuery(
                        query
                    ).field("${path}${JobCode::minor.name}.raw").analyzeWildcard(true),
                    simpleQueryStringQuery(query).field("${path}${JobCode::detailed.name}.raw")
                        .defaultOperator(Operator.AND),
                    simpleQueryStringQuery(query).field("${path}${JobCode::major.name}.raw").defaultOperator(Operator.AND),
                    simpleQueryStringQuery(query).field("${path}${JobCode::broad.name}.raw").defaultOperator(Operator.AND)
                )
            )
        } else {
            disjunctionQuery.innerQueries().addAll(
                listOf(
                    matchPhrasePrefixQuery("${path}${JobCode::name.name}", query).boost(2.0f),
                    matchPhrasePrefixQuery("${path}${JobCode::minor.name}", query),
                    matchPhrasePrefixQuery("${path}${JobCode::detailed.name}",query),
                    matchPhrasePrefixQuery("${path}${JobCode::major.name}", query),
                    matchPhrasePrefixQuery("${path}${JobCode::broad.name}", query)
                )
            )
        }

        return boolQuery().must(existsQuery("${path}${JobCode::name.name}")).must(disjunctionQuery)
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.jobcode")
class JobCodeEsRepoConfig


interface JobCodeEsRepo : ElasticsearchRepository<JobCode, Int>, CustomJobCodeRepository
