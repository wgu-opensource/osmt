package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class SearchService @Autowired constructor(
    val esRichSkillRepository: EsRichSkillRepository,
    val esCollectionRepository: EsCollectionRepository
) {

    fun findCollectionsByNameAndSkillProperties(
        queryString: String,
        pageable: Pageable = PageRequest.of(
            0,
            Queries.pageSize,
            Sort.by("name.keyword").descending()
        )
    ): Page<CollectionDoc> {
        val collectionIds = esRichSkillRepository.searchByCollectionNameOrSkillProperties(
            queryString,
            Pageable.unpaged()
        ).content.flatMap { it.collections.map { it.uuid } }
        return if (collectionIds.isEmpty()) Page.empty() else esCollectionRepository.findAllByUuidIn(
            collectionIds,
            pageable
        )
    }
}
