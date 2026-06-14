# StockMaster CM — Journal d'Implémentation

> **Référence :** GS-BACKLOG-2026-01
> **Stack :** Java 21, Spring Boot 3.3.5, PostgreSQL 16, Redis 7, MinIO
> **Architecture :** Monolithe modulaire (11 modules)
> **Version :** 1.0.0-SNAPSHOT
> **Dernière mise à jour :** 13 juin 2026 — 03:02 UTC+2

---

## 📊 Vue d'ensemble Git

```
e47186c (master) Initialisation du projet Spring Boot
    │
    ├── 0357763  (feature/GS-001)   docs: journal implémentation       ← PUSHÉ ✅
    │
    ├── c03bff2  (feature/GS-003)   test: 18 tests GlobalExceptionHandler ← PUSHÉ ✅
    │
    ├── 911fb49  (feature/GS-005)   feat: Docker + docker-compose      ← PUSHÉ ✅
    │
    ├── e74b481  (feature/GS-004)   feat: CI/CD pipelines              ← PUSHÉ ✅
    │   └── ad86aeb  (same branch)  docs: update journal US-003/004/005 ← PUSHÉ ✅
    │
    ├── 9fed1df  (feature/GS-006)   feat: JWT Auth (US-006 + US-008)  ← PUSHÉ ✅
    │
    └── a7d76eb  (feature/GS-002)   feat: Flyway migrations V1-V3     ← 🚫 NON PUSHÉ
            (HEAD actuel)
```

### Branches

| Branche locale | Branche distante | Statut |
|---|---|---|
| `master` | `origin/master` | ✅ Synchronisé |
| `feature/GS-001-initialize-spring-boot-project` | `origin/feature/GS-001-...` | ✅ Pushé |
| `feature/GS-003-centralized-error-handling` | `origin/feature/GS-003-...` | ✅ Pushé |
| `feature/GS-004-ci-cd-pipeline` | `origin/feature/GS-004-...` | ✅ Pushé |
| `feature/GS-006-jwt-auth` | `origin/feature/GS-006-...` | ✅ Pushé |
| `feature/GS-002-flyway-migrations` *(HEAD)* | *aucune* | 🚫 **Non pushée** |

---

## 📜 Chronologie des commits

| # | Horaire | Hash (court) | Auteur | Message | Fichiers | Lignes | Pushé ? |
|---|---|---|---|---|---|---|---|
| 1 | 01:55 | `e47186c` | ulrich dev | `feat(GS-001): initialize Spring Boot modular project structure` | 44 créés | ~+5 389 | ✅ |
| 2 | 02:11 | `0357763` | ulrich dev | `docs(GS-001): add implementation journal with US-001 status and upcoming US plan` | 1 modifié | +110 | ✅ |
| 3 | 02:23 | `c03bff2` | ulrich dev | `test(GS-003): add 18 unit tests for GlobalExceptionHandler covering all exception types` | 1 créé | +203 | ✅ |
| 4 | 02:28 | `911fb49` | ulrich dev | `feat(GS-005): add Docker multi-stage containerization and docker-compose environment` | 3 créés | +213 | ✅ |
| 5 | 02:30 | `e74b481` | ulrich dev | `feat(GS-004): add CI/CD pipeline with GitHub Actions, JaCoCo, SonarCloud` | 2 créés | +223 | ✅ |
| 6 | 02:31 | `ad86aeb` | ulrich dev | `docs(GS-004): update implementation journal with US-003/004/005 status` | 1 modifié | +90/-70 | ✅ |
| 7 | 02:51 | `9fed1df` | ulrich dev | `feat(GS-006): implement JWT auth - inscription entreprise unique (US-006) and login (US-008)` | 24 créés | +957 | ✅ |
| 8 | 03:02 | `a7d76eb` | ulrich dev | `feat(GS-002): add Flyway migrations V1-V3 with all tables, indexes, triggers, and rollback scripts` | 6 créés | +412 | 🚫 |

