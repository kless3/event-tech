package com.ems.ticketservice.exception

import java.util.UUID

class TicketNotFoundException(id: UUID) : RuntimeException("Ticket with id '$id' was not found")
