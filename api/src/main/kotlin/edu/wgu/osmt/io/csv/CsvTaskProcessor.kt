package edu.wgu.osmt.io.csv

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.io.common.TabularTask
import edu.wgu.osmt.richskill.RichSkillAndCollections
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.task.CsvTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskStatus
import org.jetbrains.exposed.dao.with
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("apiserver")
@Transactional
class CsvTaskProcessor : TabularTask<CsvTask>() {
    val logger: Logger = LoggerFactory.getLogger(CsvTaskProcessor::class.java)

    @RqueueListener(
        value = [TaskMessageService.skillsForCollectionCsv],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    override fun tabularSkillsInCollectionProcessor(task: CsvTask) {
        logger.info("Started processing task id: ${task.uuid}")

        val csv = collectionRepository.findByUUID(task.collectionUuid)
            ?.skills
            ?.filter { PublishStatus.Archived != it.publishStatus() }
            ?.with(RichSkillDescriptorDao::collections)
            ?.map { RichSkillAndCollections.fromDao(it) }
            ?.let { RichSkillCsvExport(appConfig).toCsv(it) }

        taskMessageService.publishResult(
            task.copy(result = csv, status = TaskStatus.Ready)
        )
        logger.info("Task ${task.uuid} completed")
    }

    @RqueueListener(
        value = [TaskMessageService.skillsForFullLibraryCsv],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    override fun tabularSkillsInFullLibraryProcessor(task: CsvTask) {
        logger.info("Started processing task for Full Library .csv export")

        val csv = richSkillRepository.findAll()
            ?.with(RichSkillDescriptorDao::collections)
            ?.map { RichSkillAndCollections.fromDao(it) }
            ?.let { RichSkillCsvExport(appConfig).toCsv(it) }

        taskMessageService.publishResult(
            task.copy(result = csv, status = TaskStatus.Ready)
        )
        logger.info("Full Library export task .csv completed")
    }

}
