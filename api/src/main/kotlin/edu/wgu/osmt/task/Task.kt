package edu.wgu.osmt.task

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import edu.wgu.osmt.api.model.ApiBatchResult
import edu.wgu.osmt.api.model.ApiSearch
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
    JsonSubTypes.Type(value = CsvTask::class, name = "CsvTask")
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
}

data class CsvTask(
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: String? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = "text/csv"

    override fun toResultResponse(): HttpEntity<*> {
        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", contentType)
        return ResponseEntity.ok().headers(responseHeaders).body(result)
    }
}


data class PublishSkillsTask(
    val search: ApiSearch,
    val filterByStatus: Set<PublishStatus> = setOf(PublishStatus.Unpublished),
    val publishStatus: PublishStatus = PublishStatus.Published,
    val userString: String,
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE

    override fun toResultResponse(): HttpEntity<ApiBatchResult> {
        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", contentType)
        return ResponseEntity.ok().headers(responseHeaders).body(result)
    }
}

enum class TaskStatus {
    Processing,
    Ready
}
