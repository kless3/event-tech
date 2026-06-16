package com.ems.eventservice.repository

import com.ems.eventservice.domain.ProcessedKafkaMessage
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedKafkaMessageRepository : JpaRepository<ProcessedKafkaMessage, UUID>
