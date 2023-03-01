package edu.wgu.osmt.task

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiSkill
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@Transactional
class TaskController @Autowired constructor(
    val taskMessageService: TaskMessageService,
    val richSkillRepository: RichSkillRepository,
    val appConfig: AppConfig
) {
    private fun taskResult(uuid: String): HttpEntity<*> {
        val task = taskMessageService.opsForHash.get(TaskMessageService.taskHashTable, uuid)
        return when (task?.status) {
            TaskStatus.Ready -> Task.resultResponse(task)
            else -> ResponseEntity.status(404).body("Task with id $uuid not ready or not found")
        }
    }

    @GetMapping(RoutePaths.TASK_DETAIL_TEXT)
    @ResponseBody
    fun textResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }

    @GetMapping(RoutePaths.TASK_DETAIL_MEDIA)
    @ResponseBody
    fun mediaResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }

    @GetMapping(RoutePaths.TASK_DETAIL_BATCH)
    @ResponseBody
    fun batchResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }

    @GetMapping(RoutePaths.TASK_DETAIL_SKILLS)
    @ResponseBody
    fun skillsResult(@PathVariable uuid: String): HttpEntity<*> {
        val task = taskMessageService.opsForHash.get(TaskMessageService.taskHashTable, uuid)
        return when (task?.status) {
            TaskStatus.Ready -> {
                // CreateSkillsTask.results is a list of skill uuids, look them up and return
                val createSkillsTask: CreateSkillsTask = task as CreateSkillsTask
                val skillDaos = richSkillRepository.findManyByUUIDs(createSkillsTask.result!!)
                val apiSkills = skillDaos?.map { ApiSkill.fromDao(it, appConfig)}

                val responseHeaders = HttpHeaders()
                responseHeaders.add("Content-Type", task.contentType)
                return ResponseEntity.ok().headers(responseHeaders).body(apiSkills)
            }
            else -> ResponseEntity.status(404).body("Task with id $uuid not ready or not found")
        }
    }
}