**Total :** 8 commits, **~7 497 lignes** ajoutées, **59 fichiers** créés/modifiés (hors .idea)

---

# EPIC 1 — Fondations techniques

---

## US-001 — Initialisation du projet Spring Boot ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-001-initialize-spring-boot-project`
> **Commit :** `e47186c`
> **Push :** ✅ Oui (`origin/master` + `origin/feature/GS-001-...`)
> **Fichiers créés :** 44 fichiers

### Fichiers impactés

| Fichier | Rôle |
|---|---|
| `pom.xml` | Parent POM Spring Boot 3.3.5, Java 21, 11 modules, dépendances versionnées |
| `.gitignore` | Java, Maven, IDE, secrets, logs, Docker |
| `stockmaster-shared/pom.xml` | Module shared (infrastructure commune) |
| `stockmaster-auth/pom.xml` | Module auth (JWT, inscriptions) |
| `stockmaster-groupe/pom.xml` | Module groupe & filiales |
| `stockmaster-utilisateur/pom.xml` | Module utilisateurs & rôles |
| `stockmaster-catalogue/pom.xml` | Module catalogue (catégories & articles) |
| `stockmaster-tiers/pom.xml` | Module tiers (clients & fournisseurs) |
| `stockmaster-achat/pom.xml` | Module achats (commandes fournisseur) |
| `stockmaster-stock/pom.xml` | Module stock (mouvements, transferts) |
| `stockmaster-vente/pom.xml` | Module ventes (B2B & caisse) |
| `stockmaster-notification/pom.xml` | Module notifications & alertes |
| `stockmaster-reporting/pom.xml` | Module reporting & statistiques |
| `stockmaster-shared/.../AbstractEntity.java` | Entité abstraite : `id`, `dateCreation`, `dateModification`, `supprime` |
| `stockmaster-shared/.../ApiResponse.java` | Wrapper réponse générique `ApiResponse<T>` |
| `stockmaster-shared/.../ProblemResponse.java` | RFC 7807 Problem Details |
| `stockmaster-shared/.../ErrorCode.java` | 30 codes d'erreur (AUTH_*, RES_*, CMD_*, GRP_*, STK_*, SEC_*, SYS_*) |
| `stockmaster-shared/.../BusinessException.java` | Exception métier avec ErrorCode |
| `stockmaster-shared/.../EntityNotFoundException.java` | 404 avec détails (par ID ou par champ) |
| `stockmaster-shared/.../InsufficientStockException.java` | 409 avec liste des ruptures |
| `stockmaster-shared/.../GlobalExceptionHandler.java` | Handler global : 9 cas d'erreur, RFC 7807, pas de stack trace |
| `stockmaster-shared/.../JwtProperties.java` | @ConfigurationProperties JWT (secret, expiration) |
| `stockmaster-shared/.../CorsProperties.java` | @ConfigurationProperties CORS |
| `stockmaster-shared/.../PaginationProperties.java` | @ConfigurationProperties pagination |
| `stockmaster-shared/.../WebConfig.java` | CORS + JPA Auditing |
| `stockmaster-shared/.../StockMasterApplication.java` | Point d'entrée, scan global `com.stockmaster` |
| `stockmaster-shared/.../application.yml` | 3 profils (dev/test/prod), open-in-view: false |
| `document/implementation.md` | Ce journal (créé vide) |
| `document/A_JIRA_ET_GIT_FLOW.md` | Documentation workflow Jira/Git |
| `document/BACKLOG_StockMaster_CM.md` | Backlog complet (75 US, 267 pts) |
| `document/CDCT_StockMaster_CM_Complet_Sections22-30.md` | Cahier des Charges Techniques |
| `document/analyse_fonctionnelle_stockmaster_cm (1).md` | Analyse fonctionnelle |
| `document/analyse_fonctionnelle_stockmaster_cm.docx` | Analyse fonctionnelle (format Word) |
| `document/representation_graphique_structure_et_flow.pdf` | Schémas d'architecture |
| `document/.idea/*` (6 fichiers) | Fichiers IDE IntelliJ *(en cours de suppression)* |

