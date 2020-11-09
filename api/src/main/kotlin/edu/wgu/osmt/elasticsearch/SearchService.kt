package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.collection.CollectionDoc
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
    val elasticsearchRestTemplate: ElasticsearchRestTemplate
) {

    val whiteLabelAllowsSearchingByAuthor: Boolean = true // TODO wire this up to white labeling

    fun collectionPropertiesMultiMatch(query: String): MultiMatchQueryBuilder {
        var fields = arrayOf(
            CollectionDoc::name.name
        )

        if (whiteLabelAllowsSearchingByAuthor) {
            fields += CollectionDoc::author.name
        }

        return multiMatchQuery(query, *fields)
    }

    fun richSkillPropertiesMultiMatch(query: String): MultiMatchQueryBuilder {
        var fields = arrayOf(
            RichSkillDoc::name.name,
            RichSkillDoc::category.name,
            RichSkillDoc::statement.name,
            RichSkillDoc::searchingKeywords.name,
            "${RichSkillDoc::jobCodes.name}.${JobCode::code.name}",
            RichSkillDoc::standards.name,
            RichSkillDoc::certifications.name,
            RichSkillDoc::employers.name,
            RichSkillDoc::alignments.name
        )

        if (whiteLabelAllowsSearchingByAuthor) {
            fields += RichSkillDoc::author.name
        }

        return multiMatchQuery(
            query,
            *fields
        )
    }

    // Query clauses for Rich Skill properties
    fun generateBoolQueriesFromApiSearch(bq: BoolQueryBuilder, advancedQuery: ApiAdvancedSearch) {
        with(advancedQuery) {
            // boolQuery.must for logical AND
            // boolQuery.should for logical OR

            skillName?.let { bq.must(matchQuery(RichSkillDoc::name.name, it)) }
            category?.let { bq.must(matchQuery(RichSkillDoc::category.name, it)) }
            skillStatement?.let { bq.must(matchQuery(RichSkillDoc::statement.name, it)) }
            keywords?.let { bq.must(termsQuery(RichSkillDoc::searchingKeywords.name, it)) }

            occupations?.let {
                bq.must(
                    boolQuery().should(
                        termsQuery(
                            "${RichSkillDoc::jobCodes.name}.${JobCode::name.name}",
                            it.mapNotNull { it.name })
                    ).should(
                        termsQuery(
                            "${RichSkillDoc::jobCodes.name}.${JobCode::code.name}",
                            it.mapNotNull { it.name })
                    )
                )
            }

            standards?.let {
                bq.must(
                    termsQuery(
                        "${RichSkillDoc::standards.name}.${ApiNamedReference::name.name}",
                        it.mapNotNull { it.name })
                )
            }
            certifications?.let {
                bq.must(
                    termsQuery(
                        RichSkillDoc::certifications.name,
                        it.mapNotNull { it.name })
                )
            }
            employers?.let {
                bq.must(
                    termsQuery(
                        RichSkillDoc::employers.name,
                        it.mapNotNull { it.name })
                )
            }
            alignments?.let {
                bq.must(
                    termsQuery(
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

        } else if (apiSearch.advanced != null){
            generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (apiSearch.advanced.collectionName != null) {
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        boolQuery().must(matchQuery("collections.name", apiSearch.advanced.collectionName)),
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
        pageable: Pageable = Pageable.unpaged()
    ): SearchHits<RichSkillDoc> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        val bq = boolQuery()
        val filter = BoolQueryBuilder().must(termsQuery(RichSkillDoc::publishStatus.name, publishStatus))

        nsq.withQuery(bq)
        nsq.withFilter(filter)

        // treat the presence of query property to mean multi field search with that term
        if (!apiSearch.query.isNullOrBlank()) {
            bq.should(richSkillPropertiesMultiMatch(apiSearch.query))

            bq.should(
                nestedQuery(
                    RichSkillDoc::collections.name,
                    matchQuery("collections.name", apiSearch.query),
                    ScoreMode.Avg
                )
            )
        } else if (apiSearch.advanced != null) {
            generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            apiSearch.advanced.collectionName?.let {
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        matchQuery("collections.name", it),
                        ScoreMode.Avg
                    )
                )
            }
        }

        return elasticsearchRestTemplate.search(nsq.build(), RichSkillDoc::class.java)
    }

    companion object {
        // TODO configurable?
        const val DEFAULT_PAGESIZE: Int = 50
    }
}
