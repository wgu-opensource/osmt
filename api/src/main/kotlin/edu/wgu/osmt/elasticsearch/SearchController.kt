package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiSearchQuery
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import kotlin.streams.toList

// TODO align with API spec once available
@Controller
@Transactional
class SearchController @Autowired constructor(
    val elasticsearchService: SearchService
) {

    @GetMapping("/collection_by_name", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findCollectionsByName(@RequestParam q: String): List<CollectionDoc> {
        val ids = elasticsearchService.esCollectionRepository.findByName(q, Pageable.unpaged()).get().toList()
        return ids
    }

    @GetMapping("/collection_by_name_and_skill_properties", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findCollectionsByNameAndSkillProperties(@RequestParam q: String): List<CollectionDoc> =
        elasticsearchService.findCollectionsByNameAndSkillProperties(q).content

    @PostMapping("/api/skills/search")
    @ResponseBody
    fun searchSkills(
        @RequestParam(required = false, defaultValue = "50") size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam status: Array<String>?,
        @RequestParam(required = false, defaultValue = "category.asc") sort: String,
        @RequestBody apiSearchQuery: ApiSearchQuery
    ): List<RichSkillDoc> {
        val publishStatuses =
            if (!status.isNullOrEmpty()) status.mapNotNull { PublishStatus.forApiValue(it) }
                .toSet() else PublishStatus.values().toSet()

        val sortEnum = SortEnum.forApiValue(sort)

        val pageable = OffsetPageable(from, size, sortEnum.sort)

        println(sortEnum)

        val results = elasticsearchService.searchRichSkillsBySearchQuery(
            apiSearchQuery,
            publishStatuses,
            pageable
        )

        return results.map { it.content }.toList()
    }
}
