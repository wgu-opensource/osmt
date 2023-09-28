package edu.wgu.osmt.richskill

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.task.CreateSkillsTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
@Profile("apiserver")
@Transactional
class CreateSkillsTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(CreateSkillsTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    private lateinit var appConfig: AppConfig

    @RqueueListener(
        value = [TaskMessageService.createSkills],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun process(task: CreateSkillsTask) {
        logger.info("Started processing createSkillsTask uuid: ${task.uuid}")

        val results = richSkillRepository.createFromApi(task.apiSkillUpdates, task.userString, task.userIdentifier).map {
            it.uuid
        }

        taskMessageService.publishResult(
            task.copy(result=results, status=TaskStatus.Ready)
        )

        logger.info("Task ${task.uuid} completed")
    }
}
