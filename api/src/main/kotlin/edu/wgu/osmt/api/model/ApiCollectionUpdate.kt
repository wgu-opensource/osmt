package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import edu.wgu.osmt.db.PublishStatus

data class ApiCollectionUpdate(
    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("status")
    val publishStatus: PublishStatus? = null,

    @JsonProperty("author")
    val author: ApiNamedReference? = null,

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
