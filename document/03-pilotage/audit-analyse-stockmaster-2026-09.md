# Audit & Analyse — StockMaster CM
### Date : 7 septembre 2026 | Périmètre : code (backend + frontend) + dossier `document/` (fondation du projet)
### Méthode : lecture effective de chaque fichier, vérification par exécution (build, tests, decodeur jjwt, greps) — aucune supposition ; toute zone d'ombre est une question ouverte (Partie 3)

---

# PARTIE 1 — LOGIQUE MÉTIER (Business Logic)

> Vision Analyste Métier Senior : cohérence entre la documentation, le schéma de données et le comportement réel.

## 1.1 Le modèle métier central (ce qui est acté)

- **SaaS multi-tenant** : chaque entreprise cliente a un espace isolé (`entreprise_id` non nullable sur toute table métier) ; modèle **groupe / filiales** natif (maison mère + N filiales, consolidation, transferts inter-sites).
- **5 acteurs** (GS-CDA-2026-01) : Super Admin (plateforme, jamais les données clients), Admin Groupe (s'inscrit, gère groupe+filiales), Admin Filiale (sa filiale), Employé (4 rôles métier : Gestionnaire stock, Resp. achats, Commercial, Caissier), Système (mouvements automatiques).
- **Contexte camerounais** acté : TVA 19,25 % configurable par catégorie, devise XAF entiers, coupures réseau fréquentes, accès mobile dominant.
- **Règles transverses** : immuabilité du journal `mouvement_stock` (aucun UPDATE/DELETE, jamais) ; stock réel = Σ ENTREE − Σ SORTIE à la volée ; prix TTC calculé côté serveur, jamais saisi ; soft delete partout.

## 1.2 Décisions métier récentes (GS-CDA-2026-02, addendum validé)

| Décision | Contenu | Statut dans le code |
|---|---|---|
| Vente directe non bloquante | La caisse ne bloque jamais sur stock système insuffisant ; alerte `ECART_STOCK_DETECTE` en compensation | ❌ Aucun code (module vente vide) — design validé |
| Annulation de vente | Mouvement compensatoire `ANNULATION_VENTE` (jamais de modification du mouvement original) ; `Vente.statut` VALIDEE/ANNULEE au lieu du booléen `annulee` | ❌ Schéma V1 contient toujours `annulee` (voir 1.3) |
| Fidélité client | `Vente.client_id` nullable pour rattacher une vente directe à un client connu | ❌ Colonne absente du schéma réel |

## 1.3 DIVERGENCES MÉTIER VÉRIFIÉES (docs ↔ schéma ↔ code)

### M1 — Le modèle `vente` existe dans trois états contradictoires

| Source | `vente` | `type_mouvement` |
|---|---|---|
| **V1 réel** (Flyway, `V1__init_schema.sql`) | `annulee BOOLEAN`, `utilisateur_id`, pas de `statut` ni `client_id` | `ENTREE, SORTIE, CORRECTION_POS, CORRECTION_NEG, TRANSFERT_ENTREE, TRANSFERT_SORTIE` |
| **GS-DATA-2026-01** (se proclame « source de vérité unique ») | `statut` VALIDEE/ANNULEE, `client_id`, `caissier_id` | + `ANNULATION_VENTE` |
| **CDCT §23.3** (migrations embarquées) | `statut`, `client_id` | + `ANNULATION_VENTE` |

- La migration V5 exigée par l'addendum (`ALTER TYPE ... ADD VALUE 'ANNULATION_VENTE'`, colonnes `statut`/`client_id`) **n'existe pas** (V1–V4 seulement).
- GS-DATA pose sa propre règle : « Toute divergence avec les migrations Flyway réellement appliquées est un bug de documentation à corriger immédiatement » et « si ce diagramme diverge de V1__init_schema.sql, **ce fichier a tort** » — or il documente l'état cible non appliqué. **Auto-contradiction de sa propre gouvernance.**
- Divergence annexe : GS-DATA dit `VENTE.caissier_id`, V1 dit `vente.utilisateur_id`.
- `notification_alerte.type_alerte` : V1 = `STOCK_BAS, RUPTURE` ; GS-DATA = `STOCK_BAS` seul ; l'addendum exige en plus `ECART_STOCK_DETECTE`. Trois états.

### M2 — Token de reset : double implémentation, triple écriture

- Schéma V1 : colonnes `utilisateur.token_reset` + `token_reset_expiry` (présentes, **mortes**).
- GS-DATA : les documente comme actuelles.
- Code (US-011/012) : tokens stockés **dans Redis** (`reset:{token}`), jamais en base.
→ Trois sources décrivent trois réalités ; les colonnes DB sont des vestiges.

### M3 — US-083 « Terminé » avec un critère d'acceptation non livré

- Backlog US-083 (CA) : « une `NotificationAlerte` de sécurité est créée pour l'utilisateur (email + in-app) » lors d'un rejeu détecté.
- Code : publication de `RefreshTokenReuseDetectedEvent` — **aucun listener n'existe** (vérifié par grep) → aucune NotificationAlerte, aucun email.
- Le ledger marque US-083 ✅ ; un critère d'acceptation du backlog n'est pas rempli.

### M4 — GS-RACI contredit KICKOFF sur l'équipe

- GS-RACI (§2) : « aucun designer ni développeur frontend n'est identifié — risque bloquant ».
- KICKOFF (même mois) : équipe Ulrich / Stephan / Siko, avec Siko designer — et un frontend complet a été construit depuis (UI kit, 15 composants, 7 écrans).
→ Deux documents de pilotage du même mois décrivent des réalités opposées.

### M5 — Le backlog frontend recommande le contraire de ce qui est livré

- US-F006 : « Token stocké de façon sécurisée (**httpOnly cookie recommandé plutôt que localStorage**) ».
- Livré (`api-client.ts`) : access et refresh token dans `localStorage` — surface XSS assumée mais non documentée comme décision.

### M6 — Incohérences chiffrées entre les backlogs

- Frontend : `GS-FRONTEND-BACKLOG` §14 totalise **246 SP** (somme vérifiable de son propre tableau) ; `GS-RACI` et `KICKOFF` disent **237 SP**.
- Backend : `implementation.md` annonce « 11 sur 86 » US terminées en listant 14 US faites dans son propre tableau.
- Statuts US : `implementation.md` marque US-009→013 « PR en attente » ; `progress-ledger` les marque mergées.

### M7 — `guideconfiguration.md` mélange `main` et `master`

- §1 : « mergées dans `main` » (correct). §2.1 : « Protection de la branche `master` » — la branche `master` **n'existe pas** (convention : `main` uniquement, pas de `develop`).

### M8 — Le diagramme d'authentification diverge du code réel

- Diagramme 02 : email → compte actif → groupe actif → **rate limit** → mot de passe.
- Code (`AuthServiceImpl.login`) : email → **mot de passe** → compte actif → groupe actif.
- Le diagramme loggue « `auth.login_failed` » (événement structuré) — n'existe pas (logs texte brut, US-086 non livrée).

---

# PARTIE 2 — TECHNOLOGIE (Technology)

> Vision Ingénieur Logiciel Principal : dette technique, risques de rupture, faits vérifiés par exécution.

## 2.1 État réel vérifié (le 7 septembre 2026)

| Élément | Fait vérifié |
|---|---|
| Build + tests (working tree complet) | **109 tests, 0 échec, 0 erreur** (26 shared + 83 auth), exécuté sans PostgreSQL ni Redis démarrés (suite 100 % mocks) |
| HEAD seul (dernier commit `07cfdce` « Fail-closed ciblé ») | **Non compilable** : 4 fichiers commités référencent `RedisHealthTracker`, classe **untracked** |
| Travail non commité | `RedisHealthTracker.java` + 3 fichiers de test (`RedisHealthTrackerTest`, `JwtAuthenticationFilterTest`, `RateLimitFilterTest`) |
| Modules | 3 sur 12 avec du code (shared 16 fichiers, auth 37, bootstrap 2) ; 9 stubs vides |
| Frontend | React 18 + TS + Vite : 7 écrans auth, 15 composants UI, guards RBAC, client Axios avec refresh |

## 2.2 Risques de rupture (code)

### R1 — HEAD non compilable
`git grep RedisHealthTracker 07cfdce` → référencé dans 4 fichiers commités ; `git status` → classe untracked. Un clone/checkout de HEAD échoue à compiler ; la CI sur ce commit serait rouge. Il manque un commit (ou amend) incluant la classe + les tests.

### R2 — Secrets JWT par défaut invalides (vérifié par exécution du decodeur jjwt 0.12.6)

| Secret par défaut | Résultat `Decoders.BASE64.decode` + `Keys.hmacShaKeyFor` |
|---|---|
| `dev-secret-key-that-is-at-least-256-bits-long-for-hs256` (docker-compose) | ❌ `DecodingException` |
| `une-cle-tres-longue-dau-moins-256-bits-pour-hs256` (.env.example) | ❌ `DecodingException` |
| `defaultsecretkeythatshouldbereplacedwithastrongone` (application.yml) | ✅ 37 octets |
| `test-secret-key-...-test` (profil test) | ❌ `DecodingException` |

Conséquence : suivre le README (`cp .env.example .env` puis `docker compose up`) produit une app qui démarre mais échoue sur **toute** opération token. Les tests passent car `JwtTokenProvider` y est mocké.

### R3 — Couverture US-085 incomplète sur `change-password`
Le fail-closed/best-effort couvre login, refresh, logout, forgot-password, reset-password (« comportement inchangé », acté). `changePassword()` fait `redisTemplate.delete()` **sans try/catch** → Redis down = 500 non protégé. L'US-085 **ne mentionne pas** `change-password` → trou de spécification.

### R4 — Course sur le compteur rate limit
`RateLimitFilter.isExceeded()` : séquence non atomique `GET` puis `INCR` (TOCTOU) → deux requêtes concurrentes peuvent dépasser le seuil. Correctif classique : `INCR` + `EXPIRE` atomique (Lua) ou `setIfAbsent` + incrément.

### R5 — Format d'erreur incohérent
Le filtre écrit du JSON brut `{"errorCode":"SEC_004"...}` / `{"errorCode":"AUTH_429"...}` — les **codes sont corrects** (vérifié : `SEC_STORE_UNAVAILABLE = "SEC_004"`, `AUTH_RATE_LIMIT = "AUTH_429"` dans `ErrorCode`), mais le **format contourne la convention RFC 7807** (`ProblemResponse`) du `GlobalExceptionHandler`. Deux formats pour la même API.

### R6 — Modèle de session mono-appareil (question métier, pas un bug)
Clé Redis `refresh:{userId}` — un second login écrase la famille du premier ; le premier appareil qui refresh déclenche la détection de rejeu → révocation totale → l'appareil 2 est déconnecté aussi.

## 2.3 Dette technique de la documentation (vérifiée)

### D1 — Le gate de couverture ≥ 80 % n'existe pas dans le build
`strategie_test.md` (×3) et `implementation.md` l'annoncent ; le pom parent n'a que `prepare-agent` + `report` JaCoCo, **aucune exécution `check`** ; `qualitygate.wait` retiré de la CI. Un module à 0 % passerait la CI.

### D2 — Le CDCT embarque des snippets de code périmés ou inexistants
- §28.1 : `RateLimitFilter` montré comme `OncePerRequestFilter` avec `MAX_ATTEMPTS = 5` en dur — le code réel est configurable, `implements Filter`, fail-closed ciblé (ADR-005).
- §29 : `CodeGeneratorService` présenté comme fichier existant (« shared/service/... ») — **n'existe pas** ; son algorithme `countBy... + 1` est une séquence par comptage → race condition et réutilisation de codes à l'implémentation.
- §23.3 : migrations embarquées avec `ANNULATION_VENTE`/`statut`/`client_id` — non appliquées en base (voir M1).

### D3 — Le tableau OWASP du CDCT (§28) affirme des contrôles inexistants
« A06 : OWASP dans la CI » — **retiré** (401 Sonatype API, implementation.md US-004). « A08 : SBOM en CI, dépendances hashées » — aucun SBOM dans les workflows, aucun checksum dans le pom. « A09 : logs JSON structurés, MDC » — US-086 non livrée, logs texte brut.

### D4 — Erreurs factuelles de chemins et de compteurs
- `strategie_test.md` §2.7 : JAR exécutable pointé vers `stockmaster-shared/target/...` — **faux**, le module exécutable est `stockmaster-bootstrap` (`shared` est une library, `<skip>true</skip>`).
- `A_JIRA_ET_GIT_FLOW.md` et `implementation.md` : chemins `document/BACKLOG_...`, `document/CDCT_...`, `document/A_JIRA_ET_GIT_FLOW.md`, `document/strategie_test.md` — tous dans des sous-dossiers depuis juillet.
- Compteurs : « 90 tests (18 + 72) » annoncés à 3 endroits ; réalité mesurée **109 (26 + 83)**. `implementation.md` daté du 16 juin 2026 (3 mois de retard), se contredit sur US-083 (terminé vs non commencé).

### D5 — La règle de maintenance documentaire n'est pas suivie
`A_JIRA_ET_GIT_FLOW` étape 8 impose la mise à jour d'`implementation.md` à chaque US — non fait depuis juin. La « seule source de vérité » (GS-DATA) et le journal (implementation.md) sont tous deux en retard sur le schéma et le code.

### D6 — Divers
- `ArchUnit` déclaré dans le pom, **aucun test ArchUnit** (la règle d'isolation des couches n'est pas mécanisée).
- Fichier `nul` (0 octet, tracké) à la racine ; ligne `[TEMPLATE]` en tête de `.env.example` (non valide pour un .env).
- `strategie_test.md` §1.2 exige PostgreSQL+Redis pour les tests — la suite actuelle (109 tests) est 100 % mocks.

---

# PARTIE 3 — INCOHÉRENCES, QUESTIONS & REFACTORISATIONS SÉCURISÉES

> Règle : rien n'est corrigé tant qu'une question n'est pas tranchée par le propriétaire du projet (zéro supposition).

## 3.1 Synthèse des incohérences (A ↔ B)

| # | Fichier A | Fichier B | Divergence |
|---|---|---|---|
| I1 | GS-DATA-2026-01 (cible) | `V1__init_schema.sql` (réel) | `vente.statut`/`client_id`, `ANNULATION_VENTE` absents du schéma réel |
| I2 | CDCT §23.3 (cible) | `V1__init_schema.sql` (réel) | Idem — le CDCT présente les migrations comme appliquées |
| I3 | GS-DATA (token_reset en DB) | Code US-011/012 (Redis) | Double implémentation ; colonnes mortes |
| I4 | Backlog US-083 (NotificationAlerte) | Code (événement sans listener) | CA non livré |
| I5 | GS-RACI (pas de designer/frontend) | KICKOFF + repo (Siko, frontend livré) | Équipe contradictoire |
| I6 | US-F006 (httpOnly cookie) | `api-client.ts` (localStorage) | Recommandation inverse de la livraison |
| I7 | Frontend backlog 246 SP | RACI/KICKOFF 237 SP | Totaux contradictoires |
| I8 | implementation.md (US-009→013 en PR) | progress-ledger (mergées) | Statuts contradictoires |
| I9 | CDCT §28 (OWASP/SBOM/logs JSON) | CI réelle + US-086 ❌ | Contrôles annoncés inexistants |
| I10 | strategie_test (90 tests, gate 80 %) | Mesure réelle (109 tests, aucun gate) | Compteurs et gate faux |
| I11 | guideconfiguration §2.1 (`master`) | Repo (`main` uniquement) | Branche inexistante |
| I12 | application.yml/.env.example/docker-compose (secrets non-base64) | `JwtTokenProvider` (base64 requis) | App non fonctionnelle sans .env correct |

## 3.2 Questions ouvertes (décisions requises)

1. **Q-SCHEMA** : GS-DATA doit-il documenter le schéma **appliqué** (sa propre règle) ou l'état **cible** ? Faut-il créer la migration **V5** dès maintenant (ANNULATION_VENTE, `statut`, `client_id`, `ECART_STOCK_DETECTE`) ou attendre les US-064/064b/067 (ce que prévoit l'addendum) ?
2. **Q-CHANGE-PASSWORD** : comportement voulu si Redis est down — 500 actuel, best-effort (comme logout), ou fail-closed ?
3. **Q-US083-CA** : le critère NotificationAlerte/email est-il reporté au module notification, ou faut-il écrire une `NotificationAlerte` dès le rejeu ?
4. **Q-TOKENS-FRONTEND** : localStorage est-il une décision assumée ? Faut-il les ADR manquants (ADR-F01 stack, ADR stockage tokens) ?
5. **Q-NUMÉROS** : frontend = 246 ou 237 SP ? (la somme vérifiable du backlog est 246)
6. **Q-STATUTS-DOCS** : les documents « Proposé » (GS-DATA, GS-IA, GS-SEQ, GS-PLAN, GS-RACI, backlogs design/frontend) doivent-ils passer à « Validé », ou un workflow de validation doit-il être défini ?
7. **Q-GATE-COUVERTURE** : configurer réellement `jacoco:check` ≥ 80 % (avec exclusions DTO/enums), ou retirer la mention des docs ?
8. **Q-EQUIPE** : l'organisation réelle est-elle Ulrich/Stephan/Siko (KICKOFF) ou Ulrich seul (RACI) ?
9. **Q-SESSION** : session mono-appareil volontaire (une famille Redis par userId) ou multi-appareils (clé par device) ?
10. **Q-IP** : le rate limit par IP fait confiance à `X-Forwarded-For` — un proxy de confiance (nginx/ALB) écrase-t-il ce header en prod ?

