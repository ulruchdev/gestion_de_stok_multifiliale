# StockMaster CM — Guide de Configuration

> **Référence :** GS-CONFIG-2026-01
> **Version :** 1.0 — Juin 2026
> **Statut :** ✅ Document actif — doit être mis à jour à chaque nouveau service configuré

---

## 📋 À propos de ce document

Ce guide liste **toutes les configurations** (internes et externes) nécessaires au bon fonctionnement de StockMaster CM. Chaque section suit l'ordre logique de mise en place : on commence par ce qui est nécessaire au développement local, puis on monte vers les services externes.

**Règle :** Ce document doit être mis à jour dès qu'une nouvelle configuration est ajoutée (nouveau service, nouveau secret, nouveau profil Spring, etc.).

---

## 🔗 Table des matières des configurations

| # | Configuration | Type | Priorité | Statut |
|---|---|---|---|---|
| 1 | Environnement local (.env) | Interne | P0 — Obligatoire | ✅ Script créé |
| 2 | Ports système | Interne | P0 — Obligatoire | ✅ Vérifié |
| 3 | Profils Spring (dev/test/prod) | Interne | P0 — Obligatoire | ✅ Configuré |
| 4 | JWT Secret | Interne/Externe | P0 — Obligatoire | ⚠️ Générer la clé |
| 5 | Docker Desktop / Moteur | Externe | P0 — Obligatoire | ⚠️ À installer |
| 6 | Docker Compose (local) | Externe | P0 — Obligatoire | ⚠️ À installer |
| 7 | PostgreSQL | Externe | P0 — Obligatoire | ⚠️ À lancer |
| 8 | Redis | Externe | P0 — Obligatoire | ⚠️ À lancer |
| 9 | MinIO (local) | Externe | P1 — Obligatoire V2 | ⚠️ À lancer |
| 10 | MailHog (dev) | Externe | P1 — Obligatoire V2 | ⚠️ À lancer |
| 11 | GitHub Repository | Externe | P0 — Obligatoire | ⚠️ À créer |
| 12 | GitHub Secrets CI/CD | Externe | P0 — Obligatoire | ⚠️ 11 secrets |
| 13 | Branches protégées GitHub | Externe | P0 — Obligatoire | ⚠️ À configurer |
| 14 | Maven Wrapper (mvnw) | Interne | P0 — Obligatoire | ⚠️ À générer |
| 15 | SonarCloud | Externe | P0 — Obligatoire | ⚠️ À configurer |
| 16 | GitHub Container Registry | Externe | P1 — Obligatoire V2 | ⚠️ À configurer |
| 17 | Serveur de Staging | Externe | P1 — Obligatoire V2 | ⚠️ À provisionner |
| 18 | Service Email (prod) | Externe | P1 — Obligatoire V2 | ⚠️ À choisir |
| 19 | OpenAPI / Swagger | Interne | P1 — Obligatoire V2 | ⚠️ À activer |
| 20 | Spring Actuator | Interne | P0 — Obligatoire | ✅ Configuré |
| 21 | OWASP Dependency Check | Interne | P0 — Obligatoire | ✅ Configuré |
| 22 | JaCoCo Coverage | Interne | P0 — Obligatoire | ✅ Configuré |
| 23 | Flyway (migrations) | Interne | P0 — Obligatoire | ✅ Configuré |
| 24 | MapStruct | Interne | P0 — Obligatoire | ✅ Configuré |
| 25 | Lombok | Interne | P0 — Obligatoire | ✅ Configuré |
| 26 | Logging (MDC) | Interne | P1 — Recommandé | ⚠️ À enrichir |
| 27 | Git LFS (fichiers volumineux) | Externe | P2 — Optionnel | 🔜 Non nécessaire |

---

# Partie 1 — Configurations Déjà en Place ✅

---

## 1.1 Profils Spring (dev / test / prod)

**Fichier :** `stockmaster-shared/src/main/resources/application.yml`
**US :** US-001

### Profil `dev` (développement local)

| Propriété | Valeur | Variable |
|---|---|---|
| Base de données | `jdbc:postgresql://localhost:5432/stockmaster_dev` | `${DB_HOST}`, `${DB_PORT}`, `${DB_NAME}` |
| Utilisateur DB | `stockmaster` | `${DB_USERNAME}` |
| Mot de passe DB | `stockmaster` | `${DB_PASSWORD}` |
| Redis | `localhost:6379` | `${REDIS_HOST}`, `${REDIS_PORT}` |
| Hibernate ddl-auto | `validate` | — |
| Flyway clean | Autorisé | — |

