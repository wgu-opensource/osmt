package edu.wgu.osmt.searchhub

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.CreateSkillsTaskProcessor
import edu.wgu.osmt.searchhub.client.apis.SharingApi
import edu.wgu.osmt.searchhub.client.models.Urls
import edu.wgu.osmt.task.ShareExternallyTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URI
import javax.transaction.Transactional

@Component
@Profile("apiserver")
@Transactional
class ShareExternallyTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(CreateSkillsTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var sharingApi: SharingApi

    @RqueueListener(
            value = [TaskMessageService.shareSkillExternally],
            deadLetterQueueListenerEnabled = "true",
            deadLetterQueue = TaskMessageService.deadLetters,
            concurrency = "1"
    )
    fun processShareSkill(task: ShareExternallyTask) {
        logger.info("Started processShareSkill uuid: ${task.uuid}")

        val urls = listOf(Urls(listOf(URI(task.canonicalUrl))))

        if (task.shared) {
            sharingApi.submitSkills(urls=urls)
        } else {
            sharingApi.removeSkills(urls=urls)
        }

        val results = "OK"
        taskMessageService.publishResult(
                task.copy(result=results, status= TaskStatus.Ready)
        )

        logger.info("Task ${task.uuid} completed")
    }

    @RqueueListener(
            value = [TaskMessageService.shareCollectionExternally],
            deadLetterQueueListenerEnabled = "true",
            deadLetterQueue = TaskMessageService.deadLetters,
            concurrency = "1"
    )
    fun processShareCollection(task: ShareExternallyTask) {
        logger.info("Started processShareCollection uuid: ${task.uuid}")

        val urls = listOf(Urls(listOf(URI(task.canonicalUrl))))

        if (task.shared) {
            sharingApi.submitCollections(urls=urls)
        } else {
            sharingApi.removeCollections(urls=urls)
        }

        val results = "OK"
        taskMessageService.publishResult(
                task.copy(result=results, status= TaskStatus.Ready)
        )

        logger.info("processShareCollection ${task.uuid} completed")
    }
}
