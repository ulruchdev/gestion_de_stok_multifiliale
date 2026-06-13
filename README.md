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
| 1 | **Shared** | `stockmaster-shared` | ✅ Actif | Infrastructure commune : entités abstraites, exceptions, config, handler erreurs, Flyway |
| 2 | **Auth** | `stockmaster-auth` | ✅ Actif | Authentification JWT, inscription, login, rate limiting |
| 3 | **Groupe** | `stockmaster-groupe` | 🔜 Stub | Groupe & Filiales multi-sites |
| 4 | **Utilisateur** | `stockmaster-utilisateur` | 🔜 Stub | Gestion des utilisateurs & rôles |
| 5 | **Catalogue** | `stockmaster-catalogue` | 🔜 Stub | Catégories & Articles |
| 6 | **Tiers** | `stockmaster-tiers` | 🔜 Stub | Clients & Fournisseurs |
| 7 | **Achat** | `stockmaster-achat` | 🔜 Stub | Commandes fournisseur |
| 8 | **Stock** | `stockmaster-stock` | 🔜 Stub | Mouvements de stock, transferts |
| 9 | **Vente** | `stockmaster-vente` | 🔜 Stub | Ventes B2B & vente directe (caisse) |
| 10 | **Notification** | `stockmaster-notification` | 🔜 Stub | Alertes & notifications |
| 11 | **Reporting** | `stockmaster-reporting` | 🔜 Stub | Statistiques & export CSV |

> **Légende :** ✅ Actif = code existant | 🔜 Stub = module créé mais vide (POM uniquement)

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
cd gestionulrich

# 2. Lancer les services (PostgreSQL + Redis + MinIO + MailHog)
docker compose up -d

# 3. Copier et personnaliser les variables d'environnement
cp .env.example .env
# Éditer .env : générer un JWT_SECRET avec `openssl rand -base64 32`

# 4. Compiler
mvn compile -q

# 5. Lancer l'application (profil dev)
mvn spring-boot:run -pl stockmaster-shared

# 6. Vérifier
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

---

## Documentation

| Document | Contenu |
|---|---|
| [`document/implementation.md`](document/implementation.md) | Journal d'implémentation avec traçabilité git complète |
| [`document/guideconfiguration.md`](document/guideconfiguration.md) | Guide de toutes les configurations (internes & externes) |
| [`document/BACKLOG_StockMaster_CM.md`](document/BACKLOG_StockMaster_CM.md) | Backlog produit complet (75 US, 267 story points) |
| [`document/CDCT_StockMaster_CM_Complet_Sections22-30.md`](document/CDCT_StockMaster_CM_Complet_Sections22-30.md) | Cahier des Charges Techniques |
| [`document/A_JIRA_ET_GIT_FLOW.md`](document/A_JIRA_ET_GIT_FLOW.md) | Workflow Jira & Git Flow |
| [`document/analyse_fonctionnelle_stockmaster_cm (1).md`](document/analyse_fonctionnelle_stockmaster_cm%20%281%29.md) | Analyse fonctionnelle |
| Modules | Voir chaque `stockmaster-*/README.md` |

---

## Structure du projet

```
gestionulrich/
├── pom.xml                     ← Parent POM (Spring Boot 3.3.5, 11 modules)
├── README.md                   ← Ce fichier
├── .gitignore
├── Dockerfile                  ← Multi-stage container
├── docker-compose.yml          ← Dev environment
├── sonar-project.properties    ← SonarCloud config
├── .env.example                ← Variables d'environnement
├── .github/workflows/
│   ├── ci.yml                  ← CI pipeline
│   └── cd.yml                  ← CD pipeline
├── document/                   ← Spécifications & guides
└── stockmaster-{module}/
    ├── pom.xml
    ├── README.md               ← README du module
    └── src/
        ├── main/java/com/stockmaster/{module}/
        │   ├── controller/     ← API REST
        │   ├── service/        ← Interfaces + implémentations
        │   ├── repository/     ← Accès données (Spring Data JPA)
        │   ├── domain/         ← Entités JPA + énumérations
        │   ├── dto/            ← Request/Response
        │   ├── mapper/         ← MapStruct
        │   └── event/          ← Événements Spring
        └── test/java/
```

---

## État d'avancement

| Métrique | Valeur |
|---|---|
| User Stories terminées | 7 sur 75 |
| Modules avec code | 2 sur 11 |
| Commits | 12 |
| Lignes de code | ~8 500 |
| Tests unitaires | 18 |

> Voir [`document/implementation.md`](document/implementation.md) pour le détail complet par US.

---

## Licence

Projet privé — StockMaster CM © 2026
