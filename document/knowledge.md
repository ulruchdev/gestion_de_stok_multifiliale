# Contexte Projet — StockMaster CM

## ⚠️ PROTOCOLE DE DÉMARRAGE — À appliquer avant toute réponse

Ce fichier est lu automatiquement à chaque session, mais **son contenu seul ne suffit pas à connaître l'état réel du repo** — il décrit des règles, pas l'avancement du jour. Avant de répondre à une demande de code, de plan, ou d'audit :

1. Si `@task-architect`, `@spring-module-guardian` ou `@react-frontend-guardian` est invoqué et qu'aucun rapport `session-bootstrap` n'existe encore dans cette session, ces agents le spawnent automatiquement — c'est déjà configuré dans `.agents/`.
2. Si tu (agent Freebuff, hors de ces agents dédiés) reçois une demande directe sans passer par eux, invoque toi-même `@session-bootstrap` en premier, ou à défaut lis manuellement : `git log -15 --oneline`, la structure réelle des dossiers `stockmaster-*` et `frontend/`, et `document/03-pilotage/progress-ledger.md`.
3. **Ne jamais affirmer qu'une US ou un module est "fait" sur la seule base du backlog ou d'une session précédente** — un backlog décrit une intention, pas un état constaté. Le repo réel (code + git log) est la seule source de vérité.

---

## Contexte Projet — StockMaster CM

SaaS multi-tenant de gestion de stock pour PME camerounaises. Monorepo : backend Spring Boot dans `backend/` (11 modules `stockmaster-*`), frontend React dans `frontend/`.

## Stack Backend
- Java 21, Spring Boot 3.3.5, Maven multi-module (11 modules : shared, auth, groupe, utilisateur, catalogue, tiers, achat, stock, vente, notification, reporting)
- PostgreSQL 16 (Spring Data JPA), Redis 7 (cache, rate limiting, blacklist JWT), MinIO (fichiers)
- Migrations : Flyway uniquement. Jamais `ddl-auto=update`, jamais `update` en non-local.
- JWT : jjwt 0.12.x, HS256, access 15 min / refresh 7 jours

## Stack Frontend
- React 18 + TypeScript, Vite, React Router v6
- TanStack Query (état serveur, cache/invalidation), Zustand (état client léger)
- Tailwind CSS + shadcn/ui — doit correspondre 1:1 aux composants Figma
- React Hook Form + Zod (validation miroir des contraintes serveur)
- Axios + intercepteur JWT (refresh automatique)
- Recharts

## Architecture imposée — Backend
- Couches strictes : `controller/` -> `service/` (interface + impl) -> `repository/` -> `domain/`
- DTO obligatoire entre Controller et Service. Jamais d'Entity JPA exposée directement.
- **Isolation multi-tenant = règle n°1 du projet.** Toute requête filtre sur `entreprise_id` (ou `group_id` pour les vues consolidées Admin Groupe). Aucune exception, même en lecture.
- Écritures financières/stock : `@Transactional(rollbackFor = Exception.class)`
- Un module ne référence jamais directement le repository d'un autre module. Communication inter-modules : Spring Application Events uniquement.
- Stock réel = calculé à la volée depuis `mouvement_stock` (`Σ ENTREE − Σ SORTIE` + variantes), jamais dénormalisé en colonne.
- Prix TTC = calculé côté service, jamais saisi manuellement.

## Architecture imposée — Frontend
- RBAC frontend = reflet strict du RBAC backend, **jamais** source de vérité. Toute garde de route a son `@PreAuthorize` correspondant côté backend.
- Aperçu client-side (TTC, stock disponible) acceptable pour l'UX, mais la valeur de vérité vient toujours de la réponse serveur. Ne jamais soumettre un calcul client comme définitif.
- Le frontend démarre le code d'un écran dès que la maquette est validée (gate **G3**), contre un mock (MSW) généré depuis l'OpenAPI — n'attend pas la livraison réelle de l'endpoint (gate **G4**) pour commencer.
- Gestion réseau dégradée obligatoire (contexte camerounais : coupures fréquentes) — jamais un simple spinner infini sans état de reconnexion.

## Anti-patterns interdits
- `@CrossOrigin("*")`
- Concaténation de chaînes dans requêtes JPQL/SQL natif
- Stack trace exposée en réponse HTTP (toujours passer par le `GlobalExceptionHandler`, format RFC 7807)
- Secrets en clair dans le code ou fichiers versionnés (`.env` jamais commité)
- Frontend qui décide localement qu'un stock est suffisant ou qu'un prix TTC est définitif
- Commit direct sur `main`, merge de sa propre PR sans review

