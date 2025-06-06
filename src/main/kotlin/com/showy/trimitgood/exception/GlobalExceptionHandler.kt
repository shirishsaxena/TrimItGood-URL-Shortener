package com.showy.trimitgood.exception

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val statusCode: Int,
    val message: String,
    val errorDetails: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

@ControllerAdvice
@RestController
class GlobalExceptionHandler {

    @ExceptionHandler(RequestException::class)
    fun handleRequestException(ex: RequestException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = ex.statusCode,
            message = ex.message,
            errorDetails = ex.errorDetails
        )

        return ResponseEntity(errorResponse, HttpStatus.valueOf(ex.statusCode))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Invalid input",
            errorDetails = ex.localizedMessage
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }

        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed",
            errorDetails = errorMessage
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred",
            errorDetails = ex.localizedMessage
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
