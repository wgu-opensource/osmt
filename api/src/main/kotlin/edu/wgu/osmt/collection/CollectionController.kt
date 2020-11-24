package edu.wgu.osmt.collection

import edu.wgu.osmt.HasAllPaginated
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.*
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.security.OAuth2Helper
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
class CollectionController @Autowired constructor(
    val collectionRepository: CollectionRepository,
    val richSkillRepository: RichSkillRepository,
    val taskMessageService: TaskMessageService,
    override val elasticRepository: EsCollectionRepository,
    val appConfig: AppConfig
): HasAllPaginated<CollectionDoc> {

    override val allPaginatedPath: String = RoutePaths.COLLECTIONS_PATH
    override val sortOrderCompanion = SkillSortEnum.Companion

    @GetMapping(RoutePaths.COLLECTIONS_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    override fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        size: Int,
        from: Int,
        status: Array<String>,
        sort: String?
    ): HttpEntity<List<CollectionDoc>> {
        return super.allPaginated(uriComponentsBuilder, size, from, status, sort)
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

    @PostMapping(RoutePaths.COLLECTIONS_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
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

        val updated = collectionRepository.updateFromApi(
            existing.id.value,
            apiUpdate,
            richSkillRepository,
            readableUsername(user)
        )
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiCollection.fromDao(updated, appConfig)
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
        val publishStatuses = status.mapNotNull { PublishStatus.forApiValue(it) }.toSet()
        val task = UpdateCollectionSkillsTask(uuid, skillListUpdate, publishStatuses=publishStatuses, userString = readableUsername(user))
        taskMessageService.enqueueJob(TaskMessageService.updateCollectionSkills, task)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        val tr = TaskResult.fromTask(task)
        return ResponseEntity.status(202).headers(responseHeaders).body(tr)
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

        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        val tr = TaskResult.fromTask(task)
        return ResponseEntity.status(202).headers(responseHeaders).body(tr)
    }

    @GetMapping(RoutePaths.COLLECTION_SKILLS, produces = ["text/csv"])
    fun getSkillsForCollectionCsv(
        @PathVariable uuid: String
    ): HttpEntity<TaskResult> {
        val task = CsvTask(result = uuid)
        val responseHeaders = HttpHeaders().apply { add("Content-Type", MediaType.APPLICATION_JSON_VALUE) }
        taskMessageService.enqueueJob(TaskMessageService.skillsForCollectionCsv, task)
        val tr = TaskResult.fromTask(task)
        return ResponseEntity.status(202).headers(responseHeaders).body(tr)
    }

}