## 3.3 Refactorisations certaines (sans ambiguïté, sans changement de comportement)

| # | Correction | Fichier(s) | Preuve |
|---|---|---|---|
| S1 | « 90 tests (18+72) » → « 109 tests (26+83) » | `strategie_test.md` (l. 178/214/224), `implementation.md` | `mvn test`, 0 échec |
| S2 | JAR exécutable → `stockmaster-bootstrap/target/` | `strategie_test.md` §2.7 | pom : `shared` a `<skip>true</skip>` sur repackage |
| S3 | Chemins `document/` → sous-dossiers réels | `A_JIRA_ET_GIT_FLOW.md`, `implementation.md` | arborescence réelle |
| S4 | « Protection de la branche `master` » → `main` | `guideconfiguration.md` §2.1 | `git branch` : `main` uniquement |
| S5 | Reformuler « tests nécessitant PostgreSQL+Redis » → « futurs tests d'intégration » | `strategie_test.md` §1.2 | 109 tests verts sans services |
| S6 | Commiter `RedisHealthTracker.java` + 3 fichiers de test (répare HEAD non compilable) | branche GS-085 | `git grep`/`git status` |
| S7 | Supprimer `nul` (0 octet) ; retirer la ligne `[TEMPLATE]` | racine, `.env.example` | inspection |
| S8 | Remplacer les secrets JWT par défaut par de vraies clés base64 (≥ 32 octets) | `docker-compose.yml`, `.env.example` | test decodeur jjwt (R2) |