### Profil `test` (CI / tests unitaires)

| Propriété | Valeur |
|---|---|
| Base de données | Testcontainers PostgreSQL 16 |
| Hibernate ddl-auto | `none` |
| Flyway | Activé |
| Redis | `localhost:6379` |

### Profil `prod` (production)

| Propriété | Valeur | Variable |
|---|---|---|
| Base de données | PostgreSQL via variables | `${DB_HOST}`, `${DB_NAME}` |
| Pool Hikari | max 20, min 5 | — |
| Redis | via variables | `${REDIS_HOST}:${REDIS_PORT}` |
| Hibernate ddl-auto | `none` | — |
| Flyway clean | **INTERDIT** (`clean-disabled: true`) | — |
| Mail SMTP | authentifié + STARTTLS | `${MAIL_HOST}`, `${MAIL_USERNAME}`, `${MAIL_PASSWORD}` |

---

## 1.2 JWT — Propriétés

**Fichier :** `stockmaster-shared/src/main/resources/application.yml`
**Classe :** `com.stockmaster.shared.config.JwtProperties`
**US :** US-001, US-008

| Propriété | Valeur | Défaut |
|---|---|---|
| `stockmaster.jwt.secret` | Aucun (doit être fourni) | `${JWT_SECRET:}` |
| `stockmaster.jwt.access-token-expiration` | 900s (15 min) | — |
| `stockmaster.jwt.refresh-token-expiration` | 604800s (7 jours) | — |
| `stockmaster.jwt.issuer` | `stockmaster` | — |

---

## 1.3 CORS

**Fichier :** `stockmaster-shared/src/main/resources/application.yml`
**Classe :** `com.stockmaster.shared.config.CorsProperties`
**US :** US-001

| Propriété | Valeur | Défaut |
|---|---|---|
| `stockmaster.cors.allowed-origins` | Frontend local | `http://localhost:5173,http://localhost:3000` |
| `stockmaster.cors.allowed-methods` | GET, POST, PUT, PATCH, DELETE, OPTIONS | — |
| `stockmaster.cors.allowed-headers` | `*` | — |
| `stockmaster.cors.max-age` | 3600s (1h) | — |

---

## 1.4 Pagination

**Fichier :** `stockmaster-shared/src/main/resources/application.yml`
**Classe :** `com.stockmaster.shared.config.PaginationProperties`

| Propriété | Valeur |
|---|---|
| `stockmaster.pagination.default-page-size` | 20 |
| `stockmaster.pagination.max-page-size` | 100 |
| `stockmaster.pagination.default-sort` | `dateCreation,desc` |

---

## 1.5 SonarCloud — Configuration locale

**Fichier :** `sonar-project.properties`
**US :** US-004

| Propriété | Valeur |
|---|---|
| Organisation | `ulruchdev` |
| Project Key | `stockmaster-cm` |
| Host | `https://sonarcloud.io` |
| Sources | 11 modules (shared, auth, groupe, ...) |
| Tests | shared + auth |
| Coverage | `**/target/site/jacoco/jacoco.xml` |
| Exclusions | `**/dto/**`, `**/mapper/**` |
| Java | 21 |
| Quality gate | wait (bloque si < 80%) |

---

## 1.6 Flyway — Migrations

**Fichier :** `stockmaster-shared/src/main/resources/application.yml`
**Scripts :** `stockmaster-shared/src/main/resources/db/migration/`
**US :** US-002

| Propriété | Valeur |
|---|---|
| `spring.flyway.enabled` | `true` |
| `spring.flyway.locations` | `classpath:db/migration` |
| `spring.flyway.baseline-on-migrate` | `false` |
| `spring.flyway.validate-on-migrate` | `true` |
| `spring.flyway.out-of-order` | `false` |
| `spring.flyway.clean-disabled` | `true` (sauf `dev`) |

**Scripts existants :**

| Fichier | Rôle | Lignes |
|---|---|---|
| `V1__init_schema.sql` | 16 tables (tenant_group à notification_alerte) | 277 |
| `V1_rollback_init_schema.sql` | Rollback V1 | 21 |
| `V2__create_indexes.sql` | 12 index de performance | 49 |
| `V2_rollback_indexes.sql` | Rollback V2 | 19 |
| `V3__functions_and_triggers.sql` | Trigger date_modification + vue matérialisée | 41 |
| `V3_rollback_triggers.sql` | Rollback V3 | 5 |

