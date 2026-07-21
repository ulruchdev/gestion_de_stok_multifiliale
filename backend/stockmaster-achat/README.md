# StockMaster CM — Achat (Commandes Fournisseur)

> **Artefact :** `stockmaster-achat`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.achat`

---

## Rôle

Cycle complet d'approvisionnement : de la création de la commande fournisseur à la réception des marchandises, avec génération automatique des entrées de stock.

### Fonctionnalités prévues

- ✅ Créer une commande fournisseur (articles, quantités, prix)
- ✅ Lister et consulter les commandes
- ✅ Modifier une commande en cours de saisie
- ✅ Valider une commande → mouvements ENTREE + mise à jour stock
- ✅ Marquer comme livrée (état final)
- ✅ Supprimer une commande non validée
- ✅ Code commande auto-généré : `CF-{ANNEE}-{SEQUENCE}`

---

## US associées (EPIC 7)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-044** | Créer une commande fournisseur | P0 | 🔜 Non commencé |
| **US-045** | Lister les commandes fournisseur | P0 | 🔜 Non commencé |
| **US-046** | Consulter le détail d'une commande | P0 | 🔜 Non commencé |
| **US-047** | Modifier commande EN_PREPARATION | P0 | 🔜 Non commencé |
| **US-048** | **Valider commande** ⭐ (mouvements ENTREE) | P0 | 🔜 Non commencé |
| **US-049** | Marquer commande comme livrée | P0 | 🔜 Non commencé |
| **US-050** | Supprimer commande EN_PREPARATION | P0 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| POST | `/api/v1/commandes-fournisseur` | US-044 |
| GET | `/api/v1/commandes-fournisseur` | US-045 |
| GET | `/api/v1/commandes-fournisseur/{id}` | US-046 |
| PUT | `/api/v1/commandes-fournisseur/{id}` | US-047 |
| POST | `/api/v1/commandes-fournisseur/{id}/valider` | US-048 ⭐ |
| POST | `/api/v1/commandes-fournisseur/{id}/livrer` | US-049 |
| DELETE | `/api/v1/commandes-fournisseur/{id}` | US-050 |

---

## Règles métier clés

- États : `EN_PREPARATION` → `VALIDEE` → `LIVREE` (immuable)
- Validation atomique : 1 mouvement `ENTREE` par ligne + rollback si échec
- Code commande auto-généré : `CF-{ANNEE}-{SEQUENCE}`
- Snapshot TVA figé à la saisie (indépendant des modifs futures)

---

## Tables BDD associées

- `commande_fournisseur` — En-tête (fournisseur, état, code)
- `ligne_commande_fournisseur` — Lignes (article, quantité, prix, TVA snapshot)

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- → `stockmaster-catalogue` (articles)
- → `stockmaster-tiers` (fournisseurs)
- → `stockmaster-stock` (création mouvements ENTREE à la validation)
