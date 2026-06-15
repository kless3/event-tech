package com.ems.userservice.domain

enum class OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}
