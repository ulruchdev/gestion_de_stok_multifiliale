# StockMaster CM — Journal d'Implémentation

> **Référence :** GS-BACKLOG-2026-01 | **Stack :** Java 21, Spring Boot 3.3.5, PostgreSQL 16, Redis 7, MinIO
> **Architecture :** Monolithe modulaire (11 modules)

---

## US-001 — Initialisation du projet Spring Boot ✅

**Branche :** `feature/GS-001-initialize-spring-boot-project` → Pushé ✅  
**Commit :** `e47186c` puis `0357763` (doc)

### Fichiers (40 fichiers, +5 389 lignes)
| Fichier | Rôle |
|---|---|
| `pom.xml` | Parent POM Spring Boot 3.3.5, Java 21, 11 modules |
| `.gitignore` | Java, Maven, IDE, secrets, logs, Docker |
| `stockmaster-shared/.../AbstractEntity.java` | id, dateCreation, dateModification, supprime |
| `stockmaster-shared/.../ApiResponse.java` | Wrapper réponse générique |
| `stockmaster-shared/.../ProblemResponse.java` | RFC 7807 Problem Details |
| `stockmaster-shared/.../ErrorCode.java` | 30 codes (AUTH, RES, CMD, GRP, STK, SEC, SYS) |
| `stockmaster-shared/.../BusinessException.java` | Exception métier avec ErrorCode |
| `stockmaster-shared/.../EntityNotFoundException.java` | 404 avec détails |
| `stockmaster-shared/.../InsufficientStockException.java` | 409 avec liste ruptures |
| `stockmaster-shared/.../GlobalExceptionHandler.java` | 9 cas couverts, logs, pas de stack trace |
| `stockmaster-shared/.../JwtProperties.java` | @ConfigurationProperties JWT |
| `stockmaster-shared/.../CorsProperties.java` | @ConfigurationProperties CORS |
| `stockmaster-shared/.../PaginationProperties.java` | @ConfigurationProperties pagination |
| `stockmaster-shared/.../WebConfig.java` | CORS + JPA Auditing |
| `stockmaster-shared/.../StockMasterApplication.java` | Point d'entrée, scan global |
| `application.yml` | 3 profils (dev/test/prod), open-in-view: false |
| 10 modules stubs | auth, groupe, utilisateur, catalogue, tiers, achat, stock, vente, notification, reporting |

---

## US-002 — Configuration Flyway et schéma initial ⏳

**Branche :** Non commencée

---

## US-003 — Gestion centralisée des erreurs (tests) ✅

**Branche :** `feature/GS-003-centralized-error-handling` → Pushé ✅  
**Commit :** `c03bff2` — `test(GS-003): add 18 unit tests for GlobalExceptionHandler`

### Tests créés
- `GlobalExceptionHandlerTest.java` — 18 tests unitaires couvrant :
  - BusinessException (6 statuts HTTP : 400, 401, 403, 404, 409, 429)
  - EntityNotFoundException (2 variantes : par ID, par champ)
  - InsufficientStockException (liste des ruptures)
  - MethodArgumentNotValidException (2 champs en erreur)
  - AccessDeniedException (403 + code SEC_001)
  - NoHandlerFoundException (404)
  - HttpRequestMethodNotSupportedException (405)
  - HttpMessageNotReadableException (400)
  - MethodArgumentTypeMismatchException (400 + nom paramètre)
  - Fallback Exception → 500 sans stack trace
  - RFC 7807 format complet + pattern du type

---

## US-004 — Pipeline CI/CD GitHub Actions ✅

**Branche :** `feature/GS-004-ci-cd-pipeline` → Pushé ✅  
**Commits :** `911fb49` puis `a214006`

### Fichiers
| Fichier | Rôle |
|---|---|
| `.github/workflows/ci.yml` | Compilation → Tests → JaCoCo ≥ 80% → SonarCloud → OWASP → JAR |
| `.github/workflows/cd.yml` | Docker build → GHCR push → Déploiement SSH staging |

### Services CI
- PostgreSQL 16 (healthcheck)
- Redis 7 (healthcheck)

---

## US-005 — Conteneurisation Docker ✅

**Branche :** `feature/GS-004-ci-cd-pipeline` (même livrable) → Pushé ✅  

### Fichiers
| Fichier | Rôle |
|---|---|
| `Dockerfile` | Multi-stage (JDK 21 builder → JRE 21 Alpine), non-root `stockmaster`, HEALTHCHECK, layered JAR, JVM flags conteneur |
| `docker-compose.yml` | API + PostgreSQL 16 + Redis 7 + MinIO + MailHog, healthchecks, volumes nommés, réseau dédié |
| `.env.example` | Template de variables d'environnement (JWT, DB, Redis, MinIO, Mail, CORS) |

---

## Architecture — Arbre complet du projet

```
gestionulrich/
├── pom.xml                     ← Parent POM (Spring Boot 3.3.5, 11 modules)
├── .gitignore
├── Dockerfile                  ← Multi-stage container
├── docker-compose.yml          ← Dev environment
├── .env.example                ← Variables d'environnement
├── .github/workflows/
│   ├── ci.yml                  ← CI pipeline
│   └── cd.yml                  ← CD pipeline
├── document/                   ← Spécifications
│   ├── A_JIRA_ET_GIT_FLOW.md
│   ├── BACKLOG_StockMaster_CM.md
│   ├── CDCT_StockMaster_CM_Complet_Sections22-30.md
│   └── implementation.md       ← Ce fichier
├── stockmaster-shared/         ← Module Shared (infra)
├── stockmaster-auth/           ← Module Auth
├── stockmaster-groupe/         ← Module Groupe & Filiales
├── stockmaster-utilisateur/    ← Module Utilisateurs
├── stockmaster-catalogue/      ← Module Catalogue
├── stockmaster-tiers/          ← Module Tiers
├── stockmaster-achat/          ← Module Achats
├── stockmaster-stock/          ← Module Stock
├── stockmaster-vente/          ← Module Ventes
├── stockmaster-notification/   ← Module Notifications
└── stockmaster-reporting/      ← Module Reporting
```

---

## Règles de commit utilisées

```
type(GS-XXX): description en minuscule
```
Types : feat, fix, docs, refactor, test, chore, style
Scope : GS-001, GS-003, GS-004, GS-005
