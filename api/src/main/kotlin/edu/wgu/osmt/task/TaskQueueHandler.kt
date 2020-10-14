package edu.wgu.osmt.task

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.config.AppConfig
import org.jetbrains.exposed.dao.with
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.wgu.osmt.richskill.*

@Component
@Profile("apiserver")
@Transactional
class TaskQueueHandler {
    val logger: Logger = LoggerFactory.getLogger(TaskQueueHandler::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var appConfig: AppConfig

    @RqueueListener(
        value = [TaskMessageService.allSkillsCsv],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun csvJobProcessor(csvTask: CsvTask) {
        logger.info("Started processing task id: ${csvTask.uuid}")

        val allSkills =
            richSkillRepository.dao.all().with(RichSkillDescriptorDao::collections)
                .map { RichSkillAndCollections.fromDao(it) }

        val csvString = RichSkillCsvExport(appConfig).toCsv(allSkills)

        taskMessageService.opsForHash.put(
            TaskMessageService.taskHashTable,
            csvTask.uuid,
            csvTask.copy(result = csvString, status = TaskStatus.Ready)
        )
        logger.info("Task ${csvTask.uuid} completed")
    }

    @RqueueListener(TaskMessageService.deadLetters)
    fun onMessage(task: Task) {
        logger.warn("Task ${task.uuid} failed, removing from hash table")
        taskMessageService.opsForHash.delete(TaskMessageService.taskHashTable, task.uuid)
    }
}
