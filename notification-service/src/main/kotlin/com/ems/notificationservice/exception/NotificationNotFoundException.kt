package com.ems.notificationservice.exception

import java.util.UUID

class NotificationNotFoundException(id: UUID) : RuntimeException("Notification with id '$id' was not found")
