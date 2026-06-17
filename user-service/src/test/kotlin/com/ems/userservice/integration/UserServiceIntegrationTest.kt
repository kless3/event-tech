package com.ems.userservice.integration

import com.ems.userservice.domain.User
import com.ems.userservice.repository.UserRepository
import com.ems.userservice.service.UserService
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
class UserServiceIntegrationTest {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanDatabase() {
        userRepository.deleteAll()
    }

    @Test
    fun `runs liquibase migrations and creates required tables`() {
        val changelogRows = jdbcTemplate.queryForObject(
            """
            select count(*)
            from databasechangelog
            where id in ('001-create-users-table', '002-create-outbox-events-table')
            """.trimIndent(),
            Int::class.java,
        )
        val userTableRows = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'users'",
            Int::class.java,
        )
        val outboxTableRows = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'outbox_events'",
            Int::class.java,
        )

        assertEquals(2, changelogRows)
        assertEquals(1, userTableRows)
        assertEquals(1, outboxTableRows)
    }

    @Test
    fun `creates user decrypts dek and deletes user`() {
        val user = userService.createUser("Alice@Example.COM")

        val savedUser = userRepository.findById(user.id).orElseThrow()
        val decryptedKey = userService.getUserDecryptedKey(user.id)

        assertEquals("alice@example.com", user.email)
        assertEquals("alice@example.com", savedUser.email)
        assertNotNull(savedUser.createdAt)
        assertNotNull(savedUser.updatedAt)
        assertTrue(savedUser.encryptedDek.isNotBlank())
        assertTrue(savedUser.iv.isNotBlank())
        assertNotEquals(savedUser.encryptedDek, decryptedKey.dekBase64)
        assertEquals(32, Base64.getDecoder().decode(decryptedKey.dekBase64).size)

        userService.deleteUser(user.id)

        assertFalse(userRepository.existsById(user.id))
    }

    @Test
    fun `enforces unique email in database schema`() {
        val firstUser = User(
            email = "alice@example.com",
            encryptedDek = "encrypted-1",
            iv = "iv-1",
        )
        val duplicateUser = User(
            email = "alice@example.com",
            encryptedDek = "encrypted-2",
            iv = "iv-2",
        )

        userRepository.saveAndFlush(firstUser)

        kotlin.test.assertFailsWith<DataIntegrityViolationException> {
            userRepository.saveAndFlush(duplicateUser)
        }
    }

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("user_service")
            withUsername("user_service")
            withPassword("user_service")
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl.withSslDisabled() }
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "none" }
            registry.add("spring.liquibase.enabled") { true }
            registry.add("spring.liquibase.change-log") { "classpath:db/changelog/db.changelog-master.yaml" }
            registry.add("app.outbox.enabled") { false }
            registry.add("app.security.kek-base64") {
                Base64.getEncoder().encodeToString(ByteArray(32) { 13 })
            }
        }

        private fun String.withSslDisabled(): String {
            val separator = if ("?" in this) "&" else "?"
            return "$this${separator}sslmode=disable"
        }
    }
}
