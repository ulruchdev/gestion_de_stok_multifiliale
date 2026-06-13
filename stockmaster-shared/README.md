# StockMaster CM — Shared (Infrastructure Commune)

> **Artefact :** `stockmaster-shared`
> **Statut :** ✅ Actif (code implémenté)
> **Dépendance :** Aucune (module racine)
> **Package :** `com.stockmaster.shared`

---

## Rôle

Module d'infrastructure partagé par tous les autres modules. Il contient les fondations techniques sans lesquelles aucun module métier ne peut fonctionner :

- ✅ Entités abstraites & base JPA
- ✅ Réponses API standardisées (RFC 7807)
- ✅ Gestion centralisée des erreurs
- ✅ Configuration Spring (JWT, CORS, Pagination)
- ✅ Point d'entrée de l'application
- ✅ Migrations Flyway (schéma BDD)

---

## Structure du package

```
com.stockmaster.shared/
├── config/
│   ├── CorsProperties.java          ← @ConfigurationProperties CORS
│   ├── JwtProperties.java           ← @ConfigurationProperties JWT
│   ├── PaginationProperties.java    ← @ConfigurationProperties pagination
│   └── WebConfig.java               ← CORS + JPA Auditing
├── dto/response/
│   ├── ApiResponse.java             ← Wrapper réponse générique <T>
│   └── ProblemResponse.java         ← RFC 7807 Problem Details
├── entity/
│   └── AbstractEntity.java          ← id, dateCreation, dateModification, supprime
├── exception/
│   ├── BusinessException.java       ← Exception métier avec ErrorCode
│   ├── EntityNotFoundException.java ← 404 avec détails
│   ├── ErrorCode.java               ← 30 codes d'erreur
│   └── InsufficientStockException.java ← 409 avec liste ruptures
├── handler/
│   └── GlobalExceptionHandler.java  ← 9 cas d'erreur, logs, pas de stack trace
└── StockMasterApplication.java      ← @SpringBootApplication (scan global)
```

---

## Ressources

```
src/main/resources/
├── application.yml                  ← 3 profils (dev/test/prod)
└── db/migration/
    ├── V1__init_schema.sql          ← 16 tables (tenant_group à notification_alerte)
    ├── V1_rollback_init_schema.sql  ← Rollback V1
    ├── V2__create_indexes.sql       ← 12 index de performance
    ├── V2_rollback_indexes.sql      ← Rollback V2
    ├── V3__functions_and_triggers.sql ← Trigger date_modification
    └── V3_rollback_triggers.sql     ← Rollback V3
```

---

## Tests

| Fichier | Nombre de tests | Couverture |
|---|---|---|
| `GlobalExceptionHandlerTest.java` | 18 tests unitaires | Tous les cas d'erreur |

---

## US associées

| US | Description | Statut |
|---|---|---|
| **US-001** | Initialisation du projet Spring Boot | ✅ Terminé |
| **US-002** | Configuration Flyway et schéma initial | ✅ Terminé |
| **US-003** | Gestion centralisée des erreurs (tests) | ✅ Terminé |
| **US-005** | Conteneurisation Docker (Dockerfile) | ✅ Terminé |

---

## Dépendances techniques

| Dépendance | Usage |
|---|---|
| `spring-boot-starter-web` | API REST |
| `spring-boot-starter-validation` | Jakarta Validation |
| `spring-boot-starter-data-jpa` | JPA / Hibernate |
| `spring-boot-starter-data-redis` | Redis |
| `spring-boot-configuration-processor` | @ConfigurationProperties |
| `postgresql` | Driver PostgreSQL |
| `flyway-core` + `flyway-database-postgresql` | Migrations |
| `springdoc-openapi-starter-webmvc-ui` | OpenAPI / Swagger |
| `jackson-databind-nullable` | JSON nullable |
| `lombok` | Génération code |
| `spring-boot-starter-test` | Tests |
| `testcontainers` | PostgreSQL en test |

---

## Configuration

Voir [`document/guideconfiguration.md`](../document/guideconfiguration.md) pour les détails de configuration (profils, JWT, CORS, Flyway).