---

## US-002 — Configuration Flyway et schéma initial ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-002-flyway-migrations`
> **Commit :** `a7d76eb`
> **Push :** 🚫 **Non** (branche locale uniquement)
> **Fichiers créés :** 6

### Fichiers impactés

| Fichier | Rôle | Lignes |
|---|---|---|
| `stockmaster-shared/.../db/migration/V1__init_schema.sql` | Schéma initial : 14 tables avec CHECK, FK, UNIQUE | 277 |
| `stockmaster-shared/.../db/migration/V1_rollback_init_schema.sql` | Rollback V1 : DROP TABLE inversé | 21 |
| `stockmaster-shared/.../db/migration/V2__create_indexes.sql` | 12 index de performance (isolation tenant, full-text, login) | 49 |
| `stockmaster-shared/.../db/migration/V2_rollback_indexes.sql` | Rollback V2 : DROP INDEX inversé | 19 |
| `stockmaster-shared/.../db/migration/V3__functions_and_triggers.sql` | Trigger `update_date_modification` sur 14 tables + vue matérialisée | 41 |
| `stockmaster-shared/.../db/migration/V3_rollback_triggers.sql` | Rollback V3 : DROP TRIGGER/VIEW/FUNCTION | 5 |

### Tables créées (V1)

1. `tenant_group` — Racine multi-tenant (plan, limite filiales)
2. `entreprise` — Maison mère / Filiales avec `parent_id`
3. `utilisateur` — Comptes avec rôles RBAC (7 rôles)
4. `categorie` — Catégories d'articles avec taux TVA
5. `article` — Articles (prix, stock, seuil alerte)
6. `client` — Clients B2B
7. `fournisseur` — Fournisseurs
8. `commande_fournisseur` — Commandes fournisseur (états)
9. `ligne_commande_fournisseur` — Lignes de commande fournisseur
10. `commande_client` — Commandes client (états)
11. `ligne_commande_client` — Lignes de commande client
12. `vente` — Ventes directes (caisse)
13. `ligne_vente` — Lignes de vente
14. `transfert_stock` — Transferts inter-filiales
15. `mouvement_stock` — Journal des mouvements (6 types)
16. `notification_alerte` — Alertes de stock

### Configuration (dans `application.yml`)

| Profil | ddl-auto | Flyway | clean-disabled |
|---|---|---|---|
| `dev` | validate | enabled | false |
| `test` | none | enabled | true |
| `prod` | none | enabled | true |

---

## US-003 — Gestion centralisée des erreurs (Tests) ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-003-centralized-error-handling`
> **Commit :** `c03bff2`
> **Push :** ✅ Oui (`origin/feature/GS-003-...`)
> **Fichiers créés :** 1 (+203 lignes)
> **Contenu :** `GlobalExceptionHandlerTest.java`

### Tests créés (18 tests unitaires)

| Catégorie | Tests | Nombre |
|---|---|---|
| BusinessException → statuts HTTP | 400, 401, 403, 404, 409, 429 | 6 |
| EntityNotFoundException | Par ID, par champ | 2 |
| InsufficientStockException | Liste des ruptures | 1 |
| MethodArgumentNotValidException | 2 champs en erreur | 1 |
| AccessDeniedException | 403 + code SEC_001 | 1 |
| NoHandlerFoundException | 404 | 1 |
| HttpRequestMethodNotSupportedException | 405 + code SYS_003 | 1 |
| HttpMessageNotReadableException | 400 + code SYS_002 | 1 |
| MethodArgumentTypeMismatchException | 400 + nom paramètre | 1 |
| Fallback Exception → 500 | Sans stack trace | 1 |
| RFC 7807 format complet | Tous les champs + pattern du type | 2 |

---

## US-004 — Pipeline CI/CD GitHub Actions ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-004-ci-cd-pipeline`
> **Commits :** `e74b481` (création) + `ad86aeb` (docs)
> **Push :** ✅ Oui (`origin/feature/GS-004-...`)
> **Fichiers créés :** 3

