package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import co.elastic.clients.elasticsearch.core.search.InnerHits
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDoc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery
import java.util.stream.Collectors

/**
 * This have been partially converted to use the ElasticSearch 8.7.X apis. Need to do full conversion to use
 * the v8.7.x ES Java API client, https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.10/searching.html
 */
interface FindsAllByPublishStatus<T> {
    val elasticSearchTemplate: ElasticsearchTemplate
    val javaClass: Class<T>

    fun findAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): SearchHits<T> {
        val nqb = createQueryBuilder(pageable, publishStatus)
        val query = createQuery("FindsAllByPublishStatus.findAllFilteredByPublishStatus()", nqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.search(query, javaClass)
    }

    fun countAllFilteredByPublishStatus(publishStatus: Set<PublishStatus>, pageable: Pageable): Long {
        val nqb = createQueryBuilder(pageable, publishStatus)
        val query = createQuery("FindsAllByPublishStatus.countAllFilteredByPublishStatus()", nqb, LoggerFactory.getLogger(FindsAllByPublishStatus::class.java))
        return elasticSearchTemplate.count(query, javaClass)
    }

    fun createQueryBuilder(pageable: Pageable, publishStatus: Set<PublishStatus>): NativeQueryBuilder {
        val MATCH_ALL = matchAll().build()._toQuery()
        var filterValues = publishStatus
                            .stream()
                            .map { ps -> ps.name}
                            .collect(Collectors.toList())
        var filter = createTermsDslQuery( RichSkillDoc::publishStatus.name, filterValues, false)
        return NativeQueryBuilder()
                    .withPageable(pageable)
                    .withQuery(MATCH_ALL)
                    .withFilter(filter)
    }

    fun createQuery(msgPrefix: String, nqb: NativeQueryBuilder, log: Logger): Query {
        val query = nqb.build()
        log.debug(String.Companion.format("\n%s query:\n\t\t%s", msgPrefix, query.query.toString()))
        log.debug(String.Companion.format("\n%s filter:\n\t\t%s", msgPrefix, query.filter.toString()))
        return query;
    }

    /**
     * Stepping stone to 100% migration to ES v8.7.x apis; see KeywordEsRepo.kt
     */
    fun convertToNativeQuery(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, nsqb: NativeSearchQueryBuilder, msgPrefix: String, log: Logger): Query {
        val oldQuery = nsqb.build()
        val nuQuery = NativeQuery.builder()
            .withFilter(filter)
            .withQuery(StringQuery(oldQuery.query.toString()))
            .withPageable(pageable)
            .build()
        log.debug(String.Companion.format("\n%s springDataQuery:\n\t\t%s", msgPrefix, (nuQuery.springDataQuery as StringQuery).source))
        log.debug(String.Companion.format("\n%s filter:\n\t\t%s", msgPrefix, nuQuery.filter.toString()))
        return nuQuery
    }

    /*
     * Below methods are all leveraging the latest ElasticSearch v8.7.X Java API
     */
    fun createMatchPhrasePrefixDslQuery(fieldName: String, searchStr: String, boostVal : Float? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return matchPhrasePrefix { qb -> qb.field(fieldName).query(searchStr).boost(boostVal) }
    }

    fun createSimpleQueryDslQuery(fieldName: String, searchStr: String, boostVal : Float? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        return simpleQueryString { qb -> qb.fields(fieldName).query(searchStr).boost(boostVal).defaultOperator(Operator.And) }
    }

    fun createNestQueryDslQuery(path: String, scoreMode: ChildScoreMode, query: co.elastic.clients.elasticsearch._types.query_dsl.Query? = null, innerHits: InnerHits? = null): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        query ?: matchAll { b-> b }
        innerHits ?: InnerHits.Builder().build()
        return nested { qb -> qb.path(path)
                                .scoreMode(ChildScoreMode.Avg)
                                .innerHits(innerHits)
                                .query(matchAll { b-> b }) }
    }

    fun createTermsDslQuery(fieldName: String, filterValues: List<String>, andFlag: Boolean = true): co.elastic.clients.elasticsearch._types.query_dsl.Query {
        val values = filterValues
            .stream()
            .map { FieldValue.of(it) }
            .collect(Collectors.toList())
        val tqf = TermsQueryField.Builder()
            .value(values)
            .build()
        val terms = terms()
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

        return bool()
            .let { if (andFlag) it.must(terms)
            else         it.should(terms) }
            .build()
            ._toQuery()
    }

    fun getCollectionUuidsFromComplexName(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, collectionName: String) : List<String> {
        val query = NativeQuery
            .builder()
            .withFilter(filter)
            .withQuery(createSimpleQueryDslQuery("${CollectionDoc::name.name}.raw", collectionName))
            .withPageable(pageable)
            .build()
        return elasticSearchTemplate
            .search( query, CollectionDoc::class.java )
            .searchHits
            .map { it.content.uuid }
    }

    fun getCollectionUuidsFromName(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, collectionName: String) : List<String> {
        val query = NativeQuery
                            .builder()
                            .withFilter(filter)
                            .withQuery(createMatchPhrasePrefixDslQuery(CollectionDoc::name.name, collectionName))
                            .withPageable(pageable)
                            .build()
        return elasticSearchTemplate
                            .search( query, CollectionDoc::class.java )
                            .searchHits
                            .map { it.content.uuid }
    }

    fun getCollectionFromUuids(pageable: Pageable, filter: co.elastic.clients.elasticsearch._types.query_dsl.Query?, uuids: List<String> ): SearchHits<CollectionDoc> {
        val query = NativeQuery
            .builder()
            .withFilter(filter)
            .withQuery(createTermsDslQuery("_id", uuids))
            .withPageable(pageable)
            .build()
        return elasticSearchTemplate.search(query, CollectionDoc::class.java)
    }
}
