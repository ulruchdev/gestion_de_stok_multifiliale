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
| US-006 | Inscription entreprise unique | ✅ | Fait + DTO aligné frontend (Juillet 2026) : `nomBoutique`→`nomEntreprise`, `prenom`/`nom`→`adminPrenom`/`adminNom`, ajout `nif`(opt.) + `telephone`(E.164), retrait `ville`/`quartier` |
| US-007 | Inscription groupe | ✅ | Merge effectué (PR #6) — code + tests OK. Validation téléphone renforcée E.164 |
| US-008 | Connexion JWT | ✅ | Fait. Rate limiting rendu configurable via properties (stockmaster.rate-limiting) |
| US-009 | Refresh token | ✅ | Merge effectué |
| US-010 | Déconnexion | ✅ | Merge effectué. Blacklist JWT vérifiée — correcte (TTL Redis) |
| US-011 | Mot de passe oublié | ✅ | Merge effectué |
| US-012 | Réinitialisation mot de passe | ✅ | Merge effectué |
| US-013 | Changement mot de passe | ✅ | Merge effectué |
| US-082 | Unicité stricte des entreprises (NIF, téléphone, email, nom groupe) | ✅ | Terminé — commit `c3dc635` (Sprint 11 add.). Index UNIQUE partiels (V4), 71 tests auth au total |

### Corrections de sécurité (Juillet 2026)
- **JwtAuthenticationFilter bugfix** : Ne bloque plus les endpoints `.permitAll()` avec un token expiré/invalide (retournait 401 au lieu de continuer la chaîne)
- **Rate limiting configurable** : `RateLimitProperties.java` avec deux niveaux (global 100 req/min + per-endpoint) — plus de constantes hardcodées
- **Rate limiting externalisé en variables d'environnement** (commit `395400f`) : plus aucune valeur numérique en dur dans `application.yml`, défauts de sécurité d'origine préservés via `${RATE_LIMIT_*:defaut}`
- **Regex mot de passe élargie** (commit `cea66de`) : jeu de caractères spéciaux étendu dans les 4 DTOs concernés

### Durcissement sécurité (EPIC 2, renuméroté GS-CDA-2026-02 : US-014-017 → US-083-086)

| US | Description | Priorité | Statut | Notes |
|----|-------------|----------|--------|-------|
| US-083 | Rotation du Refresh Token avec détection de rejeu | P0 | ✅ | Terminé (branche `feature/GS-083-rotation-refresh-token`) — familyId+jti chaînés, rotation à chaque refresh, révocation totale + événement sur rejeu détecté. 90 tests shared+auth verts, vérifié en conditions réelles (login→refresh→rejeu→401 AUTH_006) |
| US-084 | Hachage des mots de passe en Argon2id | P1 | ❌ | Non commencé — vérifié : `SecurityConfig` utilise encore uniquement `BCryptPasswordEncoder` |
| US-085 | Comportement fail-closed ciblé en cas d'indisponibilité de Redis | P0 | ✅ | Terminé (branche `feature/GS-085-fail-closed-redis`) — fail-closed **503 SEC_004** ciblé sur `POST /auth/refresh` (lecture + écriture de rotation) et rate-limiting par endpoint sensible ; **fail-open** best-effort sur login/logout/forgot, compteur global de rate-limiting et vérification blacklist JWT. Réutilise `RedisHealthTracker` (shared, déjà sur `main`) → alerte ERROR après 60 s d'indisponibilité continue. Conforme ADR-005 (Redis mono-instance, pas de HA) |
| US-086 | Logs d'audit structurés pour les événements d'authentification | P1 | ❌ | Non commencé — vérifié : aucun log JSON structuré (MDC/logstash) |

> ✅ Les deux durcissements **P0** (US-083 rotation refresh + US-085 fail-closed ciblé Redis) sont terminés → le blocage EPIC 3 au sens du backlog (`BACKLOG_StockMaster_CM.md` : dépendance EPIC 3 = « EPIC 2 complété ») est levé côté P0. Restent US-084 (Argon2id) et US-086 (logs d'audit), tous deux **P1**, non bloquants pour EPIC 3.

## EPIC 3 à 13 — (non commencé)

---

## Frontend — EPICs

### EPIC-F00 — Fondations techniques (41 SP)

| US | Description | SP | Statut | Notes |
|----|-------------|----|--------|-------|
| US-F001 | Init projet frontend Vite + React + TS | 3 | ✅ | Fait |
| US-F002 | Design system Tailwind / tokens | 5 | ✅ | Tokens StockMaster — cf. DESIGN_CORRECTIONS.md |
| US-F003 | Composants partagés (Button, Input, Table...) | 8 | ✅ | 15 composants (6 améliorés + 9 nouveaux) |
| US-F004 | Client API Axios + intercepteur JWT | 5 | ✅ | Intercepteur refresh + file d'attente |
| US-F005 | Routing + Guards RBAC | 5 | ✅ | Layouts AuthLayout + DashboardLayout |
| US-F006 | Auth state (Zustand) | 3 | ✅ | Store auth + AuthProvider |
| US-F007 | Layout par rôle (sidebar/menu) | 5 | ✅ | Menu filtré par rôle, responsive |

### EPIC-F01 — Onboarding public (24 SP)

| US | Description | SP | Statut | Notes |
|----|-------------|----|--------|-------|
| US-F010 | Page d'accueil publique | 2 | ✅ | AccueilPage.tsx (461 lignes, sections features/stats), routée sur `/` — corrigé après vérification code réel (était marquée ❌ par erreur) |
| US-F011 | Écran de choix du type d'inscription | 2 | ✅ | InscriptionChoixPage — 2 cartes avec features |
| US-F012 | Formulaire Entreprise Unique | 5 | ✅ | Stepper 3 étapes + validation Zod + API |
| US-F013 | Formulaire Groupe multi-sites | 5 | ✅ | Stepper 4 étapes + validation Zod + API |
| US-F014 | Écran de connexion | 3 | ✅ | LoginPage — validation, démos, toast |
| US-F015 | Flow mot de passe oublié + réinitialisation | 5 | ✅ | ForgotPasswordPage + ResetPasswordPage |
| US-F016 | Écran d'activation de compte | 2 | ❌ | Pas implémenté (P1) |

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

---

## Backend — Fondamentaux des 9 modules fonctionnels (session 14/09/2026)

Pose des fondamentaux module par module (entités alignées V5, repositories scoping tenant, enums de machines à états, services métier purs + tests TDD). Les développeurs n'ont plus qu'à implémenter services applicatifs, contrôleurs, mappers et cas d'usage US.

| Élément | Contenu | Statut |
|---------|---------|--------|
| Agrégats tenant | `TenantGroup`/`Entreprise`/`Utilisateur` + enums + repos déplacés de `auth` vers `shared` (tous modules référencent sans dépendre de auth) ; alignés V5 : `limite_utilisateurs`, `site_operationnel`, `code_filiale NOT NULL`, `email_verifie`, colonnes `token_reset` supprimées, `PlanAbonnement` 3 valeurs (DEC-015) | ✅ |
| catalogue | `Categorie`/`Article` niveau GROUPE (DEC-002), XAF entiers + TTC calculé (DEC-003), 2 unités + facteur (DEC-013), lot-ready (DEC-012) ; `CalculTvaService` + 12 tests | ✅ |
| tiers | `Client`/`Fournisseur` périmètre filiale + repos (unicité NIF, batch) | ✅ |
| achat | `CommandeFournisseur`(+lignes), machine à états DEC-006 (`COMMANDEE→PARTIELLEMENT_RECUE→RECEPTIONNEE\|ANNULEE`), quantités DECIMAL(12,3) | ✅ |
| vente | `Vente`/`LigneVente` (statut `PAYEE→ANNULEE\|REMBOURSEE`, client nullable, session DEC-018), `SessionCaisse` (écart anti-perte), `Paiement` mixte (DEC-009), `CommandeClient` (+règlement DEC-011/020) + 6 repos | ✅ |
| stock | `MouvementStock` journal immuable partitionné (C-12/DEC-033, PK composite documentée), `TransfertStock` multi-lignes groupe + FK composites (DEC-007), inventaire DEC-036, `CleIdempotence` JSONB (DEC-027) ; `CalculStockService` signe map + rupture `≤ seuil` + invariant non-négatif + 10 tests | ✅ |
| notification | `NotificationAlerte` refondue (destinataire explicite, type SANS CHECK — DEC-004/DEC-037) ; port `CanalNotification` (DEC-014) prêt pour EPIC 12 (email bienvenue/vérification AUTH-07) | ✅ |
| groupe / utilisateur / reporting | `ControleLimiteFilialesService` (sites non opérationnels exclus), `ControleLimiteUtilisateursService` (10/50/négocié), `CalculCaService` (CA HT facturé, TVA, TTC — DEC-020) + 11 tests | ✅ |
| **V5 validée en conditions réelles** | Flyway V1→V5 exécutée **pour de vrai** sur PostgreSQL 16 vierge (conteneur jetable port 5433) via le test d'intégration bootstrap + `ddl-auto=validate` OK sur les entités — dette de validation de V5 soldée | ✅ |
| Qualité | `mvnw test` : **125/125 verts** (dont 3 tests d'intégration démarrage complet) ; CI `mvn verify` couvre tous les modules, rien à changer | ✅ |
| Correction config | Profil `test` : datasource passée en `${DB_HOST}/${DB_PORT}/${DB_NAME}/${DB_USERNAME}/${DB_PASSWORD}` (comme les autres profils) — il était le seul codé en dur sur 5432 | ✅ |

**Note machine locale (gestionulrich)** : un PostgreSQL natif Windows écoute `localhost:5432` avec des identifiants qui ne sont pas ceux documentés ; le conteneur compose ne peut pas publier son port (règle de bind Windows) et sa base n'a PAS été migrée ni effacée. Pour tester en local : `DB_PORT=5433 ./mvnw test` avec un conteneur jetable, ou arrêter le service natif. CI non concernée (elle provisionne son propre PG).

---

*Dernière mise à jour : 14 septembre 2026 — fondamentaux des 9 modules posés, V5 validée en exécution réelle, 125/125 tests verts, branche `feature/GS-085-fail-closed-redis`*
