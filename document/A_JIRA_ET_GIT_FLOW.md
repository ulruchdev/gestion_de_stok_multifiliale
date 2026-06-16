# 🔄 JIRA & GIT FLOW — StockMaster CM

> **Ce document définit le workflow exact du projet StockMaster CM. Il doit être appliqué strictement pour chaque User Story.**
> **Référence :** GS-BACKLOG-2026-01
> **Version :** 2.0 — Juin 2026

---

## 📋 Workflow complet — Les 11 étapes obligatoires

Chaque User Story doit suivre **exactement** ce workflow. Aucune étape ne doit être sautée.

```
┌─────────────────────────────────────────────────────────────┐
│ ÉTAPE 1  — ANALYSE                                           │
│ Analyser les documents, le backlog, la codebase existante    │
│ Comprendre les critères d'acceptation et les impacts         │
│ Identifier les dépendances et les prérequis                  │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 2  — QUESTIONNEMENT                                    │
│ Si ambiguïté ou contradiction → demander clarification       │
│ Ne jamais présumer d'une règle non documentée                │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 3  — PLAN                                              │
│ Présenter un plan d'implémentation détaillé                  │
│ Inclure : fichiers modifiés, logique métier, tests prévus    │
│ Suggérer des améliorations si pertinentes                    │
│ ⏸️ ATTENDRE LA VALIDATION AVANT DE CODER                    │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 4  — PRÉPARATION DE LA BRANCHE                         │
│ git checkout main                                             │
│ git pull origin main                                          │
│ git checkout -b feature/GS-XXX-nom-court                     │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 5  — TDD — ÉCRIRE LES TESTS D'ABORD                    │
│ Écrire les tests unitaires (service + controller)             │
│ Un test = un cas (succès + échec)                             │
│ Compiler et voir les tests échouer (RED)                      │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 6  — IMPLÉMENTER                                        │
│ Implémenter le code minimum pour faire passer les tests       │
│ Commit après chaque étape stable (voir règles de commit)      │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 7  — TESTER                                             │
│ mvn test -pl stockmaster-auth (ou module concerné)            │
│ Vérifier : BUILD SUCCESS, 0 failures, 0 errors                │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 8  — METTRE À JOUR LES DOCUMENTS                        │
│ implementation.md — ajouter la section de l'US                │
│ strategie_test.md — mettre à jour les comptes                 │
│ test_postman.md / postman_collection.json — si API changée    │
│ A_JIRA_ET_GIT_FLOW.md — si workflow modifié                  │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 9  — COMMIT FINAL                                       │
│ git add <fichiers>                                            │
│ git commit -m "feat(GS-XXX): description concise"             │
│ Un commit = une chose cohérente                               │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 10 — VALIDATION AVANT PUSH                              │
│ Fournir les commandes de test manuel à l'utilisateur          │
│ ⏸️ ATTENDRE LA VALIDATION POUR PUSH                          │
├─────────────────────────────────────────────────────────────┤
│ ÉTAPE 11 — PUSH (sur validation utilisateur uniquement)       │
│ git push origin feature/GS-XXX                                │
│ L'utilisateur crée lui-même la PR vers main                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🌿 Git — Modèle de branches

### Structure des branches

```
main ───────────────────────────────────────────── (intégration DEV — tout merge ici)
  └── feature/GS-XXX-nom-court ────────────────── (nouvelle fonctionnalité)
  └── fix/GS-XXX-nom-court ───────────────────── (correction de bug)
  └── docs/GS-XXX-nom-court ──────────────────── (documentation seulement)
  └── refactor/GS-XXX-nom-court ──────────────── (refactoring sans changement fonctionnel)
```

**Pas de branche `develop`.** Tout merge de feature va directement vers `main`.

### Convention de nommage

| Type | Format | Exemple |
|---|---|---|
| Feature | `feature/GS-XXX-nom-court` | `feature/GS-012-reset-password` |
| Fix | `fix/GS-XXX-description` | `fix/GS-012-regex-password` |
| Docs | `docs/GS-XXX-description` | `docs/GS-012-update-workflow` |
| Refactor | `refactor/GS-XXX-description` | `refactor/GS-001-rename-package` |

**Règle :** Chaque branche doit être traçable à une User Story du backlog. Pas de branche sans US associée.

---

## 📝 Convention de commit — Conventional Commits

```
type(scope): description en minuscule sans point final

Types autorisés :
  feat     → nouvelle fonctionnalité ou configuration (US implémentée)
  fix      → correction de bug ou d'incohérence
  docs     → documentation seulement (implementation.md, workflow, etc.)
  refactor → refactoring sans changement fonctionnel
  test     → ajout/modification de tests
  chore    → maintenance (dépendances, CI, config)
  style    → formatage pur (pas de logique métier)

Le scope = l'ID de l'User Story (GS-XXX)

Exemples valides :
  feat(GS-012): add reset password endpoint with Redis token validation
  fix(GS-012): align password regex across all DTOs
  docs(GS-012): update implementation.md with US-012 status
  test(GS-012): add 5 tests for reset password flow
