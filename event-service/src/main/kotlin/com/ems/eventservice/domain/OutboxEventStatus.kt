package com.ems.eventservice.domain

enum class OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}
