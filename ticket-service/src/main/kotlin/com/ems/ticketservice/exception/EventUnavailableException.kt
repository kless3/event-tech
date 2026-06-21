package com.ems.ticketservice.exception

import java.util.UUID

class EventUnavailableException(eventId: UUID) :
    RuntimeException("Event '$eventId' has no remaining capacity")