> S6 et S8 touchent au code — S6 est un commit de suivi du travail en cours GS-085 ; S8 change des valeurs par défaut de sécurité. À appliquer après accord explicite.

## 3.4 Risques de rupture — ordre de priorité

| Priorité | Risque | Conséquence si non traité |
|---|---|---|
| 🔴 P0 | HEAD non compilable (R1) | CI rouge, onboarding impossible |
| 🔴 P0 | Secrets JWT par défaut invalides (R2) | `docker compose up` sans .env → auth 100 % cassée |
| 🔴 P0 | Divergence schéma/docs vente (I1-I2, M1) | Développement d'EPIC 10 sur une base fausse |
| 🟠 P1 | Gate de couverture absent (D1) | Modules livrés sans tests, non détectés |
| 🟠 P1 | Race rate limit (R4) | Brute force possible au-delà du seuil |
| 🟠 P1 | `change-password` hors US-085 (R3) | 500 non protégé sur incident Redis |
| 🟡 P2 | Compteurs/statuts/équipe contradictoires (I4-I11) | Décisions prises sur des bases fausses |

---

*Document généré à partir de l'audit des 7 septembre 2026 — deux passes : analyse du code (backend + frontend) puis analyse exhaustive du dossier `document/`. Toutes les affirmations ont été vérifiées par lecture ou exécution ; les points non tranchés sont en §3.2.*