# RÉFÉRENTIEL — Partie 14 : Workflow, CI, environnements

### `GS-REF-2026-01 §14` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **🔍 Dérivée — l'autorité est `A_JIRA_ET_GIT_FLOW.md` + `strategie_test.md` + `guideconfiguration.md`**

> **Règle (DEC-001) :** cette partie consolide les règles de workflow. Les fichiers détaillés restent les références opérationnelles.

## 14.1 Workflow git (DEC-026)

- **`main` seule** + branches `feature/GS-XXX-*` + PR obligatoire avec revue. **Pas de `develop`.**
- Branche par défaut : `main` (corriger `origin/HEAD` si besoin : `git remote set-head origin -a`).
- Convention de commit : `feat|fix|docs|refactor|test(GS-XXX): description`.

## 14.2 Workflow par US (11 étapes — `A_JIRA_ET_GIT_FLOW.md`)

Analyse → Questionnement → Plan (attendre validation) → Branche → TDD (tests d'abord) → Implémenter → Tester → **Mettre à jour les documents** → Commit → Validation → Push/PR.

**Étape 8 (mise à jour docs) — documents concernés :**
- `progress-ledger.md` (statut de l'US) — **journal unique** (remplace `implementation.md`).
- `strategie_test.md` (compteurs de tests).
- `test_postman.md` + `postman_collection.json` (si API changée).
- `referentiel/06-modele-donnees.md` (si migration) — **dans la même PR**.
- `A_JIRA_ET_GIT_FLOW.md` (si workflow modifié).

## 14.3 CI / qualité

- CI : compilation → tests → JaCoCo → SonarCloud → build JAR (`ci-backend.yml`).
- **Gate de couverture ≥ 80 % : à activer réellement** (item C-14 de la checklist) ou retirer les mentions.
- Tests : la suite unitaire actuelle est **100 % mocks** (109 tests) ; les tests d'intégration (PostgreSQL/Redis) sont **futurs** (`DEC-031` C-09).
- **Toute PR touchant au schéma** contient sa migration + la mise à jour de `referentiel/06-modele-donnees.md` dans la même PR.

## 14.4 Environnements

| Env | Base | Notes |
|---|---|---|
| dev | local | `ddl-auto=validate` |
| test | CI | mocks + (futur) services Docker |
| prod | PostgreSQL/Redis/MinIO | `ddl-auto=none`, `clean-disabled: true` |

- `TZ=Africa/Douala` (`DEC-021`) ; secrets JWT base64 ; proxy de confiance (`DEC-041`).

---

*Sources : `DEC-001`, `DEC-021`, `DEC-026`, `DEC-031`, `A_JIRA_ET_GIT_FLOW.md`, `strategie_test.md`, `guideconfiguration.md`.*