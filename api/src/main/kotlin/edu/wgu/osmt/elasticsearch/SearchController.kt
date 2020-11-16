package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.CollectionSortEnum
import edu.wgu.osmt.api.model.SkillSortEnum
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.SearchService.Companion.DEFAULT_PAGESIZE
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class SearchController @Autowired constructor(
    val elasticsearchService: SearchService,
    val appConfig: AppConfig
) {

    @PostMapping(RoutePaths.SEARCH_COLLECTIONS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchCollections(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @RequestBody apiSearch: ApiSearch
    ): HttpEntity<List<CollectionDoc>> {
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val sortEnum: CollectionSortEnum = CollectionSortEnum.forValueOrDefault(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)

        val searchHits =
            elasticsearchService.searchCollectionsByApiSearch(apiSearch, publishStatuses, pageable)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchHits.totalHits.toString())

        // build up current uri with path and params
        uriComponentsBuilder
            .path(RoutePaths.SEARCH_COLLECTIONS)
            .queryParam(RoutePaths.QueryParams.FROM, from)
            .queryParam(RoutePaths.QueryParams.SIZE, size)
            .queryParam(RoutePaths.QueryParams.STATUS, status.joinToString(",").toLowerCase())
        sort?.let { uriComponentsBuilder.queryParam(RoutePaths.QueryParams.SORT, it) }

        PaginatedLinks(
            pageable,
            searchHits.totalHits.toInt(),
            uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders).body(searchHits.map { it.content }.toList())
    }

    @PostMapping(RoutePaths.SEARCH_SKILLS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchSkills(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) collectionId: String?,
        @RequestBody apiSearch: ApiSearch
    ): HttpEntity<List<RichSkillDoc>> {
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val sortEnum = SkillSortEnum.forValueOrDefault(sort)
        val pageable = OffsetPageable(offset = from, limit = size, sort = sortEnum.sort)

        val searchHits = elasticsearchService.searchRichSkillsByApiSearch(
            apiSearch,
            publishStatuses,
            pageable,
            collectionId
        )

        // build up current uri with path and params
        uriComponentsBuilder
            .path(RoutePaths.SEARCH_SKILLS)
            .queryParam(RoutePaths.QueryParams.FROM, from)
            .queryParam(RoutePaths.QueryParams.SIZE, size)
            .queryParam(RoutePaths.QueryParams.STATUS, status.joinToString(",").toLowerCase())
        sort?.let { uriComponentsBuilder.queryParam(RoutePaths.QueryParams.SORT, it) }
        collectionId?.let { uriComponentsBuilder.queryParam(RoutePaths.QueryParams.COLLECTION_ID, it) }

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchHits.totalHits.toString())

        PaginatedLinks(
            pageable,
            searchHits.totalHits.toInt(),
            uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders)
            .body(searchHits.map { it.content }.toList())
    }

    @GetMapping(RoutePaths.COLLECTION_SKILLS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun collectionSkills(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @PathVariable uuid: String
    ): HttpEntity<List<RichSkillDoc>> {
        return searchSkills(uriComponentsBuilder, size, from, status, sort, uuid, ApiSearch(null, null, null ))
    }
}


class PaginatedLinks(
    pageable: OffsetPageable,
    total: Int,
    uriComponentsBuilder: UriComponentsBuilder
) {
    val QP = RoutePaths.QueryParams
    val hasPrevious = pageable.hasPrevious()
    val hasNext = total > pageable.offset + pageable.limit
    val previousOrLastOffset: Int =
        if (pageable.offset > total) {
            val mod = (total % pageable.limit)
            if (mod == 0) total - pageable.limit else total - mod
        } else {
            pageable.offset - pageable.limit
        }

    val nextLink =
        if (hasNext) "<${
            uriComponentsBuilder.replaceQueryParam(QP.FROM, (pageable.offset + pageable.limit)).toUriString()
        }>; rel=\"next\"" else null

    val prevLink =
        if (hasPrevious) "<${
            uriComponentsBuilder.replaceQueryParam(QP.FROM, previousOrLastOffset).toUriString()
        }>; rel=\"prev\"" else null

    fun addToHeaders(headers: HttpHeaders) {
        val links = listOfNotNull(nextLink, prevLink)
        if (links.isNotEmpty()) {
            headers.add("Link", links.joinToString(","))
        }
    }
}
