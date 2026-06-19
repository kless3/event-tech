package com.ems.paymentservice.domain

enum class OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}
