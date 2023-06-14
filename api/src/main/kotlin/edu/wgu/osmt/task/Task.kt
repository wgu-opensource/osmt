package edu.wgu.osmt.task

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.*
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
    JsonSubTypes.Type(value = CsvTaskV2::class, name = "CsvTaskV2"),
    JsonSubTypes.Type(value = XlsxTask::class, name = "XlsxTask"),
    JsonSubTypes.Type(value = ApiSearch::class, name = "ApiSearch"),
    JsonSubTypes.Type(value = ApiBatchResult::class, name = "ApiBatchResult"),
    JsonSubTypes.Type(value = PublishTask::class, name = "PublishTask"),
    JsonSubTypes.Type(value = PublishTaskV2::class, name = "PublishTaskV2"),
    JsonSubTypes.Type(value = ApiSkillListUpdate::class, name = "ApiSkillListUpdate"),
    JsonSubTypes.Type(value = UpdateCollectionSkillsTask::class, name = "UpdateCollectionSkillsTask"),
    JsonSubTypes.Type(value = CreateSkillsTask::class, name = "CreateSkillsTask"),
    JsonSubTypes.Type(value = CreateSkillsTaskV2::class, name = "CreateSkillsTaskV2"),
    JsonSubTypes.Type(value = ExportSkillsToCsvTask::class, name = "ExportSkillsToCsvTask"),
    JsonSubTypes.Type(value = ExportSkillsToXlsxTask::class, name = "ExportSkillsToXlsxTask"),
    JsonSubTypes.Type(value = RemoveCollectionSkillsTask::class, name = "RemoveCollectionSkillsTask"),
    JsonSubTypes.Type(value = ExportSkillsToCsvTaskV2::class, name = "ExportSkillsToCsvTaskV2")
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
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.TASK_DETAIL_TEXT}"
}

data class CsvTaskV2(
        val collectionUuid: String = "",
        override val uuid: String = UUID.randomUUID().toString(),
        override val start: Date = Date(),
        override val result: String? = null,
        override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = "text/csv"
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.TASK_DETAIL_TEXT}"
}

data class XlsxTask(
    val collectionUuid: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ByteArray? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = "application/vnd.ms-excel"
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.TASK_DETAIL_MEDIA}"
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
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.TASK_DETAIL_BATCH}"
}

data class ExportSkillsToCsvTaskV2(
        val collectionUuid: String = "",
        val uuids: List<String>? = null,
        override val uuid: String = UUID.randomUUID().toString(),
        override val start: Date = Date(),
        override val result: String? = null,
        override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.TASK_DETAIL_BATCH}"
}

data class ExportSkillsToXlsxTask(
    val collectionUuid: String = "",
    val uuids: List<String>? = null,
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ByteArray? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = "application/vnd.ms-excel"
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.TASK_DETAIL_BATCH}"
}

data class CreateSkillsTask(
    val apiSkillUpdates: List<ApiSkillUpdate> = listOf(),
    val userString: String = "",
    val userIdentifier: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: List<String>? = null,
    override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.TASK_DETAIL_SKILLS}"
}

data class CreateSkillsTaskV2(
        val apiSkillUpdates: List<ApiSkillUpdateV2> = listOf(),
        val userString: String = "",
        val userIdentifier: String = "",
        override val uuid: String = UUID.randomUUID().toString(),
        override val start: Date = Date(),
        override val result: List<String>? = null,
        override val status: TaskStatus = TaskStatus.Processing
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.TASK_DETAIL_SKILLS}"
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
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.TASK_DETAIL_BATCH}"
}

data class PublishTaskV2(
        val appliesToType: AppliesToType = AppliesToType.Skill,
        val search: ApiSearchV2 = ApiSearchV2(),
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
    override val apiResultPath = "${RoutePaths.API}${RoutePaths.API_V2}${RoutePaths.TASK_DETAIL_BATCH}"
}

data class UpdateCollectionSkillsTask(
    val collectionUuid: String = "",
    val skillListUpdate: ApiSkillListUpdate = ApiSkillListUpdate(),
    val publishStatuses: Set<PublishStatus> = setOf(PublishStatus.Draft),
    val userString: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing,
    override val apiResultPath: String = ""
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE
    
}

data class RemoveCollectionSkillsTask(
    val collectionUuid: String = "",
    override val uuid: String = UUID.randomUUID().toString(),
    override val start: Date = Date(),
    override val result: ApiBatchResult? = null,
    override val status: TaskStatus = TaskStatus.Processing,
    override val apiResultPath: String = ""
) : Task {
    override val contentType = MediaType.APPLICATION_JSON_VALUE

}


enum class TaskStatus {
    Processing,
    Ready
}
