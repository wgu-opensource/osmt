package edu.wgu.osmt.collection

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.task.RemoveCollectionSkillsTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskStatus
import edu.wgu.osmt.task.UpdateCollectionSkillsTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
@Profile("apiserver")
@Transactional
class UpdateCollectionSkillsTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(UpdateCollectionSkillsTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @RqueueListener(
        value = [TaskMessageService.updateCollectionSkills],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun updateCollectionSkills(task: UpdateCollectionSkillsTask) {
        logger.info("Started processing update collection skills task id: ${task.uuid}")

        val batchResult = collectionRepository.updateSkillsForTask(task.collectionUuid, task, richSkillRepository)

        taskMessageService.publishResult(
            task.copy(result=batchResult, status= TaskStatus.Ready)
        )

        logger.info("Task ${task.uuid} completed")
    }

    @RqueueListener(
        value = [TaskMessageService.updateCollectionSkills],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun removeCollectionSkills(task: RemoveCollectionSkillsTask) {
        logger.info("Started processing to remove collection skills task id: ${task.uuid}")

        val id = collectionRepository.findByUUID(task.uuid)?.id
        val batchResult = collectionRepository.remove(id?.value)

        taskMessageService.publishResult(
            task.copy(result=batchResult, status= TaskStatus.Ready)
        )

        logger.info("Task ${task.uuid} completed")
    }
}