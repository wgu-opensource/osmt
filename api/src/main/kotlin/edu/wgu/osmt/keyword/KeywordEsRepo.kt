package edu.wgu.osmt.keyword

import co.elastic.clients.elasticsearch._types.query_dsl.*
import edu.wgu.osmt.config.INDEX_KEYWORD_DOC
import edu.wgu.osmt.config.SORT_INSENSITIVE
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.jobcode.CustomJobCodeRepositoryImpl
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomKeywordRepository {
    val elasticSearchTemplate: ElasticsearchTemplate
    fun typeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword>

    fun deleteIndex() {
        elasticSearchTemplate.indexOps(IndexCoordinates.of(INDEX_KEYWORD_DOC)).delete()
    }
}

/**
 * This have been partially converted to use the ElasticSearch 8.7.X apis. For full conversion
 * replace typeAheadSearch() with TypeAheadSearchNu()
 */
class CustomKeywordRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchTemplate) :
    CustomKeywordRepository {
    val log: Logger = LoggerFactory.getLogger(CustomJobCodeRepositoryImpl::class.java)

    @Deprecated("Upgrade to ES v8.x queries", ReplaceWith("typeAheadSearchNu"), DeprecationLevel.WARNING )
    override fun typeAheadSearch(searchStr: String, type: KeywordTypeEnum): SearchHits<Keyword> {
        val limitedPageable: OffsetPageable
        val bq = QueryBuilders.boolQuery()
        val nqb: NativeSearchQueryBuilder

        if(searchStr.isEmpty()){ //retrieve all
            limitedPageable = OffsetPageable(0, 10000, null)
            nqb = NativeSearchQueryBuilder()
                .withPageable(limitedPageable)
                .withQuery(bq)
                .withSort(SortBuilders.fieldSort("${Keyword::value.name}$SORT_INSENSITIVE").order(SortOrder.ASC))
            bq
                .must(QueryBuilders.termQuery(Keyword::type.name, type.name))
                .should( QueryBuilders.matchAllQuery() )
        }
        else {
            limitedPageable  = OffsetPageable(0, 20, null)
            nqb = NativeSearchQueryBuilder()
                .withPageable(limitedPageable)
                .withQuery(bq)
                .withSort(SortBuilders.fieldSort("${Keyword::value.name}$SORT_INSENSITIVE").order(SortOrder.ASC))
            bq
                .must(QueryBuilders.termQuery(Keyword::type.name, type.name))
                .should(
                    QueryBuilders.matchBoolPrefixQuery( Keyword::value.name, searchStr )
                )
                .should(
                    QueryBuilders.matchPhraseQuery( Keyword::value.name, searchStr ).boost(5f)
                ).minimumShouldMatch(1)
        }

        val query = createStringQuery("CustomKeywordRepositoryImpl.typeAheadSearch()", nqb)
        return elasticSearchTemplate.search(query, Keyword::class.java)
    }

    /**
     * Uses the latest ES 8.7.x Java Client API
     */
    fun typeAheadSearchNu(searchStr: String, type: KeywordTypeEnum): SearchHits<Keyword> {
        val pageable: OffsetPageable
        val criteria: co.elastic.clients.elasticsearch._types.query_dsl.Query

        if (searchStr.isEmpty()) {
            pageable = OffsetPageable(0, 10000, null)
            criteria = searchAll(type)
        } else {
            pageable = OffsetPageable(0, 20, null)
            criteria = searchSpecific(searchStr, type)
        }
        log.debug(String.Companion.format("\ntypeAheadSearchNu query:\n\t\t%s", criteria.bool().toString()))
        return elasticSearchTemplate.search( NativeQuery.builder()
                                                        .withPageable(pageable)
                                                        .withQuery(criteria).build(), Keyword::class.java )
    }

    fun searchAll(type: KeywordTypeEnum): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.bool { builder: BoolQuery.Builder ->
                        builder
                            .must(co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.term {   qt: TermQuery.Builder -> qt.field(Keyword::type.name).value(type.name) } )
                            .should(co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.matchAll { q : MatchAllQuery.Builder -> q } ) }
    }

    fun searchSpecific(searchStr: String, type: KeywordTypeEnum): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.bool { builder: BoolQuery.Builder ->
                        builder
                            .must(co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.term {   qt: TermQuery.Builder -> qt.field(Keyword::type.name).value(type.name) } )
                            .should(co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.matchBoolPrefix { q : MatchBoolPrefixQuery.Builder -> q.field(Keyword::value.name).query(searchStr)} )
                            .should(co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.matchPhrase { q : MatchPhraseQuery.Builder -> q.field(Keyword::value.name).query(searchStr)} )
                            .minimumShouldMatch("1") }
    }

    fun createStringQuery(msgPrefix: String, nqb: NativeSearchQueryBuilder): Query {
        val query = nqb.build()
        log.debug(String.Companion.format("\n%s query:\n\t\t%s", msgPrefix, query.query.toString()))
        log.debug(String.Companion.format("\n%s filter:\n\t\t%s", msgPrefix, query.filter.toString()))
        //NOTE: this is causing us to lose the filter query
        return StringQuery(query.query.toString())
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.keyword")
class KeywordEsRepoConfig

interface KeywordEsRepo : ElasticsearchRepository<Keyword, Int>, CustomKeywordRepository
