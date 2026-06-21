package com.ems.ticketservice.client

import com.ems.ticketservice.dto.response.UserKeyResponse
import com.ems.ticketservice.exception.UserKeyNotFoundException
import com.ems.ticketservice.exception.UserServiceUnavailableException
import java.util.UUID
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

fun interface UserKeyClient {
    fun getUserDek(userId: UUID): UserKeyResponse
}

@Component
class HttpUserKeyClient(
    @Qualifier("userServiceRestClient")
    private val userServiceRestClient: RestClient,
) : UserKeyClient {
    override fun getUserDek(userId: UUID): UserKeyResponse =
        try {
            userServiceRestClient.get()
                .uri("/api/v1/users/{id}/decrypt-key", userId)
                .retrieve()
                .body(UserKeyResponse::class.java)
                ?: throw UserServiceUnavailableException("User Service returned an empty key response")
        } catch (exception: RestClientResponseException) {
            when (exception.statusCode) {
                HttpStatus.NOT_FOUND -> throw UserKeyNotFoundException(userId)
                else -> throw UserServiceUnavailableException("User Service request failed", exception)
            }
        } catch (exception: RestClientException) {
            throw UserServiceUnavailableException("User Service is unavailable", exception)
        }
}
