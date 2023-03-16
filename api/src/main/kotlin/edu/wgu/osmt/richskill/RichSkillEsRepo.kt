package edu.wgu.osmt.richskill

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiFilteredSearch
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSimilaritySearch
import edu.wgu.osmt.config.INDEX_RICHSKILL_DOC
import edu.wgu.osmt.config.QUOTED_SEARCH_REGEX_PATTERN
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.FindsAllByPublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.jobcode.JobCodeQueries
import edu.wgu.osmt.nullIfEmpty
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.*
import org.elasticsearch.index.query.QueryBuilders.*
import org.elasticsearch.script.Script
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

const val collectionsUuid = "collections.uuid"

interface CustomRichSkillQueries : FindsAllByPublishStatus<RichSkillDoc> {
    fun generateBoolQueriesFromApiSearch(bq: BoolQueryBuilder, advancedQuery: ApiAdvancedSearch)
    fun generateBoolQueriesFromApiSearchWithFilters(bq: BoolQueryBuilder, filteredQuery: ApiFilteredSearch, publishStatus: Set<PublishStatus>)
    fun richSkillPropertiesMultiMatch(query: String): BoolQueryBuilder
    fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = Pageable.unpaged(),
        collectionId: String? = null
    ): SearchHits<RichSkillDoc>
    fun countByApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = Pageable.unpaged(),
        collectionId: String? = null
    ): Long

    fun findSimilar(apiSimilaritySearch: ApiSimilaritySearch): SearchHits<RichSkillDoc>

    fun occupationQueries(query: String): NestedQueryBuilder


    fun deleteIndex() {
        elasticSearchTemplate.indexOps(IndexCoordinates.of(INDEX_RICHSKILL_DOC)).delete()
    }
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

    private fun buildNestedQueries(path: String?=null, queryParams: List<String>) : BoolQueryBuilder {
        val disjunctionQuery = disMaxQuery()
        val queries = ArrayList<PrefixQueryBuilder>()

        queryParams.let {
            it.map { param ->
                queries.add(
                    prefixQuery("$path.keyword", param)
                )
            }

        }
        disjunctionQuery.innerQueries().addAll(queries)

        return boolQuery().must(existsQuery("$path.keyword")).must(disjunctionQuery)
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
                    bq.must(matchBoolPrefixQuery(RichSkillDoc::name.name, it))
                }
            }
            category.nullIfEmpty()?.let {
                if (it.matches(Regex(QUOTED_SEARCH_REGEX_PATTERN))) {
                    bq.must(simpleQueryStringQuery(it).field("${RichSkillDoc::category.name}.keyword").defaultOperator(Operator.AND))
                } else {
                    bq.must(matchBoolPrefixQuery(RichSkillDoc::category.name, it))
                }
            }
            author.nullIfEmpty()?.let {
                if (it.contains("\"")) {
                    bq.must(simpleQueryStringQuery(it).field("${RichSkillDoc::authors.name}.raw").defaultOperator(Operator.AND))
                } else {
                    bq.must(matchBoolPrefixQuery(RichSkillDoc::authors.name, it))
                }
            }
            skillStatement.nullIfEmpty()?.let {
                if (it.contains("\"")) {
                    bq.must(
                        simpleQueryStringQuery(it).field("${RichSkillDoc::statement.name}.raw").defaultOperator(Operator.AND)
                    )
                } else {
                    bq.must(matchBoolPrefixQuery(RichSkillDoc::statement.name, it))
                }
            }
            keywords?.map {
                if (it.contains("\"")) {
                    bq.must(
                        simpleQueryStringQuery(it).field("${RichSkillDoc::searchingKeywords.name}.raw")
                            .defaultOperator(Operator.AND)
                    )
                } else {
                    bq.must(matchBoolPrefixQuery(RichSkillDoc::searchingKeywords.name, it))
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

    override fun generateBoolQueriesFromApiSearchWithFilters(bq: BoolQueryBuilder, filteredQuery: ApiFilteredSearch, publishStatus: Set<PublishStatus>) {
        bq.must(
            termsQuery(
                RichSkillDoc::publishStatus.name,
                publishStatus.map { ps -> ps.toString() }
            )
        )
        with(filteredQuery) {
            categories?. let {
                bq.must(buildNestedQueries(RichSkillDoc::category.name, it))
            }
            keywords?. let {
                it.mapNotNull {
                    bq.must(generateTermsSetQueryBuilder(RichSkillDoc::searchingKeywords.name, keywords))
                }
            }
            standards?. let {
                it.mapNotNull {
                    bq.must(generateTermsSetQueryBuilder(RichSkillDoc::standards.name, standards))
                }
            }
            certifications?. let {
                it.mapNotNull {
                    bq.must(generateTermsSetQueryBuilder(RichSkillDoc::certifications.name, certifications))
                }
            }
            alignments?. let {
                it.mapNotNull {
                    bq.must(generateTermsSetQueryBuilder(RichSkillDoc::alignments.name, alignments))
                }
            }
            employers?. let {
                it.mapNotNull {
                    bq.must(generateTermsSetQueryBuilder(RichSkillDoc::employers.name, employers))
                }
            }
            authors?. let {
                bq.must(buildNestedQueries(RichSkillDoc::authors.name, it))
            }
            occupations?.let {
                it.mapNotNull { value ->
                    bq.must(
                        occupationQueries(value)
                    )
                }
            }
        }
    }

    private fun generateTermsSetQueryBuilder(fieldName: String, list: List<String>): TermsSetQueryBuilder {
        return TermsSetQueryBuilder("$fieldName.keyword", list).setMinimumShouldMatchScript(Script(list.size.toString()))
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
            simpleQueryStringQuery(query).field("${RichSkillDoc::authors.name}.raw").defaultOperator(Operator.AND)
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
            matchPhrasePrefixQuery(RichSkillDoc::authors.name, query)
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
        val nsq: NativeSearchQueryBuilder = buildQuery(pageable, publishStatus, apiSearch, collectionId)

        return elasticSearchTemplate.search(nsq.build(), RichSkillDoc::class.java)
    }

    override fun countByApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus>,
        pageable: Pageable,
        collectionId: String?
    ): Long {
        val nsq: NativeSearchQueryBuilder = buildQuery(pageable, publishStatus, apiSearch, collectionId)

        return elasticSearchTemplate.count(nsq.build(), RichSkillDoc::class.java)
    }

    fun buildQuery(
        pageable: Pageable,
        publishStatus: Set<PublishStatus>,
        apiSearch: ApiSearch,
        collectionId: String?
    ): NativeSearchQueryBuilder {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        val bq = boolQuery()

        nsq.withQuery(bq)
        nsq.withFilter(
            BoolQueryBuilder().must(
                termsQuery(
                    RichSkillDoc::publishStatus.name,
                    publishStatus.map { ps -> ps.toString() }
                )
            )
        )

        apiSearch.filtered?.let { generateBoolQueriesFromApiSearchWithFilters(bq, it, publishStatus) }

        // treat the presence of query property to mean multi field search with that term
        if (!apiSearch.query.isNullOrBlank()) {

            if (collectionId.isNullOrBlank()) {
                if(apiSearch.filtered != null){
                    bq.must(richSkillPropertiesMultiMatch(apiSearch.query))
                }
                else {
                    bq.should(richSkillPropertiesMultiMatch(apiSearch.query))
                }
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
                if(apiSearch.filtered != null) {
                    bq.must(
                        nestedQuery(
                            RichSkillDoc::collections.name,
                            boolQuery().must(matchQuery(collectionsUuid, collectionId).operator(Operator.AND)),
                            ScoreMode.Avg
                        )
                    )
                }
                else {
                    bq.must(
                        nestedQuery(
                            RichSkillDoc::collections.name,
                            boolQuery().must(matchQuery(collectionsUuid, collectionId).operator(Operator.OR)),
                            ScoreMode.Avg
                        )
                    )
                }
                bq.must(
                    BoolQueryBuilder().should(richSkillPropertiesMultiMatch(apiSearch.query))
                        .should(occupationQueries(apiSearch.query))
                )

            }
        } else if (apiSearch.advanced != null) {
            generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (collectionId.isNullOrBlank()) {
                apiSearch?.advanced.collectionName?.let {
                    bq.must(
                        nestedQuery(
                            RichSkillDoc::collections.name,
                            simpleQueryStringQuery(it).field("collections.name.raw").defaultOperator(Operator.AND),
                            ScoreMode.Avg
                        )
                    )
                }
            } else {
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        boolQuery().must(matchQuery(collectionsUuid, collectionId)),
                        ScoreMode.Avg
                    )
                )
            }
        }

        else {
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
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        boolQuery()
                            .must(matchQuery(collectionsUuid, collectionId)),
                        ScoreMode.Avg
                    )
                )

            }
        }

        return nsq
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