```

**Règles :**
- Un commit = **une chose cohérente** (ne pas mélanger feat + docs dans le même commit)
- Jamais de message comme "update", "fix", "wip", "save"
- Si une US a plusieurs volets → commits séparés : `feat(GS-XXX): ...`, `test(GS-XXX): ...`, `docs(GS-XXX): ...`

---

## 🔍 Analyse pré-implémentation

Avant toute implémentation, ces éléments DOIVENT être vérifiés :

### 1. Documents à analyser

| Document | Utilité |
|---|---|
| `document/BACKLOG_StockMaster_CM.md` | Critères d'acceptation, endpoint, priorité |
| `document/implementation.md` | État actuel du projet, dépendances entre US |
| `document/A_JIRA_ET_GIT_FLOW.md` | Workflow à suivre (ce document) |
| `document/strategie_test.md` | Stratégie et comptes de tests |
| `document/test_postman.md` | Endpoints documentés |
| `document/CDCT_StockMaster_CM_Complet_Sections22-30.md` | Spécifications techniques |

### 2. Codebase à inspecter

- Les fichiers du module concerné (`stockmaster-auth/`, `stockmaster-groupe/`, etc.)
- Les DTOs existants pour respecter les conventions
- Les interfaces service pour la signature des méthodes
- Les tests existants pour le pattern de test
- La configuration de sécurité (`SecurityConfig.java`)
- Le rate limiting (`RateLimitFilter.java`)

### 3. Questions à se poser

- [ ] L'US a-t-elle des dépendances non résolues ?
- [ ] L'endpoint est-il déjà dans `SecurityConfig` en `permitAll()` ?
- [ ] Le rate limiting est-il nécessaire ?
- [ ] Quels sont les impacts sur les documents existants ?
- [ ] Y a-t-il des incohérences entre le backlog et la codebase ?

---

## 🧪 TDD — Test Driven Development

**Les tests doivent être écrits AVANT l'implémentation.**

### Structure des tests

```
Tests service (AuthServiceImplTest) :
  @Nested @DisplayName("US-XXX — Nom")
  class NomUS {
    @Test void shouldXxxWhenYyy()  // ✅ Cas nominal
    @Test void shouldThrowWhenZzz() // ❌ Cas d'erreur
  }

Tests contrôleur (AuthControllerTest) :
  @Nested @DisplayName("POST /api/v1/auth/xxx")
  class NomEndpoint {
    @Test void shouldReturn200WhenValid()     // ✅ 200 OK
    @Test void shouldReturn400WhenInvalid()   // ❌ 400 validation
    @Test void shouldReturnXxxWhenServiceError() // ❌ Erreur métier
  }
```

### Règles de test

- Chaque méthode testée doit avoir au moins : 1 test succès + 1 test échec
- Utiliser `@DisplayName` avec émoji ✅/❌
- Ne pas mocker ce qui ne peut pas l'être (BD, Redis via `StringRedisTemplate` oui)
- Vérifier les interactions : `verify(mock).method()`
- Vérifier les codes d'erreur : `hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_XXX)`

---

## 📄 Mise à jour des documents

Toute modification d'API ou de comportement DOIT être répercutée dans ces documents :

| Document | Quand ? |
|---|---|
| `document/implementation.md` | ✅ TOUJOURS — ajouter/modifier la section de l'US |
| `document/strategie_test.md` | ✅ Si comptes de tests changent |
| `document/postman_collection.json` | ✅ Si nouvel endpoint ou modification d'endpoint |
| `document/test_postman.md` | ✅ Si nouvel endpoint ou modification d'endpoint |
| `document/A_JIRA_ET_GIT_FLOW.md` | ✅ Si le workflow évolue |

---

## 🔬 Tests manuels avant push

Avant de pousser, fournir à l'utilisateur les commandes de validation :

```bash
# Compilation
mvn compile -pl stockmaster-auth

# Tests unitaires
mvn test -pl stockmaster-auth

# Vérification rapide
mvn test -pl stockmaster-auth 2>&1 | grep -E "Tests run:|BUILD"
```

⏸️ **Attendre la validation utilisateur avant d'exécuter `git push`.**

---

## 🚫 Ce qu'il ne faut JAMAIS faire

```
❌ Pusher sans validation explicite de l'utilisateur
❌ Pusher directement sur main (ni git push --force)
❌ Commiter des credentials (mots de passe, tokens, clés API)
❌ Mélanger les responsabilités des branches (docs US-010 sur branche US-011)
❌ Changer de branche avec des modifications non commitées (toujours commit avant switch)
❌ Utiliser git stash pour transporter du code entre branches (commit → rebase/merge)
❌ Ouvrir une PR avec des conflits non résolus
❌ Faire des commits avec des messages vagues ("update", "fix", "wip")
❌ Implémenter sans avoir écrit les tests d'abord
❌ Sauter l'étape d'analyse des documents avant d'implémenter
❌ Ignorer les dépendances entre US (vérifier le backlog avant chaque US)
❌ Supposer qu'une bibliothèque/fonctionnalité existe sans vérifier dans la codebase
```

---

## 📋 Définition of Done (DoD)

Une US est considérée comme DONE quand :

- [ ] Analyse pré-implémentation effectuée et validée
- [ ] Tests écrits (RED) → Implémentation (GREEN)
- [ ] `mvn test -pl stockmaster-auth` → BUILD SUCCESS (0 failure, 0 error)
- [ ] Tous les documents impactés mis à jour
- [ ] Commit effectué avec message conventionnel
- [ ] Push effectué après validation utilisateur
- [ ] PR créée par l'utilisateur vers `main`

---

## 📎 Résumé visuel du workflow

```
1. ANALYSE des docs + codebase
2. QUESTIONNEMENT si ambiguïté
3. PLAN présenté → VALIDATION utilisateur
4. git checkout main && git pull
5. git checkout -b feature/GS-XXX
6. TDD : TESTS d'abord (RED)
7. IMPLÉMENTATION (GREEN)
8. TESTS finaux
9. DOCUMENTS mis à jour
10. COMMIT final
11. TESTS MANUELS fournis → VALIDATION push
12. git push (après validation)
13. PR créée par l'utilisateur
```
