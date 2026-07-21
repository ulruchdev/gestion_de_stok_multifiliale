# StockMaster CM — Reporting (Statistiques & Export)

> **Artefact :** `stockmaster-reporting`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.reporting`

---

## Rôle

Indicateurs de performance et exports pour le pilotage de l'activité.

### Fonctionnalités prévues

- ✅ Top articles les plus vendus (par quantité ou CA)
- ✅ Clients les plus actifs (par CA ou fréquence)
- ✅ Évolution du chiffre d'affaires (journalier / mensuel)
- ✅ Alertes de rupture imminente (stock < N jours de vente)
- ✅ Export CSV des mouvements de stock

---

## US associées (EPIC 13)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-076** | Top articles les plus vendus | P1 | 🔜 Non commencé |
| **US-077** | Clients les plus actifs | P1 | 🔜 Non commencé |
| **US-078** | Évolution du chiffre d'affaires | P1 | 🔜 Non commencé |
| **US-079** | Alertes de rupture imminente | P1 | 🔜 Non commencé |
| **US-080** | Export CSV des mouvements de stock | P2 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| GET | `/api/v1/reporting/top-articles` | US-076 |
| GET | `/api/v1/reporting/top-clients` | US-077 |
| GET | `/api/v1/reporting/ca` | US-078 |
| GET | `/api/v1/reporting/ruptures-imminentes` | US-079 |
| GET | `/api/v1/reporting/export/mouvements` | US-080 |

---

## Cache Redis

Les rapports sont mis en cache pour éviter des calculs coûteux en lecture :

| Cache | TTL | US |
|---|---|---|
| `top_articles` | 10 min | US-076 |
| `ca_evolution` | 10 min | US-078 |

---

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- → `stockmaster-stock` (mouvements de stock)
- → `stockmaster-vente` (données de ventes)
- → `stockmaster-tiers` (clients)
- → `stockmaster-catalogue` (articles)
