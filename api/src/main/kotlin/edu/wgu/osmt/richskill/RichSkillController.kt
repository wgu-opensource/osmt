package edu.wgu.osmt.richskill

import edu.wgu.osmt.HasAllPaginated
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.RoutePaths.SKILLS_FILTER
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditLogSortEnum
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.elasticsearch.PaginatedLinks
import edu.wgu.osmt.io.csv.RichSkillCsvExport
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.security.OAuthHelper
import edu.wgu.osmt.task.*
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class RichSkillController @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val taskMessageService: TaskMessageService,
    val richSkillEsRepo: RichSkillEsRepo,
    val auditLogRepository: AuditLogRepository,
    val appConfig: AppConfig,
    val oAuthHelper: OAuthHelper
): HasAllPaginated<RichSkillDoc> {
    override val elasticRepository = richSkillEsRepo

    val keywordDao = KeywordDao.Companion

    override val allPaginatedPath: String = RoutePaths.SKILLS_LIST
    override val sortOrderCompanion = SkillSortEnum.Companion

    @GetMapping(RoutePaths.SKILLS_LIST, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    override fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        size: Int,
        from: Int,
        status: Array<String>,
        sort: String?,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<RichSkillDoc>> {
        if (!appConfig.allowPublicLists && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }
        return super.allPaginated(uriComponentsBuilder, size, from, status, sort, user)
    }

    @GetMapping(RoutePaths.V2_SKILLS_LIST, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allPaginatedV2(
            uriComponentsBuilder: UriComponentsBuilder,
            size: Int,
            from: Int,
            status: Array<String>,
            sort: String?,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<RichSkillDocV2>> {
        if (!appConfig.allowPublicLists && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }
        val paginated = super.allPaginated(uriComponentsBuilder, size, from, status, sort, user)
        val v2Body = paginated.body?.map { RichSkillDocV2.fromCurrent(it) }
        return ResponseEntity.status(200).headers(paginated.headers).body(v2Body)
    }

    @PostMapping(SKILLS_FILTER, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allPaginatedWithFilters(
        uriComponentsBuilder: UriComponentsBuilder,
        size: Int,
        from: Int,
        status: Array<String>,
        @RequestBody apiSearch: ApiSearch,
        sort: String?,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<RichSkillDoc>> {

        val publishStatuses = status.mapNotNull {
            val status = PublishStatus.forApiValue(it)
            if (user == null && (status == PublishStatus.Deleted  || status == PublishStatus.Draft)) null else status
        }.toSet()
        val sortEnum: SortOrder = sortOrderCompanion.forValueOrDefault(sort)
        val pageable = OffsetPageable(from, size, sortEnum.sort)
        val searchHits = richSkillEsRepo.byApiSearch(
            apiSearch,
            publishStatuses,
            pageable,
            StringUtils.EMPTY
        )
        val countAllFiltered: Long = searchHits.totalHits
        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Total-Count", countAllFiltered.toString())

        uriComponentsBuilder
            .path(SKILLS_FILTER)
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

    @PostMapping(path = [RoutePaths.SKILLS_CREATE, RoutePaths.V2_SKILLS_CREATE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createSkills(
        @RequestBody apiSkillUpdates: List<ApiSkillUpdate>,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val task = CreateSkillsTask(apiSkillUpdates, oAuthHelper.readableUserName(user), oAuthHelper.readableUserIdentifier(user))
        taskMessageService.enqueueJob(TaskMessageService.createSkills, task)
        return Task.processingResponse(task)
    }

    @GetMapping(RoutePaths.SKILL_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUID(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): ApiSkill? {
        return richSkillRepository.findByUUID(uuid)?.let {
            if (user == null && it.publishStatus() == PublishStatus.Unarchived) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }

            ApiSkill.fromDao(it, appConfig)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping(RoutePaths.V2_SKILL_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUIDv2(
            @PathVariable uuid: String,
            @AuthenticationPrincipal user: Jwt?
    ): ApiSkillV2? {
        return richSkillRepository.findByUUID(uuid)?.let {
            if (user == null && it.publishStatus() == PublishStatus.Unarchived) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }

            ApiSkillV2.fromDao(it, appConfig)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping(path = [RoutePaths.SKILL_DETAIL, RoutePaths.V2_SKILL_DETAIL], produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): String {
        return richSkillRepository.findByUUID(uuid)?.let {
            if (user == null && it.publishStatus() == PublishStatus.Unarchived) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }

            "forward:/skills/$uuid"
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping(path = [RoutePaths.SKILL_DETAIL, RoutePaths.V2_SKILL_DETAIL], produces = ["text/csv"])
    fun byUUIDCsvView(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<*> {
        return richSkillRepository.findByUUID(uuid)?.let {
            if (user == null && it.publishStatus() == PublishStatus.Unarchived) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }

            val skill = it.toModel()
            val collections = it.collections.map { it.toModel() }.toSet()
            val result = RichSkillCsvExport(appConfig).toCsv(listOf(RichSkillAndCollections(skill, collections)))
            val responseHeaders = HttpHeaders()
            responseHeaders.add("Content-Type", "text/csv")
            return ResponseEntity.ok().headers(responseHeaders).body(result)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping(RoutePaths.SKILL_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateSkill(
        @PathVariable uuid: String,
        @RequestBody skillUpdate: ApiSkillUpdate, @AuthenticationPrincipal user: Jwt?
    ): ApiSkill {

        if (oAuthHelper.hasRole(appConfig.roleCurator) && !oAuthHelper.isArchiveRelated(skillUpdate.publishStatus)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }

        val existingSkill = richSkillRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updatedSkill =
            richSkillRepository.updateFromApi(existingSkill.id.value, skillUpdate, oAuthHelper.readableUserName(user), oAuthHelper.readableUserIdentifier(user))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiSkill.fromDao(updatedSkill, appConfig)
    }

    @PostMapping(RoutePaths.V2_SKILL_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateSkillV2(
            @PathVariable uuid: String,
            @RequestBody skillUpdate: ApiSkillUpdate, @AuthenticationPrincipal user: Jwt?
    ): ApiSkillV2 {

        if (oAuthHelper.hasRole(appConfig.roleCurator) && !oAuthHelper.isArchiveRelated(skillUpdate.publishStatus)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }

        val existingSkill = richSkillRepository.findByUUID(uuid)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updatedSkill =
                richSkillRepository.updateFromApi(existingSkill.id.value, skillUpdate, oAuthHelper.readableUserName(user), oAuthHelper.readableUserIdentifier(user))
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiSkillV2.fromDao(updatedSkill, appConfig)
    }

    @PostMapping(path = [RoutePaths.SKILL_PUBLISH, RoutePaths.V2_SKILL_PUBLISH], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun publishSkills(
        @RequestBody search: ApiSearch,
        @RequestParam(
            required = false,
            defaultValue = "Published"
        ) newStatus: String,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) filterByStatus: List<String>,
        @RequestParam(
            required = false,
            defaultValue = ""
        ) collectionUuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val filterStatuses = filterByStatus.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val publishStatus = PublishStatus.forApiValue(newStatus) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val task = PublishTask(
            AppliesToType.Skill,
            search,
            filterByStatus=filterStatuses,
            publishStatus = publishStatus,
            userString = oAuthHelper.readableUserName(user),
            collectionUuid = if (collectionUuid.isNullOrBlank()) null else collectionUuid
        )

        taskMessageService.enqueueJob(TaskMessageService.publishSkills, task)
        return Task.processingResponse(task)
    }

    @GetMapping(path = [RoutePaths.SKILL_AUDIT_LOG, RoutePaths.V2_SKILL_AUDIT_LOG], produces = ["application/json"])
    fun skillAuditLog(
        @PathVariable uuid: String
    ): HttpEntity<List<AuditLog>> {

        val pageable = OffsetPageable(0, Int.MAX_VALUE, AuditLogSortEnum.forValueOrDefault(AuditLogSortEnum.DateDesc.apiValue).sort)

        val skill = richSkillRepository.findByUUID(uuid)

        val sizedIterable = auditLogRepository.findByTableAndId(RichSkillDescriptorTable.tableName, entityId = skill!!.id.value, offsetPageable = pageable)
        return ResponseEntity.status(200).body(sizedIterable.toList().map{it.toModel()})
    }

    @Transactional(readOnly = true)
    @GetMapping(path = [RoutePaths.EXPORT_LIBRARY_CSV, RoutePaths.V2_EXPORT_LIBRARY_CSV], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun exportLibraryCsv(
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }
        if (!oAuthHelper.hasRole(appConfig.roleAdmin)) {
            throw GeneralApiException("OSMT user must have an Admin role.", HttpStatus.UNAUTHORIZED)
        }

        val task = CsvTask(collectionUuid = "FullLibrary")
        taskMessageService.enqueueJob(TaskMessageService.skillsForFullLibraryCsv, task)

        return Task.processingResponse(task)
    }

    @Transactional(readOnly = true)
    @GetMapping(RoutePaths.EXPORT_LIBRARY_XLSX, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun exportLibraryXlsx(
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }
        if (!oAuthHelper.hasRole(appConfig.roleAdmin)) {
            throw GeneralApiException("OSMT user must have an Admin role.", HttpStatus.UNAUTHORIZED)
        }

        val task = XlsxTask(collectionUuid = "FullLibrary")
        taskMessageService.enqueueJob(TaskMessageService.skillsForFullLibraryXlsx, task)

        return Task.processingResponse(task)
    }

    @Transactional(readOnly = true)
    @PostMapping(RoutePaths.EXPORT_SKILLS_CSV, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun exportCustomListCsv(
        @RequestBody apiSearch: ApiSearch,
        status: Array<String>,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }
        val publishStatuses = status.mapNotNull {
            val status = PublishStatus.forApiValue(it)
            if (user == null && (status == PublishStatus.Deleted  || status == PublishStatus.Draft)) null else status
        }.toSet()
        val task = ExportSkillsToCsvTask(
            collectionUuid = "CustomList", richSkillEsRepo.getUuidsFromApiSearch(apiSearch, publishStatuses, Pageable.unpaged(), user, StringUtils.EMPTY)
        )
        taskMessageService.enqueueJob(TaskMessageService.skillsForCustomListExportCsv, task)

        return Task.processingResponse(task)
    }

    @Transactional(readOnly = true)
    @PostMapping(RoutePaths.V2_EXPORT_SKILLS, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun exportCustomList(
            @RequestBody uuids: List<String>?,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }

        val task = ExportSkillsToCsvTask(collectionUuid = "CustomList", uuids)
        taskMessageService.enqueueJob(TaskMessageService.skillsForCustomListExportCsv, task)

        return Task.processingResponse(task)
    }

    @Transactional(readOnly = true)
    @PostMapping(RoutePaths.EXPORT_SKILLS_XLSX, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun exportCustomListXlsx(
        @RequestBody apiSearch: ApiSearch,
        status: Array<String>,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        if (!appConfig.allowPublicSearching && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }
        val publishStatuses = status.mapNotNull {
            val status = PublishStatus.forApiValue(it)
            if (user == null && (status == PublishStatus.Deleted  || status == PublishStatus.Draft)) null else status
        }.toSet()
        val task = ExportSkillsToXlsxTask(collectionUuid = "CustomList", richSkillEsRepo.getUuidsFromApiSearch(apiSearch, publishStatuses, Pageable.unpaged(), user, StringUtils.EMPTY))
        taskMessageService.enqueueJob(TaskMessageService.skillsForCustomListExportXlsx, task)

        return Task.processingResponse(task)
    }
}