### Fichiers impactés

| Fichier | Rôle | Lignes |
|---|---|---|
| `.github/workflows/ci.yml` | Compilation → Tests → JaCoCo ≥ 80% → SonarCloud → OWASP → JAR | 132 |
| `.github/workflows/cd.yml` | Docker build → GHCR push → Déploiement SSH staging | 91 |
| `sonar-project.properties` | Configuration SonarCloud (`ulruchdev/stockmaster-cm`) | ~20 |

### Services CI

- PostgreSQL 16 (healthcheck `pg_isready`)
- Redis 7 (healthcheck `redis-cli ping`)

### Pipeline CI (ci.yml)

```
Checkout → Setup JDK 21 → Cache Maven → Compile
  → Tests + JaCoCo (mvn verify)
  → Upload rapport JaCoCo
  → OWASP Dependency Check (failBuildOnCVSS ≥ 7)
  → Build JAR
  → Upload artifact
```

### Pipeline CD (cd.yml)

```
Checkout → Build JAR → Login GHCR → Docker metadata
  → Build & push image → SSH staging → docker pull + run
```

---

## US-005 — Conteneurisation Docker ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-004-ci-cd-pipeline` (même livrable que US-004)
> **Commit :** `911fb49`
> **Push :** ✅ Oui
> **Fichiers créés :** 3

### Fichiers impactés

| Fichier | Rôle | Lignes |
|---|---|---|
| `Dockerfile` | Multi-stage (JDK 21 builder → JRE 21 Alpine), non-root `stockmaster`, HEALTHCHECK, layered JAR, JVM flags conteneur | 63 |
| `docker-compose.yml` | API + PostgreSQL 16 + Redis 7 + MinIO + MailHog, healthchecks, volumes nommés, réseau dédié | 119 |
| `.env.example` | Template variables d'environnement (JWT, DB, Redis, MinIO, Mail, CORS) | 31 |

### Services docker-compose

| Service | Image | Ports | Rôle |
|---|---|---|---|
| `api` | build local | 8080 | API Spring Boot |
| `postgres` | postgres:16-alpine | 5432 | Base de données |
| `redis` | redis:7-alpine | 6379 | Cache + Rate limiting |
| `minio` | minio/minio:latest | 9000/9001 | Stockage fichiers (logos, photos, PDF) |
| `mailhog` | mailhog/mailhog:latest | 1025/8025 | Capture emails (dev) |

---

# EPIC 2 — Authentification & Accès

---

## US-006 — Inscription — Entreprise unique ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-006-jwt-auth`
> **Commit :** `9fed1df`
> **Push :** ✅ Oui (`origin/feature/GS-006-...`)
> **Fichiers créés :** 17 (partie inscription)

### Fichiers impactés (regroupés par couche)

#### Contrôleur
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../controller/AuthController.java` | `POST /api/v1/auth/inscription/entreprise-unique` → 201 CREATED |

#### Service
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../service/AuthService.java` | Interface : `inscrireEntrepriseUnique()` |
| `stockmaster-auth/.../service/impl/AuthServiceImpl.java` | Implémentation : création atomique TenantGroup + Entreprise + Utilisateur (161 lignes) |

#### Entités (domaine)
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../domain/entity/TenantGroup.java` | Groupe locataire : nom, plan, limite filiales (57 lignes) |
| `stockmaster-auth/.../domain/entity/Entreprise.java` | Entreprise : nom, type (MERE/FILIALE), adresse, NIF (87 lignes) |
| `stockmaster-auth/.../domain/entity/Utilisateur.java` | Utilisateur : email, motDePasse, rôle, scope (82 lignes) |

#### Énumérations
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../domain/enums/PlanAbonnement.java` | GRATUIT, STARTER, PRO, ENTERPRISE |
| `stockmaster-auth/.../domain/enums/RoleUtilisateur.java` | 7 rôles : SUPER_ADMIN à CAISSIER |
| `stockmaster-auth/.../domain/enums/ScopeUtilisateur.java` | GROUPE, FILIALE |
| `stockmaster-auth/.../domain/enums/TypeEntreprise.java` | MERE, FILIALE |

