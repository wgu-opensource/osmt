package edu.wgu.osmt.richskill

import com.github.sonus21.rqueue.annotation.RqueueListener
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.api.model.ApiSkill
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.searchhub.SearchHubController
import edu.wgu.osmt.task.ShareExternallyTask
import edu.wgu.osmt.task.TaskMessageService
import edu.wgu.osmt.task.TaskStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
@Profile("apiserver")
@Transactional
class ImportExternalTaskProcessor {
    val logger: Logger = LoggerFactory.getLogger(CreateSkillsTaskProcessor::class.java)

    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var searchHubController: SearchHubController

    @Autowired
    private lateinit var importExternalService: ImportExternalService

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    @RqueueListener(
            value = [TaskMessageService.importSkills],
            deadLetterQueueListenerEnabled = "true",
            deadLetterQueue = TaskMessageService.deadLetters,
            concurrency = "1"
    )
    fun processImportSkill(task: ShareExternallyTask) {
        logger.info("Started processImportSkill uuid: ${task.uuid}")

        var success = false
        importExternalService.fetchSkillFromUrl(task.canonicalUrl)?.let { apiSkill ->
            val skillDao = richSkillRepository.importFromApi(apiSkill=apiSkill,
                    originalUrl=task.canonicalUrl,
                    originalLibraryName=task.libraryName,
                    user=task.userString)
            if (skillDao != null) {
                success = true
            }
        }

        val results = ApiBatchResult(success=success, totalCount=1)
        taskMessageService.publishResult(
                task.copy(result=results, status=TaskStatus.Ready)
        )

        logger.info("processImportSkill ${task.uuid} completed")
    }


    @RqueueListener(
            value = [TaskMessageService.importCollections],
            deadLetterQueueListenerEnabled = "true",
            deadLetterQueue = TaskMessageService.deadLetters,
            concurrency = "1"
    )
    fun processImportCollection(task: ShareExternallyTask) {
        logger.info("Started processImportCollection uuid: ${task.uuid}")

        var success = false
        var totalCount = 0
        var modifiedCount = 0
        importExternalService.fetchCollectionFromUrl(task.canonicalUrl)?.let { apiCollection ->

            val collectionDao = collectionRepository.importFromApi(apiCollection,
                    originalUrl=task.canonicalUrl,
                    originalLibraryName=task.libraryName,
                    richSkillRepository=richSkillRepository,
                    user=task.userString)

            if (collectionDao != null) {
                val skillUrls = apiCollection.skills?.map { it.id }
                totalCount = skillUrls.size
                skillUrls.forEach { skillUrl ->
                    importExternalService.fetchSkillFromUrl(skillUrl)?.let { apiSkill ->
                        val skillDao = richSkillRepository.importFromApi(apiSkill=apiSkill,
                                originalUrl=task.canonicalUrl,
                                originalLibraryName=task.libraryName,
                                user=task.userString)
                        if (skillDao != null) {
                            modifiedCount += 1
                            CollectionSkills.create(collectionId = collectionDao.id.value, skillId = skillDao.id.value)
                        }
                    }
                }
                success = true
            }
        }


        val results = ApiBatchResult(success=success, totalCount=totalCount, modifiedCount = modifiedCount)
        taskMessageService.publishResult(
                task.copy(result=results, status=TaskStatus.Ready)
        )

        logger.info("processImportCollection ${task.uuid} completed")
    }
}
