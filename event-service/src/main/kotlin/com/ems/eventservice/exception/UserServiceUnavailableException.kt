package com.ems.eventservice.exception

class UserServiceUnavailableException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
