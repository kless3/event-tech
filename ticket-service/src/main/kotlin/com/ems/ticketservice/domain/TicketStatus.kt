package com.ems.ticketservice.domain

enum class TicketStatus {
    PENDING_PAYMENT,
    ACTIVE,
    PAYMENT_FAILED,
    CANCELLED,
    USER_ERASED,
}
