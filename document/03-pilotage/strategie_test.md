# StockMaster CM — Stratégie de Test

> **Référence :** GS-TEST-2026-01
> **Version :** 1.0 — Juin 2026
> **Statut :** ✅ Document actif — doit être mis à jour à chaque nouveau module ou endpoint ajouté

---

## 📋 À propos de ce document

Ce guide est destiné à tout **QA** ou **développeur** travaillant sur StockMaster CM. Il décrit :

1. Les **prérequis** pour exécuter les tests
2. Les **commandes à taper avant chaque push**
3. Comment **valider que le pipeline CI passera**
4. Comment utiliser **Swagger UI** pour tester les endpoints
5. Les **règles d'écriture des tests** (conventions)

---

## 🔗 Table des matières

- [1. Prérequis](#1-prérequis)
- [2. Commandes essentielles](#2-commandes-essentielles)
- [3. Workflow pré-push (checklist)](#3-workflow-pré-push-checklist)
- [4. Pipeline CI — Comprendre ce qui est vérifié](#4-pipeline-ci--comprendre-ce-qui-est-vérifié)
- [5. Swagger UI — Tester les endpoints](#5-swagger-ui--tester-les-endpoints)
- [6. Règles d'écriture des tests](#6-règles-décriture-des-tests)
- [7. Dépannage](#7-dépannage)
- [8. Journal des modifications](#8-journal-des-modifications)

---

## 1. Prérequis

### 1.1 Outils obligatoires

| Outil | Version minimale | Vérification |
|---|---|---|
| Java (OpenJDK / Temurin) | 21 LTS | `java -version` |
| Maven | 3.9.x | `mvn -version` |
| Docker Desktop | 24+ | `docker --version` |
| Docker Compose | V2+ | `docker compose version` |
| Git | 2.40+ | `git --version` |

### 1.2 Services requis pour les tests

La **suite unitaire** (la grande majorité des tests) est **100 % mocks** : elle passe **sans aucun service démarré**.

Seuls les **futurs tests d'intégration** — et en pratique `StockMasterApplicationTest` (profil `integration`, démarrage complet de l'application) — nécessitent **PostgreSQL 16** et **Redis 7** :

```bash
# Lancer les services
docker compose up -d postgres redis

# Vérifier qu'ils sont healthy
docker compose ps
# → postgres: healthy, redis: healthy
```

**Attention :** Les tests d'intégration utilisent le PostgreSQL du docker-compose. Docker Desktop doit être en cours d'exécution avec `docker compose up -d postgres redis` avant de lancer les tests.

---

## 2. Commandes essentielles

### 2.1 Compilation seule

```bash
# Compiler tous les modules (vérifie la syntaxe, les imports, les génériques)
mvn compile -q

# Compiler un module spécifique (plus rapide)
mvn compile -pl stockmaster-shared -q
mvn compile -pl stockmaster-auth -q
```

**Temps estimé :** 5-15s (avec cache Maven) / 30-60s (première fois)

### 2.2 Exécuter tous les tests

```bash
# Compilation + Tests + JaCoCo (≥ 80% de couverture)
mvn verify
```

**Ce que fait `mvn verify` :** compile les tests → exécute les tests → vérifie la couverture JaCoCo ≥ 80% → échoue si en dessous

**Temps estimé :** 30-60s (PostgreSQL et Redis doivent être lancés via `docker compose up -d postgres redis`)

### 2.3 Exécuter les tests d'un module spécifique

```bash
# Tests unitaires du module shared uniquement
mvn test -pl stockmaster-shared

# Avec logs détaillés
mvn test -pl stockmaster-shared -X
```

### 2.4 Exécuter un test spécifique

```bash
# Une seule classe de test
mvn test -pl stockmaster-shared -Dtest=GlobalExceptionHandlerTest

# Une seule méthode
mvn test -pl stockmaster-shared -Dtest=GlobalExceptionHandlerTest#shouldReturn400ForValidationError
```

### 2.5 Voir le rapport de couverture

```bash
# Exécuter les tests avec couverture
mvn verify

# Ouvrir le rapport HTML
# Windows :
start stockmaster-shared/target/site/jacoco/index.html   # couverture du module shared

# Linux :
xdg-open stockmaster-shared/target/site/jacoco/index.html

# macOS :
open stockmaster-shared/target/site/jacoco/index.html
```

### 2.6 Analyse SonarCloud locale

```bash
# Avec SONAR_TOKEN configuré dans les variables d'environnement
mvn install -DskipTests sonar:sonar \
  -Dsonar.organization=ulruchdev \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.projectKey=gestion_de_stok_multifiliale \
  -Dsonar.token=$SONAR_TOKEN
```

### 2.7 Build JAR

```bash
# Générer le JAR exécutable
mvn package -DskipTests -q

# Le JAR se trouve dans :
# stockmaster-bootstrap/target/stockmaster-bootstrap-1.0.0-SNAPSHOT.jar
#
# ⚠️ stockmaster-shared est une LIBRARY module
#    (<skip>true</skip> sur spring-boot-maven-plugin) : il ne produit
#    PAS de JAR exécutable. Le main() vit dans stockmaster-bootstrap.
```

### 2.8 OWASP Dependency Check (optionnel)

```bash
# Analyse des vulnérabilités des dépendances
mvn dependency-check:check

# Rapport : target/dependency-check-report.html
```

**Note :** L'API Sonatype OSS Index peut retourner une erreur `401 Unauthorized`. Ce n'est pas bloquant. L'analyse NVD (National Vulnerability Database) fonctionne toujours.

---

## 3. Workflow pré-push (checklist)

Avant chaque `git push`, exécutez ces commandes dans l'ordre :

### ✅ Étape 1 — Compilation

```bash
mvn compile -q
```

**Résultat attendu :** `BUILD SUCCESS` (12 modules)

### ✅ Étape 2 — Tests + Couverture

```bash
mvn verify
```

**Résultat attendu :** `BUILD SUCCESS` + compte de tests par module réel +
`Coverage ≥ 80%` sur nouveau code.

Exemple récent (module catalogue, branche `feature/GS-027-creer-categorie`) :

```bash
cd backend && mvn -q test -pl stockmaster-catalogue \
  -Dtest=CategorieServiceTest,CategorieControllerTest,ArticleDefaultsTest \
  -DfailIfNoTests=false
```

**Résultat observé en local :** `BUILD SUCCESS` —
`CategorieServiceTest` 39 tests (2 ignorés dans `TauxTva`), `CategorieControllerTest`
20 tests, `ArticleDefaultsTest` 2 tests — aucun échec, aucune erreur.
**Résultat attendu :** `BUILD SUCCESS` + **545 tests, 0 failure, 0 error**

> **Compteur réel (mesuré le 29 oct. 2026 après merge de `main` dans `feature/GS-051-consultation-stock`, soit US-036→039 + port d'écriture stock + US-051/US-052) :** shared 71 · auth 96 · groupe 67 · catalogue 99 (dont 2 skippés — US-027→031 + defaults) · notification 8 · utilisateur 132 · tiers **102** (dont 100 US-036→039 : 60 service + 40 contrôleur, + 2 fondamentaux) · achat **6** (3 fondamentaux + 3 `CommandesFournisseurAdapterTest`) · stock **75** (12 `EcritureStockServiceTest` + 43 US-051/052 : `StockServiceTest` 24, `StockControllerTest` 11, `StatutAlerteTest` 8) · vente 8 · reporting 5 · bootstrap 7 (dont 4 ArchUnit) = **676** — `BUILD SUCCESS`, 0 failure, 0 error, 2 skipped.
> Les 3 tests de `bootstrap` (`StockMasterApplicationTest`) échouent **sans PostgreSQL** — c'est environnemental, pas un bug : `docker compose up -d postgres redis` les rend verts.
>
> ⚠️ **Le gate « Coverage ≥ 80 % » n'est PAS actif dans le build** (item **C-14** de la checklist `DEC-031` ouvert : le pom parent ne contient que `prepare-agent` + `report`, aucune exécution `jacoco:check`). Ne prétendez pas qu'il bloque la CI.

### ✅ Étape 3 — Build JAR

```bash
mvn package -DskipTests -q
```

**Résultat attendu :** `BUILD SUCCESS` + JAR dans `stockmaster-bootstrap/target/`

### ✅ Étape 4 (optionnelle locale, automatique dans le CI) — Vérifier que l'application démarre

```bash
# Lancer le JAR et vérifier le healthcheck (stockmaster-bootstrap porte le main())
java -jar stockmaster-bootstrap/target/*.jar --spring.profiles.active=test &
sleep 45
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

**Résultat attendu :** L'application démarre et répond `200 OK` sur `/actuator/health`

### ✅ Étape 5 — Push et CI

```bash
git push origin ma-branche
```

**Résultat attendu :** Le pipeline CI sur GitHub Actions doit passer :
- ✅ Compilation (12/12 modules)
- ✅ Tests unitaires par module (ex. catalogue : `CategorieServiceTest` +
  `CategorieControllerTest` + `ArticleDefaultsTest` verts en local)
- ✅ JaCoCo coverage ≥ 80% sur nouveau code
- ✅ Tests unitaires (421 tests, 0 failure, 0 error)
- ✅ JaCoCo — *rapport généré ; le `jacoco:check` ≥ 80 % reste à activer (C-14)*
- ✅ SonarCloud analysis
- ✅ Build JAR

### 🔄 Résumé visuel

```
Avant push :
  1. mvn compile -q                     → SUCCESS (12 modules)
  2. mvn verify                          → SUCCESS (421 tests, 0 échec — dont 4 ArchUnit)
  3. mvn package -DskipTests             → SUCCESS (JAR généré)
  4. java -jar .../target/*.jar          → Vérifier /actuator/health = UP (optionnel)
  5. git push                             → CI pipeline vert

Pipeline CI (automatique après push) :
  Checkout → JDK 21 → Compile → Tests + JaCoCo
  → Upload coverage → Build JAR → Upload JAR
  → ⭐ Lancer le JAR + curl /actuator/health → ✅ Application démarre
  → SonarCloud analysis
```

---

## 4. Pipeline CI — Comprendre ce qui est vérifié

### 4.1 Jobs du pipeline

Le pipeline CI du backend (`.github/workflows/ci-backend.yml`) exécute les jobs de build + tests Maven ;
le pipeline frontend (`.github/workflows/ci-frontend.yml`) exécute le build Vite + TypeScript typecheck.

#### Job 1 : `Build & Test`

| Étape | Commande | Vérifie |
|---|---|---|
| Checkout | `actions/checkout@v4` | Récupère le code |
| Setup Java | `actions/setup-java@v4` | JDK 21 Temurin |
| Cache Maven | Cache `~/.m2/repository` | Accélère les builds suivants |
| Compile | `mvn compile -q` | Syntaxe Java, imports, génériques |
| Tests + Coverage | `mvn verify` | Tests unitaires + JaCoCo ≥ 80% + **@SpringBootTest** |
| Upload JaCoCo | Artifact `jacoco-report` | Rapport de couverture conservé 7 jours |
| Build JAR | `mvn package -DskipTests -q` | JAR exécutable |
| **⭐ Vérification démarrage** | `java -jar ...` + `curl /actuator/health` | **✅ L'application démarre réellement** |
| Upload JAR | Artifact `stockmaster-jar` | JAR conservé 7 jours |

#### Job 2 : `SonarCloud Analysis`

| Étape | Commande | Vérifie |
|---|---|---|
| Checkout (full depth) | `actions/checkout@v4` avec `fetch-depth: 0` | Historique git complet |
| Install + Sonar | `mvn install -DskipTests sonar:sonar` | Quality Gate SonarCloud |

### 4.2 Services CI

Le pipeline démarre automatiquement :

| Service | Image | Utile pour |
|---|---|---|
| PostgreSQL 16 | `postgres:16-alpine` | Tests d'intégration / Flyway |
| Redis 7 | `redis:7-alpine` | Rate limiting / Cache |

### 4.3 Secrets GitHub nécessaires

| Secret | Utilisé dans | Optionnel ? |
|---|---|---|
| `SONAR_TOKEN` | Job SonarCloud | ❌ Obligatoire |

### 4.4 Branches protégées

La branche `main` est protégée :
- ✅ **PR obligatoire** avant merge
- ✅ **CI vert** obligatoire
- ✅ **1 reviewer** minimum

---

## 5. Swagger UI — Tester les endpoints

### 5.1 Accès à Swagger

**Dépendance déjà incluse :** `springdoc-openapi-starter-webmvc-ui` (version 2.7.0)

**URL d'accès (application lancée) :**

| Environnement | URL Swagger UI |
|---|---|
| Local (profil dev) | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| Local | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| API docs (JSON) | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |

### 5.2 Lancer l'application pour Swagger

```bash
# 1. Démarrer les services
docker compose up -d

# 2. Lancer l'application (profil dev)
mvn spring-boot:run -pl stockmaster-bootstrap

# 3. Ouvrir Swagger UI dans le navigateur
# → http://localhost:8080/swagger-ui.html
```

### 5.3 Tester les endpoints avec Swagger

1. Ouvrir **Swagger UI** dans le navigateur
2. Les endpoints disponibles sont listés par contrôleur
3. Cliquer sur un endpoint pour le déplier
4. Cliquer sur **"Try it out"**
5. Remplir les paramètres (ou le corps JSON)
6. Cliquer sur **"Execute"**
7. Voir la réponse (statut HTTP + corps JSON)

### 5.4 Endpoints actuellement disponibles

#### Auth — `/api/v1/auth`

| Méthode | Endpoint | Description | Body requis | Authentification |
|---|---|---|---|---|
| POST | `/api/v1/auth/inscription/entreprise-unique` | Inscription boutique unique | `InscriptionEntrepriseUniqueRequest` | ❌ Non |
| POST | `/api/v1/auth/inscription/groupe` | Inscription groupe multi-sites | `InscriptionGroupeRequest` | ❌ Non |
| POST | `/api/v1/auth/login` | Connexion JWT | `LoginRequest` | ❌ Non |
| POST | `/api/v1/auth/refresh` | Refresh token | `RefreshTokenRequest` | ❌ Non |
| POST | `/api/v1/auth/logout` | Déconnexion (révocation refresh token) | Aucun (Bearer token) | ✅ Oui |
| POST | `/api/v1/auth/forgot-password` | Mot de passe oublié | `ForgotPasswordRequest` | ❌ Non |
| POST | `/api/v1/auth/reset-password` | Réinitialisation mot de passe | `ResetPasswordRequest` | ❌ Non |
| PUT | `/api/v1/auth/change-password` | Changement mot de passe (connecté) | `ChangePasswordRequest` | ✅ Oui |

**Exemple de test — Inscription :**

```json
{
  "nomEntreprise": "Épicerie Centrale",
  "nif": "M071234567890Z",
  "email": "jean.kamga@test.cm",
  "telephone": "+237691234567",
  "adminNom": "Kamga",
  "adminPrenom": "Jean",
  "motDePasse": "Test@2026"
}
```

> ⚠️ Contraintes : `telephone` **E.164 obligatoire** · `nif` optionnel · `ville`/`quartier` **n'existent plus** (retraits du DTO en juillet 2026).

**Réponse attendue :** `201 CREATED`
```json
{
  "success": true,
  "message": "Votre espace a été créé. Vérifiez votre email pour activer votre compte.",
  "data": { "email": "jean.kamga@test.cm", "groupId": 1 }
}
```

**Exemple de test — Connexion :**

```json
{
  "email": "jean.kamga@test.cm",
  "motDePasse": "Test@2026"
}
```

**Réponse attendue :** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 900,
    "role": "ADMIN_GROUPE",
    "scope": "GROUPE"
  }
}
```

### 5.5 Swagger avec authentification JWT

Pour tester les endpoints protégés (futurs modules) :

1. D'abord, appeler `POST /api/v1/auth/login` pour obtenir un `accessToken`
2. Copier le token (commence par `eyJ...`)
3. Dans Swagger UI, cliquer sur le bouton **"Authorize"** (en haut à droite)
4. Coller le token dans le champ : `Bearer <token>`
5. Cliquer sur **"Authorize"** puis **"Close"**
6. Les requêtes ultérieures incluront automatiquement le token

---

## 6. Règles d'écriture des tests

### 6.1 Convention de nommage

```
{ClasseTestee}Test.java      → Tests unitaires (Mockito, isolation)
{ClasseTestee}IT.java        → Tests d'intégration (Spring Boot complet)
```

**Exemples :**
- `GlobalExceptionHandlerTest.java` → test unitaire ✅
- `StockMasterApplicationTest.java` → **test d'intégration** (démarrage Spring complet) ✅

### 6.2 Types de tests

| Type | Annotation | Profil | Dépendances | Rapidité |
|---|---|---|---|---|
| **Unitaire** | `@ExtendWith(MockitoExtension.class)` | Aucun | Aucune | ⚡ Très rapide (< 1s) |
| **Intégration** | `@SpringBootTest(webEnvironment = RANDOM_PORT)` | `test` | Docker (PostgreSQL via docker-compose) + Redis | ⏱️ ~30s |

```java
@ExtendWith(MockitoExtension.class)       // Mockito pour l'isolation
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("Inscription entreprise unique")
    class InscriptionTests {

        @Test
        void shouldCreateEntrepriseWhenEmailIsUnique() {
            // given (arrange)
            // when (act)
            // then (assert)
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            // given
            // when
            // then → assertThrows
        }
    }
}
```

### 6.3 Bonnes pratiques

| Règle | Pourquoi |
|---|---|
| Utiliser `@Nested` pour grouper par méthode testée | Lisibilité dans le rapport de test |
| Nommer les tests `should{Comportement}When{Condition}` | Lisibilité : "devrait faire X quand Y" |
| Un seul `assert` par test (ou assertions cohérentes) | Isolation des échecs |
| Ne pas tester Spring Boot en intégration si inutile | `@ExtendWith(MockitoExtension.class)` est + rapide |
| Tester les cas d'erreur autant que les cas heureux | Couverture des chemins d'échec |
| Utiliser `@DisplayName` en français lisible | Les QA doivent comprendre les tests |
| Coverage ≥ 80% sur chaque module | Cible actée ; **le `jacoco:check` n'est pas encore branché dans le build** (checklist `DEC-031` C-14) |
| Tester l'isolation multi-tenant | Filtre `entreprise_id` systématique |

### 6.4 Ce qui doit être testé (par priorité)

1. **Services métier** — toute la logique de validation + règles métier
2. **Contrôleurs** — statuts HTTP, corps de réponse, erreurs
3. **Exceptions** — handler global, codes d'erreur, RFC 7807
4. **Mappers** — MapStruct (conversion DTO ↔ Entité)
5. **Validations Jakarta** — contraintes sur les champs
6. **Architecture** — ArchUnit (règles de couches, dépendances)

### 6.5 Framework de test

| Framework | Usage | Dépendance |
|---|---|---|
| **JUnit 5** (Jupiter) | Structure des tests | `spring-boot-starter-test` |
| **Mockito** | Isolation (mocks, spies) | `spring-boot-starter-test` |
| **AssertJ** | Assertions fluides `assertThat(...)` | `spring-boot-starter-test` |
| **PostgreSQL JDBC** | Connexion directe au docker-compose | `postgresql` |
| **ArchUnit** | Tests d'architecture | `archunit-junit5` |

---

## 7. Dépannage

### 7.1 La compilation échoue

```bash
# Problème : package com.stockmaster.shared.exception does not exist
# Solution : Le module shared doit être installé avant auth
mvn install -DskipTests -q
```

### 7.2 Les tests échouent avec "No tests found"

```bash
# Problème : mvn test n'a trouvé aucun test
# Vérifier que le fichier se termine bien par Test.java
# Vérifier que la méthode est annotée @Test
mvn test -pl stockmaster-shared -Dtest=GlobalExceptionHandlerTest -X
```

### 7.3 JaCoCo échoue avec "Coverage < 80%"

```bash
# Voir le rapport détaillé dans le navigateur
start stockmaster-shared/target/site/jacoco/index.html
# Ajouter des tests pour les parties non couvertes
```

### 7.4 SonarCloud échoue avec "Project not found"

```bash
# Vérifier les paramètres
mvn install -DskipTests sonar:sonar \
  -Dsonar.organization=ulruchdev \
  -Dsonar.projectKey=gestion_de_stok_multifiliale \
  -Dsonar.token=$SONAR_TOKEN
```

### 7.5 Docker compose ne démarre pas

```bash
# Vérifier que Docker Desktop est lancé
docker info

# Vérifier les logs
docker compose logs postgres
docker compose logs redis
```

### 7.6 Swagger n'est pas accessible

```bash
# Vérifier que l'application tourne
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# Vérifier les logs au démarrage
# Chercher : "SpringDoc" ou "OpenAPI" dans les logs
```

---

## 8. Journal des modifications

| Date | Auteur | Changement | Raison |
|---|---|---|---|
| Juin 2026 | Codebuff | Création initiale | Guide stratégie de test |
| 2026-10-19 | clement ange | Mise à jour compte de tests catalogue et CI | Ajout exemple réel `CategorieServiceTest`/`CategorieControllerTest`/`ArticleDefaultsTest` (39 + 20 + 2 tests, 2 ignorés) et alignement workflow pré-push sur `ci-backend.yml`/`ci-frontend.yml` |
| 2026-10-27 | Codebuff | Compte 502 → **545** (stock 20 → 63) | Livraison US-051/US-052 (EPIC 8) — 43 tests ajoutés (`StockServiceTest`, `StockControllerTest`, `StatutAlerteTest`). |
| 2026-10-28 | Codebuff | Résolution du piège réseau local (5432/6379 muets) | `StockMasterApplicationTest` échouait en `EOFException` SSL quand le relais `docker-proxy` de l'hôte ne répondait plus (chaque `SYN` accepté puis `FIN+RST`, le proxy ne dial-ait jamais le conteneur) et l'ordre des règles DNAT dans la chaîne `DOCKER` pouvait pointer vers les mauvais conteneurs. **Correctifs conservés sur l'hôte** : `"userland-proxy": false` dans `/var/snap/docker/3613/config/daemon.json` (le trafic loopback passe alors par le DNAT iptables de la chaîne `DOCKER`) et reconstruction du réseau via `docker compose up -d --force-recreate` pour régénérer des règles DNAT cohérentes. ⚠️ si un `snap refresh docker` crée une nouvelle révision, son `config/daemon.json` repart des défauts — reposer `userland-proxy: false` puis `sudo snap restart docker`. `mvn test` fonctionne à nouveau **sans aucun workaround d'env** (reactor 545/545 vert, 2026-10-28). L'ancien workaround `DB_HOST=<IP conteneur>` n'est plus nécessaire |
| 2026-10-28 | baudrel | Port d'écriture stock GS-1240 : +12 tests `EcritureStockServiceTest` (module stock 20 → 32, reactor 577/577) ; whitelist ArchUnit étendue à `stock.port..` + `stock.domain.enums..` | Ticket #1240 — contrat inter-modules pour EPIC 8 (achat/vente/transferts écrivent le stock par ce port uniquement) |
---

> **Règle de gestion :** Ce document doit être mis à jour à chaque nouveau module ajouté, nouveau test écrit, ou changement dans la stack technique. Les commandes ci-dessus doivent toujours refléter l'état réel du projet.

> **Prochaine révision prévue :** Après ajout des tests d'intégration, des tests contrôleurs, ou d'ArchUnit.
