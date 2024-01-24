package edu.wgu.osmt.jobcode

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import edu.wgu.osmt.config.INDEX_JOBCODE_DOC
import edu.wgu.osmt.config.NAME_SORT_KEYWORD
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.elasticsearch.OsmtQueryHelper
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories


interface CustomJobCodeRepository {
    val elasticSearchTemplate: ElasticsearchTemplate
    fun typeAheadSearch(query: String): SearchHits<JobCode>

    fun deleteIndex() {
        elasticSearchTemplate.indexOps(IndexCoordinates.of(INDEX_JOBCODE_DOC)).delete()
    }
}

class CustomJobCodeRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchTemplate) :
    CustomJobCodeRepository {
    val log: Logger = LoggerFactory.getLogger(CustomJobCodeRepositoryImpl::class.java)

    /*
    @Deprecated("Upgrade to ES v8.x queries", ReplaceWith(""), DeprecationLevel.WARNING )
    override fun typeAheadSearch(query: String): SearchHits<JobCode> {
        val disjunctionQuery = JobCodeQueries.multiPropertySearch(query)
        val nqb = NativeSearchQueryBuilder()
                .withPageable(createOffsetPageable(query))
                .withQuery(disjunctionQuery)
                .withSort(SortBuilders.fieldSort("${JobCode::code.name}.keyword").order(SortOrder.ASC))
        val query = convertToNativeQuery(createOffsetPageable(query), null, nqb, "CustomJobCodeRepositoryImpl.typeAheadSearch()", log)
        return elasticSearchTemplate.search(query, JobCode::class.java)
    }
     */

    override fun typeAheadSearch(searchStr: String): SearchHits<JobCode> {
        var limit = if (searchStr.isEmpty()) 10000 else 20
        var nativeQuery =
                    OsmtQueryHelper.createNativeQuery(
                        OffsetPageable(0, limit, null),
                        null,
                        multiPropertySearch(searchStr),
                        "CustomJobCodeRepository.typeAheadSearch()",
                        log,
                        OsmtQueryHelper.createSort(NAME_SORT_KEYWORD) )
        return elasticSearchTemplate.search(nativeQuery, JobCode::class.java)
    }

    private fun multiPropertySearch(searchStr: String): Query {
        val disMaxQuery = createDisMaxQuery(searchStr)
        val nameMustExistQuery = QueryBuilders.exists { qt: ExistsQuery.Builder -> qt.field(JobCode::name.name).boost(1.0f)}
        return QueryBuilders.bool { builder: BoolQuery.Builder -> builder.must(disMaxQuery, nameMustExistQuery).boost(1.0f) }
    }

    private fun createDisMaxQuery(searchStr: String) : Query {
        val isComplex = searchStr.contains("\"")
        val queries = mutableListOf<Query>()

        queries.addAll(createPrefixQueries(searchStr))
        if(isComplex) {
            queries.addAll(createComplexQueries(searchStr))
        } else {
            queries.addAll(createSimpleQueries(searchStr))
        }
        return OsmtQueryHelper.createDisMaxDslQuery(queries)
    }

    private fun createPrefixQueries(searchStr: String) : List<Query>{
        return listOf(
            OsmtQueryHelper.createPrefixDslQuery("${JobCode::code.name}.keyword", searchStr, 2.0f),
            OsmtQueryHelper.createPrefixDslQuery("${JobCode::minorCode.name}.keyword", searchStr),
            OsmtQueryHelper.createPrefixDslQuery("${JobCode::detailedCode.name}.keyword", searchStr),
            OsmtQueryHelper.createPrefixDslQuery("${JobCode::majorCode.name}.keyword", searchStr),
            OsmtQueryHelper.createPrefixDslQuery("${JobCode::broadCode.name}.keyword", searchStr)
        )
    }
    private fun createSimpleQueries(searchStr: String) : List<Query>{
        return listOf(
            OsmtQueryHelper.createMatchPhrasePrefixDslQuery(JobCode::name.name, searchStr, 2.0f),
            OsmtQueryHelper.createMatchPhrasePrefixDslQuery(JobCode::minor.name, searchStr),
            OsmtQueryHelper.createMatchPhrasePrefixDslQuery(JobCode::detailed.name, searchStr),
            OsmtQueryHelper.createMatchPhrasePrefixDslQuery(JobCode::major.name, searchStr),
            OsmtQueryHelper.createMatchPhrasePrefixDslQuery(JobCode::broad.name, searchStr)
        )
    }

    private fun createComplexQueries(searchStr: String) : List<Query>{
        return listOf(
            OsmtQueryHelper.createSimpleQueryDslQuery("${JobCode::name.name}.raw", searchStr, 2.0f),
            OsmtQueryHelper.createSimpleQueryDslQuery("${JobCode::minor.name}.raw", searchStr, 1.0f, true, co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or),
            OsmtQueryHelper.createSimpleQueryDslQuery("${JobCode::detailed.name}.raw", searchStr, 1.0f),
            OsmtQueryHelper.createSimpleQueryDslQuery("${JobCode::major.name}.raw", searchStr, 1.0f),
            OsmtQueryHelper.createSimpleQueryDslQuery("${JobCode::broad.name}.raw", searchStr, 1.0f)
        )
    }
}

@Deprecated("Upgrade to ES v8.x queries", ReplaceWith("CustomJobCodeRepository.typeAheadSearch()"), DeprecationLevel.WARNING )
object JobCodeQueries {
    //TODO Convert to ES v8.7.x apis and return the newer BoolQuery.Builder instance; see KeywordEsRep.kt
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
                    simpleQueryStringQuery(query).field("${path}${JobCode::name.name}.raw").boost(2.0f).defaultOperator(Operator.AND),
                    simpleQueryStringQuery(query).field("${path}${JobCode::minor.name}.raw").analyzeWildcard(true),
                    simpleQueryStringQuery(query).field("${path}${JobCode::detailed.name}.raw").defaultOperator(Operator.AND),
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
