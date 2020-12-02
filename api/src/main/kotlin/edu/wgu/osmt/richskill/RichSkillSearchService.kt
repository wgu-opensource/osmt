package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.EsRichSkillRepository
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service

@Service
class RichSkillSearchService @Autowired constructor(
    val esRichSkillRepository: EsRichSkillRepository,
    val elasticsearchRestTemplate: ElasticsearchRestTemplate,
    val appConfig: AppConfig
) {
    fun byApiSearch(
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
                bq.should(esRichSkillRepository.richSkillPropertiesMultiMatch(apiSearch.query))
                bq.should(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        matchQuery("collections.name", apiSearch.query),
                        ScoreMode.Avg
                    )
                )
            } else {
                bq.must(esRichSkillRepository.richSkillPropertiesMultiMatch(apiSearch.query))
                bq.must(
                    nestedQuery(
                        RichSkillDoc::collections.name,
                        boolQuery().must(matchQuery("collections.uuid", collectionId)),
                        ScoreMode.Avg
                    )
                )
            }
        } else if (apiSearch.advanced != null) {
            esRichSkillRepository.generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

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
