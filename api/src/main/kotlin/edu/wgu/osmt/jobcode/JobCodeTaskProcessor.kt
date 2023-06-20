package edu.wgu.osmt.jobcode

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.collection.UpdateCollectionSkillsTaskProcessor
import edu.wgu.osmt.task.RemoveJobCodeTask
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
class JobCodeTaskProcessor {

    val logger: Logger = LoggerFactory.getLogger(UpdateCollectionSkillsTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var jobCodeRepository: JobCodeRepository

    @RqueueListener(
        value = [TaskMessageService.removeJobCode],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun removeJobCode(task: RemoveJobCodeTask) {
        logger.info("Started processing to remove job code task id: ${task.jobCodeId}")

        val batchResult = jobCodeRepository.remove(task.jobCodeId)

        taskMessageService.publishResult(
            task.copy(result=batchResult, status= TaskStatus.Ready)
        )

        logger.info("Task ${task.uuid} completed")
    }

}