## Conventions Git
- Backend : `feat(US-XXX): description` — IDs dans `document/02-backlogs/BACKLOG_StockMaster_CM.md`
- Frontend : `feat(US-FXXX): description` — IDs dans `document/02-backlogs/GS-FRONTEND-BACKLOG-2026-01.md`
- Une US = une branche = une PR. 1 reviewer minimum, jamais l'auteur lui-même.
- Avant tout merge : pipeline CI verte (build + tests + quality gate)

## Agents Freebuff (dans `.agents/`)

| Agent | Rôle | Workflow |
|-------|------|----------|
| `session-bootstrap` | Ancrage anti-hallucination : vérifie l'état RÉEL du repo (git log, fichiers, modules) avant toute action | Spawné automatiquement par task-architect et les gardiens en début de session |
| `task-architect` | Architecte de ticket : analyse la US, les dépendances, les portes G1-G7, propose un plan → attend "GO" avant dévérouillage | Induit le cycle : plan → GO → implémentation |
| `spring-module-guardian` | Gardien backend : `mvn test`, vérifie isolation multi-tenant, `@Transactional`, DTO, sécurité | Après implémentation backend |
| `react-frontend-guardian` | Gardien frontend : lint, test, vérifie TTC serveur, RBAC strict, mock MSW, gestion réseau dégradée | Après implémentation frontend |
| `doc-writer` | Documentation duale : `implementation.md` (technique) + `HUMAN_CHANGELOG.md` (métier) + `progress-ledger.md` (statut) | Après audit vert |
| `git-committer` | Prépare le commit conventionnel (`feat(US-XXX): desc`), vérifie absence de secrets, **jamais de push automatique** | Dernière étape, après doc-writer |

**Workflow type (backend) :** `@task-architect US-XXX` → implémentation → `@spring-module-guardian` → `@doc-writer` → `@git-committer`
**Workflow type (frontend) :** `@task-architect US-FXXX` → implémentation → `@react-frontend-guardian` → `@doc-writer` → `@git-committer`
**Règle :** `git-committer` ne pousse JAMAIS automatiquement. `session-bootstrap` est spawné automatiquement si absent.

## Design System — StockMaster CM

Le design system propriétaire StockMaster CM s'inspire des meilleures pratiques de l'industrie (notamment awesome-design-md) tout en étant adapté au contexte des PME camerounaises : usage mobile dominant, coupures réseau fréquentes, gestion de la devise XAF.

### Tokens principaux
- **Primary :** `#533afd` (HSL `249 98% 61%`) — actions principales
- **Texte principal :** `#0d253d` / **Texte secondaire :** `#64748b`
- **Fonds :** `#f6f9fc` / **Bordures :** `#e3e8ee`
- **Sidebar :** `#1c1e54`
- **Stock :** Warning `#faad14`, Danger `#ff4d4f`, Success `#52c41a`

### Références
- `document/01-architecture/DESIGN_TOKENS_REFERENCE.md` — Guide complet
- `document/02-backlogs/DESIGN_CORRECTIONS.md` — Suivi des corrections
- `frontend/src/index.css` — CSS custom properties (:root + .dark)
- `frontend/tailwind.config.ts` — Classes utilitaires Tailwind

### Composants UI (15 au total)
- **6 existants améliorés :** Button, Input, Badge, Modal, Table, Toast
- **9 nouveaux :** Card, Avatar, Skeleton, EmptyState, Tabs, Select, SearchInput, DataTable, OfflineBanner

## Documents de référence (dans `document/`)
- `00-fonctionnel/` : analyse fonctionnelle (GS-CDA-2026-01) + addendum décisions (GS-CDA-2026-02)
- `01-architecture/` : CDCT, modèle de données (GS-DATA), arborescence navigation (GS-IA), séquences critiques (GS-SEQ), DESIGN_TOKENS_REFERENCE.md
- `02-backlogs/` : backlog backend, design, frontend, DESIGN_CORRECTIONS.md
- `03-pilotage/` : matrice RACI (portes G1-G7), planning, kickoff, progress-ledger, strategie_test
- `MODE_EMPLOI_FREEBUFF.md` : configuration et workflow des agents Freebuff
- `HUMAN_CHANGELOG.md` : changelog métier (audience non technique)

## Ne jamais faire transiter dans les prompts
- Données clients réelles de production, contenu réel de `.env`, tokens JWT réels, clés API réelles
