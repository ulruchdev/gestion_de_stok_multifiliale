# StockMaster CM — Gestion de Stock Multi-Sites 🇨🇲

> **Solution SaaS de gestion de stock pour PME camerounaises** — Épiceries, pharmacies, quincailleries, distributeurs multi-sites.
> **Architecture :** Monolithe modulaire (11 modules) — Java 21, Spring Boot 3.3.5, PostgreSQL 16, Redis 7, MinIO.

---

## 📋 Table des matières

- [Aperçu du projet](#aperçu-du-projet)
- [Stack technique](#stack-technique)
- [Modules](#modules)
- [Prérequis](#prérequis)
- [Démarrage rapide](#démarrage-rapide)
- [Documentation](#documentation)
- [Structure du projet](#structure-du-projet)

---

## Aperçu du projet

StockMaster CM permet à un commerçant ou à un groupe de commerces de :

- ✅ Gérer son **catalogue** (catégories, articles, prix, TVA)
- ✅ Suivre son **stock en temps réel** avec alertes de rupture
- ✅ Gérer ses **achats** (commandes fournisseur → entrées stock)
- ✅ Gérer ses **ventes** B2B et **ventes directes** (caisse)
- ✅ Transférer du stock entre **filiales** d'un même groupe
- ✅ Générer des **factures PDF** et des **rapports** d'activité
- ✅ Contrôler les **accès** par rôles (RBAC : Admin Groupe, Admin Filiale, Gestionnaire Stock, Commercial, Caissier, etc.)
- ✅ Recevoir des **alertes** de stock minimum

### Marché cible

| Type de commerce | Exemple |
|---|---|
| Boutique unique | Épicerie, Pharmacie, Quincaillerie |
| Groupe multi-sites | Chaîne de distribution, Grossiste régional |
| Succursales | Entrepôt + points de vente |

---

## Stack technique

| Couche | Technologie | Version |
|---|---|---|
| Langage | Java | 21 (LTS) |
| Framework | Spring Boot | 3.3.5 |
| Base de données | PostgreSQL | 16 |
| Cache & Rate limiting | Redis | 7 |
| Stockage fichiers | MinIO (S3-compatible) | Latest |
| Mapping | MapStruct | 1.6.3 |
| JWT | jjwt | 0.12.6 |
| Migrations BDD | Flyway | Intégré |
| Qualité | SonarCloud / JaCoCo / OWASP | — |
| Conteneurisation | Docker / Docker Compose | — |
| CI/CD | GitHub Actions | — |

---

## Modules

| # | Module | Artefact | Statut | Description |
|---|---|---|---|---|
| 1 | **Shared** | `stockmaster-shared` | ✅ Actif | Infrastructure commune : entités abstraites, exceptions, config, handler erreurs, agrégats tenant, MinIO, Flyway |
| 2 | **Auth** | `stockmaster-auth` | ✅ Actif | Authentification JWT, inscription, login, rotation refresh, rate limiting |
| 3 | **Groupe** | `stockmaster-groupe` | ✅ Actif | Groupe & filiales (US-014 à US-019) — contrôleurs + services |
| 4 | **Utilisateur** | `stockmaster-utilisateur` | ✅ Actif | Gestion des utilisateurs & rôles (US-021 à US-026) |
| 5 | **Catalogue** | `stockmaster-catalogue` | 🟡 Fondations | Entités + repositories — **services/contrôleurs à venir (EPIC 5)** |
| 6 | **Tiers** | `stockmaster-tiers` | 🟡 Fondations | Entités + repositories — EPIC 6 à implémenter |
| 7 | **Achat** | `stockmaster-achat` | 🟡 Fondations | Entités + enums d'état — EPIC 7 à implémenter |
| 8 | **Stock** | `stockmaster-stock` | 🟡 Fondations | Entités + `CalculStockService` — EPIC 8/11/15/17 à implémenter |
| 9 | **Vente** | `stockmaster-vente` | 🟡 Fondations | Entités (vente, caisse, paiement) — EPIC 9/10/14 à implémenter |
| 10 | **Notification** | `stockmaster-notification` | 🟡 Partiel | Port `CanalNotification` + `EmailNotificationService` (DEC-014) — EPIC 12 à finir |
| 11 | **Reporting** | `stockmaster-reporting` | 🟡 Fondations | `CalculCaService` (DEC-020) — EPIC 13 à implémenter |
| 12 | **Bootstrap** | `stockmaster-bootstrap` | ✅ Actif | Point d'entrée `@SpringBootApplication`, `application.yml`, tests d'intégration + ArchUnit |

> **Légende :** ✅ services + contrôleurs livrés | 🟡 fondations posées (entités, repositories, enums, services métier purs + tests), couche applicative à venir | 🔜 stub vide
>
> **Note :** les « fondations » des 7 modules ont été livrées le 14/09/2026 en dehors de toute US — voir `document/03-pilotage/progress-ledger.md`.

---

## Prérequis

- **Java 21+** (OpenJDK ou Eclipse Temurin)
- **Maven 3.9+**
- **Docker Desktop** + Docker Compose V2 (pour PostgreSQL, Redis, MinIO, MailHog)
- **Git**

---

## Démarrage rapide

```bash
# 1. Cloner le projet
git clone <repo-url>
cd gestion_de_stok_multifiliale

# 2. Lancer les services (PostgreSQL + Redis + MinIO + MailHog)
docker compose up -d

# 3. Copier et personnaliser les variables d'environnement
cp .env.example .env
# Éditer .env : générer un JWT_SECRET avec `openssl rand -base64 32`
# (le défaut versionné est une clé de DÉVELOPPEMENT, jamais utiliser en prod)

# 4. Compiler (backend)
#    ⚠️ mvnw n'a pas le bit d'exécution → chmod +x, ou passer par `sh mvnw`
chmod +x backend/mvnw
cd backend && ./mvnw compile -q

# 5. Lancer l'application (profil dev)
#    ⚠️ c'est BOOTSTRAP qui porte le main(), pas shared (shared est une library)
./mvnw spring-boot:run -pl stockmaster-bootstrap

# 6. Vérifier
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
# Swagger : http://localhost:8080/swagger-ui.html

# 7. Frontend (dans un autre terminal)
cd ../frontend && npm install && npm run dev
```

### Tests

```bash
# ⚠️ -pl SANS -am échoue (dépendances inter-modules non résolues)
./mvnw test                      # réactif complet : 421 tests
./mvnw test -pl stockmaster-auth -am

# Sans PostgreSQL, 3 tests d'intégration (StockMasterApplicationTest) échouent :
# c'est environnemental. `docker compose up -d postgres redis` les rend verts.
```

---

## Documentation

| Document | Contenu |
|---|---|
| [`knowledge.md`](knowledge.md) | **Source de vérité unique** — règles, stack, conventions, protocole anti-hallucination |
| [`document/referentiel/00-INDEX.md`](document/referentiel/00-INDEX.md) | **Norme du produit** `GS-REF-2026-01` — 14 parties + journal des décisions `DEC-nnn` |
| [`document/03-pilotage/progress-ledger.md`](document/03-pilotage/progress-ledger.md) | **Journal unique d'avancement des US** (remplace `implementation.md`, supprimé le 7 sept. 2026) |
| [`document/03-pilotage/A_JIRA_ET_GIT_FLOW.md`](document/03-pilotage/A_JIRA_ET_GIT_FLOW.md) | Workflow par US (11 étapes), conventions git/commit, DoD |
| [`document/03-pilotage/strategie_test.md`](document/03-pilotage/strategie_test.md) | Stratégie de test — commandes pré-push, Swagger, pipeline CI |
| [`document/02-backlogs/BACKLOG_StockMaster_CM.md`](document/02-backlogs/BACKLOG_StockMaster_CM.md) | Backlog backend (**108 US / 354 SP**, v1.2) |
| [`document/02-backlogs/GS-FRONTEND-BACKLOG-2026-01.md`](document/02-backlogs/GS-FRONTEND-BACKLOG-2026-01.md) | Backlog frontend (246 SP) |
| [`document/02-backlogs/GS-DESIGN-BACKLOG-2026-01.md`](document/02-backlogs/GS-DESIGN-BACKLOG-2026-01.md) | Backlog design (160 SP) |
| [`document/test_postman.md`](document/test_postman.md) | Contrat API vivant (porte G4) + collection `postman_collection.json` |
| [`document/guideconfiguration.md`](document/guideconfiguration.md) | Guide de toutes les configurations (internes & externes) |
| [`document/01-architecture/CDCT_StockMaster_CM_Complet_Sections22-30.md`](document/01-architecture/CDCT_StockMaster_CM_Complet_Sections22-30.md) | Cahier des Charges Techniques — *⚠️ snippets §22.4/§28/§29 partiellement périmés, voir audit* |
| [`document/00-fonctionnel/GS-CDA-2026-02_addendum_decisions_validees.md`](document/00-fonctionnel/GS-CDA-2026-02_addendum_decisions_validees.md) | Addendum de décisions métier (analyse fonctionnelle = `.docx`) |
| Modules | Voir chaque `stockmaster-*/README.md` |

> **Ordre de lecture obligatoire avant toute contribution :** `knowledge.md` → `A_JIRA_ET_GIT_FLOW.md` → `referentiel/00-INDEX.md` → `referentiel/12-journal-decisions.md` → backlog → `progress-ledger.md`.
>
> Les 3 audits de `document/03-pilotage/` (`GS-AUDIT-*`, `audit-analyse-*`, `.docx`) sont **📦 gelés** : pièces justificatives, jamais une norme.

---

## Structure du projet

```
gestionulrich/
│
├── backend/                    ← Backend Spring Boot (11 modules)
│   ├── pom.xml                 ← Parent POM (Spring Boot 3.3.5)
│   ├── Dockerfile              ← Multi-stage container
│   ├── mvnw + .mvn/            ← Maven wrapper
│   ├── stockmaster-shared/     ← Infrastructure commune
│   ├── stockmaster-auth/       ← Authentification JWT
│   ├── stockmaster-bootstrap/  ← Point d'entrée Spring Boot
│   ├── stockmaster-achat/      ← (stub)
│   ├── stockmaster-catalogue/  ← (stub)
│   ├── stockmaster-groupe/     ← (stub)
│   ├── stockmaster-notification/ ← (stub)
│   ├── stockmaster-reporting/  ← (stub)
│   ├── stockmaster-stock/      ← (stub)
│   ├── stockmaster-tiers/      ← (stub)
│   ├── stockmaster-utilisateur/ ← (stub)
│   └── stockmaster-vente/      ← (stub)
│
├── frontend/                   ← React/TypeScript (Vite)
├── document/                   ← Spécifications & guides
├── formation/                  ← 4 formations équipe (frontend, backend, devops, design)
├── design/                     ← Design system propriétaire (README + sources)
├── docker-compose.yml          ← Dev environment
├── sonar-project.properties    ← SonarCloud config
├── README.md                   ← Ce fichier
├── .gitignore
└── .env.example
```

---

## État d'avancement

> **Instantané vérifié par exécution le 9 octobre 2026** — voir `document/03-pilotage/progress-ledger.md` pour le détail par US.

| Métrique | Valeur |
|---|---|
| User Stories backend terminées | **29 sur 108** (EPIC 1 complète, EPIC 2 sauf US-084/086, EPIC 3 sauf US-020/014b/081, EPIC 4 complète) |
| Périmètre livré (backend + frontend + design) | **≈ 181 SP sur 760 → ≈ 24 %** |
| Modules avec services + contrôleurs | **4 sur 12** (shared, auth, groupe, utilisateur) + 7 modules en fondations |
| Commits (toutes branches) | **184** — 33 merges de PR |
| Lignes de code | ~16 600 Java · ~5 100 TS/TSX |
| Tests backend | **421 exécutions** (386 méthodes `@Test`), 418 verts sans base, 421 avec PostgreSQL |
| Tests frontend | **0** (aucun script `test` dans `package.json`) |
| Pipeline CI | ✅ `ci-backend.yml` (build + tests + SonarCloud + JAR + healthcheck) · ✅ `ci-frontend.yml` (lint + typecheck + build) · ✅ `cd.yml` (GHCR) |
| Règles ArchUnit | ✅ 4/4 vertes |
| Migrations Flyway | V1 → V6 (`V5__schema_decisionnel` exécuté et validé) |
| Décisions de gouvernance | **41 `DEC-nnn`, 0 ouverte** (`referentiel/12-journal-decisions.md`) |

> **Reste à faire :** EPICs 5 à 17 côté backend (catalogue, tiers, achats, stock, ventes, caisse, transferts, inventaire, import CSV, reporting), EPICs F02→F10 côté frontend, EPICs D01→D10 côté design.
>
> **Estimation de périmètre :** ≈ 16 sprints de backlog, **≈ 24,5 sprints** une fois comptés les coûts non exprimés en US (V5, stabilisation `DEC-031`, dette de test) → **~15-16 mois à 3 personnes** (`referentiel/13-perimetre-planning.md §13.3`).

---

## Licence

Projet privé — StockMaster CM © 2026