#### DTOs (Request/Response)
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../dto/request/InscriptionEntrepriseUniqueRequest.java` | Requête : nomBoutique, ville, prenom, nom, email, motDePasse (validation Jakarta) |
| `stockmaster-auth/.../dto/response/InscriptionResponse.java` | Réponse : email, groupId, message |

#### Mapper & Events & Repositories
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../mapper/AuthMapper.java` | MapStruct : Request → Entreprise |
| `stockmaster-auth/.../event/InscriptionSuccessEvent.java` | Événement async pour email bienvenue |
| `stockmaster-auth/.../repository/EntrepriseRepository.java` | JPA Repository |
| `stockmaster-auth/.../repository/TenantGroupRepository.java` | JPA Repository |
| `stockmaster-auth/.../repository/UtilisateurRepository.java` | JPA Repository : `existsByEmail()`, `findByEmail()` |

### Logique métier (AuthServiceImpl)

1. Vérification unicité email → `BusinessException(EMAIL_ALREADY_EXISTS)` si doublon
2. Création atomique `@Transactional` : `TenantGroup` → `Entreprise` → `Utilisateur`
3. Mot de passe haché avec BCrypt
4. Publication événement `InscriptionSuccessEvent` (email async)
5. Retour `201 CREATED` avec `ApiResponse<InscriptionResponse>`

---

## US-007 — Inscription — Groupe multi-sites 🔜

> **Statut :** NON COMMENCÉ
> **Priorité :** P0 | **Sprint :** 2
> **Endpoint :** `POST /api/v1/auth/inscription/groupe`
> **Dépendance :** US-006 doit être mergé (même pattern que US-006)

---

## US-008 — Connexion JWT ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-006-jwt-auth`
> **Commit :** `9fed1df`
> **Push :** ✅ Oui (`origin/feature/GS-006-...`)
> **Fichiers créés :** 7 (partie login)

### Fichiers impactés

#### Contrôleur
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../controller/AuthController.java` | `POST /api/v1/auth/login` → 200 OK |

#### Service
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../service/impl/AuthServiceImpl.java` | `login()` : vérification email + BCrypt + statuts + génération tokens |

#### Configuration Sécurité
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../config/SecurityConfig.java` | `SecurityFilterChain` : endpoints publics, stateless, `@EnableMethodSecurity`, `BCryptPasswordEncoder` |
| `stockmaster-auth/.../config/JwtTokenProvider.java` | Génération/validation JWT (jjwt 0.12.6, HS256, claims userId/entrepriseId/groupId/role/scope/jti) |
| `stockmaster-auth/.../config/JwtAuthenticationFilter.java` | Filtre : extrait token Authorization, valide, crée `UsernamePasswordAuthenticationToken` |
| `stockmaster-auth/.../config/RateLimitFilter.java` | Filtre rate limiting `/auth/login` : 5 tentatives / 15 min par IP (Redis) |
| `stockmaster-auth/.../config/StockMasterPrincipal.java` | `UserPrincipal` avec userId, entrepriseId, groupId, role, scope |

#### DTOs
| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../dto/request/LoginRequest.java` | Requête : email, motDePasse |
| `stockmaster-auth/.../dto/response/LoginResponse.java` | Réponse : accessToken, refreshToken, expiresIn, role, scope |

### Logique métier (login)

1. Recherche email → si inconnu → `401` (message générique)
2. Vérification BCrypt → si incorrect → `401`
3. Vérification `actif = true` → si désactivé → `403 ACCOUNT_DISABLED`
4. Vérification groupe actif → si suspendu → `403 TENANT_SUSPENDED`
5. Génération access token (15 min) + refresh token (7 jours)
6. Retour `200 OK`

### Configuration JWT

| Paramètre | Valeur |
|---|---|
| Algorithme | HS256 (jjwt 0.12.6) |
| Access token TTL | 900s (15 min) |
| Refresh token TTL | 604800s (7 jours) |
| Claims | userId, entrepriseId, groupId, role, scope, jti (UUID) |
| Issuer | stockmaster |

