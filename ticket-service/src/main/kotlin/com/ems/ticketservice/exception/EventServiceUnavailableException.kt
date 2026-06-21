package com.ems.ticketservice.exception

class EventServiceUnavailableException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
