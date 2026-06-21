package com.ems.notificationservice.repository

import com.ems.notificationservice.domain.ProcessedKafkaMessage
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedKafkaMessageRepository : JpaRepository<ProcessedKafkaMessage, UUID>
