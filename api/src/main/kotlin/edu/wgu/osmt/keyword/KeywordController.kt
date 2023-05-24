package edu.wgu.osmt.keyword

import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.ApiFilteredSearch
import edu.wgu.osmt.api.model.ApiKeyword
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.KeywordSortEnum
import edu.wgu.osmt.api.model.SkillSortEnum
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.elasticsearch.PaginatedLinks
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillKeywords
import edu.wgu.osmt.security.OAuthHelper
import edu.wgu.osmt.task.TaskMessageService
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.wrapAsExpression
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class KeywordController @Autowired constructor(
    val keywordRepository: KeywordRepository,
    val richSkillEsRepo: RichSkillEsRepo,
    val taskMessageService: TaskMessageService,
    val appConfig: AppConfig,
    val oAuthHelper: OAuthHelper
) {

    @GetMapping(path = [RoutePaths.Latest.CATEGORY_LIST, RoutePaths.Unversioned.CATEGORY_LIST],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allCategoriesPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        size: Int,
        from: Int,
        sort: String?,
    ): HttpEntity<List<ApiKeyword>> {
        return allPaginated(
            keywordType = KeywordTypeEnum.Category,
            uriComponentsBuilder = uriComponentsBuilder,
            path = RoutePaths.Latest.CATEGORY_LIST,
            size = size,
            from = from,
            sort = sort,
        )
    }

    @GetMapping(path = [RoutePaths.Latest.CATEGORY_DETAIL, RoutePaths.Unversioned.CATEGORY_DETAIL],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun categoryById(
        @PathVariable identifier: String
    ): ApiKeyword? {
        val id: Long = identifier.toLong()
        return this.byId(KeywordTypeEnum.Category, id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping(path = [RoutePaths.Latest.CATEGORY_SKILLS, RoutePaths.Unversioned.CATEGORY_SKILLS],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getCategorySkills (
        uriComponentsBuilder: UriComponentsBuilder,
        @PathVariable identifier: String,
        @RequestParam(required = false, defaultValue = PaginationDefaults.size.toString()) size: Int,
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: Array<String>,
        @RequestParam(required = false) sort: String?,
        @AuthenticationPrincipal user: Jwt? = null
    ): HttpEntity<List<RichSkillDoc>> {
        return searchCategorySkills(
            uriComponentsBuilder = uriComponentsBuilder,
            identifier = identifier,
            size = size,
            from = from,
            status = status,
            sort = sort,
            user = user,
        )
    }

    @PostMapping(path = [RoutePaths.Latest.CATEGORY_SKILLS, RoutePaths.Unversioned.CATEGORY_SKILLS],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchCategorySkills (
            uriComponentsBuilder: UriComponentsBuilder,
            @PathVariable identifier: String,
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

        val id: Long = identifier.toLong()
        val keyword = keywordRepository.findById(id)

        if (keyword?.type != KeywordTypeEnum.Category) throw ResponseStatusException(HttpStatus.NOT_FOUND)

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

    private fun allPaginated(
        keywordType: KeywordTypeEnum,
        uriComponentsBuilder: UriComponentsBuilder,
        path: String,
        size: Int,
        from: Int,
        sort: String?,
    ): HttpEntity<List<ApiKeyword>> {
        val sortEnum: KeywordSortEnum = KeywordSortEnum.Companion.forValueOrDefault(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)
        val totalKeywords = keywordRepository.findByType(keywordType).count().toInt()

        val query = keywordRepository.findByType(keywordType)

        when (sortEnum) {
            KeywordSortEnum.KeywordAsc -> query.orderBy(KeywordTable.value to SortOrder.ASC)

            KeywordSortEnum.KeywordDesc -> query.orderBy(KeywordTable.value to SortOrder.DESC)

            KeywordSortEnum.SkillCountAsc -> query.orderBy(
                wrapAsExpression<Int>(
                    RichSkillKeywords
                        .slice(RichSkillKeywords.keywordId.count())
                        .select { KeywordTable.id eq RichSkillKeywords.keywordId }
                ) to SortOrder.ASC)

            KeywordSortEnum.SkillCountDesc -> query.orderBy(
                wrapAsExpression<Int>(
                    RichSkillKeywords
                        .slice(RichSkillKeywords.richSkillId.count())
                        .select { KeywordTable.id eq RichSkillKeywords.keywordId }
                ) to SortOrder.DESC)
        }

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", totalKeywords.toString())

        uriComponentsBuilder
            .path(path)
            .queryParam(RoutePaths.QueryParams.FROM, from)
            .queryParam(RoutePaths.QueryParams.SIZE, size)
            .queryParam(RoutePaths.QueryParams.SORT, sort)

        PaginatedLinks(pageable, totalKeywords, uriComponentsBuilder).addToHeaders(responseHeaders)

        return ResponseEntity.status(200)
            .headers(responseHeaders)
            .body(query.limit(size, from.toLong()).map { ApiKeyword.fromDao(it) })
    }

    private fun byId(
        keywordType: KeywordTypeEnum,
        id: Long,
    ): ApiKeyword? {
        val keyword = keywordRepository.findById(id)
        return if (keyword?.type == keywordType) ApiKeyword.fromDao(keyword) else null
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
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

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
            .path(RoutePaths.Latest.SEARCH_SKILLS)
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
