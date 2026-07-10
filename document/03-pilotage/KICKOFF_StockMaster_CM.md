# StockMaster CM — Kickoff Projet
### Équipe : Ulrich (Fullstack + DevOps) · Stephan (Fullstack) · Siko (Frontend + Design)
### Démarrage : demain | Repo : github.com/ulruchdev/gestion_de_stok_multifiliale (monorepo)

---

## 1. Décision d'organisation (finale, pas de débat)

**Un seul repo GitHub. Le frontend se crée dans un dossier `frontend/` à la racine, à côté des modules backend existants.** Aucune restructuration du backend (les 11 `stockmaster-*` restent où ils sont) — on ne perd pas de temps à déplacer ce qui marche déjà.

**Tous les documents `.md` vivent dans `document/`, réorganisés en sous-dossiers.** Le chat (moi) sert à produire/corriger les docs, jamais à les stocker — la version dans le repo fait foi, un point c'est tout.

### Structure cible du repo (état demain matin, après le PR de mise en ordre)

```
gestion_de_stok_multifiliale/
├── document/
│   ├── 00-fonctionnel/
│   │   ├── analyse_fonctionnelle_stockmaster_cm.docx      (GS-CDA-2026-01)
│   │   └── GS-CDA-2026-02_addendum_decisions_validees.md
│   ├── 01-architecture/
│   │   ├── CDCT_StockMaster_CM_Complet_Sections22-30.md
│   │   ├── GS-DATA-2026-01_modele_donnees.md
│   │   ├── GS-IA-2026-01_architecture_information.md
│   │   └── GS-SEQ-2026-01_diagrammes_sequence.md
│   ├── 02-backlogs/
│   │   ├── BACKLOG_StockMaster_CM.md              (v1.1 — remplace la v1.0 actuelle du repo)
│   │   ├── GS-DESIGN-BACKLOG-2026-01.md
│   │   └── GS-FRONTEND-BACKLOG-2026-01.md
│   ├── 03-pilotage/
│   │   ├── GS-RACI-2026-01_matrice_livraison.md
│   │   ├── GS-PLAN-2026-01_planning_projet_reorganise.md
│   │   ├── KICKOFF_StockMaster_CM.md               (ce fichier)
│   │   ├── A_JIRA_ET_GIT_FLOW.md                   (déjà existant — vérifier qu'il parle bien de StockMaster, pas d'un autre projet)
│   │   └── strategie_test.md                        (déjà existant)
│   ├── implementation.md                            (déjà existant — journal traçabilité, à continuer)
│   ├── guideconfiguration.md                        (déjà existant)
│   └── test_postman.md                              (déjà existant — contrat API vivant, MAJ à chaque endpoint livré)
├── stockmaster-shared/          (existant, actif)
├── stockmaster-auth/            (existant, actif)
├── stockmaster-groupe/          (stub — vide)
├── stockmaster-utilisateur/     (stub — vide)
├── stockmaster-catalogue/       (stub — vide)
├── stockmaster-tiers/           (stub — vide)
├── stockmaster-achat/           (stub — vide)
├── stockmaster-stock/           (stub — vide)
├── stockmaster-vente/           (stub — vide)
├── stockmaster-notification/    (stub — vide)
├── stockmaster-reporting/       (stub — vide)
├── frontend/                    ← NOUVEAU, créé demain par Siko (Vite + React + TS)
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.ts
│   ├── src/
│   │   ├── app/                 (routing, providers, layout par rôle)
│   │   ├── features/            (1 dossier par EPIC : auth, catalogue, achats, ventes, stock...)
│   │   ├── shared/ui/           (Button, Table, Modal, Toast — miroir des composants Figma)
│   │   ├── shared/lib/          (client Axios, utils)
│   │   └── mocks/               (MSW — contre l'OpenAPI backend, voir §4 gates)
│   └── README.md
├── .github/workflows/
│   ├── ci-backend.yml           (renommer ci.yml existant)
│   ├── ci-frontend.yml          ← NOUVEAU
│   └── cd.yml
├── pom.xml                      (parent Maven backend, inchangé)
├── docker-compose.yml
└── README.md
```

