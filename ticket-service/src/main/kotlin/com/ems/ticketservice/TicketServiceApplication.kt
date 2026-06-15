package com.ems.ticketservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class TicketServiceApplication

fun main(args: Array<String>) {
    runApplication<TicketServiceApplication>(*args)
}
