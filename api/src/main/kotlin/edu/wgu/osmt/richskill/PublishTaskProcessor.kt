package edu.wgu.osmt.richskill

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.task.AppliesToType
import edu.wgu.osmt.task.PublishTask
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
class PublishTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(PublishTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @RqueueListener(
        value = [TaskMessageService.publishSkills],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun process(publishTask: PublishTask) {
        logger.info("Started processing publish task id: ${publishTask.uuid}")

        val batchResult = when (publishTask.appliesToType) {
            AppliesToType.Skill -> richSkillRepository.changeStatusesForTask(publishTask)
            AppliesToType.Collection -> collectionRepository.changeStatusesForTask(publishTask)
        }

        taskMessageService.publishResult(
            publishTask.copy(result=batchResult, status=TaskStatus.Ready)
        )

        logger.info("Task ${publishTask.uuid} completed")
    }
}