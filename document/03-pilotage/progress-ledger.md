# 📊 Progress Ledger — StockMaster CM

> Journal de suivi de l'avancement des US. Mis à jour par `doc-writer.ts` après chaque US terminée.
> **Référence :** BACKLOG_StockMaster_CM.md, GS-FRONTEND-BACKLOG-2026-01.md, GS-DESIGN-BACKLOG-2026-01.md

## Légende

- [ ] Non commencé
- [~] En cours
- [✅] Terminé

---

## EPIC 1 — Fondations Techniques (19 SP)

| US | Description | Statut | Notes |
|----|-------------|--------|-------|
| US-001 | Initialisation projet Spring Boot | ✅ | Fait |
| US-002 | Configuration Flyway + schéma initial | ✅ | Fait |
| US-003 | Gestion centralisée des erreurs | ✅ | Fait |
| US-004 | Pipeline CI/CD + Docker | ✅ | Fait |
| US-005 | Conteneurisation Docker | ✅ | Fait |

## EPIC 2 — Authentification & Accès (35 SP)

| US | Description | Statut | Notes |
|----|-------------|--------|-------|
| US-006 | Inscription entreprise unique | ✅ | Fait |
| US-007 | Inscription groupe | ✅ | Merge effectué (PR #6) — code + tests OK |
| US-008 | Connexion JWT | ✅ | Fait |
| US-009 | Refresh token | ✅ | Merge effectué |
| US-010 | Déconnexion | ✅ | Merge effectué |
| US-011 | Mot de passe oublié | ✅ | Merge effectué |
| US-012 | Réinitialisation mot de passe | ✅ | Merge effectué |
| US-013 | Changement mot de passe | ✅ | Merge effectué |

## EPIC 3 à 13 — (à compléter par `doc-writer.ts`)

---

## Backend — US-F (Frontend)

| US | Description | Statut | Notes |
|----|-------------|--------|-------|
| US-F001 | Init projet frontend Vite + React + TS | ✅ | Fait |
| US-F002 | Design system Tailwind / tokens | ✅ | Tokens StockMaster — cf. DESIGN_CORRECTIONS.md |
| US-F003 | Composants partagés (Button, Input, Table...) | ✅ | 15 composants (6 améliorés + 9 nouveaux) |
| US-F004 | Client API Axios + intercepteur JWT | [~] | En cours |
| US-F005 | Routing + Guards RBAC | ✅ | Fait (layouts + routes) |
| US-F006 | Auth state (Zustand) | ✅ | Fait (store auth + provider) |

---

## Design — EPIC-D00 (Design System StockMaster)

| US | Description | SP | Statut | Notes |
|----|-------------|----|--------|-------|
| US-D001 | Palette de couleurs | 3 | ✅ | Palette StockMaster complète |
| US-D002 | Typographie | 2 | ✅ | font-display/body/mono |
| US-D003 | Composants atomiques | 6 | ✅ | 15 composants (6 existants + 9 nouveaux) |
| US-D004 | Badges d'état | 3 | ✅ | 10 variants + dot/removable |
| US-D005 | Grilles responsive | 2 | ✅ | Breakpoints xs→2xl + 8px grid |
| US-D006 | Icônes | 2 | ✅ | Lucide React intégré |
| US-D007 | Templates d'état | 5 | ✅ | EmptyState (5) + Skeleton (5) + OfflineBanner |

**Total EPIC-D00 : 23/23 SP** ✅

---

## Design System — Fichier de référence

Les fichiers `DESIGN_CORRECTIONS.md` (25 corrections) et `DESIGN_TOKENS_REFERENCE.md` (guide complet) documentent l'ensemble du design system.

---

*Dernière mise à jour : Juillet 2026*
