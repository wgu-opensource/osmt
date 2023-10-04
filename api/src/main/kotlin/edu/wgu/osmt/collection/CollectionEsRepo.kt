package edu.wgu.osmt.collection

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.config.INDEX_COLLECTION_DOC
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.FindsAllByPublishStatus
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.*
import org.elasticsearch.index.query.QueryBuilders.matchPhrasePrefixQuery
import org.elasticsearch.index.query.QueryBuilders.simpleQueryStringQuery
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
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

    fun deleteIndex() {
        elasticSearchTemplate.indexOps(IndexCoordinates.of(INDEX_COLLECTION_DOC)).delete()
    }
}

class CustomCollectionQueriesImpl @Autowired constructor(
    override val elasticSearchTemplate: ElasticsearchTemplate,
    override val richSkillEsRepo: RichSkillEsRepo
) :
    CustomCollectionQueries {
    val log: Logger = LoggerFactory.getLogger(CustomCollectionQueriesImpl::class.java)
    override val javaClass = CollectionDoc::class.java

    override fun collectionPropertiesMultiMatch(query: String): AbstractQueryBuilder<*> {
        val isComplex = query.contains("\"")

        val complexFields = arrayOf(
            "${CollectionDoc::name.name}.raw",
            "${CollectionDoc::name.name}._2gram",
            "${CollectionDoc::name.name}._3gram",
            "${CollectionDoc::description.name}.raw",
            CollectionDoc::author.name
        )

        val fields = arrayOf(
            CollectionDoc::name.name,
            "${CollectionDoc::name.name}._2gram",
            "${CollectionDoc::name.name}._3gram",
            CollectionDoc::description.name,
            CollectionDoc::author.name
        )

        return if (isComplex) {
            QueryBuilders.simpleQueryStringQuery(query).fields(complexFields.map { it to 1.0f }.toMap())
                .defaultOperator(Operator.AND)
        } else {
            QueryBuilders.multiMatchQuery(query, *fields).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
        }
    }

    override fun byApiSearch(
        apiSearch: ApiSearch,
        publishStatus: Set<PublishStatus>,
        pageable: Pageable
    ): SearchHits<CollectionDoc> {
        val nqb = NativeSearchQueryBuilder().withPageable(Pageable.unpaged())
        val bq = QueryBuilders.boolQuery()
        //TODO Replace with FindsAllByPublishStatus.createTermsQuery(publishStatus.name, publishStatus.map { ps -> ps.toString() })
        val filter = BoolQueryBuilder().must(
            QueryBuilders.termsQuery(
                RichSkillDoc::publishStatus.name,
                publishStatus.map { ps -> ps.toString() }
            )
        )
        nqb.withFilter(filter)
        nqb.withQuery(bq)

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

            val nqb = NativeSearchQueryBuilder()
                .withQuery( collectionPropertiesMultiMatch(apiSearch.query) )
                .withPageable(Pageable.unpaged())
                .withFilter(filter)
            // search on collection specific properties
            val query = convertToStringQuery("CustomCollectionQueriesImpl.byApiSearch()1", nqb, log)
            collectionMultiPropertyResults = elasticSearchTemplate
                .search(query, CollectionDoc::class.java)
                .searchHits
                .map { it.content.uuid }

        } else if (apiSearch.advanced != null) {
            richSkillEsRepo.generateBoolQueriesFromApiSearch(bq, apiSearch.advanced)

            if (!apiSearch.advanced.collectionName.isNullOrBlank()) {
                if (apiSearch.advanced.collectionName.contains("\"")) {
                    val nqb = NativeSearchQueryBuilder()
                        .withQuery( simpleQueryStringQuery(apiSearch.advanced.collectionName).field("${CollectionDoc::name.name}.raw").defaultOperator(Operator.AND) )
                        .withPageable(Pageable.unpaged())
                        .withFilter(filter)
                    val query = convertToStringQuery("CustomCollectionQueriesImpl.byApiSearch()2", nqb, log)
                    collectionMultiPropertyResults = elasticSearchTemplate
                        .search( query, CollectionDoc::class.java )
                        .searchHits
                        .map { it.content.uuid }
                } else {
                    val nqb = NativeSearchQueryBuilder()
                        .withQuery( matchPhrasePrefixQuery( CollectionDoc::name.name, apiSearch.advanced.collectionName ) )
                        .withPageable(Pageable.unpaged())
                        .withFilter(filter)
                    val query = convertToStringQuery("CustomCollectionQueriesImpl.byApiSearch()3", nqb, log)
                    collectionMultiPropertyResults = elasticSearchTemplate
                        .search( query, CollectionDoc::class.java )
                        .searchHits
                        .map { it.content.uuid }
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

        var query = convertToStringQuery("CustomCollectionQueriesImpl.byApiSearch().innerHitCollectionUuids", nqb, log)
        val results = elasticSearchTemplate.search(query, RichSkillDoc::class.java)

        val innerHitCollectionUuids =
            results.searchHits.mapNotNull { it.getInnerHits("collections")?.searchHits?.mapNotNull { it.content as CollectionDoc } }
                .flatten().map { it.uuid }.distinct()

        val nqb2 = NativeSearchQueryBuilder()
            .withQuery( QueryBuilders.termsQuery( "_id", (innerHitCollectionUuids + collectionMultiPropertyResults).distinct() ) )
            .withFilter(filter)
            .withPageable(pageable)
        query = convertToStringQuery("CustomCollectionQueriesImpl.byApiSearch()4", nqb2, log)
        return elasticSearchTemplate.search(query, CollectionDoc::class.java)
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
