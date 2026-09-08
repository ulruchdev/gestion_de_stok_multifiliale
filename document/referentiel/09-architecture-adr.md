# RÉFÉRENTIEL — Partie 9 : Architecture technique & ADR

### `GS-REF-2026-01 §9` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **🔍 Dérivée — l'autorité est le code + CDCT**

> **Règle (DEC-001) :** cette partie consolide les décisions techniques. En cas de conflit avec le code, le code a raison.

## 9.1 Stack (actée)

- Java 21, Spring Boot 3.3.5, Maven multi-module (11 modules métier + `stockmaster-bootstrap`).
- PostgreSQL 16 (Flyway uniquement, jamais `ddl-auto=update`), Redis 7 (cache, rate limit, blacklist, jetons), MinIO (fichiers).
- Frontend : React 18 + TypeScript + Vite, TanStack Query, Zustand, Tailwind + composants maison.
- JWT : jjwt 0.12.x, HS256, access 15 min / refresh 7 jours.

## 9.2 Décisions techniques (ADR consolidés)

| Décision | Contenu | Réf |
|---|---|---|
| ADR-003 | Stock = journal de deltas immuable, calculé à la volée | `DEC-023` |
| ADR-005 | Fail-closed **ciblé** (refresh, rate limit) ; fail-open sur login/logout/forgot | `US-085`, `DEC-039` |
| — | Appels inter-modules **synchrones par interface exposée**, jamais de repository externe ; événements réservés à l'asynchrone (notification, audit) | `DEC-019` |
| — | Fuseau : **UTC en base, `Africa/Douala` en métier** (journée comptable 00:00–23:59 Douala) | `DEC-021` |
| — | XAF figé, aucune colonne devise | `DEC-022` |
| — | Refresh token en **cookie httpOnly Secure SameSite=Strict** ; access en mémoire | `DEC-025` |
| — | Session **mono-appareil** en V1 (multi-appareils V1.5) | `DEC-040` |
| — | Rate limit IP : proxy de confiance seul autorisé à écraser `X-Forwarded-For` | `DEC-041` |
| — | Jeton reset : Redis, TTL 1 h, usage unique | `DEC-024` |

## 9.3 Points techniques ouverts (à trancher avec un tiers)

| Point | Question | Impact |
|---|---|---|
| **Numérotation des ventes** | continue sans trou (fisc) ou séquence PostgreSQL (trous OK) ? | compteur verrouillé vs séquence (`A-11`) |
| **Arrondi TVA** | ligne par ligne ou sur total HT ? | résultat différent sur facture (`DEC-003`) |

*Ces deux points ne bloquent pas le schéma V5 mais doivent être tranchés avant le module Vente.*

---

*Sources : `DEC-019`, `DEC-021`, `DEC-022`, `DEC-023`, `DEC-024`, `DEC-025`, `DEC-039`, `DEC-040`, `DEC-041`, `US-085`.*