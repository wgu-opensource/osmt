package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import co.elastic.clients.elasticsearch.core.search.InnerHits
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery
import java.util.stream.Collectors


/**
 * Utility class for leveraging latest ElasticSearch v8.7.X Java API
 */
object WguQueryHelper {
    /**
     * Stepping stone to 100% migration to ES v8.7.x apis; see KeywordEsRepo.kt
     */
    fun convertToNativeQuery(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, nsqb: NativeSearchQueryBuilder, msgPrefix: String, log: Logger): Query {
        val oldQuery = nsqb.build()

        val nuQuery = NativeQuery.builder()
            .withFilter(filter)
            .withQuery(StringQuery(oldQuery.query.toString()))
            .withPageable(pageable)
//            .withSort(createSort("blah"))
            .build()
        log.debug(String.Companion.format("\n%s springDataQuery:\n\t\t%s", msgPrefix, (nuQuery.springDataQuery as StringQuery).source))
        log.debug(String.Companion.format("\n%s filter:\n\t\t%s", msgPrefix, nuQuery.filter.toString()))
        return nuQuery
    }

    @Deprecated("Upgrade to ES v8.x queries", ReplaceWith("createQuery"), DeprecationLevel.WARNING )
    fun createStringQuery(msgPrefix: String, nqb: NativeSearchQueryBuilder, log: Logger): Query {
        val query = nqb.build()
        log.debug(String.Companion.format("\n%s query:\n\t\t%s", msgPrefix, query.query.toString()))
        log.debug(String.Companion.format("\n%s filter:\n\t\t%s", msgPrefix, query.filter.toString()))
        //NOTE: this is causing us to lose the filter query
        return StringQuery(query.query.toString())
    }

    fun createQuery(msgPrefix: String, nqb: NativeQueryBuilder, log: Logger): Query {
        val query = nqb.build()
        log.debug(String.Companion.format("\n%s query:\n\t\t%s", msgPrefix, query.query.toString()))
        log.debug(String.Companion.format("\n%s filter:\n\t\t%s", msgPrefix, query.filter.toString()))
        return query;
    }

    fun createMatchPhrasePrefixDslQuery(fieldName: String, searchStr: String, boostVal : Float? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.matchPhrasePrefix { qb -> qb.field(fieldName).query(searchStr).boost(boostVal) }
    }

    fun createMatchBoolPrefixDslQuery(fieldName: String, searchStr: String, boostVal : Float? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.matchBoolPrefix { qb -> qb.field(fieldName).query(searchStr).boost(boostVal) }
    }

    fun createSimpleQueryDslQuery(fieldName: String, searchStr: String, boostVal : Float? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.simpleQueryString { qb ->
            qb.fields(fieldName).query(searchStr).boost(boostVal).defaultOperator(Operator.And)
        }
    }

    fun createNestedQueryDslQuery(path: String, scoreMode: ChildScoreMode, query: co.elastic.clients.elasticsearch._types.query_dsl.Query? = null, innerHits: InnerHits? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        query ?: QueryBuilders.matchAll { b -> b }
        innerHits ?: InnerHits.Builder().build()
        return QueryBuilders.nested { qb ->
            qb.path(path)
                .scoreMode(ChildScoreMode.Avg)
                .innerHits(innerHits)
                .query(QueryBuilders.matchAll { b -> b })
        }
    }

    fun createTermsDslQuery(fieldName: String, filterValues: List<String>, andFlag: Boolean = true): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        val values = filterValues
            .stream()
            .map { FieldValue.of(it) }
            .collect(Collectors.toList())
        val tqf = TermsQueryField.Builder()
            .value(values)
            .build()
        val terms = QueryBuilders.terms()
            .field(fieldName)
            .terms(tqf)
            .build()
            ._toQuery()
        /* Short hand version https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.10/searching.html
        val terms2 = terms { qb -> qb.field(fieldName).terms(tqf) }
        return bool { qb -> if (andFlag)
                                qb.must(terms2)
                            else
                                qb.should(terms2) }
        */

        return QueryBuilders.bool()
            .let {
                if (andFlag) it.must(terms)
                else         it.should(terms) }
            .build()
            ._toQuery()
    }

    // TODO handle case sensitivity
    fun createSort(fieldName: String, sortOrder: SortOrder = SortOrder.Asc) : SortOptions {
        return SortOptions.Builder().field{f -> f.field(fieldName).order(sortOrder)}.build()
    }
}
