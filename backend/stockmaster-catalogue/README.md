# StockMaster CM — Catalogue (Catégories & Articles)

> **Artefact :** `stockmaster-catalogue`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.catalogue`

---

## Rôle

Gestion du catalogue produits : catégories et articles qui servent de référentiel à toutes les opérations (stock, achats, ventes).

### Fonctionnalités prévues

- ✅ CRUD complet des catégories (code, désignation, taux TVA)
- ✅ CRUD complet des articles (code, prix, TVA, seuil alerte, photo)
- ✅ Calcul automatique du `prix_vente_ttc`
- ✅ Recherche full-text sur les articles
- ✅ Cache Redis pour les listes

---

## US associées (EPIC 5)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-027** | Créer une catégorie | P0 | 🔜 Non commencé |
| **US-028** | Lister les catégories | P0 | 🔜 Non commencé |
| **US-029** | Modifier une catégorie | P0 | 🔜 Non commencé |
| **US-030** | Supprimer une catégorie | P0 | 🔜 Non commencé |
| **US-031** | Créer un article | P0 | 🔜 Non commencé |
| **US-032** | Lister les articles | P0 | 🔜 Non commencé |
| **US-033** | Consulter un article | P0 | 🔜 Non commencé |
| **US-034** | Modifier un article | P0 | 🔜 Non commencé |
| **US-035** | Supprimer (archiver) un article | P0 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| POST | `/api/v1/categories` | US-027 |
| GET | `/api/v1/categories` | US-028 |
| PUT | `/api/v1/categories/{id}` | US-029 |
| DELETE | `/api/v1/categories/{id}` | US-030 |
| POST | `/api/v1/articles` | US-031 |
| GET | `/api/v1/articles` | US-032 |
| GET | `/api/v1/articles/{id}` | US-033 |
| PUT | `/api/v1/articles/{id}` | US-034 |
| DELETE | `/api/v1/articles/{id}` | US-035 |

---

## Règles métier clés

- `prix_vente_ttc` = `prix_vente_ht × (1 + taux_tva / 100)` — jamais saisi manuellement
- Taux TVA hérité de la catégorie, surchargeable
- Code article unique par entreprise
- Soft delete uniquement (jamais de suppression physique)

---

## Tables BDD associées

- `categorie` — Code, désignation, taux TVA
- `article` — Code, prix HT/TTC, seuil alerte, photo

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- **Est utilisé par :** stockmaster-achat, stockmaster-vente, stockmaster-stock, stockmaster-tiers
