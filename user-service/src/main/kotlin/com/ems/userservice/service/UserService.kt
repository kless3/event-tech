package com.ems.userservice.service

import com.ems.userservice.crypto.CryptoService
import com.ems.userservice.domain.User
import com.ems.userservice.dto.response.DecryptedKeyResponse
import com.ems.userservice.dto.response.UserResponse
import com.ems.userservice.exception.EmailAlreadyExistsException
import com.ems.userservice.exception.UserNotFoundException
import com.ems.userservice.mapper.toResponse
import com.ems.userservice.messaging.OutboxEventFactory
import com.ems.userservice.repository.OutboxEventRepository
import com.ems.userservice.repository.UserRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventFactory: OutboxEventFactory,
    private val cryptoService: CryptoService,
) {
    @Transactional
    fun createUser(email: String): UserResponse {
        val normalizedEmail = email.trim().lowercase()
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw EmailAlreadyExistsException(normalizedEmail)
        }

        val dek = cryptoService.generateDek()
        val encryptedDek = cryptoService.encryptDek(dek)
        val user = User(
            email = normalizedEmail,
            encryptedDek = encryptedDek.ciphertextBase64,
            iv = encryptedDek.ivBase64,
        )

        return userRepository.save(user).toResponse()
    }

    @Transactional(readOnly = true)
    fun getUserDecryptedKey(id: UUID): DecryptedKeyResponse {
        val user = findUser(id)
        return DecryptedKeyResponse(
            userId = user.id,
            dekBase64 = cryptoService.decryptDekToBase64(user.encryptedDek, user.iv),
        )
    }

    @Transactional
    fun deleteUser(id: UUID) {
        val user = findUser(id)
        outboxEventRepository.save(outboxEventFactory.userDeleted(user.id))
        userRepository.delete(user)
    }

    private fun findUser(id: UUID): User =
        userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
}
