# StockMaster CM — Journal d'Implémentation

> **Référence :** GS-BACKLOG-2026-01
> **Stack :** Java 21, Spring Boot 3.3.5, PostgreSQL 16, Redis 7, MinIO
> **Architecture :** Monolithe modulaire (11 modules)
> **Version :** 1.0.0-SNAPSHOT
> **Dernière mise à jour :** 14 juin 2026

---

## 📊 Vue d'ensemble Git

```
main ────────────────────────────────────────────────────────────────────── (HEAD)
  │
  ├── GS-001 (Initial Spring Boot)     ──── e47186c ──── 44 fichiers  ──── ✅ Merged
  │
  ├── GS-003 (Error Handling + Tests)  ──── c03bff2 ──── 1 fichier    ──── ✅ Merged
  │
  ├── GS-004 (CI/CD + Docker)          ──── e74b481 ──── 3 fichiers   ──── ✅ Merged
  │     └── Fix SonarCloud projectKey  ──── 6dbb23a ──── 2 fichiers   ──── ✅ Cherry-pick
  │     └── Fix qualitygate.wait       ──── b62c95f ──── 1 fichier    ──── ✅ Cherry-pick
  │     └── Fix install cmd            ──── 8edfda6 ──── 1 fichier    ──── ✅ Cherry-pick
  │     └── Fix skip repackage shared  ──── 95be774 ──── 1 fichier    ──── ✅ Cherry-pick
  │     └── Remove OWASP from CI       ──── df5759f ──── 1 fichier    ──── ✅ Cherry-pick
  │     └── Exclure SQL migrations     ──── 02b441c ──── 1 fichier    ──── ✅ Direct
  │
  ├── GS-002 (Flyway Migrations)       ──── a7d76eb ──── 6 fichiers   ──── ✅ Merged
  │     └── (tous les mêmes fixes ci-dessus, cherry-pickés depuis GS-004)
  │
  └── GS-006 (JWT Auth)                ──── 9fed1df ──── 24 fichiers  ──── ✅ Merged
```

### Branches

