package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.model.ApiSkill
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.security.OAuth2Helper.readableUsername
import edu.wgu.osmt.task.CsvTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/api/skills")
@Transactional
class RichSkillController @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val taskMessageService: TaskMessageService,
    val appConfig: AppConfig
) {
    val keywordDao = KeywordDao.Companion

    // TODO pagination according to spec
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allSkills(request: HttpServletRequest): List<ApiSkill>  {
        return richSkillRepository.findAll().map {
            ApiSkill.fromDao(it, appConfig)
        }
    }

    @GetMapping(produces = ["text/csv"])
    @ResponseBody
    fun allSkillsCsv(): HttpEntity<TaskResult> {
        val task = CsvTask()
        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        taskMessageService.enqueueJob(TaskMessageService.allSkillsCsv, task)

        val tr = TaskResult.fromTask(task)
        return ResponseEntity.status(202).headers(responseHeaders).body(tr)
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createSkills(@RequestBody apiSkillUpdates: List<ApiSkillUpdate>,
                     @AuthenticationPrincipal user: Jwt?): List<ApiSkill>
    {
        return richSkillRepository.createFromApi(apiSkillUpdates, readableUsername(user)).map {
            ApiSkill.fromDao(it, appConfig)
        }
    }

    @GetMapping("/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUID(@PathVariable uuid: String,
               @AuthenticationPrincipal user: Jwt?): ApiSkill? {
        return richSkillRepository.findByUUID(uuid)?.let {
            if (user == null && it.publishStatus() == PublishStatus.Unpublished) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }

            ApiSkill.fromDao(it, appConfig)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping("/{uuid}", produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(@PathVariable uuid: String,
                       @AuthenticationPrincipal user: Jwt?): String {
        return richSkillRepository.findByUUID(uuid)?.let {
            if (user == null && it.publishStatus() == PublishStatus.Unpublished) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }

            "forward:/skills/$uuid"
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping("/{uuid}", produces = ["text/csv"])
    fun byUUIDCsvView(@PathVariable uuid: String,
                      @AuthenticationPrincipal user: Jwt?): HttpEntity<*> {
        return richSkillRepository.findByUUID(uuid)?.let {
            if (user == null && it.publishStatus() == PublishStatus.Unpublished) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }

            val skill = it.toModel()
            val collections = it.collections.map{ it.toModel() }.toSet()
            val result = RichSkillCsvExport(appConfig).toCsv(listOf(RichSkillAndCollections(skill,collections)))
            val responseHeaders = HttpHeaders()
            responseHeaders.add("Content-Type", "text/csv")
            return ResponseEntity.ok().headers(responseHeaders).body(result)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/{uuid}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateSkill(@PathVariable uuid: String,
                    @RequestBody skillUpdate: ApiSkillUpdate,
                    @AuthenticationPrincipal user: Jwt?): ApiSkill
    {
        val existingSkill = richSkillRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updatedSkill = richSkillRepository.updateFromApi(existingSkill.id.value, skillUpdate, readableUsername(user))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiSkill.fromDao(updatedSkill, appConfig)
    }
}
