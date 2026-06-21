package com.ems.notificationservice.controller

import com.ems.notificationservice.dto.response.NotificationResponse
import com.ems.notificationservice.service.NotificationService
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {
    @GetMapping("/{id}")
    suspend fun getNotification(@PathVariable id: UUID): NotificationResponse =
        blockingEndpoint { notificationService.getNotification(id) }

    @GetMapping("/users/{userId}")
    suspend fun getNotificationsForUser(@PathVariable userId: UUID): List<NotificationResponse> =
        blockingEndpoint { notificationService.getNotificationsForUser(userId) }
}
