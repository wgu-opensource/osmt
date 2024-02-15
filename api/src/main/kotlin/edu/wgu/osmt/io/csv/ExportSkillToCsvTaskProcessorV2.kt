package edu.wgu.osmt.io.csv

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillAndCollections
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.task.ExportSkillsToCsvTaskV2
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
class ExportSkillToCsvTaskProcessorV2 {
    val logger: Logger = LoggerFactory.getLogger(ExportSkillToCsvTaskProcessorV2::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var appConfig: AppConfig

    @RqueueListener(
        value = [TaskMessageService.skillsForCustomListExportCsvV2],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun csvSkillsInCustomRsdListProcessor(task: ExportSkillsToCsvTaskV2) {
        logger.info("Started processing task for Custom RSD List export")

        val csv = task.uuids?.map { richSkillRepository.findByUUID(it) }
            ?.map { RichSkillAndCollections.fromDao(it!!) }
            ?.let { RichSkillCsvExportV2(appConfig).toCsv(it) }

        taskMessageService.publishResult(
            task.copy(result = csv, status = TaskStatus.Ready)
        )
        logger.info("Custom RSD List export task completed")
    }

}
