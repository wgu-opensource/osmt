package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.richskill.RichSkillDoc
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.InnerHitBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service

@Service
class SearchService @Autowired constructor(
    val esRichSkillRepository: EsRichSkillRepository,
    val esCollectionRepository: EsCollectionRepository,
    val elasticsearchRestTemplate: ElasticsearchRestTemplate,
    val appConfig: AppConfig
) {
    fun collectionPropertiesMultiMatch(query: String): MultiMatchQueryBuilder {
        val fields = arrayOf(
            CollectionDoc::name.name,
            "${CollectionDoc::name.name}._2gram",
            "${CollectionDoc::name.name}._3gram",
            CollectionDoc::author.name
        )

        return multiMatchQuery(query, *fields).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
    }

    fun richSkillPropertiesMultiMatch(query: String): MultiMatchQueryBuilder {
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

        return multiMatchQuery(
            query,
            *fields
        ).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
    }

    // Query clauses for Rich Skill properties
    fun generateBoolQueriesFromApiSearch(bq: BoolQueryBuilder, advancedQuery: ApiAdvancedSearch) {
        with(advancedQuery) {
            // boolQuery.must for logical AND
            // boolQuery.should for logical OR

            skillName?.let { bq.must(matchBoolPrefixQuery(RichSkillDoc::name.name, it)) }
            category?.let { bq.must(matchBoolPrefixQuery(RichSkillDoc::category.name, it)) }
            skillStatement?.let { bq.must(matchBoolPrefixQuery(RichSkillDoc::statement.name, it)) }
            keywords?.map { bq.must(matchBoolPrefixQuery(RichSkillDoc::searchingKeywords.name, it)) }

            occupations?.let {
                it.mapNotNull { it.name }.map { value ->
                    bq.must(
                        boolQuery().should(
                            matchBoolPrefixQuery(
                                RichSkillDoc::majorCodes.name,
                                value
                            )
                        ).should(
                            matchBoolPrefixQuery(
                                RichSkillDoc::minorCodes.name,
                                value
                            )
                        ).should(
                            matchBoolPrefixQuery(
                                RichSkillDoc::broadCodes.name,
                                value
                            )
                        ).should(
                            matchBoolPrefixQuery(
                                RichSkillDoc::jobRoleCodes.name,
                                value
                            )
                        )
                    )
                }
            }

            standards?.let {
                bq.must(
                    matchBoolPrefixQuery(
                        "${RichSkillDoc::standards.name}.${ApiNamedReference::name.name}",
                        it.mapNotNull { it.name })
                )
            }
            certifications?.let {
                bq.must(
                    matchBoolPrefixQuery(
                        RichSkillDoc::certifications.name,
                        it.mapNotNull { it.name })
                )
            }
            employers?.let {
                bq.must(
                    matchBoolPrefixQuery(
                        RichSkillDoc::employers.name,
                        it.mapNotNull { it.name })
                )
            }
            alignments?.let {
                bq.must(
                    matchBoolPrefixQuery(
                        RichSkillDoc::alignments.name,
                        it.mapNotNull { it.name })
                )
            }
        }
    }

    fun searchCollectionsByApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = PageRequest.of(
            0,
            DEFAULT_PAGESIZE,
            Sort.by("name.keyword").descending()
        )
    ): SearchHits<CollectionDoc> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(Pageable.unpaged())
        val bq = boolQuery()
        val filter = BoolQueryBuilder().must(termsQuery(RichSkillDoc::publishStatus.name, publishStatus))
        nsq.withFilter(filter)
        nsq.withQuery(bq)

        var collectionMultiPropertyResults: List<String> = listOf()

        // treat the presence of query property to mean multi field search with that term
        if (!apiSearch.query.isNullOrBlank()) {
            // Search against rich skill properties
            bq.must(richSkillPropertiesMultiMatch(apiSearch.query))

            // always include inner collection object with rich skill search hits
            bq.must(
                nestedQuery(
                    RichSkillDoc::collections.name,
                    matchAllQuery(),
                    ScoreMode.Avg
                ).innerHit(InnerHitBuilder())
            )

            // search on collection specific properties
            collectionMultiPropertyResults = elasticsearchRestTemplate.search(
                NativeSearchQueryBuilder().withQuery(
                    boolQuery().should(collectionPropertiesMultiMatch(apiSearch.query))
                ).withPageable(Pageable.unpaged()).withFilter(filter).build(), CollectionDoc::class.java
            ).searchHits.map { it.content.uuid }

        } else if (apiSearch.advanced != null) {
            generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (apiSearch.advanced.collectionName != null) {
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        boolQuery().must(
                            multiMatchQuery(
                                apiSearch.advanced.collectionName, *listOf(
                                    "collections.name",
                                    "collections.name._2gram",
                                    "collections.name._3gram"
                                ).toTypedArray()
                            ).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
                        ),
                        ScoreMode.Avg
                    ).innerHit(InnerHitBuilder())
                )
            } else {
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        matchAllQuery(),
                        ScoreMode.Avg
                    ).innerHit(InnerHitBuilder())
                )
            }
        } else { // query nor advanced search was provided, return all collections
            bq.must(
                nestedQuery(
                    RichSkillDoc::collections.name,
                    matchAllQuery(),
                    ScoreMode.Avg
                ).innerHit(InnerHitBuilder())
            )
        }

        val results = elasticsearchRestTemplate.search(nsq.build(), RichSkillDoc::class.java)

        val innerHitCollectionUuids =
            results.searchHits.mapNotNull { it.getInnerHits("collections")?.searchHits?.mapNotNull { it.content as CollectionDoc } }
                .flatten().map { it.uuid }.distinct()

        return elasticsearchRestTemplate.search(
            NativeSearchQueryBuilder().withQuery(
                termsQuery(
                    "_id",
                    (innerHitCollectionUuids + collectionMultiPropertyResults).distinct()
                )
            ).withFilter(filter).withPageable(pageable).build(), CollectionDoc::class.java
        )
    }

    fun searchRichSkillsByApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = Pageable.unpaged(),
        collectionId: String? = null
    ): SearchHits<RichSkillDoc> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        val bq = boolQuery()
        val filter = BoolQueryBuilder().must(termsQuery(RichSkillDoc::publishStatus.name, publishStatus))

        nsq.withQuery(bq)
        nsq.withFilter(filter)

        // treat the presence of query property to mean multi field search with that term
        if (!apiSearch.query.isNullOrBlank()) {

            if (collectionId.isNullOrBlank()) {
                bq.should(richSkillPropertiesMultiMatch(apiSearch.query))
                bq.should(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        matchQuery("collections.name", apiSearch.query),
                        ScoreMode.Avg
                    )
                )
            } else {
                bq.must(richSkillPropertiesMultiMatch(apiSearch.query))
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        boolQuery().must(matchQuery("collections.uuid", collectionId)),
                        ScoreMode.Avg
                    )
                )
            }
        } else if (apiSearch.advanced != null) {
            generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (collectionId.isNullOrBlank()) {
                apiSearch.advanced.collectionName?.let {
                    bq.must(
                        nestedQuery(
                            RichSkillDoc::collections.name,
                            matchQuery("collections.name", it),
                            ScoreMode.Avg
                        )
                    )
                }
            } else {
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        boolQuery().must(matchQuery("collections.uuid", collectionId)),
                        ScoreMode.Avg
                    )
                )
            }
        } else if (!collectionId.isNullOrBlank()) {
            bq.must(
                nestedQuery(
                    RichSkillDoc::collections.name,
                    boolQuery().must(matchQuery("collections.uuid", collectionId)),
                    ScoreMode.Avg
                )
            )
        }

        return elasticsearchRestTemplate.search(nsq.build(), RichSkillDoc::class.java)
    }

    companion object {
        const val DEFAULT_PAGESIZE: Int = 50
    }
}
