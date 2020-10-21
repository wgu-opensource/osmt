package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.api.model.ApiSearchQuery
import edu.wgu.osmt.api.model.ApiSortEnum
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.SearchService.Companion.DEFAULT_PAGESIZE
import edu.wgu.osmt.richskill.RichSkillDoc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
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

    @PostMapping(COLLECTIONS_SEARCH_PATH)
    @ResponseBody
    fun searchCollections(uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false, defaultValue = "category.asc") sort: String,
        @RequestBody apiSearchQuery: ApiSearchQuery
    ): HttpEntity<List<CollectionDoc>> {
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val sortEnum = ApiSortEnum.forApiValue(sort)
        val pageable = OffsetPageable(from, size, sortEnum.esSort)

        val searchHits =
            elasticsearchService.searchCollectionsByApiSearchQuery(apiSearchQuery, publishStatuses, pageable)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchHits.totalHits.toString())

        // build up current uri with path and params
        uriComponentsBuilder
            .path(COLLECTIONS_SEARCH_PATH)
            .queryParam(QueryParams.FROM, from)
            .queryParam(QueryParams.SIZE, size)
            .queryParam(QueryParams.SORT, sort)
            .queryParam(QueryParams.STATUS, status.joinToString(",").toLowerCase())

        PaginatedLinks(
            pageable,
            searchHits.totalHits.toInt(),
            uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders).body(searchHits.map { it.content }.toList())
    }

    @PostMapping(SKILL_SEARCH_PATH)
    @ResponseBody
    fun searchSkills(uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = DEFAULT_PAGESIZE.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false, defaultValue = "category.asc") sort: String,
        @RequestBody apiSearchQuery: ApiSearchQuery
    ): HttpEntity<List<RichSkillDoc>> {
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val sortEnum = ApiSortEnum.forApiValue(sort)
        val pageable = OffsetPageable(offset = from, limit = size, sort = sortEnum.esSort)

        val searchHits = elasticsearchService.searchRichSkillsByApiSearchQuery(
            apiSearchQuery,
            publishStatuses,
            pageable
        )

        // build up current uri with path and params
        uriComponentsBuilder
            .path(SKILL_SEARCH_PATH)
            .queryParam(QueryParams.FROM, from)
            .queryParam(QueryParams.SIZE, size)
            .queryParam(QueryParams.SORT, sort)
            .queryParam(QueryParams.STATUS, status.joinToString(",").toLowerCase())

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

    companion object {
        const val SKILL_SEARCH_PATH = "/api/skills/search"
        const val COLLECTIONS_SEARCH_PATH = "/api/collections/search"

        object QueryParams {
            const val FROM = "from"
            const val SIZE = "size"
            const val STATUS = "status"
            const val SORT = "sort"
        }

    }
}


class PaginatedLinks(
    pageable: OffsetPageable,
    total: Int,
    uriComponentsBuilder: UriComponentsBuilder
) {
    val QP = SearchController.Companion.QueryParams
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
        if (hasNext) "<${uriComponentsBuilder.replaceQueryParam(QP.FROM, (pageable.offset + pageable.limit)).toUriString()}>; rel=\"next\"" else null

    val prevLink =
        if (hasPrevious) "<${uriComponentsBuilder.replaceQueryParam(QP.FROM, previousOrLastOffset).toUriString()}>; rel=\"prev\"" else null

    fun addToHeaders(headers: HttpHeaders) {
        val links = listOfNotNull(nextLink, prevLink)
        if (links.isNotEmpty()) {
            headers.add("Link", links.joinToString(","))
        }
    }
}
