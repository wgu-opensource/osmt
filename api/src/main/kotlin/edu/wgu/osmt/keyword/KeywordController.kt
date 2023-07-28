package edu.wgu.osmt.keyword

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiFilteredSearch
import edu.wgu.osmt.api.model.ApiKeyword
import edu.wgu.osmt.api.model.ApiKeywordUpdate
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.KeywordSortEnum
import edu.wgu.osmt.api.model.SkillSortEnum
import edu.wgu.osmt.api.model.SortOrder
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.elasticsearch.PaginatedLinks
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.security.OAuthHelper
import edu.wgu.osmt.task.RemoveItemTask
import edu.wgu.osmt.task.Task
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class KeywordController @Autowired constructor(
    val keywordRepository: KeywordRepository,
    val keywordEsRepo: KeywordEsRepo,
    val richSkillEsRepo: RichSkillEsRepo,
    val taskMessageService: TaskMessageService,
    val appConfig: AppConfig,
    val oAuthHelper: OAuthHelper,
) {

    @GetMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.KEYWORD_LIST}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam(required = true, defaultValue = "Category") type: String,
        @RequestParam(required = true, defaultValue = "") query: String,
        @RequestParam(required = true, defaultValue = "50") size: Int,
        @RequestParam(required = true, defaultValue = "0") from: Int,
        @RequestParam(required = true, defaultValue = "name.asc") sort: String?,
    ): HttpEntity<List<ApiKeyword>> {
        val sortEnum: SortOrder = KeywordSortEnum.forValueOrDefault(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)
        val keywordType = KeywordTypeEnum.forApiValue(type) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val searchResults = keywordEsRepo.searchKeywordsWithPageable(query, keywordType, pageable)
        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", searchResults.totalHits.toString())

        return ResponseEntity.status(200)
            .headers(responseHeaders)
            .body(searchResults.map { ApiKeyword.fromModel(it.content, appConfig) }.toList())
    }

    @RequestMapping(path = [
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.KEYWORD_DETAIL}"
    ],
        produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(@PathVariable id: String): String {
        System.out.println("here by uuid html view keyword")
        return "forward:${RoutePaths.UNVERSIONED}/metadata/keywords/$id"
    }

    @GetMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.KEYWORD_DETAIL}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun keywordById(
        @PathVariable id: Long,
    ): HttpEntity<ApiKeyword> {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(this.byId(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    }

    @PostMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.KEYWORD_CREATE}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun createKeyword(
        @RequestBody apiKeywordUpdate: ApiKeywordUpdate,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<ApiKeyword> {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(keywordRepository.createFromApi(apiKeywordUpdate)?.let { ApiKeyword(it.toModel(), it.skills.count(), appConfig) })
    }

    @PostMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.KEYWORD_UPDATE}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun updateKeyword(
        @PathVariable id: Long,
        @RequestBody apiKeywordUpdate: ApiKeywordUpdate,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<ApiKeyword> {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(keywordRepository.updateFromApi(
                id,
                apiKeywordUpdate,
                oAuthHelper.readableUserName(user)
            )
                ?.let {
                    ApiKeyword(it.toModel(), it.skills.count(), appConfig)
                }
            )
    }

    @DeleteMapping(path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.KEYWORD_REMOVE}"])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun deleteKeyword(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {

        val task = RemoveItemTask(
            identifier = id.toString(),
            apiResultPath = "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.TASK_DETAIL_BATCH}"
        )
        taskMessageService.enqueueJob(TaskMessageService.removeKeyword, task)
        return Task.processingResponse(task)
    }

    private fun byId(
        id: Long,
    ): ApiKeyword? {
        val found = keywordRepository.findById(id)?.let { ApiKeyword(it.toModel(), it.skills.count(), appConfig) }
        return found
    }

    @PostMapping(
        path = ["${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.KEYWORD_SKILLS}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    fun searchKeywordSkills (
        uriComponentsBuilder: UriComponentsBuilder,
        @PathVariable id: Long,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String? = null,
        @RequestBody(required = false) apiSearch: ApiSearch? = null,
        @AuthenticationPrincipal user: Jwt? = null
    ): HttpEntity<List<RichSkillDoc>> {
        val sortEnum = sort?.let{ SkillSortEnum.forApiValue(it)}

        val keyword = keywordRepository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return searchRelatedSkills(
            uriComponentsBuilder = uriComponentsBuilder,
            keyword = keyword,
            size = size,
            from = from,
            statusFilters = status,
            sort = sortEnum ?: SkillSortEnum.defaultSort,
            apiSearch = apiSearch ?: ApiSearch(),
            user = user
        )
    }

    private fun searchRelatedSkills (
        uriComponentsBuilder: UriComponentsBuilder,
        keyword: KeywordDao,
        size: Int,
        from: Int,
        statusFilters: Array<String>,
        sort: SkillSortEnum,
        apiSearch: ApiSearch,
        user: Jwt?
    ): HttpEntity<List<RichSkillDoc>> {

        val pageable = OffsetPageable(offset = from, limit = size, sort = sort.sort)
        val statuses = statusFilters.mapNotNull { PublishStatus.forApiValue(it) }.toMutableSet()

        if (user == null) {
            statuses.remove(PublishStatus.Deleted)
            statuses.remove(PublishStatus.Draft)
        }

        var filterAlignments = apiSearch.filtered?.alignments
        var filterAuthors = apiSearch.filtered?.authors
        var filterCategories = apiSearch.filtered?.categories
        var filterCertifications = apiSearch.filtered?.certifications
        var filterEmployers = apiSearch.filtered?.employers
        var filterKeywords = apiSearch.filtered?.keywords
        var filterStandards = apiSearch.filtered?.standards

        keyword.value?.let {
            when(keyword.type) {
                KeywordTypeEnum.Alignment -> {
                    filterAlignments = filterAlignments?.plus(listOf(it)) ?: listOf(it)
                }
                KeywordTypeEnum.Author -> {
                    filterAuthors = filterAuthors?.plus(listOf(it)) ?: listOf(it)
                }
                KeywordTypeEnum.Category -> {
                    filterCategories = filterCategories?.plus(listOf(it)) ?: listOf(it)
                }
                KeywordTypeEnum.Certification -> {
                    filterCertifications = filterCertifications?.plus(listOf(it)) ?: listOf(it)
                }
                KeywordTypeEnum.Employer -> {
                    filterEmployers = filterEmployers?.plus(listOf(it)) ?: listOf(it)
                }
                KeywordTypeEnum.Keyword ->  {
                    filterKeywords = filterKeywords?.plus(listOf(it)) ?: listOf(it)
                }
                KeywordTypeEnum.Standard -> {
                    filterStandards = filterStandards?.plus(listOf(it)) ?: listOf(it)
                }
            }
        }

        val search = ApiSearch (
            query = apiSearch.query,
            advanced = apiSearch.advanced,
            uuids = apiSearch.uuids,
            filtered = ApiFilteredSearch(
                alignments = filterAlignments,
                authors = filterAuthors,
                categories = filterCategories,
                certifications = filterCertifications,
                jobCodes = apiSearch.filtered?.jobCodes,
                keywords = filterKeywords,
                standards = filterStandards,
            )
        )

        val countByApiSearch = richSkillEsRepo.countByApiSearch(search, statuses, pageable)
        val searchHits = richSkillEsRepo.byApiSearch(search, statuses, pageable)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", countByApiSearch.toString())

        uriComponentsBuilder
            .path(RoutePaths.SEARCH_SKILLS)
            .queryParam(RoutePaths.QueryParams.FROM, from)
            .queryParam(RoutePaths.QueryParams.SIZE, size)
            .queryParam(RoutePaths.QueryParams.SORT, sort)
            .queryParam(RoutePaths.QueryParams.STATUS, statusFilters.joinToString(",").lowercase())

        PaginatedLinks(
            pageable,
            searchHits.totalHits.toInt(),
            uriComponentsBuilder
        ).addToHeaders(responseHeaders)

        return ResponseEntity.status(200).headers(responseHeaders)
            .body(searchHits.map { it.content }.toList())
    }

}
