package edu.wgu.osmt.collection

import edu.wgu.osmt.HasAllPaginated
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.RoutePaths.getApiVersionCalled
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.auditlog.AuditLogSortEnum
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.config.DEFAULT_WORKSPACE_NAME
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.security.OAuthHelper
import edu.wgu.osmt.task.*
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class CollectionController @Autowired constructor(
        val collectionRepository: CollectionRepository,
        val richSkillRepository: RichSkillRepository,
        val taskMessageService: TaskMessageService,
        val auditLogRepository: AuditLogRepository,
        val collectionEsRepo: CollectionEsRepo,
        val appConfig: AppConfig,
        val oAuthHelper: OAuthHelper
) : HasAllPaginated<CollectionDoc> {
    override val elasticRepository = collectionEsRepo
    override val allPaginatedPath: String = "${RoutePaths.LATEST}${RoutePaths.COLLECTIONS_LIST}"
    override val sortOrderCompanion = CollectionSortEnum.Companion
    
    @GetMapping(path = [
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTIONS_LIST}",
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTIONS_LIST}"
                       ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allPaginated(
            @PathVariable(name = "apiVersion", required = false) version: String?,
            uriComponentsBuilder: UriComponentsBuilder,
            size: Int,
            from: Int,
            status: Array<String>,
            sort: String?,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<List<CollectionDoc>> {
        if (!appConfig.allowPublicLists && user === null) {
            throw GeneralApiException("Unauthorized", HttpStatus.UNAUTHORIZED)
        }
        return super.allPaginated(uriComponentsBuilder, size, from, status, sort, user)
    }

    @GetMapping("${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_DETAIL}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUID(@PathVariable uuid: String): ApiCollection? {
        return collectionRepository.findByUUID(uuid)?.let {
            ApiCollection.fromDao(it, appConfig)
        }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.LEGACY}${RoutePaths.COLLECTION_DETAIL}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.COLLECTION_DETAIL}"],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun legacyByUUID(@PathVariable uuid: String): ApiCollection? {
        return collectionRepository.findByUUID(uuid)?.let {
            byUUID(uuid)?.let { ac -> ApiCollectionV2.fromLatest(ac, appConfig) }
        }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    
    @RequestMapping(path = [
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTION_DETAIL}",
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTION_DETAIL}"
                           ],
            produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(@PathVariable(name = "apiVersion", required = false) version: String?, @PathVariable uuid: String): String {
        
        return "forward:${RoutePaths.LATEST}/collections/$uuid"
    }

    @PostMapping("${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_CREATE}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createCollections(
            @RequestBody apiCollectionUpdates: List<ApiCollectionUpdate>,
            @AuthenticationPrincipal user: Jwt?
    ): List<ApiCollection> {
        return collectionRepository.createFromApi(
                apiCollectionUpdates,
                richSkillRepository,
                oAuthHelper.readableUserName(user),
                oAuthHelper.readableUserIdentifier(user)
        ).map {
            ApiCollection.fromDao(it, appConfig)
        }
    }

    @PostMapping(path = [
        "${RoutePaths.API}${RoutePaths.LEGACY}${RoutePaths.COLLECTION_CREATE}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.COLLECTION_CREATE}"
    ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun legacyCreateCollections(
            @RequestBody apiCollectionUpdates: List<ApiCollectionUpdate>,
            @AuthenticationPrincipal user: Jwt?
    ): List<ApiCollection> {
        return createCollections(apiCollectionUpdates, user).map { ApiCollectionV2.fromLatest(it, appConfig) }
    }

    
    @PostMapping(path = [
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTION_UPDATE}",
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTION_UPDATE}"
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateCollection(
            @PathVariable(name = "apiVersion", required = false) apiVersion: String?,
            @PathVariable uuid: String,
            @RequestBody apiUpdate: ApiCollectionUpdate,
            @AuthenticationPrincipal user: Jwt?
    ): ApiCollection {
        if (oAuthHelper.hasRole(appConfig.roleCurator) && !oAuthHelper.isArchiveRelated(apiUpdate.publishStatus)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        
        val existing = collectionRepository.findByUUID(uuid)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        
        val updated = collectionRepository.updateFromApi(
                existing.id.value,
                apiUpdate,
                richSkillRepository, oAuthHelper.readableUserName(user)
        )
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        
        return if (StringUtils.equals(getApiVersionCalled(apiVersion), RoutePaths.LATEST)) {
            ApiCollection.fromDao(updated, appConfig)
        } else {
            ApiCollectionV2.fromDao(updated, appConfig)
        }
    }
    
    @PostMapping(path = [
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTION_SKILLS_UPDATE}",
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTION_SKILLS_UPDATE}"
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateSkills(
            @PathVariable(name = "apiVersion", required = false) apiVersion: String?,
            @PathVariable uuid: String,
            @RequestBody skillListUpdate: ApiSkillListUpdate,
            @RequestParam(
                    required = false,
                    defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
            ) status: List<String>,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val task = UpdateCollectionSkillsTask(uuid, skillListUpdate, publishStatuses = publishStatuses, userString = oAuthHelper.readableUserName(user))
        taskMessageService.enqueueJob(TaskMessageService.updateCollectionSkills, task)
        
        return Task.processingResponse(task)
    }
    
    @PostMapping(path = [
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTION_PUBLISH}",
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTION_PUBLISH}"
                        ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun publishCollections(
            @PathVariable(name = "apiVersion", required = false) apiVersion: String?,
            @RequestBody search: ApiSearch,
            @RequestParam(
                    required = false,
                    defaultValue = "Published"
            ) newStatus: String,
            @RequestParam(
                    required = false,
                    defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
            ) filterByStatus: List<String>,
            @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val filterStatuses = filterByStatus.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val publishStatus = PublishStatus.forApiValue(newStatus)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val task = PublishTask(AppliesToType.Collection, search, filterByStatus = filterStatuses, publishStatus = publishStatus, userString = oAuthHelper.readableUserName(user))
        taskMessageService.enqueueJob(TaskMessageService.publishSkills, task)
        
        return Task.processingResponse(task)
    }
    
    @GetMapping(path = [
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTION_CSV}",
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTION_CSV}"
                       ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSkillsForCollectionCsv(
            @PathVariable(name = "apiVersion", required = false) apiVersion: String?,
            @PathVariable uuid: String
    ): HttpEntity<TaskResult> {
        if (collectionRepository.findByUUID(uuid)!!.status == PublishStatus.Draft && !oAuthHelper.hasRole(appConfig.roleAdmin)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        val task = CsvTask(collectionUuid = uuid)
        taskMessageService.enqueueJob(TaskMessageService.skillsForCollectionCsv, task)
        
        return Task.processingResponse(task)
    }
    
    @GetMapping("${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.COLLECTION_XLSX}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getSkillsForCollectionXlsx(
            @PathVariable uuid: String
    ): HttpEntity<TaskResult> {
        if (collectionRepository.findByUUID(uuid)!!.status == PublishStatus.Draft && !oAuthHelper.hasRole(appConfig.roleAdmin)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        val task = XlsxTask(collectionUuid = uuid)
        taskMessageService.enqueueJob(TaskMessageService.skillsForCollectionXlsx, task)
        
        return Task.processingResponse(task)
    }
    
    @DeleteMapping(path = [
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTION_REMOVE}",
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTION_REMOVE}"
                          ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun removeCollection(
            @PathVariable(name = "apiVersion", required = false) apiVersion: String?,
            @PathVariable uuid: String
    ): HttpEntity<TaskResult> {
        val task = RemoveCollectionSkillsTask(collectionUuid = uuid)
        taskMessageService.enqueueJob(TaskMessageService.removeCollectionSkills, task)
        
        return Task.processingResponse(task)
    }
    
    @GetMapping(path = [
        "${RoutePaths.VERSIONED_API}${RoutePaths.COLLECTION_AUDIT_LOG}",
        "${RoutePaths.UNVERSIONED_API}${RoutePaths.COLLECTION_AUDIT_LOG}"
                       ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun collectionAuditLog(
            @PathVariable(name = "apiVersion", required = false) apiVersion: String?,
            @PathVariable uuid: String
    ): HttpEntity<List<AuditLog>> {
        val pageable = OffsetPageable(0, Int.MAX_VALUE, AuditLogSortEnum.forValueOrDefault(AuditLogSortEnum.DateDesc.apiValue).sort)
        val collection = collectionRepository.findByUUID(uuid)
        val sizedIterable = auditLogRepository.findByTableAndId(CollectionTable.tableName, entityId = collection!!.id.value, offsetPageable = pageable)
        
        return ResponseEntity.status(200).body(sizedIterable.toList().map { it.toModel() })
    }

    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.WORKSPACE_PATH}",
    ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getOrCreateWorkspace(
            @AuthenticationPrincipal user: Jwt?
    ): ApiCollection? {
        return collectionRepository.findByOwner(
                oAuthHelper.readableUserIdentifier(user))?.let {
            ApiCollection.fromDao(it, appConfig
            )
        } ?: collectionRepository.createFromApi(
                listOf(
                        ApiCollectionUpdate(
                                name = DEFAULT_WORKSPACE_NAME,
                                publishStatus = PublishStatus.Workspace,
                                author = oAuthHelper.readableUserName(user),
                                skills = ApiStringListUpdate()
                        )
                ),
                richSkillRepository,
                oAuthHelper.readableUserName(user),
                oAuthHelper.readableUserIdentifier(user)
        ).firstOrNull()?.let { ApiCollection.fromDao(it, appConfig) }

    }

    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.LEGACY}${RoutePaths.WORKSPACE_PATH}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.WORKSPACE_PATH}"
                       ],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun legacyGetOrCreateWorkspace(
            @AuthenticationPrincipal user: Jwt?
    ): ApiCollection? {
        return getOrCreateWorkspace(user)?.let { ApiCollectionV2.fromLatest(it, appConfig) }

    }

}
