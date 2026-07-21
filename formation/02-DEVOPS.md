# 🎓 Formation DevOps — StockMaster CM
> **Cours exhaustif** couvrant tout le pipeline DevOps : Docker, CI/CD, infrastructure, monitoring.
> Garde ce fichier pendant ta formation, supprime-le quand tu maîtrises.
>
> **Public :** Ulrich — Tech Lead / DevOps
> **Prérequis :** Notions de base en ligne de commande
> **Concepts couverts :** 100+ concepts DevOps, Docker, CI/CD, sécurité infrastructure

---

## Table des matières

1. [Philosophie DevOps](#1-philosophie-devops)
2. [Git & Workflow de branches](#2-git--workflow-de-branches)
3. [Docker — Conteneurisation](#3-docker--conteneurisation)
4. [Docker Compose — Orchestration locale](#4-docker-compose--orchestration-locale)
5. [CI — Intégration Continue avec GitHub Actions](#5-ci--integration-continue-avec-github-actions)
6. [CD — Déploiement Continu](#6-cd--deploiement-continu)
7. [PostgreSQL 16 — Base de données](#7-postgresql-16--base-de-donnees)
8. [Redis 7 — Cache & Sessions](#8-redis-7--cache--sessions)
9. [MinIO — Stockage de fichiers S3](#9-minio--stockage-de-fichiers-s3)
10. [MailHog — Capture d'emails en dev](#10-mailhog--capture-demails-en-dev)
11. [SonarCloud — Qualité du code](#11-sonarcloud--qualite-du-code)
12. [JaCoCo — Couverture de tests](#12-jacoco--couverture-de-tests)
13. [OWASP Dependency Check — Sécurité](#13-owasp-dependency-check--securite)
14. [Profils Spring Boot (dev/test/prod)](#14-profils-spring-boot-devtestprod)
15. [Variables d'environnement & Secrets](#15-variables-denvironnement--secrets)
16. [Surveillance & Monitoring (Actuator)](#16-surveillance--monitoring-actuator)
17. [Stratégie de backup & Rollback](#17-strategie-de-backup--rollback)
18. [Glossaire & Concepts Clés](#18-glossaire--concepts-cles)

---

## 1. Philosophie DevOps

### 🤔 C'est quoi le DevOps ?

**DevOps** = Development + Operations. C'est l'ensemble des pratiques qui automatisent
tout ce qui se passe entre le moment où un développeur écrit du code et le moment
où ce code est utilisé par les clients en production.

### Le problème avant DevOps :

```
Développeur écrit du code (semaine 1)
    ↓
"Ça marche sur ma machine" 
    ↓
Jette le code "par-dessus le mur" à l'équipe Ops (semaine 2)
    ↓
Ops galère à déployer (environnement différent, dépendances manquantes)
    ↓
Bug découvert en production (semaine 3)
    ↓
Retour au développeur → Conflits, stress, nuit blanche
```

### Avec DevOps dans StockMaster CM :

```
1. Développeur commit → git push
2. CI (GitHub Actions) compile, teste, analyse automatiquement
3. Si tout est vert → build Docker
4. CD (GitHub Actions) déploie automatiquement sur staging
5. Si bug → correction → recommencer à l'étape 1
   Temps total : 10-15 minutes, pas 3 semaines
```

### Les 3 piliers DevOps de StockMaster CM :

| Pilier | Outil | Rôle |
|--------|-------|------|
| **CI** (Intégration Continue) | GitHub Actions + Maven | À chaque push : compile, teste, analyse |
| **CD** (Déploiement Continu) | GitHub Actions + Docker | Build image → Push registry → Déploiement |
| **Infrastructure as Code** | Docker Compose | L'environnement décrit dans un fichier YAML |
| **Qualité** | SonarCloud + JaCoCo | Code propre, bien testé, pas de vulnérabilités |

---

## 2. Git & Workflow de branches

### 🌿 Le modèle de branches StockMaster CM

```
main ────────────────────────────────── (intégration)
  │
  ├── feature/GS-014-refresh-token-rotation  ← Nouvelle fonctionnalité
  ├── fix/GS-008-jwt-expiration              ← Correction de bug
  ├── docs/GS-000-readme                     ← Documentation
  └── chore/GS-000-rename-ci                 ← Maintenance

RÈGLE ABSOLUE : PAS de push direct sur main !
→ Chaque modification = Pull Request
→ Minimum 1 reviewer approuvé
→ Pipeline CI doit être VERT
```

**Pourquoi pas de branche `develop` ?** Le projet StockMaster CM suit un cycle
simple : on intègre directement sur `main` via des PR. C'est délibéré — pas de
complexité inutile pour une équipe de 2-4 développeurs. (Voir `A_JIRA_ET_GIT_FLOW.md`)

### 📝 Convention de commits (Conventional Commits)

```
type(GS-XXX): description courte en minuscule

Types autorisés :
  feat     → Nouvelle fonctionnalité
  fix      → Correction de bug
  docs     → Documentation
  chore    → Maintenance (CI, config, dépendances)
  test     → Tests
  refactor → Refactoring sans changement fonctionnel
  style    → Formatage pur (pas de logique)

Exemples concrets du projet :
  feat(GS-014): ajouter rotation refresh token
  fix(GS-008): corriger expiration du JWT en millisecondes
  chore(GS-000): renommer ci.yml en ci-backend.yml
  docs(GS-012): réécrire A_JIRA_ET_GIT_FLOW.md v2.0
  test(GS-013): ajouter tests pour change-password
```

**Pourquoi cette convention ?** Parce que `git log --oneline` devient lisible.
On peut générer automatiquement un changelog. Et chaque commit est traçable
vers un ticket Jira.

### 🛡️ Protection de la branche `main`

Dans GitHub, la branche `main` est protégée :

```yaml
# Configuration GitHub (Settings → Branches → Add rule)
Requis :
  - ✅ Pull Request avant merge
  - ✅ 1 review approuvée minimum
  - ✅ Pipeline CI vert
  - ✅ Pas de conflit avec la branche de base
  - ⛔ Pas de push direct (même pour les admins)
```

### 🚀 Les commandes essentielles

```bash
# 1. Toujours partir de main à jour
git checkout main
git pull origin main

# 2. Créer une branche feature
git checkout -b feature/GS-014-refresh-token-rotation

# 3. Travailler et commiter régulièrement
git add src/mon-fichier.java
git commit -m "feat(GS-014): implementer rotation refresh token"

# 4. Pousser
git push origin feature/GS-014-refresh-token-rotation

# 5. Créer la Pull Request sur GitHub.com

# 6. Mettre à jour sa branche depuis main (en cours de route)
git fetch origin
git merge origin/main  # Résoudre conflits si besoin

# 7. Après merge de la PR, supprimer la branche locale
git checkout main && git pull
git branch -d feature/GS-014-refresh-token-rotation
```

### 🆘 Les cas d'urgence

```bash
# Annuler un commit non pushé (garder les changements)
git reset --soft HEAD~1

# Annuler un commit non pushé (perdre les changements)
git reset --hard HEAD~1

# Corriger le message du dernier commit
git commit --amend -m "feat(GS-014): nouveau message"

# Récupérer un fichier depuis main
git checkout main -- chemin/vers/fichier.java
```

---

## 3. Docker — Conteneurisation

### 🐳 C'est quoi Docker ?

Docker permet d'empaqueter une application avec **TOUT son environnement**
dans un **conteneur** standardisé.

**Sans Docker :**
```
"Ça marche sur ma machine !"
  → Sur le serveur : pas le bon JDK, pas la bonne version de PostgreSQL,
    pas les bonnes locales, lib manquante...
  → 2 jours à debugger
```

**Avec Docker :**
```
Dockerfile décrit EXACTEMENT l'environnement
  → Même conteneur tourne pareil sur PC dev, serveur staging, production
  → Zéro surprise
```

### Image vs Conteneur

```
┌─────────────────────────────────────────────┐
│  IMAGE (le moule / la recette)              │
│  stockmaster-cm:latest = 150 MB             │
│  Contient : JRE 21 + JAR + config           │
│  C'est un fichier. On le stocke, on le      │
│  partage (Docker Hub, GHCR).                │
└─────────────────────────────────────────────┘
         ↓ docker run
┌─────────────────────────────────────────────┐
│  CONTENEUR (l'instance en cours d'exécution) │
│  stockmaster-api (PID 1234, port 8080)       │
│  C'est un processus. Il tourne, consomme     │
│  de la RAM, peut être arrêté/repris.         │
└─────────────────────────────────────────────┘
```

### 📄 Dockerfile de StockMaster CM — Analyse ligne par ligne

```dockerfile
# ============================================================
# Stage 1 : Builder — JDK 21 complet (300 MB)
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
```

**Pourquoi `eclipse-temurin` ?** C'est le JDK officiel Eclipse (ex AdoptOpenJDK).
`alpine` = version légère basée sur Alpine Linux (~5 MB de base).

**Pourquoi 2 stages (multi-stage build) ?** Pour que l'image finale soit
la plus légère possible. Le JDK (300 MB) n'est nécessaire que pour COMPILER.
Pour EXÉCUTER, le JRE (80 MB) suffit.

```dockerfile
WORKDIR /app
```

Définit le répertoire de travail dans le conteneur. Toutes les commandes
suivantes s'exécutent dans `/app`.

```dockerfile
# Copie des fichiers POM d'abord (pour le cache Docker)
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY stockmaster-shared/pom.xml stockmaster-shared/
COPY stockmaster-auth/pom.xml stockmaster-auth/
# ... (11 modules)
```

**Pourquoi copier les POM d'abord ?** Docker construit une image en couches
(layers). Chaque `COPY` ou `RUN` crée une couche. Si le fichier source n'a pas
changé, Docker **réutilise la couche en cache** au lieu de tout reconstruire.

Les dépendances Maven (`pom.xml`) changent rarement. Le code source change tout le temps.
En copiant les POM d'abord, le téléchargement des dépendances est mis en cache.

```dockerfile
# Télécharger les dépendances (layer caching optimisé)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -q || true
```

**`--mount=type=cache`** : Monte un volume de cache pour Maven.
Sans ça, chaque build Docker télécharge toutes les dépendances depuis Internet.
Avec le cache, seules les nouvelles dépendances sont téléchargées.

**`dependency:go-offline`** : Télécharge TOUTES les dépendances nécessaires
sans compiler. Sépare le téléchargement de la compilation pour optimiser le cache.

```dockerfile
# Copie du code source (modules ACTIFS avec du code)
COPY stockmaster-shared/src stockmaster-shared/src
COPY stockmaster-auth/src stockmaster-auth/src
COPY stockmaster-bootstrap/src stockmaster-bootstrap/src

# Build + extraction layered JAR
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests -q && \
    java -Djarmode=layertools -jar stockmaster-bootstrap/target/*.jar \
        extract --destination extracted
```

**`jarmode=layertools`** : Découpe le JAR Spring Boot en couches :
1. `dependencies/` — Les dépendances tierces (changent rarement)
2. `spring-boot-loader/` — Le loader Spring Boot
3. `snapshot-dependencies/` — Dépendances SNAPSHOT
4. `application/` — Le code de l'application (change à chaque commit)

Grâce à ce découpage, quand le code change, seules les couches `application/`
sont reconstruites. Les dépendances sont en cache.

```dockerfile
# ============================================================
# Stage 2 : Runtime — JRE 21 Alpine (léger, 80 MB)
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# Créer un utilisateur NON-root (sécurité !)
RUN addgroup -S stockmaster && adduser -S stockmaster -G stockmaster
WORKDIR /app

# Copier les couches depuis le builder
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

# Passer à l'utilisateur non-root
USER stockmaster
```

**Pourquoi un utilisateur non-root ?** Si un attaquant trouve une faille dans
l'application et exécute du code, il aura les droits de `stockmaster`, pas de `root`.
Il ne pourra pas installer de logiciel, modifier des fichiers système, etc.
C'est une règle OWASP fondamentale.

```dockerfile
# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=30s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
```

**`HEALTHCHECK`** : Docker vérifie toutes les 30s que l'application est vivante.
Si 3 vérifications consécutives échouent, Docker redémarre le conteneur.
`start-period=30s` : Laisse 30s à l'application pour démarrer avant la première vérification.

**Pourquoi `/actuator/health` ?** C'est un endpoint Spring Boot Actuator qui
retourne `{"status":"UP"}` si tout va bien. Il vérifie aussi que PostgreSQL et Redis
sont accessibles (via les health indicators).

```dockerfile
# Flags JVM optimisés pour conteneur
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

**`-XX:+UseContainerSupport`** : Dit à la JVM qu'elle tourne DANS un conteneur.
Sans ça, la JVM voit toute la RAM de la machine hôte, pas les limites du conteneur.
Si le conteneur est limité à 512 MB, la JVM doit le savoir.

**`-XX:MaxRAMPercentage=75.0`** : La JVM utilise au maximum 75% de la RAM du conteneur.
Les 25% restants sont pour le système et les autres processus.

**`-Djava.security.egd=file:/dev/./urandom`** : Utilise `/dev/urandom` (non-bloquant)
pour la génération de nombres aléatoires, au lieu de `/dev/random` (bloquant).
JWT a besoin d'aléatoire. Avec `/dev/random`, l'app peut **bloquer** en attendant
de l'entropie. `/dev/urandom` ne bloque jamais.

### 🔧 Commandes Docker essentielles

```bash
# Voir les conteneurs en cours
docker ps

# Voir TOUS les conteneurs (même arrêtés)
docker ps -a

# Voir les images locales
docker images

# Construire l'image
docker build -t stockmaster-cm:latest .

# Lancer un conteneur
docker run -d --name stockmaster-api -p 8080:8080 stockmaster-cm:latest

# Voir les logs
docker logs -f stockmaster-api

# Entrer dans le conteneur (debug)
docker exec -it stockmaster-api sh

# Arrêter et supprimer
docker stop stockmaster-api && docker rm stockmaster-api

# Nettoyer les images inutilisées
docker image prune -f

# Nettoyer TOUT (attention)
docker system prune -a
```

### 📦 GitHub Container Registry (GHCR)

Les images Docker de StockMaster CM sont stockées sur **GHCR** (GitHub Container Registry),
intégré à GitHub :

```
ghcr.io/ulruchdev/gestion_de_stok_multifiliale:latest
ghcr.io/ulruchdev/gestion_de_stok_multifiliale:v1.0.0
ghcr.io/ulruchdev/gestion_de_stok_multifiliale:sha-a1b2c3d
```

Pour utiliser une image en local :
```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u ulruchdev --password-stdin
docker pull ghcr.io/ulruchdev/gestion_de_stok_multifiliale:latest
```

---

## 4. Docker Compose — Orchestration locale

### 🎼 C'est quoi Docker Compose ?

**Un outil pour lancer plusieurs conteneurs en même temps avec une seule commande.**

Sans Docker Compose, il faudrait lancer 5 commandes :
```bash
docker run -d --name postgres postgres:16-alpine
docker run -d --name redis redis:7-alpine
docker run -d --name minio minio/minio
docker run -d --name mailhog mailhog/mailhog
docker run -d --name api -p 8080:8080 stockmaster-cm
```

Avec Docker Compose, un seul fichier `docker-compose.yml` décrit tout :
```bash
docker compose up -d  # ← TOUT est lancé !
```

### 📄 docker-compose.yml — Analyse ligne par ligne

```yaml
services:
  # ========== API (l'application Spring Boot) ==========
  api:
    build:
      context: .            # Build depuis le Dockerfile à la racine
      dockerfile: Dockerfile
    container_name: stockmaster-api
    ports:
      - "8080:8080"         # HOST:CONTENEUR — 8080(hôte) → 8080(conteneur)
    environment:
      SPRING_PROFILES_ACTIVE: dev       # ← Profil Spring = dev
      DB_HOST: postgres                  # ← Nom du service PostgreSQL
      DB_PORT: 5432
      DB_NAME: stockmaster_dev
      DB_USERNAME: stockmaster
      DB_PASSWORD: stockmaster
      JWT_SECRET: ${JWT_SECRET:-dev-secret-...}  # ← Valeur par défaut dans .env
    depends_on:
      postgres:
        condition: service_healthy    # ← Attend que PostgreSQL soit prêt
      redis:
        condition: service_started
      minio:
        condition: service_started
    restart: unless-stopped           # ← Redémarre sauf si arrêté manuellement
    networks:
      - stockmaster-network           # ← Tous les services sur le même réseau
```

**Pourquoi `depends_on` avec `condition: service_healthy` ?** Sans ça, Docker Compose
lance l'API en même temps que PostgreSQL. L'API démarre, essaie de se connecter
à PostgreSQL, et CRASHE parce que PostgreSQL n'est pas encore prêt. Avec `service_healthy`,
Docker Compose attend que PostgreSQL réponde au `pg_isready` avant de lancer l'API.

```yaml
  # ========== PostgreSQL 16 ==========
  postgres:
    image: postgres:16-alpine
    container_name: stockmaster-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: stockmaster_dev
      POSTGRES_USER: stockmaster
      POSTGRES_PASSWORD: stockmaster
    volumes:
      - postgres-data:/var/lib/postgresql/data  # ← Persistance !
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U stockmaster -d stockmaster_dev"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - stockmaster-network
```

**Pourquoi un volume (`postgres-data`) ?** Sans volume, si le conteneur PostgreSQL
est supprimé, TOUTES les données sont perdues. Le volume **persiste** les données
même après la suppression du conteneur.

```yaml
volumes:
  postgres-data:          ← Volume nommé pour PostgreSQL
  redis-data:             ← Volume nommé pour Redis
  minio-data:             ← Volume nommé pour MinIO

networks:
  stockmaster-network:
    name: stockmaster-network
```

### Commandes Docker Compose

```bash
# Lancer TOUT l'environnement
docker compose up -d

# Voir les logs de tous les services
docker compose logs -f

# Voir les logs d'un service spécifique
docker compose logs -f api

# Arrêter sans supprimer les volumes
docker compose down

# Arrêter ET supprimer les volumes (⚠️ perte de données !)
docker compose down -v

# Reconstruire l'image et relancer
docker compose up -d --build

# Voir l'état des services
docker compose ps

# Exécuter une commande dans un service
docker compose exec api sh
```

---

## 5. CI — Intégration Continue avec GitHub Actions

### 🔄 C'est quoi un pipeline CI ?

**CI (Continuous Integration)** = À chaque push, le code est automatiquement :
1. **Compilé** → Est-ce qu'il y a des erreurs de syntaxe ?
2. **Testé** → Est-ce que les tests passent ?
3. **Analyse** → Est-ce que la qualité est bonne ?

### 📄 ci-backend.yml — Analyse ligne par ligne

```yaml
name: CI — Compilation & Tests

on:                                    # ← Déclencheurs
  push:
    branches: [main, feature/*, fix/*] # ← Sur push vers ces branches
  pull_request:
    branches: [main]                   # ← Sur PR vers main

env:
  JAVA_VERSION: '21'
  JAVA_DISTRIBUTION: 'temurin'
```

**Pourquoi `feature/*` et `fix/*` ?** Pour que le CI s'exécute dès qu'on pousse
une branche, même avant la PR. On détecte les erreurs plus tôt.

```yaml
jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest

    services:                           # ← Services DOCKER dans le CI
      postgres:                         # ← PostgreSQL 16
        image: postgres:16-alpine
        env:
          POSTGRES_DB: stockmaster_dev
          POSTGRES_USER: stockmaster
          POSTGRES_PASSWORD: stockmaster
        ports:
          - 5432:5432
        options: >-
          --health-cmd "pg_isready -U stockmaster -d stockmaster_dev"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

      redis:                            # ← Redis 7
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

      mailhog:                          # ← MailHog (emails)
        image: mailhog/mailhog:latest
        ports:
          - 1025:1025
```

**Pourquoi lancer PostgreSQL et Redis dans le CI ?** Parce que les tests
d'intégration (AuthServiceImplTest, AuthControllerTest) ont besoin d'une vraie BDD
et d'un vrai Redis. Sans ces services, les tests échouent.

**Pourquoi `--health-cmd` ?** Pour que GitHub Actions attende que PostgreSQL
soit prêt avant de lancer les tests. Sans ça, les tests s'exécutent avant
que la BDD soit initialisée.

```yaml
    steps:
      - name: Checkout code
        uses: actions/checkout@v4        # ← Clone le repo

      - name: Setup Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven                   # ← Cache les dépendances Maven

      - name: Cache Maven dependencies
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
```

**Pourquoi 2 caches différents ?** 
- `setup-java` avec `cache: maven` cache dans le répertoire standard du runner
- `actions/cache` ajoute une couche de cache avec une clé basée sur le hash des POM

Si les POM n'ont pas changé, les dépendances sont restaurées du cache.
Le cache CI est **10-20x plus rapide** que de tout télécharger depuis Maven Central.

```yaml
      - name: Compile
        run: mvn compile -q

      - name: Run tests with coverage
        run: mvn verify                  # ← mvn verify = tests + JaCoCo

      - name: Upload JaCoCo coverage report
        if: always()                     # ← Même si les tests échouent
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: '**/target/site/jacoco/'
```

**Pourquoi `mvn verify` et pas `mvn test` ?** `verify` est la phase Maven qui
exécute TOUT : compilation → tests → vérification (JaCoCo génère le rapport).
`test` s'arrête après les tests sans générer le rapport de couverture.

```yaml
      - name: Build JAR
        run: mvn package -DskipTests -q  # ← Build sans retester

      - name: Upload JAR artifact
        uses: actions/upload-artifact@v4
        with:
          name: stockmaster-jar
          path: stockmaster-bootstrap/target/*.jar
          retention-days: 7              # ← Garder 7 jours
```

**Pourquoi `artifact` ?** Les artefacts sont des fichiers produits par un job
et accessibles dans un autre job (ici : SonarCloud récupère le rapport JaCoCo).

```yaml
      - name: Integration test — Vérifier que l'app démarrer
        env:
          SPRING_PROFILES_ACTIVE: dev
          DB_HOST: localhost
          DB_PORT: 5432
          # ... (variables d'env)
        run: |
          java -jar stockmaster-bootstrap/target/*.jar > /tmp/app.log 2>&1 &
          APP_PID=$!
          for i in {1..24}; do            # ← 24 × 5s = 120s de timeout
            sleep 5
            HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
            if [ "$HTTP_CODE" = "200" ]; then
              echo "✅ Application démarrée"
              kill $APP_PID
              exit 0
            fi
          done
          echo "❌ Timeout — L'app n'a pas démarré en 120s"
          tail -100 /tmp/app.log
          exit 1
```

**Ce test est CRITIQUE :** Il vérifie que le JAR construit PEUT DÉMARRER
avec une vraie PostgreSQL et un vrai Redis. C'est un test d'intégration réel.
Si l'application ne démarre pas, le pipeline échoue — on le sait avant d'arriver
en production.

### 📄 ci-frontend.yml

```yaml
name: CI Frontend

on:
  push:
    branches: [main, 'feature/GS-*']
    paths:
      - 'frontend/**'                    # ← Seulement si le dossier frontend change
  pull_request:
    branches: [main]
    paths:
      - 'frontend/**'

defaults:
  run:
    working-directory: ./frontend        # ← Toutes les commandes dans frontend/

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: ./frontend/package-lock.json

      - name: Install dependencies
        run: npm install

      - name: Lint                      # ← ESLint
        run: npm run lint

      - name: Type check                # ← TypeScript
        run: npm run typecheck

      - name: Build                     # ← Vite build
        run: npm run build
```

**Pourquoi `paths: ['frontend/**']` ?** Pour ne pas lancer le CI frontend
quand on modifie du code backend. Si la PR ne touche que `stockmaster-auth/`,
le CI frontend ne s'exécute pas. Gain de temps et de ressources.

### Les 3 pipelines du projet

| Pipeline | Fichier | Quand ? | Quoi ? |
|----------|---------|---------|--------|
| CI Backend | `ci-backend.yml` | Push/PR sur `main`, `feature/*`, `fix/*` | Compile, tests, analyse SonarCloud, build JAR |
| CI Frontend | `ci-frontend.yml` | Push/PR touchant `frontend/**` | Lint, typecheck, build Vite |
| CD | `cd.yml` | Push sur `main` ou tag `v*` | Build image Docker, push GHCR, déploiement SSH |

---

## 6. CD — Déploiement Continu

### 🚀 C'est quoi le CD ?

**CD (Continuous Deployment)** = Dès que le code est mergé sur `main`,
il est automatiquement déployé en production (ou staging).

### 📄 cd.yml — Analyse ligne par ligne

```yaml
name: CD — Déploiement

on:
  push:
    branches: [main]                     # ← Déclenché sur push vers main
    tags:
      - 'v*'                            # ← OU sur création de tag v1.0.0, v2.1.3...

env:
  REGISTRY: ghcr.io                      # ← GitHub Container Registry
  IMAGE_NAME: ${{ github.repository }}   # ← ulruchdev/gestion_de_stok_multifiliale
```

**Pourquoi deux déclencheurs (`branches` et `tags`) ?**
- **Push sur `main`** : Déploiement automatique vers le staging
- **Tag `v*`** (ex: `v1.0.0`) : Déploiement vers la production (version officielle)

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write                    # ← Nécessaire pour push vers GHCR

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build JAR
        run: mvn package -DskipTests -q  # ← Pas de tests (déjà faits dans CI)
```

**Pourquoi `-DskipTests` dans le CD ?** Les tests ont déjà été exécutés dans
le pipeline CI. On ne va pas les re-exécuter pour le déploiement. On gagne 2-3 minutes.

```yaml
      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}  # ← Token automatique GitHub

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
        with:
          driver: docker-container       # ← Permet le build multi-architecture
```

**`secrets.GITHUB_TOKEN`** : GitHub génère automatiquement un token pour chaque
workflow. Pas besoin de créer un token manuellement. Ce token a les permissions
définies dans le job (`packages: write`).

```yaml
      - name: Extract Docker metadata    # ← Génère les tags
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=semver,pattern={{version}}       # v1.0.0, v2.1.3...
            type=semver,pattern={{major}}.{{minor}}  # v1.0, v2.1...
            type=sha,prefix=,format=short          # sha-a1b2c3d
            type=ref,event=branch                  # main, develop
            latest                                  # latest
```

**Les tags générés automatiquement :**

```
Push sur main → tags : main, latest, sha-a1b2c3d
Tag v1.0.0    → tags : 1.0.0, 1.0, latest, sha-a1b2c3d
Tag v2.1.3    → tags : 2.1.3, 2.1, latest, sha-a1b2c3d
```

```yaml
      - name: Build and push Docker image
        uses: docker/build-push-action@v6
        with:
          context: .                     # ← Contexte = racine du projet
          file: ./Dockerfile             # ← Dockerfile à utiliser
          push: true                     # ← Pousse vers le registry
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}

      - name: Deploy to staging server
        if: github.ref == 'refs/heads/main'  # ← Seulement si push vers main
        uses: appleboy/ssh-action@v1.2.0
        with:
          host: ${{ secrets.STAGING_HOST }}
          username: ${{ secrets.STAGING_USER }}
          key: ${{ secrets.STAGING_SSH_KEY }}  # ← Clé privée SSH
          script: |
            docker pull ghcr.io/${{ env.IMAGE_NAME }}:latest
            docker stop stockmaster-api || true
            docker rm stockmaster-api || true
            docker run -d \
              --name stockmaster-api \
              --restart unless-stopped \
              -p 8080:8080 \
              -e SPRING_PROFILES_ACTIVE=prod \
              -e DB_HOST=${{ secrets.DB_HOST }} \
              -e DB_NAME=${{ secrets.DB_NAME }} \
              -e DB_USERNAME=${{ secrets.DB_USERNAME }} \
              -e DB_PASSWORD=${{ secrets.DB_PASSWORD }} \
              -e REDIS_HOST=${{ secrets.REDIS_HOST }} \
              -e JWT_SECRET=${{ secrets.JWT_SECRET }} \
              ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
            docker image prune -f
```

**Le script SSH fait 4 choses :**
1. `docker pull` — Télécharge la nouvelle image
2. `docker stop/rm` — Arrête et supprime l'ancien conteneur
3. `docker run` — Lance le nouveau conteneur avec les variables d'env (secrets)
4. `docker image prune -f` — Nettoie les anciennes images

**Pourquoi `|| true` après `docker stop/rm` ?** Si le conteneur n'existe pas
(premier déploiement), la commande échoue. `|| true` fait continuer le script
malgré l'erreur.

---

## 7. PostgreSQL 16 — Base de données

### 🐘 C'est quoi PostgreSQL ?

**SGBDR** (Système de Gestion de Base de Données Relationnelle) le plus avancé
du monde open source. Stocke les données de StockMaster CM : utilisateurs, articles,
commandes, mouvements de stock...

### Pourquoi PostgreSQL 16 et pas MySQL ?

| Critère | PostgreSQL 16 | MySQL 8 |
|---------|--------------|---------|
| **ACID** | ✅ transactionnel complet | ✅ mais moins robuste |
| **Index partiels** | ✅ `WHERE supprime = FALSE` | ❌ |
| **JSONB natif** | ✅ Indexable, requêtable | ⚠️ JSON simple |
| **Full-text search** | ✅ `to_tsvector('french', ...)` | ⚠️ Moins performant |
| **Vues matérialisées** | ✅ `REFRESH MATERIALIZED VIEW` | ❌ |
| **Licence** | BSD (permissive) | GPL (restrictive) |
| **Choix StockMaster** | **✅ Gagnant** | ❌ |

### 📦 L'image Docker

```yaml
postgres:
  image: postgres:16-alpine
  ports:
    - "5432:5432"
  environment:
    POSTGRES_DB: stockmaster_dev
    POSTGRES_USER: stockmaster
    POSTGRES_PASSWORD: stockmaster
  volumes:
    - postgres-data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U stockmaster -d stockmaster_dev"]
    interval: 10s
    timeout: 5s
    retries: 5
```

**Pourquoi `alpine` ?** L'image Alpine Linux fait ~5 MB au lieu de ~150 MB.
L'image PostgreSQL complète est ~400 MB. Alpine = ~250 MB. Gain de place significatif.

**`pg_isready`** : Commande PostgreSQL qui vérifie si le serveur accepte les connexions.
Utilisé par Docker Compose et le CI pour attendre que PostgreSQL soit prêt.

### 🔌 Connexion depuis l'application (HikariCP)

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:stockmaster_dev}
    username: ${DB_USERNAME:stockmaster}
    password: ${DB_PASSWORD:stockmaster}
    hikari:
      pool-name: StockMasterPool
      maximum-pool-size: 10       # ← Max 10 connexions simultanées
      minimum-idle: 2              # ← Garder 2 connexions prêtes
      idle-timeout: 300000         # ← 5 min sans activité → fermer
      connection-timeout: 20000    # ← Timeout 20s si pas de connexion disponible
```

**HikariCP** : La pool de connexions la plus rapide pour Java. Sans pool,
l'application ouvrirait une connexion à chaque requête → lent et coûteux.
Avec pool, les connexions sont **réutilisées**.

### 🗄️ Schémas et migrations (Flyway)

Voir la formation BACKEND (section US-002) pour le détail de Flyway.

```yaml
spring:
  flyway:
    enabled: true                      # ← Actif partout
    locations: classpath:db/migration
    clean-disabled: true               # ← INTERDIT en prod
    validate-on-migrate: true          # ← Vérifie la cohérence
```

---

## 8. Redis 7 — Cache & Sessions

### ⚡ C'est quoi Redis ?

**Redis** = Remote Dictionary Server. C'est une base de données **en mémoire**
(ultra-rapide, ~0.1ms) comparée à PostgreSQL (~10ms sur disque).

### Utilisations concrètes dans StockMaster CM

| Usage | Clé Redis | TTL | Pourquoi là et pas en BDD ? |
|-------|----------|-----|---------------------------|
| **Refresh token** | `refresh:123` | 7 jours | Vérifié à CHAQUE refresh (toutes les 15 min) → rapidité |
| **Rate limiting** | `rate_limit:login:192.168.1.1` | 15 min | Incrémenté à chaque tentative → écriture rapide |
| **Blacklist JWT** | `blacklist:jti:uuid` | 15 min | Vérifié à CHAQUE requête → microsecondes |
| **Reset password** | `reset:uuid-token` | 15 min | Token temporaire, auto-supprimé au TTL |
| **Cache dashboard** | `groupe_dashboard:1` | 5 min | Données agrégées lourdes → éviter le recalcul |
| **Cache catégories** | `categories:42` | 1 heure | Changent rarement |

**Pourquoi Redis et pas une variable mémoire Java ?** Si l'application redémarre
(pour une mise à jour), les variables mémoire sont perdues. Les refresh tokens
de tous les utilisateurs sont perdus → tout le monde doit se reconnecter.
Redis **persiste sur disque** (même en mémoire, il sauvegarde régulièrement).

```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  volumes:
    - redis-data:/data              # ← Persistance des données
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

### 🔑 Commandes Redis CLI

```bash
# Se connecter à Redis
docker compose exec redis redis-cli

# Voir toutes les clés
KEYS *

# Voir une valeur spécifique
GET refresh:123

# Voir le TTL restant d'une clé
TTL refresh:123

# Supprimer une clé
DEL refresh:123

# Voir les infos du serveur
INFO server
```

---

## 9. MinIO — Stockage de fichiers S3

### 📁 C'est quoi MinIO ?

**MinIO** est un stockage de fichiers compatible **AWS S3**. Il permet de stocker
des fichiers (images, PDF) de façon scalable et standardisée.

### Pourquoi pas le disque local ?

| Critère | Disque local | MinIO (S3) |
|---------|-------------|------------|
| Scalabilité | Limité à 1 serveur | Distribution sur N serveurs |
| Backup | Sauvegarde manuelle | Réplication automatique |
| API | Pas d'API, accès fichiers | API REST standard (AWS S3) |
| Migration vers cloud | À refaire | Identique à AWS S3 |
| **Choix StockMaster** | ❌ | **✅ MinIO** |

### Buckets StockMaster

| Bucket | Contenu | Accès |
|--------|---------|-------|
| `stockmaster-logos` | Logos des entreprises | Public (lecture) |
| `stockmaster-photos` | Photos des articles et utilisateurs | Public (lecture) |
| `stockmaster-factures` | Factures PDF générées | Privé (authentifié) |

### Interface Web MinIO

MinIO fournit une interface web pour naviguer dans les fichiers :
- **URL** : http://localhost:9001 (console)
- **Login** : `stockmaster` / `stockmaster`

### Utilisation depuis Java (MinIO SDK)

```java
// Upload d'une photo d'article
@Autowired
private MinioClient minioClient;

public String uploadPhoto(MultipartFile file, Long articleId) {
    String objectName = "articles/" + articleId + "/" + file.getOriginalFilename();

    minioClient.putObject(PutObjectArgs.builder()
            .bucket("stockmaster-photos")
            .object(objectName)
            .stream(file.getInputStream(), file.getSize(), -1)
            .contentType(file.getContentType())
            .build());

    return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
            .bucket("stockmaster-photos")
            .object(objectName)
            .method(Method.GET)
            .build());
}
```

---

## 10. MailHog — Capture d'emails en dev

### 📧 C'est quoi MailHog ?

**MailHog** est un serveur SMTP **factice** pour le développement. Il intercepte
tous les emails envoyés par l'application et les affiche dans une interface web,
SANS les envoyer vraiment.

**Pourquoi c'est génial :**

Sans MailHog, pour tester "mot de passe oublié", tu devrais :
1. Configurer un vrai serveur SMTP (Gmail, SendGrid...)
2. Envoyer un vrai email à toi-même
3. Attendre la livraison
4. Vérifier ta boîte mail

Avec MailHog :
1. Tu cliques sur "Mot de passe oublié" dans l'app
2. Tu vas sur http://localhost:8025
3. L'email est là, instantanément

```yaml
mailhog:
  image: mailhog/mailhog:latest
  ports:
    - "1025:1025"    # ← Port SMTP (pour l'application)
    - "8025:8025"    # ← Interface web (pour le développeur)
```

**Interface web :** http://localhost:8025 — tu vois TOUS les emails envoyés,
avec leur contenu HTML, leurs pièces jointes, etc.

---

## 11. SonarCloud — Qualité du code

### 🔍 C'est quoi SonarCloud ?

**SonarCloud** analyse le code automatiquement et détecte :
- **Bugs** : code qui va planter (NullPointerException, etc.)
- **Vulnérabilités** : failles de sécurité (SQL injection, XSS...)
- **Code smells** : code mal écrit mais qui marche
- **Duplication** : code copié-collé
- **Couverture** : combien de lignes sont testées

### 📄 sonar-project.properties

```properties
# SonarCloud configuration
sonar.organization=ulruchdev
sonar.projectKey=gestion_de_stok_multifiliale
sonar.host.url=https://sonarcloud.io

# Sources et tests (11 modules)
sonar.sources=stockmaster-shared/src/main/java,stockmaster-auth/src/main/java,...
sonar.tests=stockmaster-shared/src/test/java,stockmaster-auth/src/test/java

# JaCoCo coverage
sonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml

# Exclusions
sonar.exclusions=**/dto/**,**/mapper/**,**/db/migration/**
sonar.coverage.exclusions=**/config/**,**/dto/**,**/db/migration/**

# Quality gate
sonar.qualitygate.wait=true        # ← Bloque le pipeline si le quality gate échoue
```

**Pourquoi exclure `**/dto/**` et `**/mapper/**` ?** Les DTOs et mappers MapStruct
sont générés automatiquement. Les mapper sont des interfaces (pas de logique).
Les DTOs sont des POJOs. Les tester ne sert à rien.

### ✅ Quality Gates (seuils de qualité)

| Seuil | Valeur | Si dépassé |
|-------|--------|-----------|
| Couverture de tests | ≥ 80% | ⛔ Pipeline rouge |
| Bugs | 0 | ⛔ Pipeline rouge |
| Vulnérabilités | 0 | ⛔ Pipeline rouge |
| Code smells | < 100 | ⚠️ Warning |
| Duplication | < 3% | ⚠️ Warning |

### Pourquoi SonarCloud et pas autre chose ?

| Outil | Prix | Cloud/Self-hosted | Intégration GitHub |
|-------|------|-------------------|-------------------|
| **SonarCloud** | **Gratuit** (public) | Cloud | ✅ Native |
| CodeClimate | Payant | Cloud | ✅ |
| Codacy | Freemium | Cloud | ✅ |
| ReviewDog | Gratuit | Les deux | ✅ |

**SonarCloud est gratuit pour les projets open source/public.**
Comme notre repo est public, on bénéficie de l'analyse gratuite.

---

## 12. JaCoCo — Couverture de tests

### 📊 C'est quoi JaCoCo ?

**JaCoCo (Java Code Coverage)** mesure combien de lignes de code sont couvertes
par les tests. Il s'intègre avec Maven et SonarCloud.

### Configuration dans le pom.xml

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <id>default-prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>  <!-- ← Agent JaCoCo dans la JVM -->
        </execution>
        <execution>
            <id>default-report</id>
            <goals><goal>report</goal></goals>          <!-- ← Génère le rapport HTML -->
        </execution>
    </executions>
</plugin>
```

**Comment JaCoCo marche :** Il ajoute un **agent** à la JVM pendant les tests.
L'agent enregistre quelles lignes de code sont exécutées par les tests.
À la fin, il génère un rapport HTML qui montre, en vert/rouge, ce qui est testé.

### Commandes

```bash
# Exécuter les tests JaCoCo pour un module
mvn verify -pl stockmaster-auth

# Le rapport est généré dans :
# stockmaster-auth/target/site/jacoco/index.html

# Voir le taux de couverture dans le terminal
mvn verify -pl stockmaster-auth | grep -i "cov\|jacoco"
```

### 📈 Métriques JaCoCo

| Métrique | Définition | Minimum StockMaster |
|----------|-----------|-------------------|
| **Instruction** | Chaque instruction Java | ≥ 80% |
| **Branch** | Chaque branche if/else | ≥ 80% |
| **Line** | Chaque ligne de code | ≥ 80% |
| **Method** | Chaque méthode | ≥ 80% |
| **Class** | Chaque classe | ≥ 80% |

---

## 13. OWASP Dependency Check — Sécurité

### 🔒 C'est quoi OWASP Dependency Check ?

Un plugin Maven qui vérifie que les dépendances du projet n'ont pas de
**vulnérabilités connues** (CVE — Common Vulnerabilities and Exposures).

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>11.1.1</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>  <!-- ← Échec si vulnérabilité ≥ 7/10 -->
        <formats>
            <format>HTML</format>
            <format>JSON</format>
        </formats>
    </configuration>
</plugin>
```

**Comment ça marche :**
1. Télécharge la base CVE (National Vulnerability Database)
2. Compare chaque dépendance du projet avec cette base
3. Génère un rapport HTML avec les vulnérabilités trouvées

**Exemple de résultat :**
```
Dépendance : io.jsonwebtoken:jjwt-api:0.9.1
CVE-2024-12345 : Score 9.8 (CRITICAL) — Signature bypass
→ Solution : Mettre à jour vers 0.12.x

Dépendance : com.fasterxml.jackson:jackson-databind:2.13.0
CVE-2024-67890 : Score 7.5 (HIGH) — DoS via JSON malformé
→ Solution : Mettre à jour vers 2.17.0+
```

**Pourquoi `failBuildOnCVSS` à 7 ?** CVSS est un score de sévérité de 0 à 10.
On ne bloque le build que pour les scores ≥ 7 (HIGH et CRITICAL).
Les scores < 7 (LOW, MEDIUM) sont documentés mais ne bloquent pas.

---

## 14. Profils Spring Boot (dev/test/prod)

### 🎭 C'est quoi un profil Spring ?

Un profil permet d'avoir des configurations DIFFÉRENTES selon l'environnement.
StockMaster CM a 3 profils :

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│     dev      │     │    test      │     │     prod     │
│  Mon PC      │     │  CI Runner   │     │  Serveur     │
├──────────────┤     ├──────────────┤     ├──────────────┤
│ ddl-auto:    │     │ ddl-auto:    │     │ ddl-auto:    │
│   validate   │     │   none       │     │   none       │
│ Flyway: ok   │     │ Flyway: ok   │     │ Flyway: ok   │
│ clean: true  │     │ clean: false │     │ clean: false │
│ Debug logs   │     │ Debug logs   │     │ Warn logs    │
│ Hikari: 10   │     │ Hikari: 5    │     │ Hikari: 20   │
│ JWT: dev     │     │ JWT: test    │     │ JWT: secret  │
└──────────────┘     └──────────────┘     └──────────────┘
```

### 📄 application-dev.yml (extrait)

```yaml
spring:
  config:
    activate:
      on-profile: dev           # ← Activé avec --spring.profiles.active=dev

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:stockmaster_dev}
    hikari:
      maximum-pool-size: 10     # ← 10 connexions en dev (suffisant)
      minimum-idle: 2

  jpa:
    hibernate:
      ddl-auto: validate        # ← Vérifie que les entités matchent le schéma

  flyway:
    clean-disabled: false       # ← AUTORISÉ en dev (pour reset rapide)

logging:
  level:
    com.stockmaster: DEBUG      # ← Logs VERBOSES en dev
```

### 📄 application-prod.yml (extrait)

```yaml
spring:
  config:
    activate:
      on-profile: prod

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}  # ← Pas de défaut !
    username: ${DB_USERNAME}                                   # ← Toutes les valeurs
    password: ${DB_PASSWORD}                                   #   Viennent des secrets
    hikari:
      maximum-pool-size: 20     # ← Plus de connexions en prod
      connection-timeout: 10000 # ← Timeout plus court
      max-lifetime: 1800000     # ← 30 min max pour une connexion

  jpa:
    hibernate:
      ddl-auto: none            # ← Flyway SEUL responsable

  flyway:
    clean-disabled: true        # ← JAMAIS flyway:clean en prod !

  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true            # ← SMTP avec AUTH en prod
          starttls:
            enable: true        # ← TLS obligatoire en prod

logging:
  level:
    root: WARN                  # ← Logs MINIMAUX en prod
    com.stockmaster: INFO
```

**Pourquoi `ddl-auto=none` en prod ?** Interdiction absolue de laisser Hibernate
modifier le schéma en prod. C'est le seul moyen d'éviter une catastrophe :
"J'ai changé un champ et Hibernate a supprimé une colonne pleine de données".

**Pourquoi `clean-disabled: true` en prod ?** `flyway:clean` DROP toutes les tables.
C'est utile en dev pour repartir de zéro. C'est un SUICIDE en prod.

---

## 15. Variables d'environnement & Secrets

### 🔐 Fichier .env (gitignoré)

```env
# STOCKMASTER CM — Configuration locale
# Copier ce fichier en .env et remplir les valeurs

# JWT — Générer avec : openssl rand -base64 32
JWT_SECRET=dev-secret-key-that-is-at-least-256-bits-long-for-hs256

# PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=stockmaster_dev
DB_USERNAME=stockmaster
DB_PASSWORD=stockmaster

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# MinIO
MINIO_ACCESS_KEY=stockmaster
MINIO_SECRET_KEY=stockmaster

# Mail
MAIL_HOST=localhost
MAIL_PORT=1025

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

**Pourquoi `.env` est gitignoré ?** Parce que le fichier `.env` peut contenir
des secrets (JWT_SECRET, mots de passe). On ne commit JAMAIS de secrets.

**Comment générer une clé JWT sécurisée ?**
```bash
# 256 bits en base64 = 44 caractères
openssl rand -base64 32
# Résultat : x7Yq8z3Pq1R5tU9vW2x4A6c8E0gH3iJ5kL7mN9oP1rS=
```

### 🔑 Secrets GitHub (Settings → Secrets and variables → Actions)

Les secrets sont injectés dans le pipeline CI/CD :

| Secret | Usage | Dans quel workflow ? |
|--------|-------|---------------------|
| `SONAR_TOKEN` | Authentification SonarCloud | `ci-backend.yml` |
| `STAGING_HOST` | IP du serveur de staging | `cd.yml` |
| `STAGING_USER` | Utilisateur SSH | `cd.yml` |
| `STAGING_SSH_KEY` | Clé privée SSH (déploiement) | `cd.yml` |
| `DB_HOST`, `DB_NAME`, ... | Connexion BDD staging | `cd.yml` |

**Règle d'or :** JAMAIS de secret en clair dans le code. Les secrets sont :
- En local : dans `.env` (gitignoré)
- En CI/CD : dans les GitHub Secrets

---

## 16. Surveillance & Monitoring (Actuator)

### 📊 Spring Boot Actuator

**Actuator** est un module Spring Boot qui expose des endpoints de surveillance :

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics   # ← Endpoints exposés
  endpoint:
    health:
      show-details: when-authorized    # ← Détails seulement pour les admin
  info:
    env:
      enabled: true
```

### Endpoints Actuator disponibles

| Endpoint | URL | Utilité |
|----------|-----|---------|
| **Health** | `GET /actuator/health` | Alive ? BDD dispo ? Redis dispo ? |
| **Info** | `GET /actuator/info` | Version, description du projet |
| **Metrics** | `GET /actuator/metrics` | RAM, CPU, threads, requêtes |
| **Metrics détail** | `GET /actuator/metrics/jvm.memory.used` | RAM utilisée par la JVM |

### Exemple de réponse Health

```json
// GET /actuator/health
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",                  // ← PostgreSQL accessible
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP"                   // ← Redis accessible
    },
    "mail": {
      "status": "UP"                   // ← Serveur SMTP accessible
    }
  }
}
```

**Pourquoi `show-details: when-authorized` ?** En production, on ne veut pas
que n'importe qui voie l'état de la BDD. Les détails sont visibles seulement
si la requête a un header `Authorization` valide.

---

## 17. Stratégie de backup & Rollback

### 💾 PostgreSQL — Backup

```bash
# Backup de la BDD
docker compose exec -T postgres pg_dump -U stockmaster stockmaster_dev > backup_20260711.sql

# Restore
cat backup_20260711.sql | docker compose exec -T postgres psql -U stockmaster stockmaster_dev
```

**Ce qui doit être backupé :**
- ✅ PostgreSQL (données métier) — Backup quotidien
- ✅ MinIO (fichiers, photos, factures) — Backup hebdomadaire
- ❌ Redis (tokens, cache) — Pas de backup (reconstruit automatiquement)
- ❌ Code source — GitHub (versionné)

### ↩️ Rollback de déploiement

```bash
# Rollback vers la version précédente
docker pull ghcr.io/ulruchdev/gestion_de_stok_multifiliale:sha-AAAAA  # ← Ancien SHA
docker stop stockmaster-api && docker rm stockmaster-api
docker run -d --name stockmaster-api ... ghcr.io/...:sha-AAAAA
```

**Stratégie de tags :**
```bash
# Toujours garder les 3 dernières versions
ghcr.io/...:latest          # Version actuelle
ghcr.io/...:v1.0.0          # Version taguée
ghcr.io/...:sha-abc123      # SHA exact (rollback possible)
```

### ⚠️ Flyway — Rollback de migration

Flyway Community NE SUPPORTE PAS le rollback automatique.
Chaque migration doit être accompagnée de son script de rollback :

```sql
-- V4_rollback_add_seuil_alerte.sql
ALTER TABLE article DROP COLUMN IF EXISTS seuil_alerte;

-- Puis en prod :
-- DELETE FROM flyway_schema_history WHERE version = '4';
-- Redéployer l'ancienne version du JAR
```

---

## 18. Glossaire & Concepts Clés

### 🐳 Docker

| Concept | Définition |
|---------|-----------|
| **Image** | Modèle (fichier) contenant l'app + son environnement |
| **Conteneur** | Instance en cours d'exécution d'une image |
| **Dockerfile** | Recette pour construire une image |
| **Layer** | Couche d'image (chaque instruction = 1 layer) |
| **Multi-stage** | Plusieurs FROM dans un Dockerfile (JDK + JRE) |
| **Volume** | Stockage persistant en dehors du conteneur |
| **Port mapping** | 8080:8080 = port hôte vers port conteneur |
| **Healthcheck** | Test périodique que l'app est vivante |
| **Registry** | Serveur qui stocke les images (Docker Hub, GHCR) |
| **Tag** | Version d'une image (latest, v1.0.0, sha-abc) |

### 🔄 CI/CD

| Concept | Définition |
|---------|-----------|
| **CI** | Intégration Continue — compiler/tester à chaque commit |
| **CD** | Déploiement Continu — déployer automatiquement après merge |
| **Workflow** | Pipeline GitHub Actions (fichier .yml) |
| **Job** | Unité de travail dans un workflow |
| **Step** | Étape d'un job (action ou commande) |
| **Action** | Composant réutilisable (checkout, setup-java) |
| **Runner** | Machine qui exécute le workflow (ubuntu-latest) |
| **Artifact** | Fichier produit par un job (rapport, JAR) |
| **Secret** | Variable d'environnement cryptée (.env, GitHub Secrets) |
| **Trigger** | Événement qui déclenche le workflow (push, PR) |

### 🛡️ Sécurité Infrastructure

| Concept | Définition | Pourquoi StockMaster l'utilise |
|---------|-----------|------------------------------|
| **Utilisateur non-root** | Conteneur pas lancé avec root | Limiter l'impact d'une faille |
| **HEALTHCHECK** | Docker vérifie l'app | Redémarrage automatique si plantage |
| **Flyway clean-disabled** | Interdire DROP TABLE en prod | Éviter la perte de données |
| **ddl-auto=none** | Pas de modif auto de schéma par Hibernate | Garder le contrôle des migrations |
| **Rate limiting** | Limiter les tentatives de login | Anti bruteforce |
| **Fail-closed** | Refuser si Redis est down | Ne pas contourner la sécurité |
| **CORS** | Limiter les origines autorisées | Empêcher les requêtes cross-site |
| **TLS** | Chiffrer les emails en prod | Confidentialité des données |

### 🗄️ Infrastructure

| Service | Version | Port | Données persistées ? |
|---------|---------|------|---------------------|
| PostgreSQL | 16 | 5432 | ✅ Volume `postgres-data` |
| Redis | 7 | 6379 | ✅ Volume `redis-data` |
| MinIO | latest | 9000 (API) + 9001 (Console) | ✅ Volume `minio-data` |
| MailHog | latest | 1025 (SMTP) + 8025 (Web) | ❌ (pas important) |

### 🔧 Commandes essentielles

```bash
# Développement
docker compose up -d                                            # Lancer l'environnement
docker compose up -d --build                                    # Rebuild + lancer
docker compose logs -f api                                      # Voir les logs
mvn compile -q                                                   # Compiler
mvn test -pl stockmaster-auth                                   # Tester
mvn verify -pl stockmaster-auth                                 # Tester + coverage
curl http://localhost:8080/actuator/health                       # Vérifier l'app

# CI/CD
git checkout main && git pull                                    # Mettre à jour
git checkout -b feature/GS-XXX-name                              # Nouvelle branche
git add . && git commit -m "feat(GS-XXX): description"           # Commit
git push origin feature/GS-XXX-name                              # Push → CI déclenché
# → Aller sur GitHub.com → Créer PR → Attendre CI vert → Merger

# Production
docker pull ghcr.io/ulruchdev/...:latest                        # Télécharger nouvelle image
docker stop stockmaster-api && docker rm stockmaster-api         # Arrêter ancien
docker run -d --name stockmaster-api ... ghcr.io/...:latest      # Lancer nouveau
docker logs -f stockmaster-api                                   # Vérifier démarrage

# Maintenance
docker compose exec postgres pg_dump -U stockmaster stockmaster_dev > backup.sql
docker image prune -f                                             # Nettoyer
docker compose down -v                                            # ⚠️ Arrêt + perte données
```

---

> **Cette formation couvre l'ensemble du pipeline DevOps de StockMaster CM.**
>
> 📖 **Sources :** Dockerfile, docker-compose.yml, ci-backend.yml, cd.yml,
> ci-frontend.yml, sonar-project.properties, application.yml (3 profils),
> pom.xml, knowledge.md
>
> 🔄 **À retenir absolument :**
> - JAMAIS de push direct sur `main` → toujours une PR
> - JAMAIS de `ddl-auto=update` en prod → Flyway est le seul responsable
> - JAMAIS de secrets dans le code → `.env` ou GitHub Secrets
> - Toujours faire `mvn test -pl stockmaster-auth` avant chaque push
> - La pipeline CI doit être VERTE avant de merger une PR
> - Les backups BDD sont automatiques, les logs sont persistés, le monitoring Actuator est actif