---

## US-009 — Refresh token 🔜

> **Statut :** NON COMMENCÉ
> **Priorité :** P0 | **Sprint :** 2
> **Endpoint :** `POST /api/v1/auth/refresh`
> **Dépendance :** US-008 terminé

---

## US-010 — Déconnexion 🔜

> **Statut :** NON COMMENCÉ
> **Priorité :** P0 | **Sprint :** 2
> **Endpoint :** `POST /api/v1/auth/logout`
> **Dépendance :** US-009 terminé

---

## US-011 — Mot de passe oublié 🔜

> **Statut :** NON COMMENCÉ
> **Priorité :** P0 | **Sprint :** 2
> **Endpoint :** `POST /api/v1/auth/forgot-password`

---

## US-012 — Réinitialisation mot de passe 🔜

> **Statut :** NON COMMENCÉ
> **Priorité :** P0 | **Sprint :** 2
> **Endpoint :** `POST /api/v1/auth/reset-password`

---

# EPIC 3 à 13 — Modules métier

> **Tous les modules suivants sont vides :**
>
> | Module | Fichiers Java | Statut |
> |---|---|---|
> | `stockmaster-groupe` | 0 | 🔜 Non commencé |
> | `stockmaster-utilisateur` | 0 | 🔜 Non commencé |
> | `stockmaster-catalogue` | 0 | 🔜 Non commencé |
> | `stockmaster-tiers` | 0 | 🔜 Non commencé |
> | `stockmaster-achat` | 0 | 🔜 Non commencé |
> | `stockmaster-stock` | 0 | 🔜 Non commencé |
> | `stockmaster-vente` | 0 | 🔜 Non commencé |
> | `stockmaster-notification` | 0 | 🔜 Non commencé |
> | `stockmaster-reporting` | 0 | 🔜 Non commencé |
>
> **US concernées :** US-014 à US-080

---

# 🔄 WORKING TREE — Modifications non commitées

### État actuel (HEAD sur `feature/GS-002-flyway-migrations`)

```
On branch feature/GS-002-flyway-migrations
Changes to be committed (staged) : 6 suppressions
Untracked files : 1 fichier
```

### 1. Fichiers staged (index) — 6 suppressions

Ces fichiers `.idea/` (IDE IntelliJ) ont été versionnés par erreur dans le commit initial. Ils sont en cours de suppression :

| Fichier | Type |
|---|---|
| `document/.idea/.gitignore` | Fichier IDE (à ignorer) |
| `document/.idea/amazonq.xml` | Fichier IDE (à ignorer) |
| `document/.idea/codestream.xml` | Fichier IDE (à ignorer) |
| `document/.idea/misc.xml` | Fichier IDE (à ignorer) |
| `document/.idea/modules.xml` | Fichier IDE (à ignorer) |
| `document/.idea/vcs.xml` | Fichier IDE (à ignorer) |

**Action recommandée :** Commiter ces suppressions avant de merger vers master, et ajouter `.idea/` au `.gitignore`.

### 2. Fichiers untracked (non versionnés) — 1 fichier

| Fichier | Contenu |
|---|---|
| `sonar-project.properties` | Configuration SonarCloud : organisation `ulruchdev`, projectKey `stockmaster-cm`, exclusions DTO/mapper, quality gate wait |

**Action recommandée :** Ajouter et commiter avant de merger.

---

# 📊 TABLEAUX RÉCAPITULATIFS

### Par US

