package com.ems.paymentservice.exception

import java.util.UUID

class PaymentNotFoundException(id: UUID) : RuntimeException("Payment with id '$id' was not found")
