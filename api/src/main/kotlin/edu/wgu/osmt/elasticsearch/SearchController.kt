package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillDocV2
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import kotlin.streams.toList

@Controller
@Transactional
class SearchController @Autowired constructor(
    val keywordEsRepo: KeywordEsRepo,
    val richSkillEsRepo: RichSkillEsRepo,
    val collectionEsRepo: CollectionEsRepo,
    val jobCodeEsRepo: JobCodeEsRepo,
    val appConfig: AppConfig
) {
    @PostMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.SEARCH_COLLECTIONS}",
        "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_COLLECTIONS}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.SEARCH_COLLECTIONS}"
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchCollections(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @RequestBody apiSearch: ApiSearch,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<CollectionDoc>> {
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

        val publishStatuses = status.mapNotNull {
            val status = PublishStatus.forApiValue(it)
            if (user == null && (status == PublishStatus.Deleted  || status == PublishStatus.Draft)) null else status
        }.toSet()
        val sortEnum: CollectionSortEnum = CollectionSortEnum.forValueOrDefault(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)

        val searchHits =
            collectionEsRepo.byApiSearch(apiSearch, publishStatuses, pageable)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchHits.totalHits.toString())

        // build up current uri with path and params
        uriComponentsBuilder
            .path("${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_COLLECTIONS}")
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

    @PostMapping("${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_SKILLS}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchSkills(
            uriComponentsBuilder: UriComponentsBuilder,
            @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
            @RequestParam(required = false, defaultValue = "0") from: Int,
            @RequestParam(
                    required = false,
                    defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
            ) status: Array<String>,
            @RequestParam(required = false) sort: String?,
            @RequestParam(required = false) collectionId: String?,
            @RequestBody apiSearch: ApiSearch,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<RichSkillDoc>> {
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

        val publishStatuses = status.mapNotNull {
            val status = PublishStatus.forApiValue(it)
            if (user == null && (status == PublishStatus.Deleted  || status == PublishStatus.Draft)) null else status
        }.toSet()
        val sortEnum = sort?.let{ SkillSortEnum.forApiValue(it)}
        val pageable = OffsetPageable(offset = from, limit = size, sort = sortEnum?.sort)

        val searchHits = richSkillEsRepo.byApiSearch(
                apiSearch,
                publishStatuses,
                pageable,
                collectionId
        )

        // build up current uri with path and params
        uriComponentsBuilder
                .path("${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_SKILLS}")
                .queryParam(RoutePaths.QueryParams.FROM, from)
                .queryParam(RoutePaths.QueryParams.SIZE, size)
                .queryParam(RoutePaths.QueryParams.STATUS, status.joinToString(",").toLowerCase())
        sort?.let { uriComponentsBuilder.queryParam(RoutePaths.QueryParams.SORT, it) }
        collectionId?.let { uriComponentsBuilder.queryParam(RoutePaths.QueryParams.COLLECTION_ID, it) }

        val countByApiSearch = richSkillEsRepo.countByApiSearch(
                apiSearch,
                publishStatuses,
                pageable,
                collectionId
        )
        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", countByApiSearch.toString())

        PaginatedLinks(
                pageable,
                searchHits.totalHits.toInt(),
                uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders)
                .body(searchHits.map { it.content }.toList())
    }

    @PostMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.SEARCH_SKILLS}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.SEARCH_SKILLS}",
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun legacySearchSkills(
            uriComponentsBuilder: UriComponentsBuilder,
            @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
            @RequestParam(required = false, defaultValue = "0") from: Int,
            @RequestParam(
                    required = false,
                    defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
            ) status: Array<String>,
            @RequestParam(required = false) sort: String?,
            @RequestParam(required = false) collectionId: String?,
            @RequestBody apiSearch: ApiSearch,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<RichSkillDocV2>> {
        val latest = searchSkills(uriComponentsBuilder, size, from, status, sort, collectionId, apiSearch, user)

        return ResponseEntity.status(200).headers(latest.headers)
                .body(latest.body?.stream()?.map { RichSkillDocV2.fromLatest(it as RichSkillDoc) }?.toList())
    }

    @PostMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.COLLECTION_SKILLS}",
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun collectionSkills(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @PathVariable uuid: String,
        @RequestBody apiSearch: ApiSearch,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<RichSkillDoc>> {
        return searchSkills(uriComponentsBuilder, size, from, status, sort, uuid, apiSearch, user)
    }

    @PostMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.COLLECTION_SKILLS}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.COLLECTION_SKILLS}"
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun legacyCollectionSkills(
            uriComponentsBuilder: UriComponentsBuilder,
            @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
            @RequestParam(required = false, defaultValue = "0") from: Int,
            @RequestParam(
                    required = false,
                    defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
            ) status: Array<String>,
            @RequestParam(required = false) sort: String?,
            @PathVariable uuid: String,
            @RequestBody apiSearch: ApiSearch,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<RichSkillDocV2>> {

        return legacySearchSkills(uriComponentsBuilder, size, from, status, sort, uuid, apiSearch, user)
    }


    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.SEARCH_JOBCODES_PATH}",
        "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_JOBCODES_PATH}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.SEARCH_JOBCODES_PATH}"
                       ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchJobCodes(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = true) query: String
    ): HttpEntity<List<ApiJobCode>> {
        val searchResults = jobCodeEsRepo.typeAheadSearch(query)

        return ResponseEntity.status(200).body(searchResults.map { ApiJobCode.fromJobCode(it.content) }.toList())
    }

    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.SEARCH_KEYWORDS_PATH}",
        "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_KEYWORDS_PATH}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.SEARCH_KEYWORDS_PATH}"
                       ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchKeywords(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = true) query: String,
        @RequestParam(required = true) type: String
    ): HttpEntity<List<ApiNamedReference>> {
        val keywordType = KeywordTypeEnum.forApiValue(type) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val searchResults = keywordEsRepo.typeAheadSearch(query, keywordType)

        return ResponseEntity.status(200).body(searchResults.map { ApiNamedReference.fromKeyword(it.content) }.toList())
    }

    @PostMapping("${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_SIMILAR_SKILLS}",
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchSimilarSkills(
            @RequestBody(required = true) apiSimilaritySearch: ApiSimilaritySearch
    ): HttpEntity<List<ApiSkillSummary>> {
        val hits = richSkillEsRepo.findSimilar(apiSimilaritySearch).toList()
        return ResponseEntity.status(200).body(hits.map{ ApiSkillSummary.fromDoc(it.content)})
    }

    @PostMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.SEARCH_SIMILAR_SKILLS}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.SEARCH_SIMILAR_SKILLS}"
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun legacySearchSimilarSkills(
            @RequestBody(required = true) apiSimilaritySearch: ApiSimilaritySearch
    ): HttpEntity<List<ApiSkillSummary>> {

        return ResponseEntity
                .status(200)
                .body(
                        searchSimilarSkills(apiSimilaritySearch).body?.map { ApiSkillSummaryV2.fromLatest(it) }
                )
    }

    @PostMapping(path = [
        "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.SEARCH_SIMILARITIES}",
        "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.SEARCH_SIMILARITIES}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.SEARCH_SIMILARITIES}"
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun similarSkillWarnings(
            @RequestBody(required = true) similarities: Array<ApiSimilaritySearch>
    ): HttpEntity<List<Boolean>> {
        val arrayLimit = 100
        if (similarities.count() > arrayLimit){
            throw GeneralApiException("Request contained more than $arrayLimit objects", HttpStatus.BAD_REQUEST)
        }
        val hits = similarities.map{richSkillEsRepo.findSimilar(it).count() > 0}
        return ResponseEntity.status(200).body(hits)
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
