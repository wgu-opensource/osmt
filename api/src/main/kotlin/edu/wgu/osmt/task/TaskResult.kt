package edu.wgu.osmt.task

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.ALWAYS)
data class TaskResult(
    val uuid: String,

    @get:JsonProperty("content-type")
    val contentType: String,

    val status: TaskStatus
) {

    @get:JsonProperty("id")
    val uri: String
        get() = "/api/tasks/$uuid"

    companion object {
        fun fromTask(task: Task) = TaskResult(task.uuid, task.contentType, task.status)
    }
}
