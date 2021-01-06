package edu.wgu.osmt.task

import com.github.sonus21.rqueue.annotation.RqueueListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("apiserver")
@Transactional
class TaskQueueHandler {
    val logger: Logger = LoggerFactory.getLogger(TaskQueueHandler::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @RqueueListener(TaskMessageService.deadLetters)
    fun onMessage(task: Task) {
        logger.warn("Task ${task.uuid} failed, removing from hash table")
        taskMessageService.opsForHash.delete(TaskMessageService.taskHashTable, task.uuid)
    }
}
