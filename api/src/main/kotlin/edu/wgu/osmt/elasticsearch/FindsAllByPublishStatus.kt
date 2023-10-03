package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDoc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.query.StringQuery
import java.util.stream.Collectors

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
        var filter = createTermsQuery(false, RichSkillDoc::publishStatus.name, filterValues)
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
     * Useful utility method to creat TermsQuery
     */
    fun createTermsQuery(andFlag: Boolean, fieldName: String, filterValues: List<String>): co.elastic.clients.elasticsearch._types.query_dsl.Query? {
        val values = filterValues
                        .stream()
                        .map { FieldValue.of(it) }
                        .collect(Collectors.toList())
        val tqf = TermsQueryField.Builder()
                        .value(values)
                        .build()
        val terms = terms()
                        .field(RichSkillDoc::publishStatus.name)
                        .terms(tqf)
                        .build()
                        ._toQuery()
        return bool()
                .let { if (andFlag) it.must(terms)
                       else         it.should(terms) }
                .build()
                ._toQuery()
    }

    fun createTermsQuery(fieldName: String, filterValues: List<String>): co.elastic.clients.elasticsearch._types.query_dsl.Query? {
        return createTermsQuery(true, fieldName, filterValues)
    }

    @Deprecated("", ReplaceWith("createQuery"), DeprecationLevel.WARNING)
    fun createStringQuery(msgPrefix: String, nqb: NativeSearchQueryBuilder, log: Logger): Query {
        val query = nqb.build()
        log.debug(String.Companion.format("\n%s query:\n\t\t%s", msgPrefix, query.query.toString()))
        log.debug(String.Companion.format("\n%s filter:\n\t\t%s", msgPrefix, query.filter.toString()))
        //NOTE: this is causing us to lose the filter query
        return StringQuery(query.query.toString())
    }
}
