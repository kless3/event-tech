package com.ems.userservice.integration

import com.ems.userservice.domain.User
import com.ems.userservice.repository.UserRepository
import com.ems.userservice.service.UserService
import java.net.ServerSocket
import java.time.Duration
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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
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

        assertEquals(ALICE_EMAIL, user.email)
        assertEquals(ALICE_EMAIL, savedUser.email)
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
            email = ALICE_EMAIL,
            encryptedDek = "encrypted-1",
            iv = "iv-1",
        )
        val duplicateUser = User(
            email = ALICE_EMAIL,
            encryptedDek = "encrypted-2",
            iv = "iv-2",
        )

        userRepository.saveAndFlush(firstUser)

        kotlin.test.assertFailsWith<DataIntegrityViolationException> {
            userRepository.saveAndFlush(duplicateUser)
        }
    }

    companion object {
        private const val ALICE_EMAIL = "alice@example.com"
        private const val POSTGRES_DATABASE = "user_service"
        private const val POSTGRES_USERNAME = "user_service"
        private const val POSTGRES_PASSWORD = "user_service"
        private val postgresPort = ServerSocket(0).use { it.localPort }

        @Container
        @JvmStatic
        val postgres: GenericContainer<Nothing> = GenericContainer<Nothing>("postgres:17-alpine").apply {
            withEnv("POSTGRES_DB", POSTGRES_DATABASE)
            withEnv("POSTGRES_USER", POSTGRES_USERNAME)
            withEnv("POSTGRES_PASSWORD", POSTGRES_PASSWORD)
            withCommand("postgres", "-c", "port=$postgresPort", "-c", "fsync=off")
            waitingFor(
                Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2)
                    .withStartupTimeout(Duration.ofSeconds(30)),
            )
            withCreateContainerCmdModifier { command ->
                command.hostConfig?.withNetworkMode("host")
            }
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.hostNetworkJdbcUrl().withStableConnectionSettings() }
            registry.add("spring.datasource.username") { POSTGRES_USERNAME }
            registry.add("spring.datasource.password") { POSTGRES_PASSWORD }
            registry.add("spring.datasource.hikari.connection-timeout") { "10000" }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "none" }
            registry.add("spring.liquibase.enabled") { true }
            registry.add("spring.liquibase.change-log") { "classpath:db/changelog/db.changelog-master.yaml" }
            registry.add("app.outbox.enabled") { false }
            registry.add("app.security.kek-base64") {
                Base64.getEncoder().encodeToString(ByteArray(32) { 13 })
            }
        }

        private fun GenericContainer<Nothing>.hostNetworkJdbcUrl(): String =
            "jdbc:postgresql://127.0.0.1:$postgresPort/$POSTGRES_DATABASE"

        private fun String.withStableConnectionSettings(): String {
            val separator = if ("?" in this) "&" else "?"
            return "$this${separator}sslmode=disable&connectTimeout=5&socketTimeout=10&tcpKeepAlive=true"
        }
    }
}
