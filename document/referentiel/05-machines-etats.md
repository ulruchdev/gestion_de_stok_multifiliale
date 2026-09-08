# RÉFÉRENTIEL — Partie 5 : Machines à états

### `GS-REF-2026-01 §5` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **Validée (DEC-002, DEC-006, DEC-009, DEC-010, DEC-018)**

> **Règle :** toute transition d'état est un `@Transactional` avec `Idempotency-Key` (`DEC-027`) et renvoie `409` si l'état ne le permet pas (`DEC-017`). Une transition non listée ici n'existe pas.

## 5.1 Vente directe (caisse)

```
         ┌─────────────┐
         │    PAYEE    │──────────────► ANNULEE
         └─────┬───────┘        (jour même, sa session, DEC-018)
               │
               └──────────────► REMBOURSEE
                    (en caisse, sans avoir, DEC-010)
```

- Création : ouverture d'une session de caisse obligatoire (`DEC-009`).
- Paiement mixte possible (`ESPECES`, `MOBILE_MONEY`, `CARTE`).
- `ANNULATION_VENTE` = mouvement compensatoire **entrant** (+).

## 5.2 Commande client (B2B)

```
VALIDEE ──► LIVREE ──► (règlement REGLEE + date d'échéance, DEC-011)
    │
    └──► ANNULEE
```

- Livraison = création du mouvement de stock sortant.
- Règlement : `NON_REGLEE` → `REGLEE` (avec `date_reglement`, `DEC-020`).

## 5.3 Commande fournisseur (achats)

```
COMMANDEE ──► PARTIELLEMENT_RECUE ──► RECEPTIONNEE
    │                │
    └──► ANNULEE     └──► (réceptions partielles successives)
```

- Réception = création du mouvement de stock entrant (+ conditionnement `DEC-013`).
- `RECEPTIONNEE` remplace l'ancien `VALIDEE` (`DEC-006`).

## 5.4 Transfert inter-filiales

```
DEMANDE ──► VALIDE ──► EN_TRANSIT ──► RECU
    │          │            │
    │          └──► REFUSE   └──► ECART (réception partielle, écart motivé)
    └──► ANNULE
```

- `DEMANDE` : créée par l'Admin Filiale (ou Admin Groupe) — `DEC-002`.
- `VALIDE` : approbation par l'Admin Groupe.
- `EN_TRANSIT` : mouvement `TRANSFERT_SORTIE` sur la source.
- `RECU` : mouvement `TRANSFERT_ENTREE` sur la cible, avec quantité réellement reçue.
- `ECART` : différence expédié/reçu, corrigée et motivée.

## 5.5 Filiale et utilisateur

| Objet | États | Transitions |
|---|---|---|
| Filiale | `ACTIVE` / `SUSPENDUE` / `SUPPRIMEE` | `ACTIVE ⇄ SUSPENDUE` (US-019, `DEC-015`) ; `→ SUPPRIMEE` uniquement après **transfert du stock résiduel** (`DEC-015`) |
| Utilisateur | `ACTIF` / `INACTIF` | désactivation par admin ; `actif` ≠ `email_verifie` (`DEC-016`) |
| Session de caisse | `OUVERTE` / `CLOTUREE` | clôture avec écart constaté (`DEC-009`) |
| Inventaire | `OUVERTE` / `VALIDE` | ouverture+comptage par GESTIONNAIRE_STOCK, validation par ADMIN_FILIALE, le jour même (`DEC-036`) |

## 5.6 Règles transverses

- **Pas de transition rétroactive** : un état clos (CA, clôture de caisse) ne change jamais (`DEC-020`).
- **`409` pour toute transition impossible** (`DEC-017`).
- Les états du design system sont **réduits à 4 badges** : `en_preparation`, `validee`, `livree`, `annulee` — les badges `en_attente`, `partiel`, `rembourse` sont **retirés** (`DEC-006`).

---

*Sources : `DEC-002`, `DEC-006`, `DEC-009`, `DEC-010`, `DEC-011`, `DEC-015`, `DEC-016`, `DEC-017`, `DEC-018`, `DEC-020`, `DEC-027`, `DEC-036`.*