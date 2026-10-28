# StockMaster CM — Tiers (Clients & Fournisseurs)

> **Artefact :** `stockmaster-tiers`
> **Statut :** 🟡 EPIC 6 en cours — fondations + **US-036 livrée** (`POST /api/v1/fournisseurs`) + **US-037 livrée** (`GET /api/v1/fournisseurs`) + **US-039 livrée** (`DELETE /api/v1/fournisseurs/{id}`)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.tiers`

---

## Rôle

Gestion des partenaires commerciaux : clients B2B et fournisseurs.

### Fonctionnalités prévues

- ✅ CRUD complet des fournisseurs (raison sociale, NIF, contact, adresse)
- ✅ CRUD complet des clients B2B (nom, téléphone, email, adresse)
- ✅ Recherche par nom, téléphone, ville
- ✅ Isolation tenant stricte

---

## US associées (EPIC 6)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-036** | Créer un fournisseur | P0 | ✅ Livré (branche `feature/GS-036-creer-fournisseur`) |
| **US-037** | Lister / Rechercher les fournisseurs | P0 | ✅ Livré (branche `feature/GS-037-lister-fournisseurs`) |
| **US-038** | Modifier un fournisseur | P0 | 🔜 Non commencé |
| **US-039** | Supprimer (archiver) un fournisseur | P0 | ✅ Livré (branche `feature/GS-039-supprimer-fournisseur`) |
| **US-040** | Créer un client | P0 | 🔜 Non commencé |
| **US-041** | Lister / Rechercher les clients | P0 | 🔜 Non commencé |
| **US-042** | Modifier un client | P0 | 🔜 Non commencé |
| **US-043** | Supprimer un client | P0 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| POST | `/api/v1/fournisseurs` | US-036 |
| GET | `/api/v1/fournisseurs` | US-037 |
| PUT | `/api/v1/fournisseurs/{id}` | US-038 |
| DELETE | `/api/v1/fournisseurs/{id}` | US-039 ✅ |
| POST | `/api/v1/clients` | US-040 |
| GET | `/api/v1/clients` | US-041 |
| PUT | `/api/v1/clients/{id}` | US-042 |
| DELETE | `/api/v1/clients/{id}` | US-043 |

---

## Tables BDD associées

- `fournisseur` — Raison sociale, NIF, contact, téléphone, email, ville
- `client` — Nom, prénom, téléphone, email, ville, quartier

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- → `stockmaster-achat` **uniquement via le port** `tiers.port.CommandesFournisseurPort` (US-039) : la dépendance est inverse (`achat` implémente le contrat exposé par `tiers`), aucun import `achat` dans ce module — règle ArchUnit n°1.
- **Est utilisé par :** stockmaster-achat (commande fournisseur), stockmaster-vente (commande client)
