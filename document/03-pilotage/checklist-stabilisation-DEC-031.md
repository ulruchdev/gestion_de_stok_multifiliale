# Checklist de stabilisation — DEC-031

### Référence : `DEC-031` | Statut : lot d'implémentation | Ouverte le 7 septembre 2026

> **Règle :** chaque item = une **petite PR isolée**, sans changement de comportement, avec **preuve de sortie** vérifiable. Un item est **fermé** quand sa preuve est fournie. Cette checklist est la **seule référence de suivi** du lot (pas le journal).
> Sources : `GS-AUDIT-2026-01` (B-15), `INCOHERENCES_DOCUMENTAIRES.docx` (familles B, E, G, H), `audit-analyse-stockmaster-2026-09.md` (S1–S8, D1–D6, I7–I12).

## Bloc 1 — P0 « machine » (préalable à tout) 

| # | Item | Fichier(s) | Preuve de sortie | Source | Statut |
|---|---|---|---|---|---|
| C-01 | Commiter `RedisHealthTracker.java` + les 3 fichiers de test (JwtAuthenticationFilter, RateLimitFilter, RedisHealthTracker) | branche de travail | `mvn test -pl stockmaster-shared,stockmaster-auth` → BUILD SUCCESS, 0 échec | audit-09 R1 / S6 | ✅ 8 sept. — commit `31026e8` (tests réécrits pour matcher les constructeurs réels de `main`) |
| C-02 | Remplacer les secrets JWT par défaut par de vraies clés base64 (≥ 32 octets) : docker-compose, `.env.example`, `application.yml`, profil test | `docker-compose.yml`, `.env.example`, `backend/stockmaster-bootstrap/src/main/resources/application.yml` | decodeur jjwt OK (62 octets base64 ≥ 32) | audit-09 R2 / S8 / I12 | ✅ `e800a7e` |
| C-03 | Supprimer le fichier `nul` (0 octet) et la ligne `[TEMPLATE]` de `.env.example` | racine, `.env.example` | `git status` propre | audit-09 S7 | ✅ 8 sept. (`nul` supprimé ; `[TEMPLATE]` déjà absent) |
| C-04 | Resynchroniser `test_postman.md` + `postman_collection.json` sur l'API réelle (DTO inscription, `pm.collectionVariables`, `/refresh` avec `refreshToken`) | `document/test_postman.md`, `document/postman_collection.json` | aucun endpoint documenté absent du code | GS-AUDIT B-15 / docx B-04 | ⏳ |

## Bloc 2 — Compteurs et chemins

| # | Item | Fichier(s) | Prévoir | Source |
|---|---|---|---|---|
| C-05 | « 90 tests (18+72) » → « 109 tests (26+83) » | `document/03-pilotage/strategie_test.md` (3 occurrences) | actualisé à chaque évolution | audit-09 S1 |
| C-06 | JAR exécutable → `stockmaster-bootstrap/target/` (shared = library `<skip>true</skip>`) | `document/03-pilotage/strategie_test.md` §2.7 | commande testée | audit-09 S2 / D4 |
| C-07 | Chemins `document/…` → sous-dossiers réels (`02-backlogs/`, `01-architecture/`, `03-pilotage/`) | `A_JIRA_ET_GIT_FLOW.md`, autres docs | aucun chemin mort | audit-09 S3 / D4 |
| C-08 | « Protection de la branche `master` » → `main` | `document/guideconfiguration.md` §2.1 | `git branch` = `main` seul | audit-09 S4 / I11 |
| C-09 | Reformuler « tests nécessitant PostgreSQL+Redis » → « futurs tests d'intégration » | `document/03-pilotage/strategie_test.md` §1.2 | suite verte sans services | audit-09 S5 |
| C-10 | `git remote set-head origin -a` (pointeur `origin/HEAD` sur `main`) | dépôt local | `git remote show origin` → HEAD branch: main | DEC-026 |
| C-11 | Somme des SPR frontend : 246 (vérifiable) partout | `GS-FRONTEND-BACKLOG`, `GS-RACI`, `KICKOFF` | totaux cohérents | audit-09 I7 |
| C-12 | Statuts US : `progress-ledger.md` = journal unique ; supprimer `implementation.md` (fait le 7 sept. 2026) | `document/03-pilotage/progress-ledger.md` | aucune référence à `implementation.md` | audit-09 I8 / GS-AUDIT B-15 |

## Bloc 3 — Gouvernance et CI

| # | Item | Fichier(s) | Prévoir | Source |
|---|---|---|---|---|
| C-13 | Aligner `GS-RACI` §2/§7 sur l'équipe réelle ; reformuler ou supprimer la porte G3 Figma | `document/03-pilotage/GS-RACI-2026-01_*.md` | porte franchie ou supprimée | GS-AUDIT B-15 |
| C-14 | Activer réellement `jacoco:check` ≥ 80 % (exclusions DTO/enum) — ou retirer les mentions | `pom.xml` parent, CI, `strategie_test.md` | CI rouge sur couverture < 80 % | audit-09 D1 / Q-gate |
| C-15 | Actualiser le tableau OWASP du CDCT §28 (A06/A08/A09 réels) + CDCT §29 (CodeGeneratorService absent) | `document/01-architecture/CDCT_*.md` | tableau = contrôles réels | audit-09 D3 / D2 |
| C-16 | A_JIRA : harmoniser 11/13 étapes, corriger la numérotation dupliquée de `strategie_test.md` | `A_JIRA_ET_GIT_FLOW.md`, `strategie_test.md` | workflow lisible | GS-AUDIT B-15 |
| C-17 | Ordre des contrôles de connexion (rate limit → mot de passe → statut) + anti-énumération | `diagrams/02`, code `AuthServiceImpl` | test anti-énumération | GS-AUDIT B-12 |

## Bloc 4 — Détails métier / schéma (sans nouveau comportement)

| # | Item | Fichier(s) | Prévoir | Source |
|---|---|---|---|---|
| C-18 | Déclencheur `update_date_modification` : exclure `mouvement_stock` (immuable) | migration V5 | trigger absent sur la table journal | docx C-12 |
| C-19 | Docx/documenter l'interdiction d'usage de `mouvement_stock.supprime` | `GS-DATA`, migration V5 | colonne préservée mais interdite | docx C-11 |
| C-20 | Aligner l'index `(entreprise_id, article_id)` entre GS-DATA et V2 | `GS-DATA` §3 | vérif plan d'exécution | docx C-10 |
| C-21 | `test_postman.md` DTO inscription + `pm.*` → état réel | voir C-04 | — | docx B-04 |
| C-22 | Supprimer la branche mémo `docs/GS-REF-2026-01-referentiel` après revue | git | branche supprimée | DEC-026 |

## Suivi

| Champs | Règle |
|---|---|
| Coche | `[ ]` → `[x]` + date |
| Preuve | chemin de fichier, aka commande + sortie, lien PR |