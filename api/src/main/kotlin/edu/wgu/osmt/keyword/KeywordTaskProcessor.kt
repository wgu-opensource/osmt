package edu.wgu.osmt.keyword

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.task.RemoveItemTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
@Profile("apiserver")
@Transactional
class KeywordTaskProcessor {

    val logger: Logger = LoggerFactory.getLogger(KeywordTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    @RqueueListener(
        value = [TaskMessageService.removeKeyword],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun removeKeyword(task: RemoveItemTask) {
        logger.info("Started processing to remove keyword task id: ${task.identifier}")

        val batchResult = keywordRepository.remove(task.identifier.toLong())

        taskMessageService.publishResult(
            task.copy(result=batchResult, status= TaskStatus.Ready)
        )

        logger.info("Task ${task.uuid} completed")
    }
}
