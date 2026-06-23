package com.ems.notificationservice.api

import com.ems.notificationservice.dto.response.NotificationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@Tag(name = "Notifications", description = "Notification delivery lookup endpoints")
interface NotificationApi {
    @Operation(summary = "Get notification details")
    @ApiResponse(responseCode = "200", description = "Notification found")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    suspend fun getNotification(
        @Parameter(description = "Notification id") id: UUID,
    ): NotificationResponse

    @Operation(summary = "Get notifications for a user")
    @ApiResponse(responseCode = "200", description = "Notifications found")
    suspend fun getNotificationsForUser(
        @Parameter(description = "User id") userId: UUID,
    ): List<NotificationResponse>
}
