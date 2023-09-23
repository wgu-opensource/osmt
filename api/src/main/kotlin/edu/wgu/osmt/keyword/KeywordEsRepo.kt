package edu.wgu.osmt.keyword

import co.elastic.clients.elasticsearch._types.FieldSort
import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*
import edu.wgu.osmt.config.INDEX_KEYWORD_DOC
import edu.wgu.osmt.config.SORT_INSENSITIVE
import edu.wgu.osmt.elasticsearch.OffsetPageable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
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
    override fun typeAheadSearch(query: String, type: KeywordTypeEnum): SearchHits<Keyword> {
        /*
        val limitedPageable: OffsetPageable
        val bq = QueryBuilders.boolQuery()
        val nsq: NativeSearchQueryBuilder

        if(query.isEmpty()){ //retrieve all
            limitedPageable = OffsetPageable(0, 10000, null)
            nsq = NativeSearchQueryBuilder()
                .withPageable(limitedPageable)
                .withQuery(bq)
                .withSort(SortBuilders.fieldSort("${Keyword::value.name}$SORT_INSENSITIVE").order(SortOrder.ASC))
            bq
                .must(QueryBuilders.termQuery(Keyword::type.name, type.name))
                .should( QueryBuilders.matchAllQuery() )
        }
        else {
            limitedPageable  = OffsetPageable(0, 20, null)
            nsq = NativeSearchQueryBuilder().withPageable(limitedPageable).withQuery(bq)
                .withSort(SortBuilders.fieldSort("${Keyword::value.name}$SORT_INSENSITIVE").order(SortOrder.ASC))
            bq
                .must(QueryBuilders.termQuery(Keyword::type.name, type.name))
                .should(
                    QueryBuilders.matchBoolPrefixQuery(
                        Keyword::value.name,
                        query
                    )
                )
                .should(
                    QueryBuilders.matchPhraseQuery(
                        Keyword::value.name,
                        query
                    ).boost(5f)
                ).minimumShouldMatch(1)
        }
        val nsqb: NativeSearchQuery = nsq.build()
        val searchQuery: Query = StringQuery(nsqb.getQuery().toString())

        return elasticSearchTemplate.search(searchQuery, Keyword::class.java)
*/
        val nsq1 = if (query.isEmpty())
                        createNativeQuery_retrieveAll(type)
                    else
                        createNativeQuery_retrieveExact(query, type)
        return elasticSearchTemplate.search(nsq1.build(), Keyword::class.java)
    }


    private fun createNativeQuery_retrieveExact(queryStr: String, type: KeywordTypeEnum) : NativeQueryBuilder {
        val termQuery = term().field(Keyword::type.name).value(type.name).build()._toQuery()
        val prefixQuery =  matchBoolPrefix().field(Keyword::type.name).query(queryStr).build()._toQuery()
        val phraseQuery = matchPhrase().field(Keyword::value.name).query(queryStr).build()._toQuery()
        val bq = bool()
            .must(termQuery)
            .should(prefixQuery)
            .should(phraseQuery)
            .build()
            ._toQuery()
        return NativeQueryBuilder()
            .withPageable(createPageable(20))
            .withSort(createCaseInsensitiveSortOptions())
            .withQuery(bq)
    }

    private fun createNativeQuery_retrieveAll(type: KeywordTypeEnum) : NativeQueryBuilder {
        val MATCH_ALL =  matchAll().build()._toQuery()
        val termQuery = term().field(Keyword::type.name).value(type.name).build()._toQuery()
        val bq = bool()
            .must(termQuery)
            .should(MATCH_ALL)
            .build()
            ._toQuery()
        return NativeQueryBuilder()
            .withPageable(createPageable(1000))
            .withSort(createCaseInsensitiveSortOptions())
            .withQuery(bq)
    }

    private fun createPageable(limit: Int) : OffsetPageable {
        return OffsetPageable(0, 20, null)
    }

    private fun createCaseInsensitiveSortOptions() : SortOptions {
        val fieldSort = FieldSort.Builder().field("${Keyword::value.name}$SORT_INSENSITIVE").order(SortOrder.Asc).build()
        return SortOptions
            .Builder()
            .field(fieldSort)
            .build()
    }
}



@Configuration
@EnableElasticsearchRepositories("edu.wgu.osmt.keyword")
class KeywordEsRepoConfig

interface KeywordEsRepo : ElasticsearchRepository<Keyword, Int>, CustomKeywordRepository
