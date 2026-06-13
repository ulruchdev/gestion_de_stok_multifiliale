# StockMaster CM — Stock (Gestion du Stock & Mouvements)

> **Artefact :** `stockmaster-stock`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.stock`

---

## Rôle

Cœur du système : journal des mouvements de stock en temps réel, corrections, et alertes de stock minimum.

### Fonctionnalités prévues

- ✅ Calcul du stock réel en temps réel (agrégation des mouvements)
- ✅ Historique des mouvements d'un article
- ✅ Correction positive et négative de stock
- ✅ Stock consolidé pour l'Admin Groupe
- ✅ Événements `StockUpdatedEvent` publiés (déclenchent alertes)

---

## US associées (EPIC 8)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-051** | Consulter le stock réel par article | P0 | 🔜 Non commencé |
| **US-052** | Historique des mouvements d'un article | P0 | 🔜 Non commencé |
| **US-053** | Correction positive de stock | P0 | 🔜 Non commencé |
| **US-054** | Correction négative de stock | P0 | 🔜 Non commencé |
| **US-055** | Stock consolidé groupe | P1 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| GET | `/api/v1/stock` | US-051 |
| GET | `/api/v1/stock/articles/{articleId}/mouvements` | US-052 |
| POST | `/api/v1/stock/corrections` | US-053, US-054 |
| GET | `/api/v1/groupe/stock-consolide` | US-055 |

---

## Types de mouvements

| Type | Description | Direction |
|---|---|---|
| `ENTREE` | Réapprovisionnement (validation commande fournisseur) | + |
| `SORTIE` | Vente (validation commande client ou vente directe) | - |
| `CORRECTION_POS` | Correction manuelle positive (inventaire) | + |
| `CORRECTION_NEG` | Correction manuelle négative (casse, perte) | - |
| `TRANSFERT_ENTREE` | Réception d'un transfert inter-filiales | + |
| `TRANSFERT_SORTIE` | Envoi d'un transfert inter-filiales | - |

### Calcul du stock réel
```
stock_réel = Σ(ENTREE + CORRECTION_POS + TRANSFERT_ENTREE)
           - Σ(SORTIE + CORRECTION_NEG + TRANSFERT_SORTIE)
```

---

## Tables BDD associées

- `mouvement_stock` — Journal immuable des mouvements (6 types)
- `article.seuil_alerte` — Seuil déclenchant les alertes
- Index : `(article_id, entreprise_id)` pour le calcul temps réel

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- **Est utilisé par :** stockmaster-achat, stockmaster-vente, stockmaster-groupe, stockmaster-notification
