package edu.wgu.osmt.richskill

import edu.wgu.osmt.HasAllPaginated
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditLogSortEnum
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.security.OAuth2Helper.readableUsername
import edu.wgu.osmt.task.*
import org.springframework.beans.factory.annotation.Autowired
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
    val appConfig: AppConfig
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

    @PostMapping(RoutePaths.SKILLS_CREATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createSkills(
        @RequestBody apiSkillUpdates: List<ApiSkillUpdate>,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val task = CreateSkillsTask(apiSkillUpdates)
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

    @RequestMapping(RoutePaths.SKILL_DETAIL, produces = [MediaType.TEXT_HTML_VALUE])
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

    @RequestMapping(RoutePaths.SKILL_DETAIL, produces = ["text/csv"])
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
        @RequestBody skillUpdate: ApiSkillUpdate,
        @AuthenticationPrincipal user: Jwt?
    ): ApiSkill {
        val existingSkill = richSkillRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (existingSkill.importedFrom != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Imported Skills are Read Only")
        }

        val updatedSkill =
            richSkillRepository.updateFromApi(existingSkill.id.value, skillUpdate, readableUsername(user))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiSkill.fromDao(updatedSkill, appConfig)
    }

    @PostMapping(RoutePaths.SKILL_SHARE_EXTERNALLY, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun shareSkillExternally(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): ApiSkill {
        val existingSkill = richSkillRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (existingSkill.importedFrom != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Imported Skills are Read Only")
        }

        val updatedSkill =
            richSkillRepository.updateIsExternallyShared(existingSkill.id.value, true, readableUsername(user))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiSkill.fromDao(updatedSkill, appConfig)
    }

    @PostMapping(RoutePaths.SKILL_UNSHARE_EXTERNALLY, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun unshareSkillExternally(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): ApiSkill {
        val existingSkill = richSkillRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updatedSkill =
            richSkillRepository.updateIsExternallyShared(existingSkill.id.value, false, readableUsername(user))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiSkill.fromDao(updatedSkill, appConfig)
    }

    @PostMapping(RoutePaths.SKILL_PUBLISH, produces = [MediaType.APPLICATION_JSON_VALUE])
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
            userString = readableUsername(user),
            collectionUuid = if (collectionUuid.isNullOrBlank()) null else collectionUuid
        )

        taskMessageService.enqueueJob(TaskMessageService.publishSkills, task)
        return Task.processingResponse(task)
    }

    @GetMapping(RoutePaths.SKILL_AUDIT_LOG, produces = ["application/json"])
    fun skillAuditLog(
        @PathVariable uuid: String
    ): HttpEntity<List<AuditLog>> {
        val pageable = OffsetPageable(0, Int.MAX_VALUE, AuditLogSortEnum.forValueOrDefault(AuditLogSortEnum.DateDesc.apiValue).sort)

        val skill = richSkillRepository.findByUUID(uuid)

        val sizedIterable = auditLogRepository.findByTableAndId(RichSkillDescriptorTable.tableName, entityId = skill!!.id.value, offsetPageable = pageable)
        return ResponseEntity.status(200).body(sizedIterable.toList().map{it.toModel()})
    }


    @PostMapping(RoutePaths.SKILL_IMPORT, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun importSkill(
        @RequestBody skillReference: ApiNamedReference,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val skillUrl = skillReference.id ?:  throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val task = ShareExternallyTask(
                canonicalUrl = skillUrl,
                libraryName = skillReference.name,
                userString = readableUsername(user))
        taskMessageService.enqueueJob(TaskMessageService.importSkills, task)
        return Task.processingResponse(task)
    }

    @PostMapping(RoutePaths.SKILL_REMOVE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun removeSkill(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): ApiBatchResult {
        val existingSkill = richSkillRepository.findByUUID(uuid)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        /* can only remove readonly imported skills */
        if (existingSkill.importedFrom == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        val skillDoc = RichSkillDoc.fromDao(existingSkill, appConfig)
        richSkillEsRepo.delete(skillDoc)
        existingSkill.delete()

        return ApiBatchResult(success=true, modifiedCount=1, totalCount=1)
    }
}
