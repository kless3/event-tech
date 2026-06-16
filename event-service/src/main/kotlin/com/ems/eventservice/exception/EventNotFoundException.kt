package com.ems.eventservice.exception

import java.util.UUID

class EventNotFoundException(id: UUID) : RuntimeException("Event with id '$id' was not found")
