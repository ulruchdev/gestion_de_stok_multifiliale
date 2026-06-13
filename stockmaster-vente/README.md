# StockMaster CM — Vente (Commandes Client & Vente Directe)

> **Artefact :** `stockmaster-vente`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.vente`

---

## Rôle

Gestion des ventes B2B (commandes client) et des ventes comptoir (caisse).

### Fonctionnalités prévues

- ✅ Créer, lister, consulter des commandes client
- ✅ Valider une commande → vérification stock + mouvements SORTIE
- ✅ Marquer comme livrée
- ✅ Générer facture PDF
- ✅ Enregistrer une vente directe sans client
- ✅ Annuler une vente du jour

---

## US associées (EPIC 9 — Commandes Client + EPIC 10 — Vente Directe)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-056** | Créer une commande client | P0 | 🔜 Non commencé |
| **US-057** | Lister les commandes client | P0 | 🔜 Non commencé |
| **US-058** | Consulter le détail commande client | P0 | 🔜 Non commencé |
| **US-059** | Modifier commande client | P0 | 🔜 Non commencé |
| **US-060** | **Valider commande client** ⭐ (vérification stock + SORTIE) | P0 | 🔜 Non commencé |
| **US-061** | Marquer commande client livrée | P0 | 🔜 Non commencé |
| **US-062** | Supprimer commande client | P0 | 🔜 Non commencé |
| **US-063** | Générer facture PDF | P1 | 🔜 Non commencé |
| **US-064** | Enregistrer une vente directe (caisse) | P0 | 🔜 Non commencé |
| **US-065** | Lister les ventes directes | P0 | 🔜 Non commencé |
| **US-066** | Consulter une vente directe | P0 | 🔜 Non commencé |
| **US-067** | Annuler une vente directe | P1 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| POST | `/api/v1/commandes-client` | US-056 |
| GET | `/api/v1/commandes-client` | US-057 |
| GET | `/api/v1/commandes-client/{id}` | US-058 |
| PUT | `/api/v1/commandes-client/{id}` | US-059 |
| POST | `/api/v1/commandes-client/{id}/valider` | US-060 ⭐ |
| POST | `/api/v1/commandes-client/{id}/livrer` | US-061 |
| DELETE | `/api/v1/commandes-client/{id}` | US-062 |
| GET | `/api/v1/commandes-client/{id}/facture` | US-063 |
| POST | `/api/v1/ventes` | US-064 |
| GET | `/api/v1/ventes` | US-065 |
| GET | `/api/v1/ventes/{id}` | US-066 |
| POST | `/api/v1/ventes/{id}/annuler` | US-067 |

---

## Règles métier clés

- Validation commande client : vérification stock **avant** création des mouvements
- Stock insuffisant → `409 INSUFFICIENT_STOCK` avec liste des articles en rupture
- Vente directe : pas de client requis (vente anonyme)
- Annulation possible uniquement le jour même

---

## Tables BDD associées

- `commande_client` + `ligne_commande_client` — Ventes B2B
- `vente` + `ligne_vente` — Ventes directes

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- → `stockmaster-catalogue` (articles)
- → `stockmaster-tiers` (clients)
- → `stockmaster-stock` (vérification stock + création mouvements SORTIE)
- → `stockmaster-groupe` (CA dashboard)
