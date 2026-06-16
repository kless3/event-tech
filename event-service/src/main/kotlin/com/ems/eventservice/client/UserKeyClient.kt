package com.ems.eventservice.client

import com.ems.eventservice.dto.response.UserKeyResponse
import com.ems.eventservice.exception.UserKeyNotFoundException
import com.ems.eventservice.exception.UserServiceUnavailableException
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

interface UserKeyClient {
    fun getUserDek(userId: UUID): UserKeyResponse
}

@Component
class HttpUserKeyClient(
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
