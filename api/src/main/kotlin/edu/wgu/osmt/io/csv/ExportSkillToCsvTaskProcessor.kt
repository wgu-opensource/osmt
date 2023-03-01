package edu.wgu.osmt.io.csv

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillAndCollections
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.task.ExportSkillsToCsvTask
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
class ExportSkillToCsvTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(ExportSkillToCsvTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var appConfig: AppConfig

    @RqueueListener(
        value = [TaskMessageService.skillsForCustomListExportCsv],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun csvSkillsInCustomRsdListProcessor(task: ExportSkillsToCsvTask) {
        logger.info("Started processing task for Custom RSD List export")

        val csv = task.uuids?.map { richSkillRepository.findByUUID(it) }
            ?.map { RichSkillAndCollections.fromDao(it!!) }
            ?.let { RichSkillCsvExport(appConfig).toCsv(it) }

        taskMessageService.publishResult(
            task.copy(result = csv, status = TaskStatus.Ready)
        )
        logger.info("Custom RSD List export task completed")
    }

}