**Corrections appliquées dans la session de correction documentaire du 10/07/2026 :**
- ✅ Supprimer/remplacer `document/analyse_fonctionnelle_stockmaster_cm (1).md` — fichier CBS en trop, déjà en staged.
- ✅ ~~Renuméroter dans `BACKLOG_StockMaster_CM.md` les US sécurité `US-014-017` en `US-081-084`~~ — effectué dans la v1.1 du backlog.
- ✅ ~~Corriger `GS-SEQ-2026-01` §4 : `/api/v1/transferts` → `/api/v1/groupe/transferts`~~ — effectué.
- ✅ ~~Corriger les endpoints dans `GS-FRONTEND-BACKLOG-2026-01.md` : `/auth/register/` → `/auth/inscription/`~~ — effectué (v1.1).
- ✅ ~~Purger la contradiction dans `GS-RACI-2026-01.md` sur son propre statut~~ — effectué.

---

## 2. Qui a besoin de quel document — table de démarrage

| Document | Ulrich | Stephan | Siko |
|---|:---:|:---:|:---:|
| `BACKLOG_StockMaster_CM.md` (v1.1) | ✅ Lecture complète — son backlog | ✅ Lecture complète — il va y contribuer | Survol seulement (comprendre les endpoints à venir) |
| `CDCT_...Sections22-30.md` | ✅ Obligatoire (archi qu'il a écrite) | ✅ Obligatoire (onboarding code) | ❌ Pas nécessaire |
| `GS-DATA-2026-01_modele_donnees.md` | ✅ Obligatoire | ✅ Utile | ❌ Pas nécessaire |
| `GS-CDA-2026-02_addendum.md` | ✅ Obligatoire — 3 décisions à coder (vente non bloquante, ANNULATION_VENTE, client_id) | ✅ Utile | ❌ Pas nécessaire |
| `analyse_fonctionnelle...docx` (GS-CDA-2026-01) | Référence si doute métier | Référence si doute métier | ✅ Obligatoire — §1.4 (contraintes Cameroun) et §2 (acteurs) |
| `GS-IA-2026-01_architecture_information.md` | Pas prioritaire | ✅ Obligatoire (routing/RBAC frontend) | ✅ Obligatoire — base de CHAQUE maquette |
| `GS-SEQ-2026-01_diagrammes_sequence.md` | ✅ Référence (il l'a écrit) | ✅ Obligatoire (contrats d'erreur exacts) | ❌ Pas nécessaire |
| `GS-DESIGN-BACKLOG-2026-01.md` | ❌ | Survol | ✅ Obligatoire — son backlog, démarre demain sur EPIC-D00 |
| `GS-FRONTEND-BACKLOG-2026-01.md` (v1.1) | ❌ | ✅ Obligatoire — son futur backlog | ✅ Obligatoire — où atterrit son design |
| `GS-RACI-2026-01_matrice_livraison.md` | ✅ Obligatoire — les portes G1-G7 | ✅ Obligatoire | ✅ Obligatoire |
| `GS-PLAN-2026-01_planning_projet.md` | Contexte global | Contexte global | Contexte global |
| Ce fichier (`KICKOFF`) | ✅ Tous | ✅ Tous | ✅ Tous |

**Règle simple à retenir** : chacun lit d'abord son backlog (Ulrich → BACKLOG backend, Stephan → FRONTEND-BACKLOG, Siko → DESIGN-BACKLOG), puis GS-RACI pour savoir quand il a le droit de commencer, puis GS-IA s'il touche à un écran ou une route.

---

## 3. État réel du projet (audit du repo, pas une estimation)

- **7 US mergées sur `main`** (US-001 à 006, 008) sur 80 — fondations techniques + inscription entreprise unique + login. 27 SP faits sur 285.
- **2 modules sur 11 ont du code** (`stockmaster-shared`, `stockmaster-auth`). Les 9 autres sont des stubs vides.
- **5 branches déjà codées mais pas mergées** : `GS-007` (inscription groupe), `GS-009` à `GS-013` (refresh, logout, forgot/reset/change password) — soit 17 SP déjà écrits, à ne pas refaire, juste à merger.
- **Design : 0%.** **Frontend : 0%.**
- CI (build + tests + SonarCloud + JAR) fonctionne déjà sur le backend.

### Reste à faire

| Lot | SP | Qui |
|---|---|---|
| Merger les 5 PR en attente | 17 (review seulement) | Ulrich, jour 1 |
| Sécurité durcissement (US-081-084) | 14 | Ulrich |
| Backend EPIC 3 à 13 | ~180 | Ulrich + Stephan |
| Design (EPIC-D00 à D10) | 154 | Siko |
| Frontend (EPIC-F00 à F10) | 237 | Siko + Stephan (en renfort) |

---

## 4. Planning complet — Sprints de 2 semaines, à partir de demain

**Règle de gates rappelée (GS-RACI)** : Siko peut commencer le design dès demain (G1 rempli — le fonctionnel est figé). Le frontend peut coder contre un mock dès qu'une maquette est validée (G3), sans attendre l'endpoint réel (G4) — voir stratégie `mocks/` en §1.

| Sprint | Dates indicatives | Ulrich | Stephan | Siko |
|---|---|---|---|---|
| **Semaine 0** *(cette semaine, avant Sprint 1)* | Demain → +5j | Merge des 5 PR en attente + PR de réorganisation `document/` (§1) + corrige le fichier CBS mal placé + push la v1.1 du backlog | Onboarding sur le code existant (review des PR avec Ulrich) + `npm create vite@latest frontend -- --template react-ts` + config Tailwind/shadcn/ESLint/Prettier (US-F001) | **EPIC-D00 Design System (23 SP), seul** — bloquant, personne ne peut faire de maquette avant que ce soit validé |
| **Sprint 1** | +2 sem | Sécurité P0 : US-081 (RTR) + US-083 (fail-closed) | EPIC-F00 suite : client Axios (US-F004), routing + garde de rôle (US-F005), state auth (US-F006) — contre l'API auth déjà réelle (US-006/008 mergées) | Fin D00, démarre **EPIC-D01 Onboarding public (16 SP)** |
| **Sprint 2** | +2 sem | Sécurité P1 restante (US-082 Argon2id, US-084 logs) + démarre **EPIC 3 Groupe/Filiales** (US-014-017 canonique) | Layout par rôle (US-F007) avec Siko dès D01 validé, démarre intégration écrans onboarding contre mock | Finalise D01, démarre **EPIC-D08 Groupe/Filiales/Utilisateurs** (10 SP) |
| **Sprint 3** | +2 sem | Fin EPIC 3 (US-018-020, dashboard consolidé) | Intègre onboarding contre API réelle (G4 rempli), démarre EPIC-F08 | **EPIC-D03 Catalogue** (14 SP) |
| **Sprint 4** | +2 sem | **EPIC 4 Utilisateurs** (US-021-026) | EPIC-F08 (contre mock D08), renfort sur F00 restant | Fin D03, **EPIC-D04 Tiers** (9 SP) |
| **Sprint 5** | +2 sem | **EPIC 5 Catalogue** (US-027-035) | EPIC-F03 Catalogue (mock D03 → réel), F04 Tiers | **EPIC-D02 Dashboards** (14 SP) |
| **Sprint 6** | +2 sem | **EPIC 6 Tiers** (US-036-043) | F04 réel, EPIC-F02 Dashboards avec Siko | Fin D02, **EPIC-D05 Commandes Fournisseur** (13 SP) |
| **Sprint 7** | +2 sem | **EPIC 7 Commandes Fournisseur** (US-044-050, le plus gros lot — 23 SP) | EPIC-F05 (mock D05) | **Session UAT n°1 : Achats** (staging) + démarre **EPIC-D06 Commandes Client & Caisse (20 SP — flow le plus critique)** |
| **Sprint 8** | +2 sem | **EPIC 8 Stock** (US-051-055) | EPIC-F05 intégration réelle, démarre EPIC-F07 Stock (mock) | D06 fin, **EPIC-D07 Stock** (18 SP) |
| **Sprint 9** | +2 sem | **EPIC 9 Commandes Client** (US-056-060, validation stock ⭐ 8 SP) | EPIC-F07 (mock D07), avec Siko sur F06 dès D06 validé | Renfort sur **EPIC-F06 Commandes Client & Vente Directe (30 SP — le plus gros lot frontend)** avec Stephan |
| **Sprint 10** | +2 sem | Fin EPIC 9 (facture PDF) + **EPIC 10 Vente Directe** (US-064-065) | F06 suite (le plus gros morceau, priorité absolue des deux) | F06 avec Stephan, démarre **EPIC-D09 Statistiques** (9 SP) |
| **Sprint 11** | +2 sem | Fin EPIC 10 (annulation vente) + **EPIC 11 Transferts** (US-068-070) | F06 fin, F07 intégration réelle | D09 fin, renfort F07/F08 |
| **Sprint 12** | +2 sem | **EPIC 12 Notifications** (US-071-075) | EPIC-F09 Statistiques (mock D09) | **Session UAT n°2 : Ventes/Caisse** (le cœur du produit) |
| **Sprint 13** | +2 sem | **EPIC 13 Statistiques** (US-076-080) | F09 intégration réelle, F02 finalisé | Polish UI, tests responsive mobile réel (contexte camerounais, réseau 3G) |
| **Sprint 14** | +2 sem | Procédure UAT formelle + RGPD (GS-PLAN Phase 1/3) + hardening final | Tests de bout en bout, corrections bugs UAT | Corrections UI post-UAT |
| **Sprint 15** | +2 sem | **UAT globale + préparation release prod** | idem | idem — EPIC-D10/F10 Back-office Super Admin si temps restant, sinon reporté V2 |

**~16 sprints ≈ 7-8 mois** avec cette équipe de 3, en démarrant demain.

---

## 5. Jour 1 (demain matin) — actions concrètes par personne

**Ulrich :**
1. Ouvre les 5 PR en attente (`GS-007`, `GS-009` à `GS-013`), review et merge (ou corrige si conflits) — avant tout autre travail.
2. Crée la branche `docs/reorganisation-2026-01`, applique la structure `document/` du §1, pousse la v1.1 du backlog, corrige le fichier CBS mal placé, corrige l'endpoint transfert dans GS-SEQ.
3. Renomme le workflow CI existant en `ci-backend.yml`.

**Stephan :**
1. Clone le repo, lit `BACKLOG_StockMaster_CM.md` (v1.1) + `GS-FRONTEND-BACKLOG-2026-01.md` + `GS-IA-2026-01`.
2. Pair-review avec Ulrich sur les 5 PR (onboarding sur le code existant).
3. En fin de journée : `npm create vite@latest frontend -- --template react-ts` dans le repo, premier commit `chore(GS-F001): init frontend project`.

**Siko :**
1. Lit `GS-DESIGN-BACKLOG-2026-01.md`, `GS-IA-2026-01`, et §1.4/§2 de l'analyse fonctionnelle.
2. Ouvre Figma, crée le fichier projet, démarre US-D001 (palette couleurs) — premier livrable de EPIC-D00.
3. Ne touche à aucun code avant que D00 soit validé.

**Tous :** lire `GS-RACI-2026-01` en entier (15 min de lecture, évite 90% des blocages de coordination plus tard).
