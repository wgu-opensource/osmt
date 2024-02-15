package edu.wgu.osmt.io.xlsx

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillAndCollections
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.task.ExportSkillsToXlsxTask
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
class ExportSkillToXlsxTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(ExportSkillToXlsxTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var appConfig: AppConfig

    @RqueueListener(
        value = [TaskMessageService.skillsForCustomListExportXlsx],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun xlsxSkillsInCustomRsdListProcessor(task: ExportSkillsToXlsxTask) {
        logger.info("Started processing task for Custom RSD List .xlsx export")

        val xlsx = task.uuids?.map { richSkillRepository.findByUUID(it) }
            ?.map { RichSkillAndCollections.fromDao(it!!) }
            ?.let { RichSkillXlsxExport(appConfig).toXlsx(it) }

        taskMessageService.publishResult(
            task.copy(result = xlsx, status = TaskStatus.Ready)
        )
        logger.info("Custom RSD List .xlsx export task completed")
    }

}
