package edu.wgu.osmt.richskill

import edu.wgu.osmt.HasAllPaginated
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.*
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.elasticsearch.*
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
    override val elasticRepository: EsRichSkillRepository,
    val appConfig: AppConfig
): HasAllPaginated<RichSkillDoc> {
    val keywordDao = KeywordDao.Companion

    override val allPaginatedPath: String = RoutePaths.SKILLS_PATH
    override val sortOrderCompanion = SkillSortEnum.Companion

    @GetMapping(RoutePaths.SKILLS_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    override fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        size: Int,
        from: Int,
        status: Array<String>,
        sort: String?
    ): HttpEntity<List<RichSkillDoc>> {
        return super.allPaginated(uriComponentsBuilder, size, from, status, sort)
    }

    @GetMapping(RoutePaths.SKILLS_PATH, produces = ["text/csv"])
    @ResponseBody
    fun allSkillsCsv(): HttpEntity<TaskResult> {
        val task = CsvTask()
        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        taskMessageService.enqueueJob(TaskMessageService.allSkillsCsv, task)

        val tr = TaskResult.fromTask(task)
        return ResponseEntity.status(202).headers(responseHeaders).body(tr)
    }

    @PostMapping(RoutePaths.SKILLS_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createSkills(
        @RequestBody apiSkillUpdates: List<ApiSkillUpdate>,
        @AuthenticationPrincipal user: Jwt?
    ): List<ApiSkill> {
        return richSkillRepository.createFromApi(apiSkillUpdates, readableUsername(user)).map {
            ApiSkill.fromDao(it, appConfig)
        }
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

        val updatedSkill =
            richSkillRepository.updateFromApi(existingSkill.id.value, skillUpdate, readableUsername(user))
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

        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        val tr = TaskResult.fromTask(task)
        return ResponseEntity.status(202).headers(responseHeaders).body(tr)
    }
}