**Règle :** Chaque nouveau script `V{n}__*.sql` DOIT être accompagné de son rollback `V{n}_rollback_*.sql`

---

## 1.7 Docker — Multi-stage

**Fichier :** `Dockerfile`
**US :** US-005

| Étape | Base | Rôle |
|---|---|---|
| Stage 1 — Builder | `eclipse-temurin:21-jdk-alpine` | Compilation Maven, extraction layered JAR |
| Stage 2 — Runtime | `eclipse-temurin:21-jre-alpine` | Exécution non-root, JVM conteneur |

**Flags JVM :** `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`

**Healthcheck :** `GET /actuator/health` sur `localhost:8080`

---

## 1.8 Docker Compose — Environnement local

**Fichier :** `docker-compose.yml`
**US :** US-005

| Service | Image | Ports exposés |
|---|---|---|
| `api` | Build local | `8080` |
| `postgres` | `postgres:16-alpine` | `5432` |
| `redis` | `redis:7-alpine` | `6379` |
| `minio` | `minio/minio:latest` | `9000` (API), `9001` (Console) |
| `mailhog` | `mailhog/mailhog:latest` | `1025` (SMTP), `8025` (UI) |

**Volumes :** `postgres-data`, `redis-data`, `minio-data`
**Réseau :** `stockmaster-network`

---

## 1.9 .gitignore

**Fichier :** `.gitignore`
**US :** US-001, mise à jour US-002

Éléments ignorés : Java/Maven, IDE (IntelliJ, VS Code, Eclipse, NetBeans), OS, secrets, logs, Docker, Node, Flyway temporaires, tests.

**Correction appliquée (US-002) :** `document/.idea/*` ajouté pour éviter que les fichiers IDE du dossier `document/` ne soient recommités.

---

## 1.10 CI/CD Pipelines

**Fichiers :** `.github/workflows/ci.yml`, `.github/workflows/cd.yml`
**US :** US-004

### Pipeline CI

```
Checkout → JDK 21 → Cache Maven → Compile
  → Tests + JaCoCo (≥ 80%)
  → Upload JaCoCo report
  → OWASP Dependency Check
  → Build JAR
  → Upload artifact
  → SonarCloud (job séparé)
```

### Pipeline CD

```
Checkout → Build JAR → Login GHCR
  → Docker metadata + tags (semver, sha, latest)
  → Build & push image
  → Deploy staging via SSH (appleboy/ssh-action)
```

---

# Partie 2 — Configurations Externes à Mettre en Place ⚠️

---

## 2.1 GitHub — Dépôt et Branches Protégées

### Création du dépôt

1. Créer un dépôt GitHub nommé `stockmaster-cm` (ou `gestionulrich` selon le remote existant)
2. Pousser la branche `master` existante vers origin

### Protection de la branche `master`

Dans **Settings → Branches → Add branch protection rule** :

| Règle | Valeur |
|---|---|
| Branch name pattern | `master` |
| Require pull request before merging | ✅ Oui |
| Require approvals | 1 |
| Dismiss stale reviews | ✅ Oui |
| Require status checks | ✅ Oui |
| Status checks requis | `Build & Test`, `SonarCloud Analysis` |
| Require branches up to date | ✅ Oui |
| Include administrators | ✅ Oui |
| Allow force pushes | ❌ Non |
| Allow deletions | ❌ Non |

**Référence CDCT :** Section 28 — OWASP checklist

---

## 2.2 GitHub Secrets — 11 variables obligatoires

Les pipelines CI/CD utilisent les secrets suivants. Ils doivent être configurés dans **Settings → Secrets and variables → Actions** :

### Secrets CI (obligatoires pour la CI)

| Secret | Source | Utilisé dans | Description |
|---|---|---|---|
| `SONAR_TOKEN` | SonarCloud | `ci.yml` (job sonar) | Token d'analyse SonarCloud. À générer dans SonarCloud → Account → Security → Generate Tokens |

### Secrets CD (obligatoires pour le déploiement)

