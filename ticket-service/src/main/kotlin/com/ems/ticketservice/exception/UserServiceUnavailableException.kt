package com.ems.ticketservice.exception

class UserServiceUnavailableException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
