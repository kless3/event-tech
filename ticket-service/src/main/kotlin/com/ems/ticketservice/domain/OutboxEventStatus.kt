package com.ems.ticketservice.domain

enum class OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}
