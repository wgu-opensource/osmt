package edu.wgu.osmt.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties("apiResultPath")
data class TaskResult(
    val uuid: String,

    @get:JsonProperty("content-type")
    val contentType: String,

    val status: TaskStatus,

    val apiResultPath: String
) {

    @get:JsonProperty("id")
    val resultUri: String
        get() = apiResultPath.replace("{uuid}", uuid)

    companion object {
        fun fromTask(task: Task) = TaskResult(task.uuid, task.contentType, task.status, task.apiResultPath)
    }
}
