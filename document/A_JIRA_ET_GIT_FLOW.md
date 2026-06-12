# 🔄 JIRA & GIT FLOW — CBS Project (TB-610 / TB-613 / TB-614 / TB-615)

> **Ce document reflète le workflow exact du projet CBS. À lire et appliquer tel quel pour livrer tes tickets.**

---

## 🌿 Git — Le modèle de branches du projet CBS

### La structure des branches permanentes

```
main ─────────────────────────────── (intégration DEV — tout merge ici)
  └── release-X.Y ──────────────── (branche de release, créée par le lead)
        └── hotfix-X.Y/TB-XXX ─── (fix urgent sur une release en cours)
```

**Attention** : le projet CBS n'a **pas** de branche `develop`. La branche d'intégration principale est `main`. Tout merge de feature va vers `main`.

### Les environnements et ce qui y est déployé

```
DEV      → dernier commit sur main (auto à chaque merge)
QA       → image Docker taguée release-X.Y (déclenché manuellement par le lead)
PRE-PROD → même image que QA après sign-off QA
```

### Les types de branches et leur convention de nommage

| Type | Usage | Nommage |
|---|---|---|
| `feature/*` | Nouvelle fonctionnalité | `feature/TB-613-register-image-type-fields` |
| `bugfix/*` | Bug non bloquant (prochain sprint) | `bugfix/TB-XXX` |
| `hotfix-X.Y/*` | Fix urgent sur release en cours | `hotfix-0.1/TB-XXX` |
| `docs/*` | Documentation seulement | `docs/TB-XXX` |
| `chore/*` | Maintenance, CI/CD, config | `chore/TB-XXX` |
| `refactor/*` | Refactoring sans changement fonctionnel | `refactor/TB-XXX` |

**Règle clé** : chaque branche doit être traçable à un ticket Jira. Si le travail n'a pas de ticket → créer le ticket d'abord, puis la branche.

### Workflow pas à pas pour commencer tes tickets

```bash
# 1. Toujours partir de main à jour
git checkout main
git pull origin main

# 2. Créer ta branche (une seule branche couvre TB-613 + TB-614 + TB-615 car même livrable)
git checkout -b feature/TB-610-register-document-management-fields

# 3. Travailler et commiter régulièrement
git add .
git commit -m "feat(TB-613): register fields for IMAGE.TYPE table"

# 4. Pousser ta branche
git push origin feature/TB-610-register-document-management-fields

# 5. Créer une Merge Request vers main sur GitLab
```

---

## 📝 Convention de commit messages — Conventional Commits

```
type(scope): description courte en minuscule

Types autorisés :
  feat     → nouvelle fonctionnalité ou configuration
  fix      → correction de bug
  docs     → documentation seulement
  refactor → refactoring sans changement fonctionnel
  test     → ajout/modification de tests
  chore    → maintenance (dépendances, CI...)
  style    → formatage pur (pas de logique)

Le scope = l'ID du ticket Jira

Exemples pour tes tickets :
  feat(TB-613): register fields for IMAGE.TYPE table
  feat(TB-614): register fields for DOCUMENT.IMAGE table
  feat(TB-615): register fields for DOCUMENT.UPLOAD table
```

**Règle** : un commit = une chose cohérente. Jamais "update stuff", "fix", "wip" comme message final.

---

## 🔁 Merge Request (MR) — Format officiel CBS

### Format du titre de MR

```
TB-610 - Register Fields Definition for Document Management tables
```
*(JIRA-KEY - Titre du ticket ou résumé court)*

### Template de description MR

```markdown
## Pourquoi cette MR est nécessaire ?

Ticket TB-610 (sous-tickets TB-613, TB-614, TB-615) : enregistrement des définitions
de champs pour les tables du domaine Document Management.
Ces définitions sont nécessaires pour que le système CBS valide et persiste
correctement les documents capturés via les tables IMAGE.TYPE, DOCUMENT.IMAGE
et DOCUMENT.UPLOAD.

[Lien Jira : https://pkf-cbs.atlassian.net/browse/TB-610]

## Changements apportés

- TB-613 : Ajout fichier Bruno pour enregistrement des champs IMAGE.TYPE
- TB-614 : Ajout fichier Bruno pour enregistrement des champs DOCUMENT.IMAGE
- TB-615 : Ajout fichier Bruno pour enregistrement des champs DOCUMENT.UPLOAD
- Création de la structure de dossiers Document Management dans cbs-bruno-api

## Critères d'acceptance validés

- [x] Champs présents dans field_def table du schéma image_type (TB-613)
- [x] Champs présents dans field_def table du schéma document_image (TB-614)
- [x] Champs présents dans field_def table du schéma document_upload (TB-615)
- [x] Réponses 201 obtenues pour chaque appel POST /api/v1/table-defs/{TABLE}/fields

## Screenshots / Preuves

[Coller ici les réponses JSON des 3 appels API + screenshots de vérification en base]
```

