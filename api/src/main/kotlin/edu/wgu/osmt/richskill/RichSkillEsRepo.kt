package edu.wgu.osmt.richskill

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSimilaritySearch
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.FindsAllByPublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.jobcode.JobCodeQueries
import edu.wgu.osmt.nullIfEmpty
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.*
import org.elasticsearch.index.query.QueryBuilders.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomRichSkillQueries : FindsAllByPublishStatus<RichSkillDoc> {
    fun generateBoolQueriesFromApiSearch(bq: BoolQueryBuilder, advancedQuery: ApiAdvancedSearch)
    fun richSkillPropertiesMultiMatch(query: String): BoolQueryBuilder
    fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = Pageable.unpaged(),
        collectionId: String? = null
    ): SearchHits<RichSkillDoc>

    fun findSimilar(apiSimilaritySearch: ApiSimilaritySearch): SearchHits<RichSkillDoc>

    fun occupationQueries(query: String): NestedQueryBuilder
}

class CustomRichSkillQueriesImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomRichSkillQueries {
    override val javaClass = RichSkillDoc::class.java

    override fun occupationQueries(query: String): NestedQueryBuilder {
        val jobCodePath = RichSkillDoc::jobCodes.name
        return QueryBuilders.nestedQuery(
            jobCodePath,
            JobCodeQueries.multiPropertySearch(query, jobCodePath),
            ScoreMode.Max
        )
    }