| Secret | Source | Utilisé dans | Description |
|---|---|---|---|
| `STAGING_HOST` | Hébergeur | `cd.yml` | IP ou nom DNS du serveur de staging |
| `STAGING_USER` | Hébergeur | `cd.yml` | Utilisateur SSH (ex: `ubuntu`, `root`) |
| `STAGING_SSH_KEY` | Généré local | `cd.yml` | Clé privée SSH (format PEM). Générer avec `ssh-keygen -t ed25519` |
| `DB_HOST` | Hébergeur | `cd.yml` | Hôte PostgreSQL en staging |
| `DB_NAME` | Hébergeur | `cd.yml` | Nom de la base de données en staging |
| `DB_USERNAME` | Hébergeur | `cd.yml` | Utilisateur PostgreSQL staging |
| `DB_PASSWORD` | Hébergeur | `cd.yml` | Mot de passe PostgreSQL staging |
| `REDIS_HOST` | Hébergeur | `cd.yml` | Hôte Redis en staging |
| `JWT_SECRET` | Généré local | `cd.yml` | Clé secrète JWT 256-bit (voir 2.4) |
| `MAIL_HOST` | Fournisseur email | `cd.yml` | Serveur SMTP prod |

### Secrets manquants détectés

**Note :** Le pipeline CD référence `STAGING_HOST`, `STAGING_USER`, `STAGING_SSH_KEY` (via `appleboy/ssh-action`), mais le workflow `ci.yml` nécessite aussi `STAGING_HOST` qui est référencé indirectement. Vérifier la cohérence.

---

## 2.3 Maven Wrapper

**Requis pour :** CI/CD (`./mvnw` dans Dockerfile)
**Fichier :** `mvnw` + `.mvn/`

Si le Maven Wrapper n'existe pas encore :

```bash
# Depuis la racine du projet (nécessite Maven installé)
mvn wrapper:wrapper -Dmaven=3.9.9
```

**Pourquoi :** Le Dockerfile utilise `./mvnw` pour builder l'image. Sans le wrapper, la compilation Docker échoue.

---

## 2.4 JWT Secret — Génération

**Requis pour :** Local, CI, Staging, Production
**Format :** Clé Base64 de 256 bits minimum pour HS256

```bash
# Linux / macOS
openssl rand -base64 32

# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach {Get-Random -Maximum 256}))

# Alternative (Node.js)
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

**Ne JAMAIS utiliser la clé par défaut** `dev-secret-key-that-is-at-least-256-bits-long-for-hs256` en production.

---

## 2.5 SonarCloud — Organisation & Token

### Étapes

1. Aller sur [SonarCloud.io](https://sonarcloud.io)
2. Se connecter avec GitHub
3. Créer l'organisation `ulruchdev` (déjà référencée dans `sonar-project.properties`)
4. Créer le projet `stockmaster-cm`
5. Générer un token : **Account → Security → Generate token**
6. Ajouter le token dans GitHub Secrets comme `SONAR_TOKEN`

### Qualité

| Seuil | Valeur | Fichier |
|---|---|---|
| Coverage minimale | 80% | `pom.xml` (JaCoCo) + `sonar-project.properties` |
| Blocage pipeline | Oui (qualitygate.wait=true) | `ci.yml` |

---

## 2.6 Docker Hub / GitHub Container Registry

**Requis pour :** CD pipeline (push d'image Docker)

### GitHub Container Registry (GHCR)

Le pipeline CD pousse vers `ghcr.io/<repository>/stockmaster-cm`. Configuration :

1. Aller dans **GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)**
2. Créer un token avec scope `write:packages`, `read:packages`
3. Le `GITHUB_TOKEN` est automatiquement injecté par GitHub Actions (pas besoin de secret manuel)

### Tags Docker générés par le pipeline

| Tag | Format | Exemple |
|---|---|---|
| Semver | `v{major}.{minor}.{patch}` | `v1.0.0` |
| Semver majeur | `v{major}.{minor}` | `v1.0` |
| SHA court | 7 premiers chars du commit | `a7d76eb` |
| Branche | Nom de branche | `feature/GS-002` |
| Latest | `latest` | latest |

---

## 2.7 Serveur de Staging

**Requis pour :** CD pipeline (déploiement automatique)

### Configuration du serveur

| Prérequis | Version minimale |
|---|---|
| OS | Ubuntu 22.04+ ou Debian 12+ |
| Docker Engine | 24+ |
| Docker Compose | V2+ |
| Ports ouverts | 8080, 5432 (optionnel), 6379 (optionnel) |

### Clé SSH

```bash
# Générer une clé de déploiement (sur votre machine)
ssh-keygen -t ed25519 -f ~/.ssh/stockmaster-staging -C "ci@stockmaster.cm"

