package edu.wgu.osmt.richskill

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.elasticsearch.SearchService
import edu.wgu.osmt.task.PublishSkillsTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskQueueHandler
import edu.wgu.osmt.task.TaskStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
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

    @Autowired
    lateinit var searchService: SearchService


    @Autowired
    lateinit var appConfig: AppConfig

    private fun publish_skill(skillDao: RichSkillDescriptorDao, task: PublishSkillsTask): Boolean {
        if (skillDao.publishStatus() != task.publishStatus) {
            val updateObj = RsdUpdateObject(publishStatus = task.publishStatus)
            richSkillRepository.update(updateObj, task.userString)
            return true
        }
        return false
    }

    @RqueueListener(
        value = [TaskMessageService.publishSkills],
        deadLetterQueueListenerEnabled = "true",
        deadLetterQueue = TaskMessageService.deadLetters,
        concurrency = "1"
    )
    fun publishSkills(publishSkillsTask: PublishSkillsTask) {
        logger.info("Started processing publish skills task id: ${publishSkillsTask.uuid}")

        var modifiedCount = 0
        var totalCount = 0

        val handle_skill_dao = {skillDao: RichSkillDescriptorDao? ->
            skillDao?.let {
                if (publish_skill(it, publishSkillsTask)) {
                    modifiedCount += 1
                }
            }
        }

        if (!publishSkillsTask.search.uuids.isNullOrEmpty()) {
            totalCount = publishSkillsTask.search.uuids.size
            publishSkillsTask.search.uuids.forEach { uuid ->
                handle_skill_dao(richSkillRepository.findByUUID(uuid))
            }
        } else {
            val searchHits = searchService.searchRichSkillsByApiSearch(
                publishSkillsTask.search,
                publishSkillsTask.filterByStatus,
                Pageable.unpaged()
            )
            totalCount = searchHits.totalHits.toInt()
            searchHits.forEach { hit ->
                handle_skill_dao(richSkillRepository.findById(hit.content.id))
            }
        }

        val result = ApiBatchResult(
            success = true,
            modifiedCount = modifiedCount,
            totalCount = totalCount
        )
        taskMessageService.publishResult(
            publishSkillsTask.copy(result=result, status=TaskStatus.Ready)
        )

        logger.info("Task ${publishSkillsTask.uuid} completed")
    }
}