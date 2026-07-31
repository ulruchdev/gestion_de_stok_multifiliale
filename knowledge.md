# StockMaster CM — Connaissance du projet

> Fichier unique, auto-chargé par **Freebuff** (convention Codebuff : `knowledge.md` à la racine) et par **Claude Code** (via `CLAUDE.md` → `@knowledge.md`). Une seule source de vérité pour les deux outils — ne pas dupliquer ce contenu ailleurs.

## ⚠️ Protocole de démarrage — à appliquer avant toute réponse

Ce fichier ne contient que des **règles et une architecture cible** — pas l'état réel du repo au jour J. Avant de répondre à une demande de code, de plan, ou d'audit :

1. Vérifie l'état réel par lecture/commande effective : `git log -15 --oneline`, `git status`, la structure réelle des dossiers `stockmaster-*/src` et `frontend/src`, et `document/03-pilotage/progress-ledger.md`.
2. **Ne jamais affirmer qu'une US ou un module est « fait » sur la seule base du backlog ou d'une session précédente** — un backlog décrit une intention, pas un état constaté. Le repo réel (code + git log) est la seule source de vérité.
3. Sous Freebuff : si `@task-architect`, `@spring-module-guardian` ou `@react-frontend-guardian` est invoqué et qu'aucun rapport `session-bootstrap` n'existe encore dans la session, ces agents le spawnent automatiquement (déjà configuré dans `.agents/`).
4. Sous Claude Code (pas d'agents `.agents/*.ts`, ce sont des définitions propriétaires Freebuff) : fais toi-même la vérification du point 1 avant de proposer un plan.

---

## Vue d'ensemble

**StockMaster CM** — SaaS multi-tenant de gestion de stock pour PME camerounaises (boutiques, pharmacies, distributeurs multi-sites). Monorepo : backend Spring Boot dans `backend/`, frontend React dans `frontend/`.

> ⚠️ Toute implémentation DOIT commencer par lire `document/03-pilotage/A_JIRA_ET_GIT_FLOW.md` et en respecter scrupuleusement le workflow (11 étapes, TDD, Definition of Done). Aucune étape ne peut être sautée.

### Stack Backend
- Java 21, Spring Boot 3.3.5, Maven multi-module (11 modules métier + `stockmaster-bootstrap`)
- PostgreSQL 16 (Spring Data JPA), Redis 7 (cache, rate limiting, blacklist JWT), MinIO (fichiers)
- Migrations : Flyway uniquement. Jamais `ddl-auto=update`, jamais `update` en non-local.
- JWT : jjwt 0.12.x, HS256, access 15 min / refresh 7 jours

### Stack Frontend
- React 18 + TypeScript, Vite, React Router v6
- TanStack Query (état serveur), Zustand (état client léger)
- Tailwind CSS + composants maison (`shared/ui/`) — le design system vit dans le code, pas dans Figma (voir `design/README.md`)
- React Hook Form + Zod (validation miroir des contraintes serveur)
- Axios + intercepteur JWT (refresh automatique)

### Emplacements clés du code

| Chemin | Contenu | État réel |
|---|---|---|
| `stockmaster-shared/` | `AbstractEntity`, `ApiResponse<T>`, `ProblemResponse` (RFC 7807), `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`, migrations Flyway V1-V4 | ✅ Implémenté |
| `stockmaster-auth/` | JWT (jjwt 0.12.6, HS256), inscription (entreprise unique + groupe), login, refresh, logout, forgot/reset/change password, rate limiting configurable | ✅ Implémenté |
| `stockmaster-bootstrap/` | Point d'entrée `@SpringBootApplication`, seul module avec `spring-boot-maven-plugin`, `application.yml` (3 profils dev/test/prod) | ✅ Implémenté |
| `stockmaster-groupe/` | Groupe & filiales | 🔜 Stub vide (0 fichier .java) |
| `stockmaster-utilisateur/` | Gestion des utilisateurs | 🔜 Stub vide |
| `stockmaster-catalogue/` | Catalogue articles | 🔜 Stub vide |
| `stockmaster-tiers/` | Clients & fournisseurs | 🔜 Stub vide |
| `stockmaster-achat/` | Commandes fournisseur | 🔜 Stub vide |
| `stockmaster-stock/` | Mouvements de stock, transferts | 🔜 Stub vide |
| `stockmaster-vente/` | Ventes B2B & caisse | 🔜 Stub vide |
| `stockmaster-notification/` | Notifications & alertes | 🔜 Stub vide |
| `stockmaster-reporting/` | Statistiques & reporting | 🔜 Stub vide |
| `document/` | Specs, backlog, CDCT, workflow, stratégie de test, collection Postman | — |
| `design/` | Design system propriétaire (tokens, composants), références d'inspiration | — |

## Commandes utiles

```bash
# Compiler (tous les modules)
mvn compile -q

# Lancer les tests d'un module précis (auth = le plus actif)
mvn test -pl stockmaster-auth

# Lancer une classe de test précise
mvn test -pl stockmaster-auth -Dtest=AuthServiceImplTest

# Lancer tous les tests
mvn test

# Build sans les tests
mvn package -DskipTests

# Démarrer l'environnement de dev (PostgreSQL + Redis + MinIO + MailHog)
docker compose up -d

# Lancer l'application (profil dev) — c'est bootstrap qui porte le main(), pas shared
mvn spring-boot:run -pl stockmaster-bootstrap

# Vérifier le démarrage
curl http://localhost:8080/actuator/health
```

## Architecture imposée — Backend
- Couches strictes : `controller/` → `service/` (interface + impl) → `repository/` → `domain/`
- DTO obligatoire entre Controller et Service. Jamais d'Entity JPA exposée directement.
- **Isolation multi-tenant = règle n°1.** Toute requête filtre sur `entreprise_id` (JWT claim via `StockMasterPrincipal`, jamais depuis le body) — ou `group_id` pour les vues consolidées Admin Groupe. Aucune exception, même en lecture.
- Écritures financières/stock : `@Transactional(rollbackFor = Exception.class)`
- Un module ne référence jamais directement le repository d'un autre module. Communication inter-modules : Spring Application Events uniquement.
- Stock réel = calculé à la volée depuis `mouvement_stock` (`Σ ENTREE − Σ SORTIE` + variantes), jamais dénormalisé en colonne.
- Prix TTC = calculé côté service, jamais saisi manuellement.

## Architecture imposée — Frontend
- RBAC frontend = reflet strict du RBAC backend, **jamais** source de vérité. Toute garde de route a son `@PreAuthorize` correspondant côté backend.
- Aperçu client-side (TTC, stock disponible) acceptable pour l'UX, mais la valeur de vérité vient toujours de la réponse serveur.
- Le frontend démarre le code d'un écran dès que la maquette/le composant est validé, contre un mock (MSW), sans attendre l'endpoint réel.
- Gestion réseau dégradée obligatoire (contexte camerounais : coupures fréquentes) — jamais un simple spinner infini sans état de reconnexion.

## Conventions & contraintes clés
- **JWT :** chaque endpoint protégé exige `Authorization: Bearer <token>`. Endpoints publics listés dans `SecurityConfig.java` `.permitAll()`.
- **Soft delete :** toutes les entités héritent de `AbstractEntity` (`supprime` boolean).
- **Flyway uniquement :** `ddl-auto=none` en prod/test, `ddl-auto=validate` en dev.
- **Gestion d'erreur :** `BusinessException(ErrorCode)` → `GlobalExceptionHandler` → RFC 7807 `ProblemResponse`. Jamais de stack trace exposée en réponse HTTP.
- **Mot de passe :** min 8 caractères, 1 majuscule, 1 chiffre, 1 caractère spécial. BCrypt.
- **Redis :** refresh tokens (`refresh:{userId}`), rate limiting, blacklist JWT (`blacklist:jti:{jti}`), tokens reset (`reset:{token}`).
- **Rate limiting :** configurable via `stockmaster.rate-limiting.*` dans `application.yml` — plus de constantes en dur.

## Anti-patterns interdits
- `@CrossOrigin("*")`
- Concaténation de chaînes dans requêtes JPQL/SQL natif
- Secrets en clair dans le code ou fichiers versionnés (`.env` jamais commité)
- Frontend qui décide localement qu'un stock est suffisant ou qu'un prix TTC est définitif
- Commit direct sur `main`, merge de sa propre PR sans review

## Gotchas
- `stockmaster-shared` est une **library module** — `<skip>true</skip>` sur `spring-boot-maven-plugin` pour éviter que le repackage fat-JAR masque ses classes aux modules dépendants.
- `AuthTestApplication` (dans `stockmaster-auth/src/test/`) fournit sa propre `SecurityFilterChain` — les tests contrôleur ne chargent pas le vrai `JwtAuthenticationFilter`.
- Redis doit tourner pour les tests auth (`StringRedisTemplate`). La CI le fournit via un service docker.
- JaCoCo n'est activement mesuré que sur `stockmaster-shared` et `stockmaster-auth` — le job Sonar de la CI est volontairement scopé à `-pl stockmaster-shared,stockmaster-auth,stockmaster-bootstrap` (voir `document/implementation.md`). **Tout nouveau module qui reçoit du code doit être ajouté à ce `-pl` dans `.github/workflows/ci-backend.yml`, sinon il passera la CI sans jamais être scanné.**
- `document/implementation.md` est la source de vérité pour le suivi des US — à mettre à jour à chaque merge.

## Conventions Git
- `main` (intégration) ← `feature/GS-XXX-nom-court` / `fix/...` / `docs/...` / `refactor/...`. Pas de branche `develop`.
- Backend : `feat(GS-XXX): description` — IDs dans `document/02-backlogs/BACKLOG_StockMaster_CM.md`.
- Frontend : `feat(GS-FXXX): description` — IDs dans `document/02-backlogs/GS-FRONTEND-BACKLOG-2026-01.md`.
- Une US = une branche = une PR. Avant tout merge : pipeline CI verte (build + tests + quality gate).
- Avant tout push : `mvn test -pl <module>` → BUILD SUCCESS, 0 failure.

## Variables d'environnement (`.env`, jamais commité)

```env
JWT_SECRET=<clé encodée base64, ≥256 bits>
DB_HOST=localhost
DB_PORT=5432
DB_NAME=stockmaster_dev
DB_USERNAME=stockmaster
DB_PASSWORD=stockmaster
REDIS_HOST=localhost
REDIS_PORT=6379
MINIO_ACCESS_KEY=stockmaster
MINIO_SECRET_KEY=stockmaster
MAIL_HOST=localhost
MAIL_PORT=1025
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

## Agents Freebuff (dans `.agents/`)

| Agent | Rôle | Workflow |
|-------|------|----------|
| `session-bootstrap` | Ancrage anti-hallucination : vérifie l'état RÉEL du repo (git log, fichiers, modules) avant toute action | Spawné automatiquement par `task-architect` et les gardiens en début de session |
| `task-architect` | Architecte de ticket : analyse la US, les dépendances, les portes G1-G7, propose un plan → attend « GO » avant de dévérouiller | Induit le cycle : plan → GO → implémentation |
| `spring-module-guardian` | Gardien backend : `mvn test`, vérifie isolation multi-tenant, `@Transactional`, DTO, sécurité | Après implémentation backend |
| `react-frontend-guardian` | Gardien frontend : lint, test, vérifie TTC serveur, RBAC strict, mock MSW, gestion réseau dégradée | Après implémentation frontend |
| `doc-writer` | Documentation duale : `implementation.md` (technique) + `HUMAN_CHANGELOG.md` (métier) + `progress-ledger.md` (statut) | Après audit vert |
| `git-committer` | Prépare le commit conventionnel (`feat(GS-XXX): desc`), vérifie l'absence de secrets, **jamais de push automatique** | Dernière étape, après `doc-writer` |

**Workflow type (backend) :** `@task-architect GS-XXX` → implémentation → `@spring-module-guardian` → `@doc-writer` → `@git-committer`
**Workflow type (frontend) :** `@task-architect GS-FXXX` → implémentation → `@react-frontend-guardian` → `@doc-writer` → `@git-committer`

## Design System — StockMaster CM

Design system propriétaire, codé directement (pas de Figma — voir `design/README.md`, principe « le code = le design »), inspiré des meilleures pratiques du secteur (`design/sources/.../awesome-design-md/`) et adapté au contexte des PME camerounaises : usage mobile dominant, coupures réseau fréquentes, devise XAF.

### Tokens principaux
- **Primary :** `#533afd` (HSL `249 98% 61%`)
- **Texte principal :** `#0d253d` / **Texte secondaire :** `#64748b`
- **Fonds :** `#f6f9fc` / **Bordures :** `#e3e8ee`
- **Sidebar :** `#1c1e54`
- **Stock :** Warning `#faad14`, Danger `#ff4d4f`, Success `#52c41a`

### Références
- `document/01-architecture/DESIGN_TOKENS_REFERENCE.md` — guide complet
- `document/02-backlogs/DESIGN_CORRECTIONS.md` — suivi des corrections
- `frontend/src/index.css` — variables CSS (`:root` + `.dark`)
- `frontend/tailwind.config.ts` — classes utilitaires Tailwind

## Documents de référence (dans `document/`)
- `00-fonctionnel/` : analyse fonctionnelle (GS-CDA-2026-01) + addendum décisions (GS-CDA-2026-02)
- `01-architecture/` : CDCT, modèle de données (GS-DATA), arborescence navigation (GS-IA), séquences critiques (GS-SEQ), DESIGN_TOKENS_REFERENCE.md
- `02-backlogs/` : backlog backend, design, frontend, DESIGN_CORRECTIONS.md
- `03-pilotage/` : matrice RACI (portes G1-G7), planning, kickoff, progress-ledger, strategie_test
- `MODE_EMPLOI_FREEBUFF.md` : configuration et workflow des agents Freebuff
- `HUMAN_CHANGELOG.md` : changelog métier (audience non technique)

## Ne jamais faire transiter dans les prompts
- Données clients réelles de production, contenu réel de `.env`, tokens JWT réels, clés API réelles
