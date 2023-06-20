package edu.wgu.osmt.task

import com.github.sonus21.rqueue.core.RqueueMessageSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class TaskMessageService {

    @Autowired
    lateinit var rqueueMessageSender: RqueueMessageSender

    @Autowired
    lateinit var redisTaskTemplate: RedisTemplate<String, Task>

    val opsForHash by lazy { redisTaskTemplate.opsForHash<String, Task>() }

    fun enqueueJob(key: String, task: Task) {
        rqueueMessageSender.enqueue(key, task)
        this.opsForHash.put(taskHashTable, task.uuid, task)
    }

    final inline fun <reified T : Task> taskResultForUuidAndType(uuid: String): T? {
        val result = opsForHash.get(taskHashTable, uuid)
        return if (result is T) {
            result
        } else {
            null
        }
    }

    fun publishResult(result: Task) {
        this.opsForHash.put(taskHashTable, result.uuid, result)
    }

    companion object {
        const val taskHashTable = "tasks"
        const val allSkillsCsv = "all-skills-csv-process"
        const val deadLetters = "dead-letters"

        const val createSkills = "create-skills"
        const val publishSkills = "batch-publish-skills"
        const val updateCollectionSkills = "update-collection-skills"
        const val skillsForCollectionCsv = "collection-skills-csv-process"
        const val skillsForCollectionCsvV2 = "collection-skills-csv-process-v2"
        
        const val skillsForCollectionXlsx = "collection-skills-xlsx-process"
        const val removeCollectionSkills = "remove-collection"
        const val skillsForFullLibraryCsv = "full-library-skills-csv-process"
        const val skillsForFullLibraryCsvV2 = "full-library-skills-csv-process-v2"
        const val skillsForFullLibraryXlsx = "full-library-skills-xlsx-process"
        const val skillsForCustomListExportCsv = "custom-rsd-list-csv-export"
        const val skillsForCustomListExportCsvV2 = "custom-rsd-list-csv-export-v2"
        const val skillsForCustomListExportXlsx = "custom-rsd-list-xlsx-export"
        const val removeJobCode = "remove-job-code"
    }
}
