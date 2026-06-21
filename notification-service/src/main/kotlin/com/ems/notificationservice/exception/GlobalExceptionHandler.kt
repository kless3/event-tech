package com.ems.notificationservice.exception

import jakarta.servlet.http.HttpServletRequest
import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NotificationNotFoundException::class)
    fun handleNotificationNotFound(exception: NotificationNotFoundException, request: HttpServletRequest): ProblemDetail =
        problemDetail(HttpStatus.NOT_FOUND, "Notification not found", exception.message ?: "Notification was not found", request)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException, request: HttpServletRequest): ProblemDetail {
        val errors = exception.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to fieldError.safeMessage()
        }
        return problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed", request).apply {
            setProperty("errors", errors)
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(exception: HttpMessageNotReadableException, request: HttpServletRequest): ProblemDetail =
        problemDetail(HttpStatus.BAD_REQUEST, "Malformed request", "Request body is missing or malformed", request)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(exception: Exception, request: HttpServletRequest): ProblemDetail {
        log.error("Unhandled exception while processing ${request.method} ${request.requestURI}", exception)
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Unexpected error occurred", request)
    }

    private fun problemDetail(
        status: HttpStatus,
        title: String,
        detail: String,
        request: HttpServletRequest,
    ): ProblemDetail = ProblemDetail.forStatusAndDetail(status, detail).apply {
        this.title = title
        instance = URI.create(request.requestURI)
    }

    private fun FieldError.safeMessage(): String =
        defaultMessage ?: "Invalid value"
}
