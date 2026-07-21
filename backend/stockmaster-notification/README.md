# StockMaster CM — Notification (Alertes & Notifications)

> **Artefact :** `stockmaster-notification`
> **Statut :** 🔜 Stub (POM créé, code à implémenter)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.notification`

---

## Rôle

Gestion des notifications système et alertes de stock. Écoute les événements métier et notifie les utilisateurs concernés.

### Fonctionnalités prévues

- ✅ Alerte automatique quand le stock passe sous le seuil minimum
- ✅ Anti-spam : pas de doublon d'alerte < 24h pour le même article
- ✅ Consultation des alertes en cours
- ✅ Email de bienvenue à l'inscription
- ✅ Email d'invitation employé

---

## US associées (EPIC 12)

| US | Description | Priorité | Statut |
|---|---|---|---|
| **US-071** | Alerte automatique seuil stock minimum | P0 | 🔜 Non commencé |
| **US-072** | Consulter les alertes en cours | P1 | 🔜 Non commencé |
| **US-073** | Marquer une alerte comme lue | P1 | 🔜 Non commencé |
| **US-074** | Email de bienvenue à l'inscription | P1 | 🔜 Non commencé |
| **US-075** | Email invitation employé | P1 | 🔜 Non commencé |

---

## Endpoints prévus

| Méthode | Endpoint | US |
|---|---|---|
| GET | `/api/v1/alertes` | US-072 |
| PATCH | `/api/v1/alertes/{id}/lire` | US-073 |
| POST | `/api/v1/auth/activer-compte` | US-075 |

---

## Architecture événementielle

```
[StockUpdatedEvent] → @EventListener → AlerteStockListener
    ↓                        ↓
  Publié par            Transaction REQUIRES_NEW
  stock/service         (indépendante)
                              ↓
                    AlerteService.verifierEtCreerAlerte()
                              ↓
                    Anti-spam 24h → Création alerte → Email
```

## Tables BDD associées

- `notification_alerte` — Type (STOCK_BAS / RUPTURE), stock actuel, seuil, lue

## Dépendances inter-modules

- → `stockmaster-shared` (infrastructure)
- **Écoute :** stockmaster-stock (`StockUpdatedEvent`)
- **Est utilisé par :** stockmaster-auth (email bienvenue)