# Ajouter la clé publique au serveur
ssh-copy-id -i ~/.ssh/stockmaster-staging.pub user@staging-host

# Ajouter la clé PRIVÉE dans GitHub Secrets → STAGING_SSH_KEY
cat ~/.ssh/stockmaster-staging
```

---

## 2.8 Service Email (Production)

**Requis pour :** US-006 (email bienvenue), US-011 (mot de passe oublié), US-074, US-075

### Profil prod (application.yml)

```yaml
spring:
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Services recommandés

| Service | Offre gratuite | SMTP |
|---|---|---|
| **MailHog** (dev) | Gratuit (local Docker) | Port 1025 |
| **SendGrid** | 100 emails/jour | smtp.sendgrid.net:587 |
| **Mailgun** | 100 emails/jour | smtp.mailgun.org:587 |
| **Amazon SES** | 62 000 emails/mois | email-smtp.<region>.amazonaws.com:587 |

---

## 2.9 Environnement Local — Fichier `.env`

**Fichier :** `.env` (à créer à partir de `.env.example`)

```bash
# Copier le template
cp .env.example .env

# Éditer .env avec les valeurs réelles
```

**Fichier `.env.example` existant :**
```env
JWT_SECRET=une-cle-tres-longue-dau-moins-256-bits-pour-hs256
DB_HOST=localhost
DB_PORT=5432
DB_NAME=stockmaster_dev
DB_USERNAME=stockmaster
DB_PASSWORD=stockmaster
REDIS_HOST=localhost
REDIS_PORT=6379
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=stockmaster
MINIO_SECRET_KEY=stockmaster
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

**⚠️ Ne JAMAIS commiter `.env`** — il est dans `.gitignore`.

---

## 2.10 MinIO — Buckets à créer

**Requis pour :** US-014 (logo groupe), US-026 (photo utilisateur), US-031 (photo article), US-063 (facture PDF)

### Buckets requis

| Bucket | Usage | Politique |
|---|---|---|
| `stockmaster-logos` | Logo des entreprises | Privé |
| `stockmaster-photos` | Photos articles + utilisateurs | Privé |
| `stockmaster-factures` | Factures PDF générées | Privé |

### Création (Console MinIO)

1. Accéder à `http://localhost:9001` (Console MinIO)
2. Se connecter avec `stockmaster` / `stockmaster` (ou les valeurs `.env`)
3. Créer chaque bucket via l'interface

### Création (API)

```bash
# Avec mc (MinIO Client)
mc alias set stockmaster http://localhost:9000 stockmaster stockmaster
mc mb stockmaster/stockmaster-logos
mc mb stockmaster/stockmaster-photos
mc mb stockmaster/stockmaster-factures
```

---

# Partie 3 — Configurations Internes à Activer

---

## 3.1 SpringDoc OpenAPI / Swagger UI

**Requis pour :** US-003 (DoD : "Documentation OpenAPI à jour")

### Dépendance (déjà dans `pom.xml`)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc.version}</version>
</dependency>
```

### Accès

| Environnement | URL |
|---|---|
| Local | `http://localhost:8080/swagger-ui.html` |
| Staging | `https://staging.stockmaster.cm/swagger-ui.html` |
| Production | À configurer (désactiver si non souhaité) |

---

## 3.2 Logging MDC (Contexte)

**Requis pour :** Traçabilité des requêtes (CDCT Section 28 — A09 Logging Failures)

