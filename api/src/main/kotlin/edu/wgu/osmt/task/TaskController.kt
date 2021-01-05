package edu.wgu.osmt.task

import edu.wgu.osmt.RoutePaths
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class TaskController @Autowired constructor(
    val taskMessageService: TaskMessageService
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

    @GetMapping(RoutePaths.TASK_DETAIL_BATCH)
    @ResponseBody
    fun batchResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }

    @GetMapping(RoutePaths.TASK_DETAIL_SKILLS)
    @ResponseBody
    fun skillsResult(@PathVariable uuid: String): HttpEntity<*> {
        return taskResult(uuid)
    }
}
