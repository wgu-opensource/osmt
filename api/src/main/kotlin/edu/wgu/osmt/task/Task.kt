package edu.wgu.osmt.task

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSkillListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.db.PublishStatus
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CsvTask::class, name = "CsvTask"),
    JsonSubTypes.Type(value = ApiSearch::class, name = "ApiSearch"),
    JsonSubTypes.Type(value = ApiBatchResult::class, name = "ApiBatchResult"),
    JsonSubTypes.Type(value = PublishTask::class, name = "PublishTask"),
    JsonSubTypes.Type(value = ApiSkillListUpdate::class, name = "ApiSkillListUpdate"),
    JsonSubTypes.Type(value = UpdateCollectionSkillsTask::class, name = "UpdateCollectionSkillsTask"),
    JsonSubTypes.Type(value = CreateSkillsTask::class, name = "CreateSkillsTask"),
    JsonSubTypes.Type(value = ExportSkillsToCsvTask::class, name = "ExportSkillsToCsvTask"),
    JsonSubTypes.Type(value = RemoveCollectionSkillsTask::class, name = "RemoveCollectionSkillsTask")
)

interface Task {
    val uuid: String
    val start: Date
    val result: Any?
    val status: TaskStatus
    val contentType: String
    val apiResultPath: String

    /**
     * Define the response to generate for the task type
     */
    companion object {
        fun resultResponse(task: Task): HttpEntity<*> {
            val responseHeaders = HttpHeaders()
            responseHeaders.add("Content-Type", task.contentType)
            return ResponseEntity.ok().headers(responseHeaders).body(task.result)
        }

        fun processingResponse(task: Task): HttpEntity<TaskResult> {
            val taskResult = TaskResult.fromTask(task)
            val responseHeaders = HttpHeaders()
            responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            return ResponseEntity.status(202).headers(responseHeaders).body(taskResult)
        }
    }
}

data class CsvTask(
    val collectionUuid: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: String? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = "text/csv"
    override val apiResultPath = RoutePaths.TASK_DETAIL_TEXT

}

data class ExportSkillsToCsvTask(
    val collectionUuid: String = "",
    val uuids: List<String>? = null,
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: String? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = RoutePaths.TASK_DETAIL_BATCH
}

data class CreateSkillsTask(
    val apiSkillUpdates: List<ApiSkillUpdate> = listOf(),
    val userString: String = "",
    val userEmail: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: List<String>? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = RoutePaths.TASK_DETAIL_SKILLS
}


enum class AppliesToType {
    Collection,
    Skill,
}
data class PublishTask(
    val appliesToType: AppliesToType = AppliesToType.Skill,
    val search: ApiSearch = ApiSearch(),
    val filterByStatus: Set<PublishStatus> = setOf(PublishStatus.Draft),
    val publishStatus: PublishStatus = PublishStatus.Published,
    val userString: String = "",
    val collectionUuid: String? = null,
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = RoutePaths.TASK_DETAIL_BATCH
}

data class UpdateCollectionSkillsTask(
    val collectionUuid: String = "",
    val skillListUpdate: ApiSkillListUpdate = ApiSkillListUpdate(),
    val publishStatuses: Set<PublishStatus> = setOf(PublishStatus.Draft),
    val userString: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = RoutePaths.TASK_DETAIL_BATCH
}

data class RemoveCollectionSkillsTask(
    val collectionUuid: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = RoutePaths.TASK_DETAIL_BATCH
}

enum class TaskStatus {
    Processing,
    Ready
}
