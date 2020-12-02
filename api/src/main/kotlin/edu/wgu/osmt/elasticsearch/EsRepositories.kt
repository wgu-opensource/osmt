package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillSearchService.Companion.DEFAULT_PAGESIZE
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.richskill.RichSkillDoc
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchRestTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)
        nsq.withQuery(QueryBuilders.matchAllQuery())
        nsq.withFilter(
            BoolQueryBuilder().should(
                QueryBuilders.termsQuery(
                    "publishStatus",
                    publishStatus
                )
            )
        )
        return elasticSearchTemplate.search(nsq.build(), javaClass)
    }
}

interface CustomRichSkillQueries : FindsAllByPublishStatus<RichSkillDoc>{
    fun generateBoolQueriesFromApiSearch(bq: BoolQueryBuilder, advancedQuery: ApiAdvancedSearch)
    fun richSkillPropertiesMultiMatch(query: String): MultiMatchQueryBuilder
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
}

interface CustomCollectionQueries: FindsAllByPublishStatus<CollectionDoc>{
    fun collectionPropertiesMultiMatch(query: String): MultiMatchQueryBuilder
}

class CustomCollectionQueriesImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchRestTemplate) :
    CustomCollectionQueries {

    override val javaClass = CollectionDoc::class.java

    override fun collectionPropertiesMultiMatch(query: String): MultiMatchQueryBuilder {
        val fields = arrayOf(
            CollectionDoc::name.name,
            "${CollectionDoc::name.name}._2gram",
            "${CollectionDoc::name.name}._3gram",
            CollectionDoc::author.name
        )

        return QueryBuilders.multiMatchQuery(query, *fields).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX)
    }
}

interface EsRichSkillRepository : ElasticsearchRepository<RichSkillDoc, Int>, CustomRichSkillQueries {
    fun findByUuid(
        uuid: String,
        pageable: Pageable = PageRequest.of(0, DEFAULT_PAGESIZE, Sort.by("name.keyword").descending())
    ): Page<RichSkillDoc>
}

interface EsJobCodeRepository : ElasticsearchRepository<JobCode, Int>

interface EsKeywordRepository : ElasticsearchRepository<Keyword, Int>

interface EsCollectionRepository : ElasticsearchRepository<CollectionDoc, Int>, CustomCollectionQueries {
    fun findByUuid(uuid: String, pageable: Pageable): Page<CollectionDoc>

    fun findAllByUuidIn(
        uuids: List<String>,
        pageable: Pageable
    ): Page<CollectionDoc>

    fun findByName(q: String, pageable: Pageable = PageRequest.of(0, DEFAULT_PAGESIZE)): Page<CollectionDoc>
}

