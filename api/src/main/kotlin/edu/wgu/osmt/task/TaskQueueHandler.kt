package edu.wgu.osmt.task

import com.github.sonus21.rqueue.annotation.RqueueListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TaskQueueHandler {
    val logger: Logger = LoggerFactory.getLogger(TaskQueueHandler::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @RqueueListener(
        value = [TaskMessageService.allSkillsCsv],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun csvJobProcessor(csvTask: CsvTask) {
        logger.info("Started processing task id: ${csvTask.uuid}")

        val updatedStatus = csvTask.copy(status = TaskStatus.Processing)
        taskMessageService.opsForHash.put(TaskMessageService.taskHashTable, csvTask.uuid, updatedStatus)

        val retrieved = taskMessageService.taskResultForUuidAndType<CsvTask>(csvTask.uuid)

        // TODO actual processing
        Thread.sleep(20000)
        taskMessageService.opsForHash.put(
            TaskMessageService.taskHashTable,
            csvTask.uuid,
            updatedStatus.copy(result = "TEST RESULT STRING", status = TaskStatus.Ready)
        )
        logger.info("Task ${csvTask.uuid} completed")
    }

    @RqueueListener(TaskMessageService.deadLetters)
    fun onMessage(task: Task) {
        logger.warn("Task ${task.uuid} failed, removing from hash table")
        taskMessageService.opsForHash.delete(TaskMessageService.taskHashTable, task.uuid)
    }
}
