package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus

data class ApiCollectionUpdate(
    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("description")
    val description: String? = null,

    @JsonFormat(with= [JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES])
    @JsonProperty("status")
    val publishStatus: PublishStatus? = null,

    @JsonProperty("author")
    val author: String? = null,

    @JsonProperty("skills")
    val skills: ApiStringListUpdate? = null
) {

    fun validate(rowNumber:Number? = null): List<ApiFieldError>? {
        val errors = mutableListOf<ApiFieldError>()
        return if (errors.size > 0) errors else null
    }

    fun validateForCreation(rowNumber:Number? = null): List<ApiFieldError>? {
        val errors = mutableListOf<ApiFieldError>()

        if (name.isNullOrBlank()) {
            errors.add(ApiFieldError(field = "name", message = "Name is required", rowNumber = rowNumber))
        }
        validate()?.let { errors.addAll(it) }
        return if (errors.size > 0) errors else null
    }
}
