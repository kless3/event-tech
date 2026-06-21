package com.ems.paymentservice.repository

import com.ems.paymentservice.domain.ProcessedKafkaMessage
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedKafkaMessageRepository : JpaRepository<ProcessedKafkaMessage, UUID>
