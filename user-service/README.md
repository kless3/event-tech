# User Service

`user-service` manages users for the Event Management System. It stores user accounts and owns per-user cryptographic keys used to protect sensitive user-related data.

The service is built with Kotlin, Spring Boot 4, Spring Data JPA, and PostgreSQL. It uses envelope encryption to keep user DEKs encrypted at rest and supports crypto-shredding by deleting the encrypted key material together with the user record.

## Key Features

- User creation by unique email.
- PostgreSQL persistence with Spring Data JPA.
- Database schema management with Liquibase.
- Per-user 256-bit DEK generation.
- Envelope encryption: each user DEK is encrypted with a service-level KEK.
- AES/GCM/NoPadding authenticated encryption.
- Crypto-shredding through physical deletion of the user row, encrypted DEK, and IV.
- Decrypted DEK retrieval in Base64 for trusted internal service usage.
- Request validation with `jakarta.validation`.
- Standardized RFC 7807 error responses with `ProblemDetail`.
- Dockerfile for the service and shared Docker Compose setup at the EMS project root.

## Package Layout

```text
config       # application configuration and KEK setup
controller   # REST API
crypto       # DEK generation, encryption, and decryption
domain       # JPA entities
dto          # request and response models
exception    # domain exceptions and global error handling
mapper       # entity-to-DTO mapping
repository   # data access
service      # transactional business logic
```

## Run

The KEK is required and must be a 256-bit AES key encoded as Base64:

```bash
export APP_SECURITY_KEK_BASE64="$(openssl rand -base64 32)"
```

From the EMS project root:

```bash
docker compose up --build
```

## Build

```bash
./gradlew test
./gradlew bootJar
```
