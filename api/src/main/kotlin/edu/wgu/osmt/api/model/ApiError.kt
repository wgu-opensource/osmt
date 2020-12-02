package edu.wgu.osmt.api.model

import com.fasterxml.jackson.annotation.JsonInclude


@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiError(
    val message: String? = null,
    val errors: List<ApiFieldError> = listOf()
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiFieldError(
    val field: String,
    val message: String,
    val rowNumber: Number? = null
)
