package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.CollectionSortEnum
import edu.wgu.osmt.api.model.SkillSortEnum
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.collection.CollectionSearchService
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillSearchService.Companion.DEFAULT_PAGESIZE
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeSearchService
import edu.wgu.osmt.keyword.KeywordSearchService
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillSearchService
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
    val richSkillSearchService: RichSkillSearchService,
    val collectionSearchService: CollectionSearchService,
    val keywordSearchService: KeywordSearchService,
    val jobCodeSearchService: JobCodeSearchService,
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
            collectionSearchService.byApiSearch(apiSearch, publishStatuses, pageable)

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

        val searchHits = richSkillSearchService.byApiSearch(
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

    @PostMapping(RoutePaths.COLLECTION_SKILLS, produces = [MediaType.APPLICATION_JSON_VALUE])
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
        @PathVariable uuid: String,
        @RequestBody apiSearch: ApiSearch
    ): HttpEntity<List<RichSkillDoc>> {
        return searchSkills(uriComponentsBuilder, size, from, status, sort, uuid, apiSearch)
    }

    @GetMapping(RoutePaths.SEARCH_JOBCODES_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchJobCodes(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = true) query: String
    ): HttpEntity<List<JobCode>> {
        val searchResults = jobCodeSearchService.jobCodeTypeAheadSearch(query)

        return ResponseEntity.status(200).body(searchResults.map { it.content }.toList())
    }

    @GetMapping(RoutePaths.SEARCH_KEYWORDS_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchKeywords(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = true) query: String,
        @RequestParam(required = true) type: String
    ): HttpEntity<List<String>> {
        val searchResults = keywordSearchService.keywordTypeAheadSearch(query, KeywordTypeEnum.valueOf(type.toLowerCase().capitalize()))

        return ResponseEntity.status(200).body(searchResults.mapNotNull { it.content.value }.toList())
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
