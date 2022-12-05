package edu.wgu.osmt.collection

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillAndCollections
import edu.wgu.osmt.richskill.RichSkillCsvExport
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.task.CsvTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskStatus
import org.jetbrains.exposed.dao.with
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("apiserver")
@Transactional
class CsvTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(CsvTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var appConfig: AppConfig

    @RqueueListener(
        value = [TaskMessageService.skillsForCollectionCsv],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun csvSkillsInCollectionProcessor(csvTask: CsvTask) {
        logger.info("Started processing task id: ${csvTask.uuid}")

        val csv = collectionRepository.findByUUID(csvTask.collectionUuid)
            ?.skills
            ?.with(RichSkillDescriptorDao::collections)
            ?.map { RichSkillAndCollections.fromDao(it) }
            ?.let { RichSkillCsvExport(appConfig).toCsv(it) }

        taskMessageService.publishResult(
            csvTask.copy(result = csv, status = TaskStatus.Ready)
        )
        logger.info("Task ${csvTask.uuid} completed")
    }

    @RqueueListener(
        value = [TaskMessageService.skillsForFullLibraryCsv],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun csvSkillsInFullLibraryProcessor(csvTask: CsvTask) {
        logger.info("Started processing task for Full Library export")

        val csv = richSkillRepository.findAll()
            ?.with(RichSkillDescriptorDao::collections)
            ?.map { RichSkillAndCollections.fromDao(it) }
            ?.let { RichSkillCsvExport(appConfig).toCsv(it) }

        taskMessageService.publishResult(
            csvTask.copy(result = csv, status = TaskStatus.Ready)
        )
        logger.info("Full Library export task completed")
    }

}
