package com.ems.paymentservice.exception

import com.ems.paymentservice.domain.PaymentStatus
import java.util.UUID

class PaymentStateException(id: UUID, status: PaymentStatus) :
    RuntimeException("Payment '$id' cannot be changed from status '$status'")
