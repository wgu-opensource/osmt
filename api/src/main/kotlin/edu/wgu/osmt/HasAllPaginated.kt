package edu.wgu.osmt

import edu.wgu.osmt.api.model.ApiSortEnum
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder

/**
 * Provides offset pagination, sorting and filtering by PublishStatus
 * using Elasticsearch repositories that implement [[FindsAllByPublishStatus]].
 * Also generates next, prev paginated link headers in response
 */
interface HasAllPaginated<T> {
    val elasticRepository: FindsAllByPublishStatus<T>

    /**
     * override in `@Controller` implementors with the path that [[allPaginated]]
     * handles
     */
    val allPaginatedPath: String

    fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = SearchService.DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false, defaultValue = "category.asc") sort: String
    ): HttpEntity<List<T>> {

        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val sortEnum = ApiSortEnum.forApiValue(sort)
        val pageable = OffsetPageable(from, size, sortEnum.esSort)

        val searchHits = elasticRepository.findAllFilteredByPublishStatus(publishStatuses, pageable)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchHits.totalHits.toString())

        // build up current uri with path and params
        uriComponentsBuilder
            .path(allPaginatedPath)
            .queryParam(SearchController.Companion.QueryParams.FROM, from)
            .queryParam(SearchController.Companion.QueryParams.SIZE, size)
            .queryParam(SearchController.Companion.QueryParams.SORT, sort)
            .queryParam(SearchController.Companion.QueryParams.STATUS, status.joinToString(",").toLowerCase())

        PaginatedLinks(
            pageable,
            searchHits.totalHits.toInt(),
            uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders)
            .body(searchHits.map { it.content }.toList())
    }
}
