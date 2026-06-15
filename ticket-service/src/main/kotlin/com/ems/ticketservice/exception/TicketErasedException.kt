package com.ems.ticketservice.exception

import java.util.UUID

class TicketErasedException(id: UUID) : RuntimeException("Ticket with id '$id' has been erased for GDPR")