| # | Titre | Statut | Branche | Commit | Pushé | Fichiers | Lignes |
|---|---|---|---|---|---|---|---|
| **US-001** | Initialisation Spring Boot | ✅ Terminé | feature/GS-001 | `e47186c` | ✅ | 44 | ~5 389 |
| **US-002** | Flyway & schéma initial | ✅ Terminé | feature/GS-002 | `a7d76eb` | 🚫 | 6 | +412 |
| **US-003** | Gestion erreurs + tests | ✅ Terminé | feature/GS-003 | `c03bff2` | ✅ | 1 | +203 |
| **US-004** | CI/CD pipelines | ✅ Terminé | feature/GS-004 | `e74b481` + `ad86aeb` | ✅ | 3 | +223 |
| **US-005** | Docker + docker-compose | ✅ Terminé | feature/GS-004 | `911fb49` | ✅ | 3 | +213 |
| **US-006** | Inscription entreprise unique | ✅ Terminé | feature/GS-006 | `9fed1df` | ✅ | 17 | +500 |
| **US-007** | Inscription groupe multi-sites | 🔜 Non commencé | — | — | — | — | — |
| **US-008** | Connexion JWT | ✅ Terminé | feature/GS-006 | `9fed1df` | ✅ | 7 | +457 |
| **US-009** | Refresh token | 🔜 Non commencé | — | — | — | — | — |
| **US-010** | Déconnexion | 🔜 Non commencé | — | — | — | — | — |
| **US-011** | Mot de passe oublié | 🔜 Non commencé | — | — | — | — | — |
| **US-012** | Réinitialisation mot de passe | 🔜 Non commencé | — | — | — | — | — |
| **US-013** | Changement mot de passe | 🔜 Non commencé | — | — | — | — | — |
| **US-014 à 080** | EPIC 3 à 13 (67 US) | 🔜 Non commencé | — | — | — | — | — |

### Par branche — Statut de push

| Branche | Commit | Poussé vers origin |
|---|---|---|
| `master` | `e47186c` | ✅ Oui |
| `feature/GS-001-initialize-spring-boot-project` | `0357763` | ✅ Oui |
| `feature/GS-003-centralized-error-handling` | `c03bff2` | ✅ Oui |
| `feature/GS-004-ci-cd-pipeline` | `ad86aeb` | ✅ Oui |
| `feature/GS-006-jwt-auth` | `9fed1df` | ✅ Oui |
| `feature/GS-002-flyway-migrations` | `a7d76eb` | 🚫 **Non poussé** |

### Bilan global

| Métrique | Valeur |
|---|---|
| US terminées | 6 sur 75 (US-001, 002, 003, 004, 005, 006, 008) |
| US en cours | 0 |
| US non commencées | 68 |
| Total commits | 8 |
| Total fichiers créés | ~65 |
| Total lignes de code | ~8 500 |
| Tests unitaires | **41** (18 shared + 3 intégration + 7 auth service + 13 auth contrôleur) |
| Branches créées | 7 (feature/GS-006-008-auth-tests) |
| Branches poussées | 5 (sur 7) |
| Modules avec code + tests | 2 sur 11 (shared + auth) |
| Fichiers en attente (staged) | 6 suppressions (fichiers IDE) |
| Fichiers untracked | 1 (`sonar-project.properties`) |

---

## 📋 LÉGENDE

| Symbole | Signification |
|---|---|
| ✅ | Terminé / Pushé |
| 🚫 | Non poussé |
| 🔜 | Non commencé |
| `e47186c` | Hash de commit (7 caractères) |
| `+N` | N lignes ajoutées |
| `-M` | M lignes supprimées |

---

> **Règle de gestion du journal :** Ce fichier doit être mis à jour à chaque nouveau commit.
> La section de la US modifiée doit refléter le hash du commit, la branche, et le statut de push.
> **Prochaine mise à jour prévue :** Après implémentation de US-007, US-009, US-010.

---

## 📎 Documents connexes

| Document | Rôle |
|---|---|
| `document/guideconfiguration.md` | Guide complet des configurations internes et externes (GitHub, SonarCloud, MinIO, secrets, etc.) |
| `document/BACKLOG_StockMaster_CM.md` | Backlog produit 75 US |
| `document/CDCT_StockMaster_CM_Complet_Sections22-30.md` | Cahier des Charges Techniques |
| `document/A_JIRA_ET_GIT_FLOW.md` | Workflow Jira & Git