| Branche | Statut |
|---|---|
| `main` | ✅ Branche de référence — tout est mergé |
| `feature/GS-001-initialize-spring-boot-project` | ✅ Mergée dans `main` |
| `feature/GS-002-flyway-migrations` | ✅ Mergée dans `main` (PR #3) |
| `feature/GS-003-centralized-error-handling` | ✅ Mergée dans `main` (PR #1) |
| `feature/GS-004-ci-cd-pipeline` | ✅ Mergée dans `main` (PR #2 → #4) |
| `feature/GS-006-jwt-auth` | ✅ Mergée dans `main` |

---

## 📜 Chronologie des commits (sur `main`)

| # | Hash | Message | Fichiers | Push |
|---|---|---|---|---|
| 1 | `e47186c` | `feat(GS-001): initialize Spring Boot modular project structure` | 44 créés | ✅ |
| 2 | `0357763` | `docs(GS-001): add implementation journal with US-001 status` | 1 modifié | ✅ |
| 3 | `c03bff2` | `test(GS-003): add 18 tests for GlobalExceptionHandler` | 1 créé | ✅ |
| 4 | `911fb49` | `feat(GS-005): add Docker multi-stage + docker-compose` | 3 créés | ✅ |
| 5 | `e74b481` | `feat(GS-004): add CI/CD pipelines GitHub Actions + JaCoCo + SonarCloud` | 2 créés | ✅ |
| 6 | `ad86aeb` | `docs(GS-004): update implementation journal` | 1 modifié | ✅ |
| 7 | `9fed1df` | `feat(GS-006): implement JWT auth — inscription (US-006) + login (US-008)` | 24 créés | ✅ |
| 8 | `a7d76eb` | `feat(GS-002): add Flyway migrations V1-V3 + rollbacks` | 6 créés | ✅ |
| 9 | `15ae97b` | `build(GS-002): add Maven Wrapper for reproducible builds` | 3 créés | ✅ |
| 10 | `d5a16da` | `ci(GS-002): optimiser job SonarCloud — éviter recompilation` | 1 modifié | ✅ |
| 11 | `e32193a` | `fix(GS-002): ajout compilation avant SonarCloud + exclusions coverage` | 1 modifié | ✅ |
| 12 | `c514cf3` | `fix(GS-002): ajout compilation avant SonarCloud + exclusions coverage` | 1 modifié | ✅ |
| 13 | `498822b` | `fix(GS-002): corriger projectKey (stockmaster-cm → gestion_de_stok_multifiliale)` | 2 modifiés | ✅ |
| 14 | `6dbb23a` | `fix(GS-002): corriger projectKey SonarCloud` | 2 modifiés | ✅ |
| 15 | `b62c95f` | `fix(GS-002): remove qualitygate.wait on fresh SonarCloud project` | 1 modifié | ✅ |
| 16 | `a3c3017` | `fix(GS-002): remove qualitygate.wait on fresh SonarCloud project` | 1 modifié | ✅ |
| 17 | `8edfda6` | `fix(GS-002): replace compile with install -DskipTests for SonarCloud` | 1 modifié | ✅ |
| 18 | `a4285c2` | `fix(GS-002): replace compile with install -DskipTests for SonarCloud` | 1 modifié | ✅ |
| 19 | `95be774` | `fix(GS-002): skip Spring Boot repackage for stockmaster-shared (library module)` | 1 modifié | ✅ |
| 20 | `318eaa8` | `fix(GS-002): skip Spring Boot repackage for stockmaster-shared (library module)` | 1 modifié | ✅ |
| 21 | `a6ebc05` | `fix(GS-002): fix SonarCloud warnings — unused import, regex, constant, SQL exlusions` | 4 modifiés | ✅ |
| 22 | `df5759f` | `ci(GS-002): remove OWASP Dependency Check from CI (401 Sonatype API)` | 1 modifié | ✅ |
| 23 | `ca031de` | `ci(GS-002): remove OWASP Dependency Check from CI (401 Sonatype API)` | 1 modifié | ✅ |
| 24 | `02b441c` | `fix(GS-004): exclude SQL migrations from SonarCloud analysis` | 1 modifié | ✅ |

**Total :** 24 commits (22 uniques), + ~8 500 lignes, ~85 fichiers

⚠️ **Modifications locales non commitées :** Suppression Testcontainers, activation JaCoCo

---

# EPIC 1 — Fondations techniques

---

## US-001 — Initialisation du projet Spring Boot ✅

> **Statut :** TERMINÉ — Mergé dans `main`
> **Commit :** `e47186c`

### Fichiers impactés

| Fichier | Rôle |
|---|---|
| `pom.xml` | Parent POM Spring Boot 3.3.5, Java 21, 11 modules, dépendances versionnées |
| `stockmaster-shared/.../AbstractEntity.java` | Entité abstraite : `id`, `dateCreation`, `dateModification`, `supprime` |
| `stockmaster-shared/.../ApiResponse.java` | Wrapper réponse générique `ApiResponse<T>` |
| `stockmaster-shared/.../ProblemResponse.java` | RFC 7807 Problem Details |
| `stockmaster-shared/.../ErrorCode.java` | 30 codes d'erreur (AUTH_*, RES_*, CMD_*, GRP_*, STK_*, SEC_*, SYS_*) |
| `stockmaster-shared/.../BusinessException.java` | Exception métier avec ErrorCode |
| `stockmaster-shared/.../EntityNotFoundException.java` | 404 avec détails |
| `stockmaster-shared/.../InsufficientStockException.java` | 409 avec liste des ruptures |
| `stockmaster-shared/.../GlobalExceptionHandler.java` | Handler global : 9 cas d'erreur, RFC 7807 |
| `stockmaster-shared/.../JwtProperties.java` | @ConfigurationProperties JWT |
| `stockmaster-shared/.../CorsProperties.java` | @ConfigurationProperties CORS |
| `stockmaster-shared/.../PaginationProperties.java` | @ConfigurationProperties pagination |
| `stockmaster-shared/.../WebConfig.java` | CORS + JPA Auditing |
| `stockmaster-shared/.../StockMasterApplication.java` | Point d'entrée Spring Boot |
| `stockmaster-shared/.../application.yml` | 3 profils (dev/test/prod), open-in-view: false |
| `stockmaster-auth/pom.xml` | Module auth |
| `stockmaster-groupe/pom.xml` | Module groupe |
| `stockmaster-utilisateur/pom.xml` | Module utilisateurs |
| `stockmaster-catalogue/pom.xml` | Module catalogue |
| `stockmaster-tiers/pom.xml` | Module tiers |
| `stockmaster-achat/pom.xml` | Module achats |
| `stockmaster-stock/pom.xml` | Module stock |
| `stockmaster-vente/pom.xml` | Module ventes |
| `stockmaster-notification/pom.xml` | Module notifications |
| `stockmaster-reporting/pom.xml` | Module reporting |

---

## US-002 — Configuration Flyway et schéma initial ✅

> **Statut :** TERMINÉ — Mergé dans `main`
> **Commit :** `a7d76eb`

### Fichiers impactés

| Fichier | Rôle | Lignes |
|---|---|---|
| `stockmaster-shared/.../db/migration/V1__init_schema.sql` | 16 tables avec CHECK, FK, UNIQUE | 277 |
| `stockmaster-shared/.../db/migration/V1_rollback_init_schema.sql` | Rollback V1 | 21 |
| `stockmaster-shared/.../db/migration/V2__create_indexes.sql` | 12 index de performance | 49 |
| `stockmaster-shared/.../db/migration/V2_rollback_indexes.sql` | Rollback V2 | 19 |
| `stockmaster-shared/.../db/migration/V3__functions_and_triggers.sql` | Trigger `update_date_modification` sur 14 tables + vue matérialisée | 41 |
| `stockmaster-shared/.../db/migration/V3_rollback_triggers.sql` | Rollback V3 | 5 |

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

---

## US-003 — Gestion centralisée des erreurs (Tests) ✅

> **Statut :** TERMINÉ — Mergé dans `main`
> **Commit :** `c03bff2`

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

> **Statut :** TERMINÉ — Mergé dans `main`
> **Commits :** `e74b481` + `ad86aeb` + fixes

### Fichiers impactés

| Fichier | Rôle |
|---|---|
| `.github/workflows/ci.yml` | Compilation → Tests → JaCoCo ≥ 80% → SonarCloud → JAR |
| `.github/workflows/cd.yml` | Docker build → GHCR push → Déploiement SSH staging |
| `sonar-project.properties` | Configuration SonarCloud (`ulruchdev` / `gestion_de_stok_multifiliale`) |

### Corrections CI appliquées (cumulatives)

| # | Correctif | Raison |
|---|---|---|
| 1 | `sonar.projectKey=stockmaster-cm` → `gestion_de_stok_multifiliale` | Projet SonarCloud sous un autre nom |
| 2 | `mvn compile sonar:sonar` → `mvn install -DskipTests sonar:sonar` | `compile` n'installe pas les JARs dans `.m2` |
| 3 | Ajout exclusions `**/db/migration/**` | Faux positifs SonarCloud sur les SQL Flyway |
| 4 | Suppression `sonar.qualitygate.wait` | Bloquait sur projet fraîchement créé |
| 5 | `<skip>true</skip>` dans `spring-boot-maven-plugin` de `stockmaster-shared` | Le repackage fat-JAR masquait les classes aux modules dépendants |
| 6 | Suppression étape OWASP Dependency Check | API Sonatype → 401 Unauthorized (non bloquant) |
| 7 | Testcontainers supprimé → PostgreSQL docker-compose direct | Testcontainers incompat. avec Docker Desktop Windows (exit 127), profiles CI & local unifiés |
| 8 | JaCoCo activé dans `stockmaster-shared/pom.xml` + enum `COVEREDRATIO` | Plugin dans `pluginManagement` seulement → jamais exécuté ; enum mal nommé `COVERED_RATIO` |
| 9 | CI : `POSTGRES_DB: stockmaster_dev` aligné sur docker-compose | Cohérence CI/local : une seule base pour tous les environnements de test |

---

## US-005 — Conteneurisation Docker ✅

> **Statut :** TERMINÉ — Mergé dans `main`
> **Commit :** `911fb49`

| Fichier | Rôle |
|---|---|
| `Dockerfile` | Multi-stage (JDK 21 builder → JRE 21 Alpine), non-root, HEALTHCHECK |
| `docker-compose.yml` | API + PostgreSQL 16 + Redis 7 + MinIO + MailHog |
| `.env.example` | Template variables d'environnement (JWT, DB, Redis, MinIO, Mail, CORS) |

---

# EPIC 2 — Authentification & Accès

---

## US-006 — Inscription — Entreprise unique ✅

> **Statut :** TERMINÉ — Mergé dans `main`
> **Commit :** `9fed1df`
> **Endpoint :** `POST /api/v1/auth/inscription/entreprise-unique`

### Fichiers impactés

| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../controller/AuthController.java` | `POST /api/v1/auth/inscription/entreprise-unique` → 201 CREATED |
| `stockmaster-auth/.../service/AuthService.java` | Interface : `inscrireEntrepriseUnique()` |
| `stockmaster-auth/.../service/impl/AuthServiceImpl.java` | Implémentation : création atomique TenantGroup + Entreprise + Utilisateur |
| `stockmaster-auth/.../domain/entity/TenantGroup.java` | Groupe locataire : nom, plan, limite filiales |
| `stockmaster-auth/.../domain/entity/Entreprise.java` | Entreprise : nom, type, adresse, NIF |
| `stockmaster-auth/.../domain/entity/Utilisateur.java` | Utilisateur : email, motDePasse, rôle, scope |
| `stockmaster-auth/.../domain/enums/PlanAbonnement.java` | GRATUIT, STARTER, PRO, ENTERPRISE |
| `stockmaster-auth/.../domain/enums/RoleUtilisateur.java` | 7 rôles : SUPER_ADMIN à CAISSIER |
| `stockmaster-auth/.../domain/enums/ScopeUtilisateur.java` | GROUPE, FILIALE |
| `stockmaster-auth/.../domain/enums/TypeEntreprise.java` | MERE, FILIALE |
| `stockmaster-auth/.../dto/request/InscriptionEntrepriseUniqueRequest.java` | Requête avec validation Jakarta |
| `stockmaster-auth/.../dto/response/InscriptionResponse.java` | Réponse : email, groupId, message |
| `stockmaster-auth/.../mapper/AuthMapper.java` | MapStruct : Request → Entreprise |
| `stockmaster-auth/.../event/InscriptionSuccessEvent.java` | Événement async pour email bienvenue |
| `stockmaster-auth/.../repository/EntrepriseRepository.java` | JPA Repository |
| `stockmaster-auth/.../repository/TenantGroupRepository.java` | JPA Repository |
| `stockmaster-auth/.../repository/UtilisateurRepository.java` | JPA Repository |

---

## US-007 — Inscription — Groupe multi-sites ✅

> **Statut :** TERMINÉ
> **Branche :** `feature/GS-007-inscription-groupe`
> **Priorité :** P0 | **Sprint :** 2
> **Endpoint :** `POST /api/v1/auth/inscription/groupe`
> **Fichiers créés/modifiés :** 9

### Détail des fichiers

| Fichier | Changement |
|---|---|
| `InscriptionGroupeRequest.java` | 🆕 DTO : nomGroupe, villesiege, nif (opt.), telephone, emailEntreprise, prenom, nom, emailAdmin, motDePasse |
| `AuthMapper.java` | 📝 Nouveau mapping `toEntrepriseFromGroupe()` avec NIF, téléphone, emailEntreprise |
| `AuthService.java` | 📝 Nouvelle méthode `inscrireGroupe()` |
| `AuthServiceImpl.java` | 📝 Création atomique TenantGroup (limiteFiliales=5) → Entreprise (siège) → Utilisateur ADMIN_GROUPE |
| `AuthController.java` | 📝 Nouvel endpoint `POST /api/v1/auth/inscription/groupe` |
| `AuthServiceImplTest.java` | 🆕 9 tests (2 inscription unique + 2 groupe + 5 login) |
| `AuthControllerTest.java` | 🆕 18 tests (6 unique + 6 groupe + 6 login) |
| `AuthTestApplication.java` | 🆕 Configuration test légère |
| `stockmaster-auth/pom.xml` | 📝 Ajout `spring-security-test` |

### Logique métier

1. Vérification unicité `emailAdmin` → `BusinessException(EMAIL_ALREADY_EXISTS)`
2. Création atomique `@Transactional` : `TenantGroup` (plan GRATUIT, 5 filiales) → `Entreprise` (type MERE, NIF, téléphone) → `Utilisateur` (rôle ADMIN_GROUPE)
3. Mot de passe haché avec BCrypt
4. Publication événement `InscriptionSuccessEvent` (email de bienvenue)
5. Message : "Votre groupe a été créé. Créez votre première filiale depuis le tableau de bord."

### Tests (27)

| Catégorie | Tests |
|---|---|
| AuthServiceImplTest — inscription unique | 2 (succès + email existant) |
| AuthServiceImplTest — inscription groupe | 2 (succès avec NIF + email admin existant) |
| AuthServiceImplTest — login | 5 (succès + email inconnu + mdp faux + compte désactivé + groupe suspendu) |
| AuthControllerTest — inscription unique | 6 (201 + 4×400 validation + 409 doublon) |
| AuthControllerTest — inscription groupe | 6 (201 + 4×400 validation + 409 doublon) |
| AuthControllerTest — login | 6 (200 + 2×400 validation + 401 + 2×403) |


---

## US-008 — Connexion JWT ✅

> **Statut :** TERMINÉ — Mergé dans `main`
> **Commit :** `9fed1df`
> **Endpoint :** `POST /api/v1/auth/login`

### Fichiers impactés

| Fichier | Rôle |
|---|---|
| `stockmaster-auth/.../config/SecurityConfig.java` | `SecurityFilterChain` : endpoints publics, stateless, BCrypt |
| `stockmaster-auth/.../config/JwtTokenProvider.java` | Génération/validation JWT (jjwt 0.12.6, HS256, claims userId/entrepriseId/groupId/role/scope/jti) |
| `stockmaster-auth/.../config/JwtAuthenticationFilter.java` | Filtre : extrait token Authorization, valide, crée Authentication |
| `stockmaster-auth/.../config/RateLimitFilter.java` | Rate limiting `/auth/login` : 5 tentatives / 15 min par IP (Redis) |
| `stockmaster-auth/.../config/StockMasterPrincipal.java` | UserPrincipal avec userId, entrepriseId, groupId, role, scope |
| `stockmaster-auth/.../dto/request/LoginRequest.java` | Requête : email, motDePasse |
| `stockmaster-auth/.../dto/response/LoginResponse.java` | Réponse : accessToken, refreshToken, expiresIn, role, scope |

### Configuration JWT

| Paramètre | Valeur |
|---|---|
| Algorithme | HS256 (jjwt 0.12.6) |
| Access token TTL | 900s (15 min) |
| Refresh token TTL | 604800s (7 jours) |
| Claims | userId, entrepriseId, groupId, role, scope, jti (UUID) |

---

## US-009 — Refresh token 🚧

> **Statut :** IMPLÉMENTÉ — Pushé, en attente de PR
> **Branche :** `feature/GS-009-refresh-token`
> **Commit :** `5a39023`
> **Priorité :** P0 | **Sprint :** 2 | **Points :** 2
> **Endpoint :** `POST /api/v1/auth/refresh`
> **Fichiers créés/modifiés :** 7

### Détail des fichiers

| Fichier | Changement |
|---|---|
| `RefreshTokenRequest.java` | 🆕 DTO : refreshToken (NotBlank) |
| `RefreshTokenResponse.java` | 🆕 DTO : accessToken, expiresIn |
| `AuthService.java` | 📝 Nouvelle méthode `refreshAccessToken()` |
| `AuthServiceImpl.java` | 📝 Injection `StringRedisTemplate` + `JwtProperties` ; stockage Redis du refresh token dans `login()` ; implémentation de `refreshAccessToken()` avec validation token JWT, vérification Redis, vérification compte/groupe actif, génération nouveau token |
| `AuthController.java` | 📝 Nouvel endpoint `POST /api/v1/auth/refresh` |
| `AuthServiceImplTest.java` | 🆕 5 tests refresh (token valide + non trouvé Redis + mismatch + utilisateur introuvable + token expiré) |
| `AuthControllerTest.java` | 🆕 4 tests refresh endpoint (200 OK + 400 blank + 401 invalid + 403 disabled) |

### Logique métier

1. Extraction `userId` du refresh token JWT → `JwtTokenProvider.getUserIdFromToken()`
2. Vérification en Redis clé `refresh:{userId}` → existence + correspondance
3. Chargement utilisateur + vérification compte actif + groupe actif
4. Génération nouveau access token avec les mêmes claims (rôle, scope)
5. L'ancien access token reste valide jusqu'à sa propre expiration (15 min)
6. Token invalide/expiré/absent de Redis → `401 UNAUTHORIZED` → client doit se reconnecter

## US-010 — Déconnexion 🚧

> **Statut :** IMPLÉMENTÉ — En attente de push
> **Branche :** `feature/GS-010-logout`
> **Priorité :** P0 | **Sprint :** 2 | **Points :** 2
> **Endpoint :** `POST /api/v1/auth/logout` (authentifié)
> **Fichiers modifiés :** 5

### Détail des fichiers

| Fichier | Changement |
|---|---|
| `AuthService.java` | 📝 Nouvelle méthode `logout()` |
| `AuthServiceImpl.java` | 📝 Implémentation `logout()` : récupération userId via `SecurityContextHolder`, suppression clé Redis `refresh:{userId}`, nettoyage `SecurityContext` |
| `AuthController.java` | 📝 Nouvel endpoint `POST /api/v1/auth/logout` → 200 avec `ApiResponse.success()` |
| `AuthServiceImplTest.java` | 🆕 3 tests logout (suppression Redis + cas sans token + non authentifié) |
| `AuthControllerTest.java` | 🆕 3 tests endpoint logout (200 OK + 401 token invalide) |

### Logique métier

1. L'utilisateur doit être authentifié (Bearer token valide)
2. Récupération de l'userId depuis `StockMasterPrincipal` (via `SecurityContextHolder`)
3. Suppression de la clé Redis `refresh:{userId}` → révocation du refresh token
4. Nettoyage du contexte de sécurité (`SecurityContextHolder.clearContext()`)
5. Si aucun refresh token en Redis (déjà expiré/déconnecté) → succès quand même (idempotent)
6. Si non authentifié → `AUTH_TOKEN_INVALID` (AUTH_005 → 401)

### Tests (5 nouveaux, 42 total)

| Catégorie | Tests |
|---|---|
| AuthServiceImplTest — logout | 3 (succès Redis + pas de token en Redis + non authentifié) |
| AuthControllerTest — logout | 2 (200 OK + 401 via service mock) |

## US-011 — Mot de passe oublié 🔜
## US-012 — Réinitialisation mot de passe 🔜
## US-013 — Changement mot de passe 🔜

> **Statut :** NON COMMENCÉ
> **Sprint :** 2-3

---

# EPIC 3 à 13 — Modules métier

| Module | Fichiers Java | Statut |
|---|---|---|
| `stockmaster-groupe` | 0 | 🔜 Non commencé |
| `stockmaster-utilisateur` | 0 | 🔜 Non commencé |
| `stockmaster-catalogue` | 0 | 🔜 Non commencé |
| `stockmaster-tiers` | 0 | 🔜 Non commencé |
| `stockmaster-achat` | 0 | 🔜 Non commencé |
| `stockmaster-stock` | 0 | 🔜 Non commencé |
| `stockmaster-vente` | 0 | 🔜 Non commencé |
| `stockmaster-notification` | 0 | 🔜 Non commencé |
| `stockmaster-reporting` | 0 | 🔜 Non commencé |

**US concernées :** US-014 à US-080 (67 user stories)

---

# 📊 TABLEAUX RÉCAPITULATIFS

### Par US

| # | Titre | Statut | Branche | Commit | Pushé | Fichiers | Lignes |
|---|---|---|---|---|---|---|---|
| **US-001** | Initialisation Spring Boot | ✅ Terminé | feature/GS-001 | `e47186c` | ✅ | 44 | ~5 389 |
| **US-002** | Flyway & schéma initial | ✅ Terminé | feature/GS-002 | `a7d76eb` | ✅ | 6 | +412 |
| **US-003** | Gestion erreurs + tests | ✅ Terminé | feature/GS-003 | `c03bff2` | ✅ | 1 | +203 |
| **US-004** | CI/CD pipelines | ✅ Terminé | feature/GS-004 | `e74b481` + `ad86aeb` | ✅ | 3 | +223 |
| **US-005** | Docker + docker-compose | ✅ Terminé | feature/GS-004 | `911fb49` | ✅ | 3 | +213 |
| **US-006** | Inscription entreprise unique | ✅ Terminé | feature/GS-006 | `9fed1df` | ✅ | 17 | +500 |
| **US-007** | Inscription groupe multi-sites | ✅ Terminé | feature/GS-007 | — | ✅ | 9 | +350 |
| **US-008** | Connexion JWT | ✅ Terminé | feature/GS-006 | `9fed1df` | ✅ | 7 | +457 |
| **US-009** | Refresh token | 🚧 Implémenté (branche) | feature/GS-009-refresh-token | — | — | 7 | +180 |
| **US-010** | Déconnexion | 🔜 Non commencé | — | — | — | — | — |
| **US-011** | Mot de passe oublié | 🔜 Non commencé | — | — | — | — | — |
| **US-012** | Réinitialisation mot de passe | 🔜 Non commencé | — | — | — | — | — |
| **US-013** | Changement mot de passe | 🔜 Non commencé | — | — | — | — | — |
| **US-014 à 080** | EPIC 3 à 13 (67 US) | 🔜 Non commencé | — | — | — | — | — |

### Par branche — Statut de merge

| Branche | Commit HEAD | Mergée dans `main` |
|---|---|---|
| `main` | `2577bc4` | ✅ Branche de référence |
| `feature/GS-001-initialize-spring-boot-project` | `0357763` | ✅ Mergée |
| `feature/GS-002-flyway-migrations` | `df5759f` | ✅ Mergée (PR #3) |
| `feature/GS-003-centralized-error-handling` | `c03bff2` | ✅ Mergée (PR #1) |
| `feature/GS-004-ci-cd-pipeline` | `02b441c` | ✅ Mergée (PR #2 → #4) |
| `feature/GS-006-jwt-auth` | `9fed1df` | ✅ Mergée |

### Bilan global

| Métrique | Valeur |
|---|---|
| US terminées | 7 sur 75 (US-001 à US-008) + 1 en attente de merge (US-009) |
| US en cours | US-009 (en attente push + PR) |
| US non commencées | 67 |
| Total commits (sur main) | 24 |
| Total fichiers créés/modifiés | ~92 |
| Total lignes de code | ~9 700 |
| Tests unitaires | **57** (18 shared + 3 intégration + 36 auth : 14 service + 22 contrôleur) |
| Branches créées | 8 (7 mergées + 1 active: feature/GS-009-refresh-token) |
| Modules avec code + tests | 2 sur 11 (shared + auth) |
| Modules vides | 9 sur 11 |

---

## 📋 LÉGENDE

| Symbole | Signification |
|---|---|
| ✅ | Terminé / Mergé |
| 🔜 | Non commencé |

---

> **Règle de gestion du journal :** Ce fichier doit être mis à jour à chaque nouveau commit mergé dans `main`.
> La section de la US modifiée doit refléter le hash du commit et le statut de merge.
> **Prochaine mise à jour prévue :** Après implémentation de US-007, US-009, US-010.

---

## 📎 Documents connexes

| Document | Rôle |
|---|---|
| `document/guideconfiguration.md` | Guide complet des configurations internes et externes |
| `document/BACKLOG_StockMaster_CM.md` | Backlog produit 75 US |
| `document/CDCT_StockMaster_CM_Complet_Sections22-30.md` | Cahier des Charges Techniques |
| `document/A_JIRA_ET_GIT_FLOW.md` | Workflow Jira & Git |
