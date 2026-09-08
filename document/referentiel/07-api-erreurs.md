# RÉFÉRENTIEL — Partie 7 : Contrats d'API & erreurs

### `GS-REF-2026-01 §7` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **🔍 Dérivée — l'autorité est `test_postman.md` (porte G4) et le code**

> **Règle (DEC-001) :** cette partie est une vue. Le contrat opposable est `document/test_postman.md` + `postman_collection.json` (porte G4). En cas de divergence, le code a raison et la doc est corrigée.

## 7.1 Format des erreurs (RFC 7807)

- `ProblemResponse` : `type`, `title`, `status`, `detail`, `instance`, `errorCode`, `timestamp`, `errors[]`.
- **`409 Conflict`** pour toute contrainte d'état : stock insuffisant, transfert déjà reçu, vente déjà annulée, catégorie non supprimable (`DEC-017`, `DEC-028`).
- **`422`** réservé à la validation de champ (jamais pour l'état).
- Les filtres (rate limit, JWT) écrivent du JSON brut `{"errorCode":...}` — à harmoniser sur le format RFC 7807 (item C-15 de la checklist).

## 7.2 Endpoints Auth (état réel vérifié le 8 sept. 2026)

| Endpoint | Méthode | Corps | Notes |
|---|---|---|---|
| `/api/v1/auth/inscription/entreprise-unique` | POST | `InscriptionEntrepriseUniqueRequest` | compte créé **inactif** (`DEC-016`) |
| `/api/v1/auth/inscription/groupe` | POST | `InscriptionGroupeRequest` | idem + groupe |
| `/api/v1/auth/login` | POST | `LoginRequest` | vérifie `actif` + `email_verifie` |
| `/api/v1/auth/refresh` | POST | `RefreshTokenRequest` | rotation + détection de rejeu (`US-083`) |
| `/api/v1/auth/logout` | POST | — | blacklist jti |
| `/api/v1/auth/forgot-password` | POST | `ForgotPasswordRequest` | jeton Redis TTL 1 h (`DEC-024`) |
| `/api/v1/auth/reset-password` | POST | `ResetPasswordRequest` | usage unique |
| `/api/v1/auth/change-password` | PUT | `ChangePasswordRequest` | best-effort Redis (`DEC-039`) |

**À ajouter (décisions) :** endpoint « renvoyer le lien d'activation » (`DEC-016`) ; endpoint d'import CSV (`DEC-038`).

## 7.3 Règles transverses d'API

- `entreprise_id` / `group_id` : **jamais** dans le corps de requête (issus du jeton) ; `filialeId` explicite validé côté serveur (`DEC-018`).
- `Idempotency-Key` obligatoire sur toute écriture créant un mouvement de stock (`DEC-027`).
- Réponse `409` pour toute transition d'état impossible (`DEC-017`).
- Refresh token : **cookie httpOnly Secure SameSite=Strict** (`DEC-025`) ; access token en mémoire.
- Rate limit : `X-Forwarded-For` ignoré hors proxy de confiance (`DEC-041`) ; `429 AUTH_429` au dépassement.

---

*Sources : `DEC-017`, `DEC-018`, `DEC-024`, `DEC-025`, `DEC-027`, `DEC-039`, `DEC-041`, `US-083` ; code réel `AuthController`.*