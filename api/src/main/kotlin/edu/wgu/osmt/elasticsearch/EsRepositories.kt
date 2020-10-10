package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface EsRichSkillRepository : ElasticsearchRepository<RichSkillDoc, Int> {
    fun findByUuid(
        uuid: String,
        pageable: Pageable = PageRequest.of(0, Queries.pageSize, Sort.by("name.keyword").descending())
    ): Page<RichSkillDoc>

    @Query(
        """
        {"multi_match": {
            "query": "?0",
            "fields": [
                "collections.name",
                "author",
                "name",
                "category",
                "statement",
                "searchingKeywords",
                "jobCodes",
                "standards",
                "employers",
                "alignments"
            ]
        }}
    """
    )
    fun searchByCollectionNameOrSkillProperties(
        q: String,
        pageable: Pageable = PageRequest.of(0, Queries.pageSize, Sort.by("name.keyword").descending())
    ): Page<RichSkillDoc>
}

interface EsKeywordRespository : ElasticsearchRepository<Keyword, Int> {

    fun findByValueAndType(value: String, type: KeywordTypeEnum): Keyword
}

interface EsCollectionRepository : ElasticsearchRepository<CollectionDoc, Int> {
    fun findByUuid(uuid: String, pageable: Pageable): Page<CollectionDoc>

    fun findAllByUuidIn(
        uuids: List<String>,
        pageable: Pageable = PageRequest.of(0, Queries.pageSize, Sort.by("name.keyword").descending())
    ): Page<CollectionDoc>

    fun findByName(q: String, pageable: Pageable = PageRequest.of(0, Queries.pageSize)): Page<CollectionDoc>
}

object Queries {
    // TODO configurable?
    const val pageSize: Int = 50
}

