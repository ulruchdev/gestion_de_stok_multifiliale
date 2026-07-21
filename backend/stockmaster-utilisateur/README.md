# StockMaster CM — Utilisateur (Gestion des Utilisateurs)

> **Artefact :** `stockmaster-utilisateur`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.utilisateur`

---

## Rôle

Gestion des comptes utilisateurs et de leurs rôles métier.

### Fonctionnalités prévues

- ✅ Créer un Admin Filiale (par Admin Groupe)
- ✅ Créer un employé avec rôle métier
- ✅ Lister, modifier, désactiver des utilisateurs
- ✅ Consulter et modifier son propre profil

---

## US associées (EPIC 4)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-021** | Créer un Admin Filiale | P0 | 🔜 Non commencé |
| **US-022** | Créer un employé | P0 | 🔜 Non commencé |
| **US-023** | Lister les utilisateurs | P0 | 🔜 Non commencé |
| **US-024** | Modifier un utilisateur | P0 | 🔜 Non commencé |
| **US-025** | Désactiver un utilisateur | P0 | 🔜 Non commencé |
| **US-026** | Consulter et modifier son profil | P1 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| POST | `/api/v1/utilisateurs/admin-filiale` | US-021 |
| POST | `/api/v1/utilisateurs/employes` | US-022 |
| GET | `/api/v1/utilisateurs` | US-023 |
| PUT | `/api/v1/utilisateurs/{id}` | US-024 |
| PATCH | `/api/v1/utilisateurs/{id}/statut` | US-025 |
| GET | `/api/v1/utilisateurs/profil` | US-026 |
| PUT | `/api/v1/utilisateurs/profil` | US-026 |

---

## Rôles métier

| Rôle | Scope | Créé par |
|---|---|---|
| `SUPER_ADMIN` | GLOBAL | Système (hors scope StockMaster CM) |
| `ADMIN_GROUPE` | GROUPE | Inscription |
| `ADMIN_FILIALE` | FILIALE | Admin Groupe |
| `GESTIONNAIRE_STOCK` | FILIALE | Admin Filiale |
| `RESP_ACHATS` | FILIALE | Admin Filiale |
| `COMMERCIAL` | FILIALE | Admin Filiale |
| `CAISSIER` | FILIALE | Admin Filiale |

---

## Tables BDD associées

- `utilisateur` — Comptes avec rôle, email, mot de passe BCrypt

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- → `stockmaster-groupe` (vérification d'appartenance à une filiale)
