package com.ems.ticketservice.repository

import com.ems.ticketservice.domain.ProcessedKafkaMessage
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedKafkaMessageRepository : JpaRepository<ProcessedKafkaMessage, UUID>