### Règles MR à respecter

- **Target branch** : `main` (jamais rien d'autre pour une feature)
- **Squash commits** avant merge (la politique du projet sur les merges vers main)
- **Minimum 1 reviewer** assigné — ne jamais merger sans review approuvée
- **Pipeline verte** avant de demander la review
- **Pas de conflits** avec la target branch
- **Screenshots ou logs** fournis dans la description
- Résoudre tous les commentaires avant de merger (ou justifier pourquoi on ne le fait pas)

### Labels GitLab à appliquer

Chaque MR doit avoir au minimum :
- 1 label **Type** : `feature`
- 1 label **Priority** : `P2-high`
- 1 label **Status** (mis à jour au fil de l'avancement) : `in-progress` → `ready-for-review`

---

## 🎫 Jira — Cycle de vie des tickets TB-610, TB-613, TB-614, TB-615

### Les statuts et quand les changer

```
TO DO → IN PROGRESS → IN REVIEW → DONE
```

| Statut | Quand ? |
|---|---|
| `TO DO` | Ticket assigné, pas encore commencé |
| `IN PROGRESS` | Tu as créé ta branche et commencé à travailler |
| `IN REVIEW` | Ta MR est ouverte et en attente de review |
| `DONE` | MR mergée, critères d'acceptance validés |

**Ordre de passage en IN PROGRESS** : TB-610 d'abord (parent), puis TB-613 → TB-614 → TB-615 dans l'ordre.

**Important** : TB-613 dit "TBD: fields definition here (TB-607)". Avant de commencer, vérifie que TB-607 a été résolu et que les définitions de champs sont disponibles. Si TB-607 n'est pas DONE, informe ton lead **immédiatement** — c'est un bloquant.

### Lier la MR au ticket Jira

Inclure l'ID du ticket dans :
- Le nom de ta branche
- Tous tes commits
- Le titre de la MR
- La description de la MR (avec le lien Jira)

---

## 💬 Communication dans l'équipe CBS

### Contacts principaux du projet

| Rôle | Qui contacter |
|---|---|
| Team Lead | Vaneck LELE |
| AQA/QA Lead | Loic YOUGA |
| BA Lead | Aurel TOUKAM |
| DevOps | Fabrice MVAH |
| Designer Lead | Syntyche DEMGNE |

### Quand tu es bloqué

1. Essaie de te débloquer seul **max 30 minutes**
2. Poste dans le canal du projet : ce que tu essaies de faire + ce que tu as essayé + le message d'erreur exact
3. Tag la bonne personne (lead technique pour le code, BA Lead pour les specs)

### Point bloquant critique pour tes tickets

TB-613 contient la mention : **"TBD: fields definition here (TB-607)"**. Cela signifie que les champs exacts à enregistrer pour IMAGE.TYPE dépendent du ticket TB-607.

→ **Avant de coder** : va dans Jira, ouvre TB-607, et vérifie si les champs sont définis.
→ Si TB-607 est `DONE` : utilise ses définitions.
→ Si TB-607 est encore `TO DO` ou `IN PROGRESS` : **bloque TB-613 et notifie ton lead aujourd'hui**.

---

## 🚫 Ce qu'il ne faut JAMAIS faire

```
❌ Pusher directement sur main (ni git push --force)
❌ Commiter des credentials (mots de passe, tokens, clés API)
❌ Merger ta propre MR sans review approuvée
❌ Laisser un ticket en IN PROGRESS plus de 3 jours sans update
❌ Ouvrir une MR avec des conflits non résolus
❌ Ouvrir une MR sans screenshots / preuves si applicable
❌ Créer une branche sans ticket Jira associé
❌ Commencer TB-614 et TB-615 avant d'avoir clarifié TB-607 pour TB-613
```

---
