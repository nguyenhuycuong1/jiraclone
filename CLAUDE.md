# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
mvn clean package

# Run (requires PostgreSQL running)
mvn spring-boot:run

# Run tests (uses H2 in-memory, no PostgreSQL needed)
mvn test

# Run a single test class
mvn test -Dtest=JiraCloneApplicationTests
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `JWT_SECRET` | (base64 default in yml) | Base64-encoded 256-bit JWT secret |

Database: `jdbc:postgresql://localhost:5432/jiraclone` — create it with `createdb jiraclone`. Hibernate auto-updates the schema (`ddl-auto: update`).

## Architecture

**Stack:** Spring Boot 3.3.5, Spring Security + JWT (JJWT 0.12.3), Spring Data JPA, PostgreSQL, Java 17, Lombok.

**Package layout** (`com.jiraclone`):

- `controller/` — REST controllers; currently only `AuthController` at `/api/auth`
- `service/` — business logic; currently `AuthService`
- `security/` — JWT infrastructure: `JwtTokenProvider`, `JwtAuthenticationFilter`, `UserDetailsServiceImpl`
- `entity/` — JPA entities (`User`, `Role` enum)
- `repository/` — Spring Data JPA repositories
- `dto/auth/` — request/response DTOs
- `exception/` — `AppException` (runtime) + `GlobalExceptionHandler` (`@RestControllerAdvice`)
- `config/` — `SecurityConfig` (Spring Security filter chain)

**Auth flow:**
1. `POST /api/auth/register` and `POST /api/auth/login` are the only public endpoints.
2. All other requests require `Authorization: Bearer <token>`.
3. `JwtAuthenticationFilter` validates the token and populates the `SecurityContext`.
4. Sessions are stateless; CSRF is disabled.

**Testing:** Tests run against H2 in-memory with profile `test` (`src/test/resources/application-test.yml`, `ddl-auto: create-drop`). Add `@ActiveProfiles("test")` when writing new test classes.