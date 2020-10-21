package edu.wgu.osmt.richskill

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.task.PublishSkillsTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskQueueHandler
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
class PublishSkillsTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(TaskQueueHandler::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @RqueueListener(
        value = [TaskMessageService.publishSkills],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun publishSkills(publishSkillsTask: PublishSkillsTask) {
        logger.info("Started processing publish skills task id: ${publishSkillsTask.uuid}")

        val result = richSkillRepository.changeStatusesForTask(publishSkillsTask)

        taskMessageService.publishResult(
            publishSkillsTask.copy(result=result, status=TaskStatus.Ready)
        )

        logger.info("Task ${publishSkillsTask.uuid} completed")
    }
}