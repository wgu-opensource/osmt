package edu.wgu.osmt.richskill

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSimilaritySearch
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.FindsAllByPublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchPhraseQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
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
    fun richSkillPropertiesMultiMatch(query: String): MultiMatchQueryBuilder
    fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = Pageable.unpaged(),
        collectionId: String? = null
    ): SearchHits<RichSkillDoc>
    fun findSimilar(apiSimilaritySearch: ApiSimilaritySearch): SearchHits<RichSkillDoc>
}

class CustomRichSkillQueriesImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomRichSkillQueries {
    override val javaClass = RichSkillDoc::class.java

    // Query clauses for Rich Skill properties
    override fun generateBoolQueriesFromApiSearch(bq: BoolQueryBuilder, advancedQuery: ApiAdvancedSearch) {
        with(advancedQuery) {
            // boolQuery.must for logical AND
            // boolQuery.should for logical OR

            skillName?.let { bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::name.name, it)) }
            category?.let { bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::category.name, it)) }
            skillStatement?.let { bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::statement.name, it)) }
            keywords?.map { bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::searchingKeywords.name, it)) }

            occupations?.let {
                it.mapNotNull { it.name }.map { value ->
                    bq.must(
                        QueryBuilders.boolQuery().should(
                            QueryBuilders.matchBoolPrefixQuery(
                                RichSkillDoc::majorCodes.name,
                                value
                            )
                        ).should(
                            QueryBuilders.matchBoolPrefixQuery(
                                RichSkillDoc::minorCodes.name,
                                value
                            )
                        ).should(
                            QueryBuilders.matchBoolPrefixQuery(
                                RichSkillDoc::broadCodes.name,
                                value
                            )
                        ).should(
                            QueryBuilders.matchBoolPrefixQuery(
                                RichSkillDoc::jobRoleCodes.name,
                                value
                            )
                        )
                    )
                }
            }

            standards?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::standards.name, s))
                }
            }

            certifications?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::certifications.name, s))
                }
            }

            employers?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::employers.name, s))
                }
            }

            alignments?.let { it ->
                it.mapNotNull { it.name }.map { s ->
                    bq.must(QueryBuilders.matchBoolPrefixQuery(RichSkillDoc::alignments.name, s))
                }
            }
        }
    }

    override fun richSkillPropertiesMultiMatch(query: String): MultiMatchQueryBuilder {
        val fields = arrayOf(
            "${RichSkillDoc::name.name}",
            "${RichSkillDoc::name.name}._2gram",
            "${RichSkillDoc::name.name}._3gram",
            RichSkillDoc::statement.name,
            "${RichSkillDoc::statement.name}._2gram",
            "${RichSkillDoc::statement.name}._3gram",
            RichSkillDoc::category.name,
            "${RichSkillDoc::category.name}._2gram",
            "${RichSkillDoc::category.name}._3gram",
            RichSkillDoc::searchingKeywords.name,
            "${RichSkillDoc::searchingKeywords.name}._2gram",
            "${RichSkillDoc::searchingKeywords.name}._3gram",
            RichSkillDoc::majorCodes.name,
            RichSkillDoc::minorCodes.name,
            RichSkillDoc::broadCodes.name,
            "${RichSkillDoc::jobRoleCodes.name}",
            RichSkillDoc::standards.name,
            "${RichSkillDoc::standards.name}._2gram",
            "${RichSkillDoc::standards.name}._3gram",
            RichSkillDoc::certifications.name,
            "${RichSkillDoc::certifications.name}._2gram",
            "${RichSkillDoc::certifications.name}._3gram",
            RichSkillDoc::employers.name,
            "${RichSkillDoc::employers.name}._2gram",
            "${RichSkillDoc::employers.name}._3gram",
            RichSkillDoc::alignments.name,
            "${RichSkillDoc::alignments.name}._2gram",
            "${RichSkillDoc::alignments.name}._3gram",
            RichSkillDoc::author.name,
            "${RichSkillDoc::author.name}._2gram",
            "${RichSkillDoc::author.name}._3gram"
        )

        return QueryBuilders.multiMatchQuery(
            query,
            *fields
        ).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
    }

    override fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus>,
        pageable: Pageable,
        collectionId: String?
    ): SearchHits<RichSkillDoc> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        val bq = QueryBuilders.boolQuery()
        val filter = BoolQueryBuilder().must(QueryBuilders.termsQuery(RichSkillDoc::publishStatus.name, publishStatus))

        nsq.withQuery(bq)
        nsq.withFilter(filter)

        // treat the presence of query property to mean multi field search with that term
        if (!apiSearch.query.isNullOrBlank()) {

            if (collectionId.isNullOrBlank()) {
                bq.should(richSkillPropertiesMultiMatch(apiSearch.query))
                bq.should(
                    QueryBuilders.nestedQuery(
                        RichSkillDoc::collections.name,
                        QueryBuilders.matchQuery("collections.name", apiSearch.query),
                        ScoreMode.Avg
                    )
                )
            } else {
                bq.must(richSkillPropertiesMultiMatch(apiSearch.query))
                bq.must(
                    QueryBuilders.nestedQuery(
                        RichSkillDoc::collections.name,
                        QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("collections.uuid", collectionId)),
                        ScoreMode.Avg
                    )
                )
            }
        } else if (apiSearch.advanced != null) {
            generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (collectionId.isNullOrBlank()) {
                apiSearch.advanced.collectionName?.let {
                    bq.must(
                        QueryBuilders.nestedQuery(
                            RichSkillDoc::collections.name,
                            QueryBuilders.matchQuery("collections.name", it),
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
        } else if (!collectionId.isNullOrBlank()) {
            bq.must(
                QueryBuilders.nestedQuery(
                    RichSkillDoc::collections.name,
                    QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("collections.uuid", collectionId)),
                    ScoreMode.Avg
                )
            )
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
