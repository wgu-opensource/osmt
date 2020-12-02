package edu.wgu.osmt.collection

import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.EsCollectionRepository
import edu.wgu.osmt.elasticsearch.EsRichSkillRepository
import edu.wgu.osmt.richskill.RichSkillSearchService
import edu.wgu.osmt.richskill.RichSkillDoc
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.InnerHitBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service

@Service
class CollectionSearchService @Autowired constructor(
    val esRichSkillRepository: EsRichSkillRepository,
    val esCollectionRepository: EsCollectionRepository,
    val elasticsearchRestTemplate: ElasticsearchRestTemplate,
    val appConfig: AppConfig
){
    fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = PageRequest.of(
            0,
            RichSkillSearchService.DEFAULT_PAGESIZE,
            Sort.by("name.keyword").descending()
        )
    ): SearchHits<CollectionDoc> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(Pageable.unpaged())
        val bq = QueryBuilders.boolQuery()
        val filter = BoolQueryBuilder().must(QueryBuilders.termsQuery(RichSkillDoc::publishStatus.name, publishStatus))
        nsq.withFilter(filter)
        nsq.withQuery(bq)

        var collectionMultiPropertyResults: List<String> = listOf()

        // treat the presence of query property to mean multi field search with that term
        if (!apiSearch.query.isNullOrBlank()) {
            // Search against rich skill properties
            bq.must(esRichSkillRepository.richSkillPropertiesMultiMatch(apiSearch.query))

            // always include inner collection object with rich skill search hits
            bq.must(
                QueryBuilders.nestedQuery(
                    RichSkillDoc::collections.name,
                    QueryBuilders.matchAllQuery(),
                    ScoreMode.Avg
                ).innerHit(InnerHitBuilder())
            )

            // search on collection specific properties
            collectionMultiPropertyResults = elasticsearchRestTemplate.search(
                NativeSearchQueryBuilder().withQuery(
                    QueryBuilders.boolQuery().should(esCollectionRepository.collectionPropertiesMultiMatch(apiSearch.query))
                ).withPageable(Pageable.unpaged()).withFilter(filter).build(), CollectionDoc::class.java
            ).searchHits.map { it.content.uuid }

        } else if (apiSearch.advanced != null) {
            esRichSkillRepository.generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (apiSearch.advanced.collectionName != null) {
                bq.must(
                    QueryBuilders.nestedQuery(
                        RichSkillDoc::collections.name,
                        QueryBuilders.boolQuery().must(
                            QueryBuilders.multiMatchQuery(
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
                    QueryBuilders.nestedQuery(
                        RichSkillDoc::collections.name,
                        QueryBuilders.matchAllQuery(),
                        ScoreMode.Avg
                    ).innerHit(InnerHitBuilder())
                )
            }
        } else { // query nor advanced search was provided, return all collections
            bq.must(
                QueryBuilders.nestedQuery(
                    RichSkillDoc::collections.name,
                    QueryBuilders.matchAllQuery(),
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
                QueryBuilders.termsQuery(
                    "_id",
                    (innerHitCollectionUuids + collectionMultiPropertyResults).distinct()
                )
            ).withFilter(filter).withPageable(pageable).build(), CollectionDoc::class.java
        )
    }
}
