package com.ems.userservice.service

import com.ems.userservice.crypto.CryptoService
import com.ems.userservice.domain.OutboxEvent
import com.ems.userservice.domain.User
import com.ems.userservice.exception.EmailAlreadyExistsException
import com.ems.userservice.exception.UserNotFoundException
import com.ems.userservice.messaging.OutboxEventFactory
import com.ems.userservice.repository.OutboxEventRepository
import com.ems.userservice.repository.UserRepository
import java.util.Base64
import java.util.Optional
import java.util.UUID
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class UserServiceTest {
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val outboxEventRepository = Mockito.mock(OutboxEventRepository::class.java)
    private val outboxEventFactory = Mockito.mock(OutboxEventFactory::class.java)
    private val cryptoService = CryptoService(SecretKeySpec(ByteArray(32) { 11 }, "AES"))
    private val userService = UserService(userRepository, outboxEventRepository, outboxEventFactory, cryptoService)

    @Test
    fun `creates user with normalized email and encrypted dek`() {
        Mockito.`when`(userRepository.existsByEmail("alice@example.com")).thenReturn(false)
        Mockito.`when`(userRepository.save(Mockito.any(User::class.java))).thenAnswer { invocation ->
            invocation.getArgument<User>(0)
        }

        val response = userService.createUser("  Alice@Example.COM  ")

        val userCaptor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(userRepository).save(userCaptor.capture())
        val savedUser = userCaptor.value

        assertEquals(savedUser.id, response.id)
        assertEquals("alice@example.com", response.email)
        assertEquals("alice@example.com", savedUser.email)
        assertTrue(savedUser.encryptedDek.isNotBlank())
        assertTrue(savedUser.iv.isNotBlank())
        assertEquals(12, Base64.getDecoder().decode(savedUser.iv).size)
        assertNotEquals(savedUser.encryptedDek, cryptoService.decryptDekToBase64(savedUser.encryptedDek, savedUser.iv))
    }

    @Test
    fun `throws when email already exists`() {
        Mockito.`when`(userRepository.existsByEmail("alice@example.com")).thenReturn(true)

        assertFailsWith<EmailAlreadyExistsException> {
            userService.createUser("alice@example.com")
        }

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User::class.java))
    }

    @Test
    fun `returns decrypted user dek`() {
        val dek = cryptoService.generateDek()
        val encryptedDek = cryptoService.encryptDek(dek)
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "alice@example.com",
            encryptedDek = encryptedDek.ciphertextBase64,
            iv = encryptedDek.ivBase64,
        )
        Mockito.`when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val response = userService.getUserDecryptedKey(userId)

        assertEquals(userId, response.userId)
        assertEquals(Base64.getEncoder().encodeToString(dek.encoded), response.dekBase64)
    }

    @Test
    fun `deletes existing user`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "alice@example.com",
            encryptedDek = "encrypted",
            iv = "iv",
        )
        val outboxEvent = OutboxEvent(
            aggregateType = "user",
            aggregateId = userId,
            eventType = "user.deleted",
            topic = "ems.user.deleted",
            messageKey = userId.toString(),
            payload = "{}",
        )
        Mockito.`when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        Mockito.`when`(outboxEventFactory.userDeleted(userId)).thenReturn(outboxEvent)

        userService.deleteUser(userId)

        Mockito.verify(outboxEventRepository).save(outboxEvent)
        Mockito.verify(userRepository).delete(user)
    }

    @Test
    fun `throws when user is missing`() {
        val userId = UUID.randomUUID()
        Mockito.`when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        assertFailsWith<UserNotFoundException> {
            userService.getUserDecryptedKey(userId)
        }
    }
}