    // Query clauses for Rich Skill properties
    override fun generateBoolQueriesFromApiSearch(bq: BoolQueryBuilder, advancedQuery: ApiAdvancedSearch) {
        with(advancedQuery) {
            // boolQuery.must for logical AND
            // boolQuery.should for logical OR
            skillName.nullIfEmpty()?.let {
                if (it.contains("\"")) {
                    bq.must(simpleQueryStringQuery(it).field("${RichSkillDoc::name.name}.raw").defaultOperator(Operator.AND))
                } else {
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::name.name, it))
                }

            }
            category.nullIfEmpty()?.let {
                if (it.contains("\"")) {
                    bq.must(simpleQueryStringQuery(it).field("${RichSkillDoc::category.name}.raw").defaultOperator(Operator.AND))
                } else {
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::category.name, it))
                }
            }
            author.nullIfEmpty()?.let {
                if (it.contains("\"")) {
                    bq.must(simpleQueryStringQuery(it).field("${RichSkillDoc::author.name}.raw").defaultOperator(Operator.AND))
                } else {
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::author.name, it))
                }
            }
            skillStatement.nullIfEmpty()?.let {
                if (it.contains("\"")) {
                    bq.must(
                        simpleQueryStringQuery(it).field("${RichSkillDoc::statement.name}.raw").defaultOperator(Operator.AND)
                    )
                } else {
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::statement.name, it))
                }
            }
            keywords?.map {
                if (it.contains("\"")) {
                    bq.must(
                        simpleQueryStringQuery(it).field("${RichSkillDoc::searchingKeywords.name}.raw")
                            .defaultOperator(Operator.AND)
                    )
                } else {
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::searchingKeywords.name, it))
                }
            }

            occupations?.let {
                it.mapNotNull { value ->
                    bq.must(
                        occupationQueries(value)
                    )
                }
            }

            standards?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    if (s.contains("\"")) {
                        bq.must(
                            simpleQueryStringQuery(s).field("${RichSkillDoc::standards.name}.raw").defaultOperator(Operator.AND)
                        )
                    } else {
                        bq.must(matchBoolPrefixQuery(RichSkillDoc::standards.name, s))
                    }
                }
            }

            certifications?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    if (s.contains("\"")) {
                        bq.must(
                            simpleQueryStringQuery(s).field("${RichSkillDoc::certifications.name}.raw")
                                .defaultOperator(Operator.AND)
                        )
                    } else {
                        bq.must(matchBoolPrefixQuery(RichSkillDoc::certifications.name, s))
                    }
                }
            }

            employers?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    if (s.contains("\"")) {
                        bq.must(
                            simpleQueryStringQuery(s).field("${RichSkillDoc::employers.name}.raw").defaultOperator(Operator.AND)
                        )
                    } else {
                        bq.must(matchBoolPrefixQuery(RichSkillDoc::employers.name, s))
                    }
                }
            }

            alignments?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    if (s.contains("\"")) {
                        bq.must(
                            simpleQueryStringQuery(s).field(RichSkillDoc::alignments.name).defaultOperator(Operator.AND)
                        )
                    } else {
                        bq.must(matchBoolPrefixQuery(RichSkillDoc::alignments.name, s))
                    }
                }
            }
        }
    }

    override fun richSkillPropertiesMultiMatch(query: String): BoolQueryBuilder {
        val isComplex = query.contains("\"")

        val boolQuery = boolQuery()

        val complexQueries = listOf(
            simpleQueryStringQuery(query).field("${RichSkillDoc::name.name}.raw").boost(2.0f)
                .defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::statement.name}.raw").defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::category.name}.raw").defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::searchingKeywords.name}.raw")
                .defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::standards.name}.raw").defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::certifications.name}.raw")
                .defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::employers.name}.raw").defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::alignments.name}.raw").defaultOperator(Operator.AND),
            simpleQueryStringQuery(query).field("${RichSkillDoc::author.name}.raw").defaultOperator(Operator.AND)
        )

        val queries = listOf(
            matchPhrasePrefixQuery(RichSkillDoc::name.name, query).boost(2.0f),
            matchPhrasePrefixQuery(RichSkillDoc::statement.name, query),
            matchPhrasePrefixQuery(RichSkillDoc::category.name, query),
            matchPhrasePrefixQuery(RichSkillDoc::searchingKeywords.name, query),
            matchPhrasePrefixQuery(RichSkillDoc::standards.name, query),
            matchPhrasePrefixQuery(RichSkillDoc::certifications.name, query),
            matchPhrasePrefixQuery(RichSkillDoc::employers.name, query),
            matchPhrasePrefixQuery(RichSkillDoc::alignments.name, query),
            matchPhrasePrefixQuery(RichSkillDoc::author.name, query)
        )

        if (isComplex) {
            complexQueries.map { boolQuery.should(it) }
        } else {
            queries.map { boolQuery.should(it) }
        }

        return boolQuery
    }

    override fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus>,
        pageable: Pageable,
        collectionId: String?
    ): SearchHits<RichSkillDoc> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        val bq = boolQuery()

        nsq.withQuery(bq)
        nsq.withFilter(BoolQueryBuilder().must(
            termsQuery(
                RichSkillDoc::publishStatus.name,
                publishStatus.map { ps -> ps.toString() }
            )
        ))

        // treat the presence of query property to mean multi field search with that term
        if (!apiSearch.query.isNullOrBlank()) {

            if (collectionId.isNullOrBlank()) {
                bq.should(richSkillPropertiesMultiMatch(apiSearch.query))
                bq.should(occupationQueries(apiSearch.query))
                bq.should(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        simpleQueryStringQuery(apiSearch.query).field("collections.name.raw")
                            .defaultOperator(Operator.AND),
                        ScoreMode.Avg
                    )
                )
            } else {
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("collections.uuid", collectionId)),
                        ScoreMode.Avg
                    )
                )
                bq.must(BoolQueryBuilder().should(richSkillPropertiesMultiMatch(apiSearch.query)).should(occupationQueries(apiSearch.query)))
            }
        } else if (apiSearch.advanced != null) {
            generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (collectionId.isNullOrBlank()) {
                apiSearch.advanced.collectionName?.let {
                    bq.must(
                        QueryBuilders.nestedQuery(
                            RichSkillDoc::collections.name,
                            simpleQueryStringQuery(it).field("collections.name.raw").defaultOperator(Operator.AND),
                            ScoreMode.Avg
                        )
                    )
                }
            } else {
                bq.must(
                    QueryBuilders.nestedQuery(
                        RichSkillDoc::collections.name,
                        QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("collections.uuid", collectionId)),
                        ScoreMode.Avg
                    )
                )
            }
        } else {
            var apiSearchUuids = apiSearch.uuids?.filterNotNull()?.filter { x: String? -> x != "" }

            if (!apiSearchUuids.isNullOrEmpty()) {
                nsq.withFilter(
                    BoolQueryBuilder().must(
                        termsQuery(
                            RichSkillDoc::uuid.name,
                            apiSearchUuids
                        )
                    )
                )
            }
            if (!collectionId.isNullOrBlank()) {
                bq.must(
                    QueryBuilders.nestedQuery(
                        RichSkillDoc::collections.name,
                        QueryBuilders.boolQuery()
                            .must(QueryBuilders.matchQuery("collections.uuid", collectionId)),
                        ScoreMode.Avg
                    )
                )

            }
        }

        return elasticSearchTemplate.search(nsq.build(), RichSkillDoc::class.java)
    }

    override fun findSimilar(apiSimilaritySearch: ApiSimilaritySearch): SearchHits<RichSkillDoc> {
        val limitedPageable = OffsetPageable(0, 10, null)
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(
            MatchPhraseQueryBuilder(RichSkillDoc::statement.name, apiSimilaritySearch.statement).slop(4)
        )
        return elasticSearchTemplate.search(nsq.build(), RichSkillDoc::class.java)
    }
}


@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.richskill")
class RichSkillEsRepoConfig

interface RichSkillEsRepo : ElasticsearchRepository<RichSkillDoc, Int>, CustomRichSkillQueries {
    fun findByUuid(
        uuid: String,
        pageable: Pageable = PageRequest.of(0, PaginationDefaults.size, Sort.by("name.keyword").descending())
    ): Page<RichSkillDoc>
}


