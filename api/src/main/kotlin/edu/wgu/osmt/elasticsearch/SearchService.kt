package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiSearchQuery
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.richskill.RichSkillDoc
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Component

@Component
class SearchService @Autowired constructor(
    val esRichSkillRepository: EsRichSkillRepository,
    val esCollectionRepository: EsCollectionRepository,
    val elasticsearchRestTemplate: ElasticsearchRestTemplate
) {

    fun findCollectionsByNameAndSkillProperties(
        queryString: String,
        pageable: Pageable = PageRequest.of(
            0,
            pageSize,
            Sort.by("name.keyword").descending()
        )
    ): Page<CollectionDoc> {
        val foundCollectionsByName =
            esCollectionRepository.findByName(queryString, Pageable.unpaged()).content.map { it.uuid }

        val collectionIdsByFoundRichSkills = esRichSkillRepository.searchBySkillProperties(
            queryString,
            Pageable.unpaged()
        ).content.flatMap { it.collections.map { it.uuid } }

        val distinctCollections = (foundCollectionsByName + collectionIdsByFoundRichSkills).distinct()

        return if (distinctCollections.isEmpty()) Page.empty() else esCollectionRepository.findAllByUuidIn(
            distinctCollections,
            pageable
        )
    }

    fun searchRichSkillsBySearchQuery(
        apiSearchQuery: ApiSearchQuery,
        publishStatus: Set<PublishStatus>,
        pageable: Pageable
    ): SearchHits<RichSkillDoc> {
        val nsq: NativeSearchQueryBuilder = NativeSearchQueryBuilder().withPageable(pageable)

        // treat the presence of query property to mean multi field search with that term
        if (apiSearchQuery.query != null) {
            nsq.withQuery(
                multiMatchQuery(
                    apiSearchQuery.query,
                    RichSkillDoc::name.name,
                    RichSkillDoc::category.name,
                    RichSkillDoc::statement.name,
                    RichSkillDoc::searchingKeywords.name,
                    RichSkillDoc::jobCodes.name,
                    RichSkillDoc::standards.name,
                    RichSkillDoc::certifications.name,
                    RichSkillDoc::employers.name,
                    RichSkillDoc::alignments.name,
                    "${RichSkillDoc::collections.name}.${CollectionDoc::name.name}"
                )
            )
        } else {
            with(apiSearchQuery) {

                // boolQuery.must for logical AND
                // boolQuery.should for logical OR
                val bq = boolQuery()
                nsq.withQuery(bq)

                skillName?.let { bq.must(matchQuery(RichSkillDoc::name.name, it)) }
                category?.let { bq.must(matchQuery(RichSkillDoc::category.name, it)) }
                skillStatement?.let { bq.must(matchQuery(RichSkillDoc::statement.name, it)) }
                keywords?.let { bq.must(termsQuery(RichSkillDoc::searchingKeywords.name, it)) }

                // TODO how exactly to search by job code?
                occupations?.let {
                    bq.must(
                        boolQuery().should(
                            termsQuery(
                                "${RichSkillDoc::jobCodes.name}.${JobCode::name.name}",
                                it.mapNotNull { it.name })
                        ).should(
                            termsQuery(
                                "${RichSkillDoc::jobCodes.name}.${JobCode::code.name}",
                                it.mapNotNull { it.id })
                        )
                    )
                }

                standards?.let {
                    bq.must(
                        termsQuery(
                            "${RichSkillDoc::standards.name}.${ApiNamedReference::name.name}",
                            it.mapNotNull { it.name })
                    )
                }
                certifications?.let {
                    bq.must(
                        termsQuery(
                            RichSkillDoc::certifications.name,
                            it.mapNotNull { it.name })
                    )
                }
                employers?.let {
                    bq.must(
                        termsQuery(
                            RichSkillDoc::employers.name,
                            it.mapNotNull { it.name })
                    )
                }
                alignments?.let {
                    bq.must(
                        termsQuery(
                            RichSkillDoc::alignments.name,
                            it.mapNotNull { it.name })
                    )
                }
            }
        }


        val filter = BoolQueryBuilder().should(termsQuery(RichSkillDoc::publishStatus.name, publishStatus))
        nsq.withFilter(filter)

        return elasticsearchRestTemplate.search(nsq.build(), RichSkillDoc::class.java)
    }

    companion object {
        // TODO configurable?
        const val pageSize: Int = 50
    }
}
