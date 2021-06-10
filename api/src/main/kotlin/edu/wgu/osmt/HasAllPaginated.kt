package edu.wgu.osmt

import edu.wgu.osmt.api.model.SortOrder
import edu.wgu.osmt.api.model.SortOrderCompanion
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
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
    val sortOrderCompanion: SortOrderCompanion<*>

    fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<T>> {

        val publishStatuses = status.mapNotNull {
            val status = PublishStatus.forApiValue(it)
            if (user == null && (status == PublishStatus.Deleted  || status == PublishStatus.Draft)) null else status
        }.toSet()
        val sortEnum: SortOrder = sortOrderCompanion.forValueOrDefault(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)

        val searchHits = elasticRepository.findAllFilteredByPublishStatus(publishStatuses, pageable)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchHits.totalHits.toString())

        // build up current uri with path and params
        uriComponentsBuilder
            .path(allPaginatedPath)
            .queryParam(RoutePaths.QueryParams.FROM, from)
            .queryParam(RoutePaths.QueryParams.SIZE, size)
            .queryParam(RoutePaths.QueryParams.SORT, sort)
            .queryParam(RoutePaths.QueryParams.STATUS, status.joinToString(",").toLowerCase())

        PaginatedLinks(
            pageable,
            searchHits.totalHits.toInt(),
            uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders)
            .body(searchHits.map { it.content }.toList())
    }
}

object PaginationDefaults {
    const val size: Int = 50
}
