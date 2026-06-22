# StockMaster CM — Project Knowledge

## 💎 Règles Strictes — à respecter impérativement

> ⚠️ **Toute implémentation, modification ou suggestion DOIT commencer par analyser `document/A_JIRA_ET_GIT_FLOW.md` et en respecter scrupuleusement les instructions.**
> Ce fichier définit le workflow complet du projet (11 étapes obligatoires), le modèle de branches, les conventions de commit, la stratégie TDD, et la Définition of Done.
> Aucune étape ne peut être sautée, sous peine de rejet de la PR.

## Overview
**StockMaster CM** is a modular monolith SaaS for inventory management targeting Cameroonian SMBs (grocery stores, pharmacies, multi-site distributors).  
**Stack:** Java 21, Spring Boot 3.3.5, PostgreSQL 16, Redis 7, MinIO (S3), Flyway, MapStruct, jjwt 0.12.6.  
**Architecture:** 11 Maven modules sharing `stockmaster-shared` as the common library.

## Key Code Locations

| Path | Content |
|---|---|
| `stockmaster-shared/` | Core library: `AbstractEntity`, `ApiResponse<T>`, `ProblemResponse` (RFC 7807), `ErrorCode` enum, `BusinessException`, `GlobalExceptionHandler`, Flyway migrations V1-V3, `application.yml` with 3 profiles (dev/test/prod). |
| `stockmaster-auth/` | Auth module: JWT auth (jjwt 0.12.6, HS256), inscription, login, refresh, logout, forgot/reset/change password, rate limiting. `SecurityConfig.java` with `.permitAll()` for public auth endpoints. `StockMasterPrincipal` with `userId`, `entrepriseId`, `groupId`, `role`, `scope`. |
| `stockmaster-groupe/` | Group & subsidiaries — currently empty (stub). |
| `stockmaster-utilisateur/` | User management — currently empty (stub). |
| `stockmaster-catalogue/` | Catalog — currently empty (stub). |
| `stockmaster-tiers/` | Clients & suppliers — currently empty (stub). |
| `stockmaster-achat/` | Purchase orders — currently empty (stub). |
| `stockmaster-stock/` | Stock movements — currently empty (stub). |
| `stockmaster-vente/` | Sales — currently empty (stub). |
| `stockmaster-notification/` | Notifications — currently empty (stub). |
| `stockmaster-notification/` | Reporting — currently empty (stub). |
| `document/` | Specifications, backlog (75 US, 267 pts), CDCT, workflow, test strategy, Postman collection. |

## Commands

```bash
# Compile (all modules, quiet)
mvn compile -q

# Run tests (auth module — most active)
mvn test -pl stockmaster-auth

# Run specific test class
mvn test -pl stockmaster-auth -Dtest=AuthServiceImplTest

# Run all tests
mvn test

# Build without tests
mvn package -DskipTests

# Start dev environment (PostgreSQL + Redis + MinIO + MailHog)
docker compose up -d

# Run app (dev profile)
mvn spring-boot:run -pl stockmaster-shared

# Verify health
curl http://localhost:8080/actuator/health
```

## Key Conventions & Constraints

- **JWT auth:** Every protected endpoint requires `Authorization: Bearer <token>`. Public endpoints are listed in `SecurityConfig.java` `.permitAll()`.
- **Multi-tenant isolation:** `entreprise_id` comes from JWT claims (`StockMasterPrincipal.getEntrepriseId()`), NEVER from request body.
- **Soft delete:** All entities extend `AbstractEntity` with `supprime` boolean. Use `marquerCommeSupprime()`.
- **Flyway only:** `ddl-auto=none` in prod/test, `ddl-auto=validate` in dev. Never `update`.
- **Error handling:** `BusinessException(ErrorCode)` → `GlobalExceptionHandler` → RFC 7807 `ProblemResponse`.
- **Password policy:** Min 8 chars, 1 uppercase, 1 digit, 1 special char. BCrypt hashing. Pattern: `^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+={}\\[\\]|:;<>,.?/~`-]).{8,50}$`.
- **Redis usage:** Refresh tokens (`refresh:{userId}`), rate limiting, token blacklist (`blacklist:jti:{jti}`), reset tokens (`reset:{token}`).
- **Branch model:** `main` (integration) ← `feature/GS-XXX-name`. No `develop` branch.
- **Commit convention:** `type(GS-XXX): description` (feat/fix/docs/test/refactor/chore).
- **Always run before push:** `mvn test -pl stockmaster-auth` → BUILD SUCCESS, 0 failures.
- **Postman collection:** `document/postman_collection.json` — auto-chained auth, collection-wide Bearer token, random test data.

## Gotchas

- `stockmaster-shared` is a **library module** — its `pom.xml` has `<skip>true</skip>` for `spring-boot-maven-plugin` to avoid fat-JAR masking classes.
- The `AuthTestApplication` in `stockmaster-auth/src/test/` provides its own `SecurityFilterChain` — controller tests don't load the real `JwtAuthenticationFilter`, so no need to mock `StringRedisTemplate` there.
- Redis must be running for auth tests (they use `StringRedisTemplate`). CI provides Redis 7 via docker-compose services.
- Testcontainers was removed — CI uses docker-compose services directly. PostgreSQL 16 + Redis 7 must be available.
- JaCoCo coverage is configured in parent POM but only actively measured for `stockmaster-shared` and `stockmaster-auth`.
- The `implementation.md` document is the single source of truth for US tracking — must be updated on every merge.

## Environment Variables (`.env`)

```env
JWT_SECRET=<base64-encoded-256-bit-key>
DB_HOST=localhost
DB_PORT=5432
DB_NAME=stockmaster_dev
DB_USERNAME=stockmaster
DB_PASSWORD=stockmaster
REDIS_HOST=localhost
REDIS_PORT=6379
MINIO_ACCESS_KEY=stockmaster
MINIO_SECRET_KEY=stockmaster
MAIL_HOST=localhost
MAIL_PORT=1025
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```