### Configuration à ajouter dans `application.yml`

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{userId:-anonymous}] [%X{requestId:-}] - %msg%n"
```

### MDC Filter à implémenter

Un filtre doit enrichir le MDC avec `userId` et `requestId` (UUID) pour chaque requête. Le `JwtAuthenticationFilter` existant peut être étendu pour :

```java
// À ajouter dans JwtAuthenticationFilter
MDC.put("userId", String.valueOf(userId));
MDC.put("requestId", UUID.randomUUID().toString());
```

et un `MDC.clear()` dans `finally` du filtre.

---

## 3.3 Cache Redis — Annotation `@Cacheable`

**Requis pour :** US-020 (dashboard groupé), US-028 (liste catégories), US-055 (stock consolidé)

Cache déjà attendu dans le code. Les caches à déclarer dans la config :

```java
@Configuration
@EnableCaching
public class CacheConfig {
    // Les caches seront créés dynamiquement par @Cacheable
}
```

Les clés de cache suivantes seront utilisées par les US futures :

| Cache | TTL | US |
|---|---|---|
| `categories` | 5 min | US-028 |
| `dashboard_groupe` | 5 min | US-020 |
| `stock_consolide` | 2 min | US-055 |
| `top_articles` | 10 min | US-076 |
| `ca_evolution` | 10 min | US-078 |

---

## 3.4 Actuator — Endpoints exposés

**Déjà configuré dans `application.yml` :**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### Endpoints disponibles

| Endpoint | Usage |
|---|---|
| `GET /actuator/health` | Healthcheck Docker + Kubernetes |
| `GET /actuator/info` | Infos build (version, nom) |
| `GET /actuator/metrics` | Métriques JVM, HikariCP, etc. |

**En production**, envisager de restreindre `/actuator/metrics` si non utilisé par un monitoring externe.

---

## 3.5 OWASP Dependency Check

**Déjà configuré dans `pom.xml` :**

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>${owasp-dependency-check.version}</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

**Exécution locale :**
```bash
mvn dependency-check:check
```

**Rapports générés :** `target/dependency-check-report.html` et `.json`

---

# Partie 4 — Vérifications et Commandes

---

## 4.1 Checklist de démarrage local

```bash
# 1. Vérifier Java
java -version    # Doit être ≥ 21

# 2. Vérifier Maven
mvn -version     # Doit être ≥ 3.9.x

# 3. Vérifier Docker
docker --version && docker compose version

# 4. Lancer l'environnement
docker compose up -d

# 5. Vérifier que les services tournent
docker compose ps
# Attendre : postgres healthy, redis healthy, minio running

# 6. Copier .env
cp .env.example .env

# 7. Générer un vrai JWT_SECRET
# (remplacer la valeur par défaut dans .env)

# 8. Lancer l'application
mvn spring-boot:run -pl stockmaster-shared
# ou via IntelliJ → StockMasterApplication (profil dev)

# 9. Vérifier le healthcheck
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# 10. Tester Flyway (vérifier les logs au démarrage)
# → "Successfully applied 3 migrations"
```

---

## 4.2 Checklist pré-déploiement

```bash
# 1. Compilation
mvn compile -q

# 2. Tests + Coverage
mvn verify
# Coverage ≥ 80% obligatoire

# 3. OWASP
mvn dependency-check:check
# Échoue si CVE avec CVSS ≥ 7

# 4. Build JAR
mvn package -DskipTests

# 5. Docker build local
docker compose build

# 6. Vérifier le bon fonctionnement
docker compose up -d
```

---

# Partie 5 — Traçabilité des Configurations

---

## 5.1 Correspondance US → Configuration

| US | Configuration concernée | Fichier(s) |
|---|---|---|
| **US-001** | Spring Boot, POM, JPA, Jackson, Logging | `pom.xml`, `application.yml` |
| **US-002** | Flyway, schéma BDD, index, triggers | `application.yml`, `V*.sql`, `rollback_*.sql` |
| **US-003** | GlobalExceptionHandler, ErrorCode RFC 7807 | `GlobalExceptionHandler.java` |
| **US-004** | CI/CD, GitHub Secrets, SonarCloud, OWASP, JaCoCo | `ci.yml`, `cd.yml`, `sonar-project.properties`, `pom.xml` |
| **US-005** | Docker, docker-compose, MinIO, MailHog | `Dockerfile`, `docker-compose.yml`, `.env.example` |
| **US-006** | JWT, BCrypt, validation Jakarta, email | `application.yml`, `JwtTokenProvider.java`, `SecurityConfig.java` |
| **US-008** | JWT, Rate limiting Redis, Security chain | `application.yml`, `RateLimitFilter.java`, `JwtAuthenticationFilter.java` |
| **US-020** | Cache Redis dashboard | `@Cacheable` (à implémenter) |
| **US-063** | MinIO stockage factures | Bucket `stockmaster-factures` |

---

## 5.2 Journal des modifications

| Date | Modifieur | Changement | Raison |
|---|---|---|---|
| Juin 2026 | ulrich dev | Création initiale du guide | Documenter toutes les configurations |
| | | | |

---

> **Règle de gestion du guide :** Chaque fois qu'une nouvelle US ajoute une configuration (nouveau service externe, nouvelle variable d'environnement, nouveau bucket, nouveau profil Spring, nouveau secret GitHub), mettre à jour ce guide dans la même PR.
>
> **Prochaine révision prévue :** Après implémentation d'US-009 (Refresh token Redis), US-014 (MinIO logos), US-020 (Cache Redis).
