package edu.wgu.osmt.task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

interface TaskApi {
    fun queueCsvJob(): HttpEntity<TaskResult>
    fun checkTaskOrResult(uuid: String): HttpEntity<*>
}

@Controller
class TaskApiImpl @Autowired constructor(
    val taskMessageService: TaskMessageService
) : TaskApi {

    @GetMapping("/api/tasks/{uuid}")
    @ResponseBody
    override fun checkTaskOrResult(@PathVariable uuid: String): HttpEntity<*> {
        val task = taskMessageService.opsForHash.get(TaskMessageService.taskHashTable, uuid)

        return when (task?.status) {
            TaskStatus.Ready -> task.toResultResponse()
            TaskStatus.Processing -> {
                val responseHeaders = HttpHeaders()
                responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                val taskResult = TaskResult.fromTask(task)
                ResponseEntity.status(202).headers(responseHeaders).body(taskResult)
            }
            null -> {
                ResponseEntity.status(404).body("Task with id $uuid not found")
            }
        }
    }

    @GetMapping("/api/skills", produces = ["text/csv"])
    @ResponseBody
    override fun queueCsvJob(): HttpEntity<TaskResult> {
        val task = CsvTask()
        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        taskMessageService.enqueueJob(TaskMessageService.allSkillsCsv, task)
        
        val tr = TaskResult.fromTask(task)
        return ResponseEntity.status(202).headers(responseHeaders).body(tr)
    }
}
