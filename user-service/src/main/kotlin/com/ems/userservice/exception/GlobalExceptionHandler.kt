package com.ems.userservice.exception

import jakarta.servlet.http.HttpServletRequest
import javax.crypto.AEADBadTagException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(
        exception: UserNotFoundException,
        request: HttpServletRequest,
    ): ProblemDetail = problemDetail(
        status = HttpStatus.NOT_FOUND,
        title = "User not found",
        detail = exception.message ?: "User was not found",
        request = request,
    )

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(
        exception: EmailAlreadyExistsException,
        request: HttpServletRequest,
    ): ProblemDetail = problemDetail(
        status = HttpStatus.CONFLICT,
        title = "Email already exists",
        detail = exception.message ?: "Email already exists",
        request = request,
    )

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(
        exception: DataIntegrityViolationException,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.warn("Database integrity violation", exception)
        return problemDetail(
            status = HttpStatus.CONFLICT,
            title = "Data integrity violation",
            detail = "Request conflicts with an existing resource",
            request = request,
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ProblemDetail {
        val errors = exception.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to fieldError.safeMessage()
        }

        return problemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Validation failed",
            detail = "Request validation failed",
            request = request,
        ).apply {
            setProperty("errors", errors)
        }
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleMethodValidation(
        exception: HandlerMethodValidationException,
        request: HttpServletRequest,
    ): ProblemDetail = problemDetail(
        status = HttpStatus.BAD_REQUEST,
        title = "Validation failed",
        detail = "Request validation failed",
        request = request,
    )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ProblemDetail = problemDetail(
        status = HttpStatus.BAD_REQUEST,
        title = "Malformed request",
        detail = "Request body is missing or malformed",
        request = request,
    )

    @ExceptionHandler(IllegalArgumentException::class, AEADBadTagException::class)
    fun handleCryptoFailure(
        exception: Exception,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.warn("Cryptographic operation failed", exception)
        return problemDetail(
            status = HttpStatus.UNPROCESSABLE_ENTITY,
            title = "Cryptographic operation failed",
            detail = "Unable to process encrypted user key",
            request = request,
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(
        exception: NoResourceFoundException,
        request: HttpServletRequest,
    ): ProblemDetail = problemDetail(
        status = HttpStatus.NOT_FOUND,
        title = "Resource not found",
        detail = "Requested resource was not found",
        request = request,
    )

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        exception: Exception,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.error("Unhandled exception while processing ${request.method} ${request.requestURI}", exception)
        return problemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            title = "Internal server error",
            detail = "Unexpected error occurred",
            request = request,
        )
    }

    private fun problemDetail(
        status: HttpStatus,
        title: String,
        detail: String,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            this.title = title
            instance = java.net.URI.create(request.requestURI)
        }

    private fun FieldError.safeMessage(): String =
        defaultMessage ?: "Invalid value"
}
