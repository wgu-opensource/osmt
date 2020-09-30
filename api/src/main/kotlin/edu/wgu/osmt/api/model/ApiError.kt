package edu.wgu.osmt.api.model


data class ApiError(
    val message: String? = null,
    val errors: List<ApiFieldError> = listOf()
)

data class ApiFieldError(
    val field: String,
    val message: String,
    val rowNumber: Number? = null
)
