# StockMaster CM — Journal d'Implémentation

> **Référence :** GS-BACKLOG-2026-01 | **Projet :** StockMaster CM
> **Stack :** Java 21, Spring Boot 3.3.5, PostgreSQL 16, Redis 7, MinIO
> **Architecture :** Monolithe modulaire (11 modules)

---

## US-001 — Initialisation du projet Spring Boot ✅

**Statut :** Terminé — Commit `e47186c` → `fe9b5c9` (amendé)
**Branche :** `feature/GS-001-initialize-spring-boot-project`
**Push :** ✅ Vers `origin/feature/GS-001-initialize-spring-boot-project`

### Fichiers créés (40 fichiers, +5 389 lignes)

| Fichier | Rôle |
|---|---|
| `pom.xml` | Parent POM Spring Boot 3.3.5, Java 21, 11 modules |
| `.gitignore` | Java, Maven, IDE, OS, secrets, logs, Docker |
| `stockmaster-shared/pom.xml` | Dépendances : Web, JPA, Security, Validation, Redis, jjwt 0.12.x, MapStruct, MinIO, Flyway, PostgreSQL, Testcontainers |
| `stockmaster-shared/.../AbstractEntity.java` | Classe de base JPA : id, dateCreation, dateModification, supprime |
| `stockmaster-shared/.../ApiResponse.java` | Wrapper générique de réponse API (success, message, data) |
| `stockmaster-shared/.../ProblemResponse.java` | Réponse d'erreur RFC 7807 avec extensions StockMaster |
| `stockmaster-shared/.../ErrorCode.java` | Enum complet (AUTH_*, RES_*, CMD_*, GRP_*, STK_*, SEC_*, SYS_*) |
| `stockmaster-shared/.../BusinessException.java` | Exception métier avec ErrorCode |
| `stockmaster-shared/.../EntityNotFoundException.java` | Exception 404 avec nom d'entité et ID |
| `stockmaster-shared/.../InsufficientStockException.java` | Exception 409 avec liste des ruptures |
| `stockmaster-shared/.../GlobalExceptionHandler.java` | Handler centralisé (7 cas) — logs, jamais de stack trace exposée |
| `stockmaster-shared/.../JwtProperties.java` | @ConfigurationProperties pour JWT |
| `stockmaster-shared/.../CorsProperties.java` | @ConfigurationProperties pour CORS |
| `stockmaster-shared/.../PaginationProperties.java` | @ConfigurationProperties pour pagination |
| `stockmaster-shared/.../WebConfig.java` | CORS + JPA Auditing (Instant) |
| `stockmaster-shared/.../StockMasterApplication.java` | @SpringBootApplication avec @ComponentScan global |
| `stockmaster-shared/src/main/resources/application.yml` | 3 profils (dev/test/prod), open-in-view: false |
| 10 modules stubs (auth, groupe, utilisateur, catalogue, tiers, achat, stock, vente, notification, reporting) | Structure standard vide (controller/service/repository/domain/dto/mapper/event) |

### Choix techniques importants

1. **Monolithe modulaire** — Pas de microservices. 11 modules internes isolés par package, communication par événements Spring.
2. **ErrorCode enum** — Catalogue centralisé unique pour toutes les erreurs métier. Utilisé par BusinessException → GlobalExceptionHandler → RFC 7807.
3. **Flag JWT version** — jjwt 0.12.6 obligatoire (pas 0.9.x).
4. **open-in-view: false** — Évite les LazyInitializationException silencieuses.
5. **Flyway seul maître du schéma** — ddl-auto=none en prod/test, validate en dev.
6. **Soft delete systématique** — Champ supprime=true sur toutes les entités.
7. **Stock à la volée** — Pas de colonne dénormalisée, calcul par agrégation depuis mouvement_stock.

---

## US-002 — Configuration Flyway et schéma initial

**Statut :** ⏳ Non commencé
**Branche :** N/A

---

## US-003 — Gestion centralisée des erreurs (tests)

**Statut :** 🔄 En cours
**Branche :** `feature/GS-003-centralized-error-handling`

### Ce qui a été fait dans US-001 (partiel)
- ✅ `ErrorCode` enum complet
- ✅ `BusinessException`, `EntityNotFoundException`, `InsufficientStockException`
- ✅ `GlobalExceptionHandler` avec 7 cas couverts
- ✅ Format RFC 7807
- ❌ Tests unitaires manquants

### À faire dans cette US
- [ ] Tests unitaires pour GlobalExceptionHandler (tous les cas)
- [ ] Tests des exceptions métier
- [ ] Tests de validation RFC 7807 (stack trace jamais exposée)

---

## US-004 — Pipeline CI/CD GitHub Actions

**Statut :** ⏳ Non commencé
**Branche :** N/A

### Critères
- [ ] Workflow CI : compilation → tests → JaCoCo ≥ 80% → SonarCloud → JAR
- [ ] Workflow CD : build Docker → push GHCR → déploiement SSH staging
- [ ] Services Docker PostgreSQL 16 + Redis 7 dans CI
- [ ] Stratégie de branches documentée

---

## US-005 — Conteneurisation Docker

**Statut :** ⏳ Non commencé
**Branche :** N/A

### Critères
- [ ] Dockerfile multi-stage (JDK 21 → JRE 21 Alpine)
- [ ] Utilisateur non-root `stockmaster`
- [ ] HEALTHCHECK /actuator/health
- [ ] docker-compose.yml : API + PostgreSQL 16 + Redis 7 + MinIO + MailHog
- [ ] .env.example (gitignored .env)
- [ ] Flags JVM conteneur optimisés

---

## Règles de commit utilisées

```
type(GS-XXX): description en minuscule
```

Types : feat, fix, docs, refactor, test, chore, style
Scope = ID du ticket Jira (ex: GS-001)
