# StockMaster CM — Groupe (Groupe & Filiales)

> **Artefact :** `stockmaster-groupe`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.groupe`

---

## Rôle

Gestion des groupes multi-sites et de leurs filiales.

### Fonctionnalités prévues

- ✅ Modifier les informations du groupe (logo, NIF, coordonnées)
- ✅ Consulter les infos du groupe + plan d'abonnement
- ✅ Créer, lister, modifier, activer/désactiver des filiales
- ✅ Dashboard consolidé du groupe (stock total, CA, alertes)

---

## US associées (EPIC 3)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-014** | Modifier les informations du groupe | P0 | 🔜 Non commencé |
| **US-015** | Consulter les informations du groupe | P0 | 🔜 Non commencé |
| **US-016** | Créer une filiale | P0 | 🔜 Non commencé |
| **US-017** | Lister les filiales du groupe | P0 | 🔜 Non commencé |
| **US-018** | Modifier une filiale | P0 | 🔜 Non commencé |
| **US-019** | Activer / Désactiver une filiale | P1 | 🔜 Non commencé |
| **US-020** | Dashboard consolidé groupe | P0 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| PUT | `/api/v1/groupe` | US-014 |
| GET | `/api/v1/groupe` | US-015 |
| POST | `/api/v1/groupe/filiales` | US-016 |
| GET | `/api/v1/groupe/filiales` | US-017 |
| PUT | `/api/v1/groupe/filiales/{id}` | US-018 |
| PATCH | `/api/v1/groupe/filiales/{id}/statut` | US-019 |
| GET | `/api/v1/groupe/dashboard` | US-020 |

---

## Tables BDD associées

- `tenant_group` — Groupe (plan, limite filiales)
- `entreprise` — Filiales avec `parent_id` → maison mère

## Dépendances inter-modules

- → `stockmaster-shared` (AbstractEntity, ApiResponse, exceptions)
- → `stockmaster-stock` (pour le dashboard : stock consolidé)
- → `stockmaster-vente` (pour le dashboard : CA)
