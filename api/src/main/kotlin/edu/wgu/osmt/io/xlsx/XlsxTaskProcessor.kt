package edu.wgu.osmt.io.xlsx

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.io.common.TabularTask
import edu.wgu.osmt.richskill.RichSkillAndCollections
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.task.XlsxTask
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
class XlsxTaskProcessor : TabularTask<XlsxTask>() {
    val logger: Logger = LoggerFactory.getLogger(XlsxTaskProcessor::class.java)

    @RqueueListener(
        value = [TaskMessageService.skillsForCollectionXlsx],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    override fun tabularSkillsInCollectionProcessor(task: XlsxTask) {
        logger.info("Started processing task id: ${task.uuid}")

        val xlsx = collectionRepository.findByUUID(task.collectionUuid)
            ?.skills
            ?.filter { PublishStatus.Archived != it.publishStatus() }
            ?.with(RichSkillDescriptorDao::collections)
            ?.map { RichSkillAndCollections.fromDao(it) }
            ?.let { RichSkillXlsxExport(appConfig).toXlsx(it) }

        taskMessageService.publishResult(
            task.copy(result = xlsx, status = TaskStatus.Ready)
        )
        logger.info("Task ${task.uuid} completed")
    }

    @RqueueListener(
        value = [TaskMessageService.skillsForFullLibraryXlsx],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    override fun tabularSkillsInFullLibraryProcessor(task: XlsxTask) {
        logger.info("Started processing task for Full Library .xlsx export")

        val xlsx = richSkillRepository.findAll()
            ?.with(RichSkillDescriptorDao::collections)
            ?.map { RichSkillAndCollections.fromDao(it) }
            ?.let { RichSkillXlsxExport(appConfig).toXlsx(it) }

        taskMessageService.publishResult(
            task.copy(result = xlsx, status = TaskStatus.Ready)
        )
        logger.info("Full Library .xlsx export task completed")
    }
}
