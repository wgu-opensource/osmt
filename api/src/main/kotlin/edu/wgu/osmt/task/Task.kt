package edu.wgu.osmt.task

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSkillListUpdate
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
    JsonSubTypes.Type(value = PublishSkillsTask::class, name = "PublishSkillsTask"),
    JsonSubTypes.Type(value = ApiSkillListUpdate::class, name = "ApiSkillListUpdate"),
    JsonSubTypes.Type(value = UpdateCollectionSkillsTask::class, name = "UpdateCollectionSkillsTask")
)

interface Task {
    val uuid: String
    val start: Date
    val result: Any?
    val status: TaskStatus
    val contentType: String

    /**
     * Define the response to generate for the task type
     */
    fun toResultResponse(): HttpEntity<*>
    companion object {
        fun resultResponse(task: Task): HttpEntity<*> {
            val responseHeaders = HttpHeaders()
            responseHeaders.add("Content-Type", task.contentType)
            return ResponseEntity.ok().headers(responseHeaders).body(task.result)
        }
    }
}

data class CsvTask(
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: String? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = "text/csv"

    override fun toResultResponse(): HttpEntity<*> {
        return Task.resultResponse(this)
    }
}


data class PublishSkillsTask(
    val search: ApiSearch = ApiSearch(),
    val filterByStatus: Set<PublishStatus> = setOf(PublishStatus.Unarchived),
    val publishStatus: PublishStatus = PublishStatus.Published,
    val userString: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override fun toResultResponse(): HttpEntity<*> {
        return Task.resultResponse(this)
    }
}

data class UpdateCollectionSkillsTask(
    val collectionUuid: String,
    val skillListUpdate: ApiSkillListUpdate = ApiSkillListUpdate(),
    val publishStatuses: Set<PublishStatus> = setOf(PublishStatus.Unarchived),
    val userString: String,
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override fun toResultResponse(): HttpEntity<*> {
        return Task.resultResponse(this)
    }
}

enum class TaskStatus {
    Processing,
    Ready
}
