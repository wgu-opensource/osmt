package edu.wgu.osmt.collection

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
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.security.OAuth2Helper
import edu.wgu.osmt.security.OAuth2Helper.readableUsername
import edu.wgu.osmt.task.*
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
    val appConfig: AppConfig
): HasAllPaginated<CollectionDoc> {

    override val elasticRepository = collectionEsRepo

    override val allPaginatedPath: String = RoutePaths.COLLECTIONS_LIST
    override val sortOrderCompanion = CollectionSortEnum.Companion

    @GetMapping(RoutePaths.COLLECTIONS_LIST, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    override fun allPaginated(
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

    @GetMapping(RoutePaths.COLLECTION_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUID(@PathVariable uuid: String): ApiCollection? {
        return collectionRepository.findByUUID(uuid)?.let {
            ApiCollection.fromDao(it, appConfig)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping(RoutePaths.COLLECTION_DETAIL, produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(@PathVariable uuid: String): String {
        return "forward:/collections/$uuid"
    }

    @PostMapping(RoutePaths.COLLECTION_CREATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createCollections(
        @RequestBody apiCollectionUpdates: List<ApiCollectionUpdate>,
        @AuthenticationPrincipal user: Jwt?
    ): List<ApiCollection> {
        return collectionRepository.createFromApi(
            apiCollectionUpdates,
            richSkillRepository,
            OAuth2Helper.readableUsername(user)
        ).map {
            ApiCollection.fromDao(it, appConfig)
        }

    }

    @PostMapping(RoutePaths.COLLECTION_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateCollection(
        @PathVariable uuid: String,
        @RequestBody apiUpdate: ApiCollectionUpdate,
        @AuthenticationPrincipal user: Jwt?
    ): ApiCollection {
        val existing = collectionRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (existing.importedFrom != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Imported Collections are Read Only")
        }

        val updated = collectionRepository.updateFromApi(
            existing.id.value,
            apiUpdate,
            richSkillRepository,
            readableUsername(user)
        )
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiCollection.fromDao(updated, appConfig)
    }

    @PostMapping(RoutePaths.COLLECTION_SHARE_EXTERNALLY, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun shareCollectionExternally(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): ApiCollection {
        val existingCollection = collectionRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (existingCollection.importedFrom != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Imported Collections are Read Only")
        }

        val updatedCollection = collectionRepository.updateIsExternallyShared(
                existingCollection.id.value,
                true,
                readableUsername(user)
            ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiCollection.fromDao(updatedCollection, appConfig)
    }

    @PostMapping(RoutePaths.COLLECTION_UNSHARE_EXTERNALLY, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun unshareCollectionExternally(
        @PathVariable uuid: String,
        @AuthenticationPrincipal user: Jwt?
    ): ApiCollection {
        val existingCollection = collectionRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (existingCollection.importedFrom != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Imported Collections are Read Only")
        }

        val updatedCollection = collectionRepository.updateIsExternallyShared(
                existingCollection.id.value,
                false,
                readableUsername(user)
            ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiCollection.fromDao(updatedCollection, appConfig)
    }

    @PostMapping(RoutePaths.COLLECTION_SKILLS_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateSkills(
        @PathVariable uuid: String,
        @RequestBody skillListUpdate: ApiSkillListUpdate,
        @RequestParam(
            required = false,
            defaultValue = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET
        ) status: List<String>,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val existingCollection = collectionRepository.findByUUID(uuid)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        if (existingCollection.importedFrom != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Imported Collections are Read Only")
        }

        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val task = UpdateCollectionSkillsTask(uuid, skillListUpdate, publishStatuses=publishStatuses, userString = readableUsername(user))

        taskMessageService.enqueueJob(TaskMessageService.updateCollectionSkills, task)
        return Task.processingResponse(task)
    }

    @PostMapping(RoutePaths.COLLECTION_PUBLISH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun publishCollections(
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
        val publishStatus = PublishStatus.forApiValue(newStatus) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val task = PublishTask(AppliesToType.Collection, search, filterByStatus=filterStatuses, publishStatus = publishStatus, userString = readableUsername(user))

        taskMessageService.enqueueJob(TaskMessageService.publishSkills, task)
        return Task.processingResponse(task)
    }

    @GetMapping(RoutePaths.COLLECTION_CSV, produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSkillsForCollectionCsv(
        @PathVariable uuid: String
    ): HttpEntity<TaskResult> {
        val task = CsvTask(collectionUuid = uuid)
        taskMessageService.enqueueJob(TaskMessageService.skillsForCollectionCsv, task)
        return Task.processingResponse(task)
    }

    @GetMapping(RoutePaths.COLLECTION_AUDIT_LOG, produces = ["application/json"])
    fun collectionAuditLog(
        @PathVariable uuid: String
    ): HttpEntity<List<AuditLog>> {
        val pageable = OffsetPageable(0, Int.MAX_VALUE, AuditLogSortEnum.forValueOrDefault(AuditLogSortEnum.DateDesc.apiValue).sort)

        val collection = collectionRepository.findByUUID(uuid)

        val sizedIterable = auditLogRepository.findByTableAndId(CollectionTable.tableName, entityId = collection!!.id.value, offsetPageable = pageable)
        return ResponseEntity.status(200).body(sizedIterable.toList().map{it.toModel()})
    }


    @PostMapping(RoutePaths.COLLECTION_IMPORT, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun importCollection(
        @RequestBody reference: ApiImportReference,
        @AuthenticationPrincipal user: Jwt?
    ): HttpEntity<TaskResult> {
        val collectionUrl = reference.canonicalUrl ?:  throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val task = ShareExternallyTask(
                canonicalUrl = collectionUrl,
                libraryName = reference.libraryName,
                userString = readableUsername(user))
        taskMessageService.enqueueJob(TaskMessageService.importCollections, task)
        return Task.processingResponse(task)
    }

    @PostMapping(RoutePaths.COLLECTION_REMOVE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun removeCollection(
            @PathVariable uuid: String,
            @AuthenticationPrincipal user: Jwt?
    ): ApiBatchResult {
        val existing = collectionRepository.findByUUID(uuid)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        /* can only remove readonly imported collections */
        if (existing.importedFrom == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        collectionEsRepo.delete(existing.toDoc())
        existing.delete()

        return ApiBatchResult(success=true, modifiedCount=1, totalCount=1)
    }
}
