package com.ems.ticketservice.exception

import java.util.UUID

class UserKeyNotFoundException(userId: UUID) : RuntimeException("User key for user '$userId' was not found")
