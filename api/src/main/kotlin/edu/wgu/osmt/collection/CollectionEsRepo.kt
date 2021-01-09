package edu.wgu.osmt.collection

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.FindsAllByPublishStatus
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillDoc
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.*
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


interface CustomCollectionQueries : FindsAllByPublishStatus<CollectionDoc> {
    val richSkillEsRepo: RichSkillEsRepo

    fun collectionPropertiesMultiMatch(query: String): AbstractQueryBuilder<*>
    fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus> = PublishStatus.publishStatusSet,
        pageable: Pageable = PageRequest.of(
            0,
            PaginationDefaults.size,
            Sort.by("name.keyword").descending()
        )
    ): SearchHits<CollectionDoc>
}

class CustomCollectionQueriesImpl @Autowired constructor(
    override val elasticSearchTemplate: ElasticsearchRestTemplate,
    override val richSkillEsRepo: RichSkillEsRepo
) :
    CustomCollectionQueries {

    override val javaClass = CollectionDoc::class.java

    override fun collectionPropertiesMultiMatch(query: String): AbstractQueryBuilder<*> {
        val isComplex = query.contains("\"")

        val fields = arrayOf(
            CollectionDoc::name.name,
            "${CollectionDoc::name.name}._2gram",
            "${CollectionDoc::name.name}._3gram",
            CollectionDoc::author.name
        )

        return if (isComplex) {
            QueryBuilders.simpleQueryStringQuery(query).fields(fields.map { it to 1.0f }.toMap())
        } else {
            QueryBuilders.multiMatchQuery(query, *fields).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
        }
    }

    override fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus>,
        pageable: Pageable
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
            bq.should(
                BoolQueryBuilder()
                    .must(richSkillEsRepo.richSkillPropertiesMultiMatch(apiSearch.query))
                    .must(
                        QueryBuilders.nestedQuery(
                            RichSkillDoc::collections.name,
                            QueryBuilders.matchAllQuery(),
                            ScoreMode.Avg
                        ).innerHit(InnerHitBuilder())
                    )
            )
            bq.should(richSkillEsRepo.occupationQueries(apiSearch.query))

            // search on collection specific properties
            collectionMultiPropertyResults = elasticSearchTemplate.search(
                NativeSearchQueryBuilder().withQuery(
                    QueryBuilders.boolQuery().should(collectionPropertiesMultiMatch(apiSearch.query))
                ).withPageable(Pageable.unpaged()).withFilter(filter).build(), CollectionDoc::class.java
            ).searchHits.map { it.content.uuid }

        } else if (apiSearch.advanced != null) {
            richSkillEsRepo.generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (!apiSearch.advanced.collectionName.isNullOrBlank()) {
                if (apiSearch.advanced.collectionName.contains("\"")) {
                    collectionMultiPropertyResults = elasticSearchTemplate.search(
                        NativeSearchQueryBuilder().withQuery(
                            QueryBuilders.boolQuery().should(
                                QueryBuilders.simpleQueryStringQuery(
                                    apiSearch.advanced.collectionName
                                ).field(CollectionDoc::name.name)
                            )
                        ).withPageable(Pageable.unpaged()).withFilter(filter).build(), CollectionDoc::class.java
                    ).searchHits.map { it.content.uuid }
                } else {
                    collectionMultiPropertyResults = elasticSearchTemplate.search(
                        NativeSearchQueryBuilder().withQuery(
                            QueryBuilders.boolQuery().should(
                                QueryBuilders.matchPhrasePrefixQuery(
                                    CollectionDoc::name.name,
                                    apiSearch.advanced.collectionName
                                )
                            )
                        ).withPageable(Pageable.unpaged()).withFilter(filter).build(), CollectionDoc::class.java
                    ).searchHits.map { it.content.uuid }
                }
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

        val results = elasticSearchTemplate.search(nsq.build(), RichSkillDoc::class.java)

        val innerHitCollectionUuids =
            results.searchHits.mapNotNull { it.getInnerHits("collections")?.searchHits?.mapNotNull { it.content as CollectionDoc } }
                .flatten().map { it.uuid }.distinct()

        return elasticSearchTemplate.search(
            NativeSearchQueryBuilder().withQuery(
                QueryBuilders.termsQuery(
                    "_id",
                    (innerHitCollectionUuids + collectionMultiPropertyResults).distinct()
                )
            ).withFilter(filter).withPageable(pageable).build(), CollectionDoc::class.java
        )
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.collection")
class CollectionEsRepoConfig

interface CollectionEsRepo : ElasticsearchRepository<CollectionDoc, Int>, CustomCollectionQueries {
    fun findByUuid(uuid: String, pageable: Pageable): Page<CollectionDoc>

    fun findAllByUuidIn(
        uuids: List<String>,
        pageable: Pageable
    ): Page<CollectionDoc>

    fun findByName(q: String, pageable: Pageable = PageRequest.of(0, PaginationDefaults.size)): Page<CollectionDoc>
}
