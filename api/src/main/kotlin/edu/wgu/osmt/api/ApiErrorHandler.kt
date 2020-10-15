package edu.wgu.osmt.api

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import edu.wgu.osmt.api.model.ApiError
import edu.wgu.osmt.api.model.ApiFieldError
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class FormValidationException(override val message: String, val errors:List<ApiFieldError>): Exception(message)


@ControllerAdvice
class ApiErrorHandler : ResponseEntityExceptionHandler() {

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val apiError = when (ex.rootCause) {
            is MismatchedInputException -> {
                val mie = ex.rootCause as MismatchedInputException
                ApiError("JSON Parse Error", listOf(ApiFieldError(field="body", message=mie.message!!)))
            }
            else -> ApiError("Bad Request")
        }
        return ResponseEntity(apiError, status)
    }

    @ExceptionHandler(FormValidationException::class)
    fun handleFormValidationException(ex: FormValidationException): ResponseEntity<Any> {
        val apiError = ApiError(ex.message, ex.errors)
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<Any> {
        val apiError = ApiError(ex.status.toString())
        return ResponseEntity(apiError, ex.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleResponseStatus(ex: Exception, request: HttpServletRequest?, response: HttpServletResponse?): ResponseEntity<Any> {
        val apiError = ApiError(ex.message ?: "500 INTERNAL_SERVER_ERROR")
        return ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}