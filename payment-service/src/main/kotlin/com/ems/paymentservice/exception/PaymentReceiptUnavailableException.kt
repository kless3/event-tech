package com.ems.paymentservice.exception

import java.util.UUID

class PaymentReceiptUnavailableException(id: UUID) :
    RuntimeException("Receipt for payment '$id' is not available")
