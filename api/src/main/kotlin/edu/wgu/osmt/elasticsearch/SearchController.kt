package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiSearchQuery
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.SearchService.Companion.DEFAULT_PAGESIZE
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@Controller
@Transactional
class SearchController @Autowired constructor(
    val elasticsearchService: SearchService
) {
    // TODO return headers from API spec
    @PostMapping("/api/collections/search")
    @ResponseBody()
    fun searchCollections(
        @RequestParam(required = false, defaultValue = DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(required = false, defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET) status: Array<String>,
        @RequestParam(required = false, defaultValue = "category.asc") sort: String,
        @RequestBody apiSearchQuery: ApiSearchQuery
    ): List<CollectionDoc> {
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val sortEnum = SortEnum.forApiValue(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)

        return elasticsearchService.searchCollectionsByApiSearchQuery(apiSearchQuery, publishStatuses, pageable)
            .map { it.content }.toList()
    }

    // TODO return headers from API spec
    @PostMapping("/api/skills/search")
    @ResponseBody
    fun searchSkills(
        @RequestParam(required = false, defaultValue = DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(required = false, defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET) status: Array<String>,
        @RequestParam(required = false, defaultValue = "category.asc") sort: String,
        @RequestBody apiSearchQuery: ApiSearchQuery
    ): List<RichSkillDoc> {
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val sortEnum = SortEnum.forApiValue(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)

        val results = elasticsearchService.searchRichSkillsByApiSearchQuery(
            apiSearchQuery,
            publishStatuses,
            pageable
        )

        return results.map { it.content }.toList()
    }
}
