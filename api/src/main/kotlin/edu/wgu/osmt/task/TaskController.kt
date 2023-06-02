package edu.wgu.osmt.task

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiSkill
import edu.wgu.osmt.api.model.ApiSkillV2
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

    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_TEXT}",
        "${RoutePaths.API}${RoutePaths.LEGACY}${RoutePaths.TASK_DETAIL_TEXT}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.TASK_DETAIL_TEXT}"
    ])
    @ResponseBody
    fun textResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }

    @GetMapping("${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_MEDIA}")
    @ResponseBody
    fun mediaResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }

    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_BATCH}",
        "${RoutePaths.API}${RoutePaths.LEGACY}${RoutePaths.TASK_DETAIL_BATCH}",
        "${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.TASK_DETAIL_BATCH}"
    ])
    @ResponseBody
    fun batchResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }

    @GetMapping("${RoutePaths.API}${RoutePaths.LATEST}${RoutePaths.TASK_DETAIL_SKILLS}")
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

    @GetMapping(path = [
        "${RoutePaths.API}${RoutePaths.LEGACY}${RoutePaths.TASK_DETAIL_SKILLS}",
    ])    @ResponseBody
    fun legacySkillsResult(@PathVariable uuid: String): HttpEntity<*> {
        val task = taskMessageService.opsForHash.get(TaskMessageService.taskHashTable, uuid)
        return when (task?.status) {
            TaskStatus.Ready -> {
                val createSkillsTaskV2: CreateSkillsTaskV2 = task as CreateSkillsTaskV2
                val skillDaos = richSkillRepository.findManyByUUIDs(createSkillsTaskV2.result!!)
                val apiSkillsV2 = skillDaos?.map { ApiSkillV2.fromDao(it, appConfig)}

                val responseHeaders = HttpHeaders()
                responseHeaders.add("Content-Type", task.contentType)
                return ResponseEntity.ok().headers(responseHeaders).body(apiSkillsV2)
            }
            else -> ResponseEntity.status(404).body("Task with id $uuid not ready or not found")
        }
    }

    @GetMapping(path = ["${RoutePaths.API}${RoutePaths.UNVERSIONED}${RoutePaths.TASK_DETAIL_SKILLS}"])
    @ResponseBody
    fun unversionedSkillsResult(@PathVariable uuid: String): HttpEntity<*> {

        return when(RoutePaths.DEFAULT) {
            RoutePaths.LATEST -> skillsResult(uuid)
            RoutePaths.LEGACY -> legacySkillsResult(uuid)
            else -> {
                skillsResult(uuid)
            }
        }
    }
}
