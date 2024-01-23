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
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery
import java.util.stream.Collectors


/**
 * Utility class for leveraging latest ElasticSearch v8.7.X Java API
 */
object OsmtQueryHelper {
    @Deprecated("Upgrade to ES v8.x queries", ReplaceWith("createNativeQuery"), DeprecationLevel.WARNING )
    fun convertToNativeQuery(pageable: Pageable, dslFilter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, nsqb: NativeSearchQueryBuilder, msgPrefix: String, log: Logger): Query {
        val springDataQuery = StringQuery(nsqb.build().query.toString())
        return createNativeQuery(pageable, dslFilter, springDataQuery, msgPrefix, log)
    }

    @Deprecated("Upgrade to ES v8.x queries", ReplaceWith("createNativeQuery"), DeprecationLevel.WARNING )
    fun createNativeQuery(pageable: Pageable, dslFilter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, springDataQuery: Query, msgPrefix: String? = null, log: Logger? = null): NativeQuery {
        val query = NativeQuery
                            .builder()
                            .withFilter(dslFilter)
                            .withQuery(springDataQuery)
                            .withPageable(pageable)
                            .build()
        log(query, msgPrefix, log)
        return query;
    }

    fun createNativeQuery(pageable: Pageable, dslFilter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, dslQuery: co.elastic.clients.elasticsearch._types.query_dsl.Query, msgPrefix: String? = null, log: Logger? = null, sortOptions: SortOptions? = null): NativeQuery {
        val nqb = NativeQuery.builder()

        nqb.withFilter(dslFilter).withQuery(dslQuery).withPageable(pageable)
        sortOptions ?.let{ nqb.withSort(sortOptions) }

        val query = nqb.build()
        log(query, msgPrefix, log)
        return query;
    }

    private fun log(nativeQuery: NativeQuery, msgPrefix: String?, log: Logger?) {
        log?.debug(msgPrefix)
        if (nativeQuery.springDataQuery != null) {
            log?.debug(String.Companion.format("\t\t%s", (nativeQuery.springDataQuery as StringQuery).source))
        }
        if (nativeQuery.filter != null) {
            log?.debug(String.Companion.format("\t\t%s", nativeQuery.filter.toString()))
        }
        if (nativeQuery.query != null) {
            log?.debug(String.Companion.format("\t\t%s", nativeQuery.query.toString()))
        }
        if (nativeQuery.sortOptions != null) {
            log?.debug(String.Companion.format("\t\t%s", nativeQuery.sortOptions.toString()))
        }
    }

    fun createMatchPhrasePrefixDslQuery(fieldName: String, searchStr: String, boostVal : Float? = 1.0f): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.matchPhrasePrefix { qb -> qb.field(fieldName).query(searchStr).boost(boostVal) }
    }

    fun createMatchBoolPrefixDslQuery(fieldName: String, searchStr: String, boostVal : Float? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.matchBoolPrefix { qb -> qb.field(fieldName).query(searchStr).boost(boostVal) }
    }

    fun createPrefixDslQuery(fieldName: String, searchStr: String, boostVal : Float? = 1.0f): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.prefix { qb -> qb.field(fieldName).value(searchStr).boost(boostVal) }
    }
    fun createSimpleQueryDslQuery(fieldName: String, searchStr: String, boostVal : Float? = null, wildCardFlag: Boolean? = null, operatorVal : Operator? = Operator.And): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.simpleQueryString { qb ->
                             qb.fields(fieldName)
                               .query(searchStr)
                               .boost(boostVal)
                               .defaultOperator(operatorVal)
                               .analyzeWildcard(wildCardFlag)
        }
    }

    fun createDisMaxDslQuery(queries: List<co.elastic.clients.elasticsearch._types.query_dsl.Query>, tieBreakerVal : Double? = 0.0, boostVal: Float? = 1.0f ): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return QueryBuilders.disMax() { qb -> qb.queries(queries).tieBreaker(tieBreakerVal).boost(boostVal) }
    }

    //TODO: Finish this in total elasticSearch conversion to 8.X story
    fun createNestedQueryDslQuery(path: String, scoreMode: ChildScoreMode, query: co.elastic.clients.elasticsearch._types.query_dsl.Query? = null, innerHits: InnerHits? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        query ?: QueryBuilders.matchAll { b -> b }
        innerHits ?: InnerHits.Builder().build()
        return QueryBuilders.nested { qb ->   qb.path(path)
                                                .scoreMode(ChildScoreMode.Avg)
                                                .innerHits(innerHits)
                                                .query(QueryBuilders.matchAll { b -> b }) }
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

    fun createSort(fieldName: String, sortOrder: SortOrder = SortOrder.Asc) : SortOptions {
        return SortOptions.Builder().field{f -> f.field(fieldName).order(sortOrder)}.build()
    }
}
