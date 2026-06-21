package com.ems.ticketservice.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(TicketNotFoundException::class)
    fun handleTicketNotFound(exception: TicketNotFoundException, request: HttpServletRequest): ProblemDetail =
        problemDetail(HttpStatus.NOT_FOUND, "Ticket not found", exception.message ?: "Ticket was not found", request)

    @ExceptionHandler(TicketErasedException::class)
    fun handleTicketErased(exception: TicketErasedException, request: HttpServletRequest): ProblemDetail =
        problemDetail(HttpStatus.GONE, "Ticket erased", exception.message ?: "Ticket has been erased", request)

    @ExceptionHandler(UserKeyNotFoundException::class)
    fun handleUserKeyNotFound(exception: UserKeyNotFoundException, request: HttpServletRequest): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "User key not found", exception.message ?: "User key was not found", request)

    @ExceptionHandler(EventUnavailableException::class)
    fun handleEventUnavailable(exception: EventUnavailableException, request: HttpServletRequest): ProblemDetail =
        problemDetail(HttpStatus.CONFLICT, "Event unavailable", exception.message ?: "Event cannot reserve a ticket", request)

    @ExceptionHandler(UserServiceUnavailableException::class)
    fun handleUserServiceUnavailable(
        exception: UserServiceUnavailableException,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.warn("User Service interaction failed", exception)
        return problemDetail(HttpStatus.BAD_GATEWAY, "User Service unavailable", "Unable to retrieve user key", request)
    }

    @ExceptionHandler(EventServiceUnavailableException::class)
    fun handleEventServiceUnavailable(
        exception: EventServiceUnavailableException,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.warn("Event Service interaction failed", exception)
        return problemDetail(HttpStatus.BAD_GATEWAY, "Event Service unavailable", "Unable to verify event availability", request)
    }

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
