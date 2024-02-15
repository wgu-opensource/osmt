package edu.wgu.osmt.keyword

import co.elastic.clients.elasticsearch._types.query_dsl.*
import edu.wgu.osmt.config.INDEX_KEYWORD_DOC
import edu.wgu.osmt.config.VALUE_SORT_INSENSITIVE
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.elasticsearch.OsmtQueryHelper.createNativeQuery
import edu.wgu.osmt.elasticsearch.OsmtQueryHelper.createSort
import edu.wgu.osmt.jobcode.CustomJobCodeRepositoryImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

interface CustomKeywordRepository {
    val elasticSearchTemplate: ElasticsearchTemplate
    fun typeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword>

    fun deleteIndex() {
        elasticSearchTemplate.indexOps(IndexCoordinates.of(INDEX_KEYWORD_DOC)).delete()
    }
}

class CustomKeywordRepositoryImpl @Autowired constructor(override val elasticSearchTemplate: ElasticsearchTemplate) :
    CustomKeywordRepository {
    val log: Logger = LoggerFactory.getLogger(CustomJobCodeRepositoryImpl::class.java)

    override fun typeAheadSearch(searchStr: String, type: KeywordTypeEnum): SearchHits<Keyword> {
        val pageable: OffsetPageable
        val criteria: Query

        if (searchStr.isEmpty()) {
            pageable = OffsetPageable(0, 10000, null)
            criteria = searchAll(type)
        } else {
            pageable = OffsetPageable(0, 20, null)
            criteria = searchSpecific(searchStr, type)
        }

        var nativeQuery = createNativeQuery(pageable, null, criteria, "CustomKeywordRepositoryImpl.typeAheadSearch()", log, createSort(VALUE_SORT_INSENSITIVE))
        return elasticSearchTemplate.search(nativeQuery, Keyword::class.java)
    }

    fun searchAll(type: KeywordTypeEnum): Query {
        return QueryBuilders.bool { builder: BoolQuery.Builder ->
                        builder
                            .must(QueryBuilders.term {   qt: TermQuery.Builder -> qt.field(Keyword::type.name).value(type.name) } )
                            .should(QueryBuilders.matchAll { q : MatchAllQuery.Builder -> q } ) }
    }

    fun searchSpecific(searchStr: String, type: KeywordTypeEnum): Query {
        return QueryBuilders.bool { builder: BoolQuery.Builder ->
                        builder
                            .must(QueryBuilders.term {   qt: TermQuery.Builder -> qt.field(Keyword::type.name).value(type.name) } )
                            .should(QueryBuilders.matchBoolPrefix { q : MatchBoolPrefixQuery.Builder -> q.field(Keyword::value.name).query(searchStr)} )
                            .should(QueryBuilders.matchPhrase { q : MatchPhraseQuery.Builder -> q.field(Keyword::value.name).query(searchStr)} )
                            .minimumShouldMatch("1") }
    }
}

@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.keyword")
class KeywordEsRepoConfig

interface KeywordEsRepo : ElasticsearchRepository<Keyword, Int>, CustomKeywordRepository
