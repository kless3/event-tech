package com.ems.eventservice.exception

import java.util.UUID

class EventUnavailableException(id: UUID) : RuntimeException("Event '$id' is not available")
