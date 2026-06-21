package com.ems.notificationservice.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> blockingEndpoint(block: () -> T): T =
    withContext(Dispatchers.IO) { block() }
