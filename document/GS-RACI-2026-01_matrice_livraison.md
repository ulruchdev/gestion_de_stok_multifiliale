# Matrice RACI & Séquencement des Livraisons — StockMaster CM
### Référence : GS-RACI-2026-01 | Version : 1.0 | Date : Juillet 2026 | Statut : Proposé
### Documents parents : GS-CDA-2026-01/02, BACKLOG_StockMaster_CM.md (backend), GS-DESIGN-BACKLOG-2026-01, GS-FRONTEND-BACKLOG-2026-01, GS-IA-2026-01, GS-PLAN-2026-01

---

## 1. Objet

Ce document répond à une question qu'aucun des documents précédents ne tranchait : **qui doit livrer quoi, avant qui**, entre Design, Backend, Frontend, DevOps, QA et BA (analyse métier). C'était la pièce manquante identifiée dans `GS-FRONTEND-BACKLOG-2026-01` §17 et `GS-DESIGN-BACKLOG-2026-01` §14 — désormais produite et versionnée sous cette référence.

**Constat de départ à ne pas ignorer :** à ce jour, aucun designer ni développeur frontend n'est identifié sur le projet (`GS-DESIGN-BACKLOG-2026-01` §16, risque #1) — le projet a été porté en solo côté BA + Backend. Ce document formalise donc deux choses différentes à la fois :
1. **Une logique de séquencement** (quel travail bloque quel autre travail) — valable quel que soit le nombre de personnes.
2. **Une répartition par rôle** — utile dès qu'une deuxième personne (designer, développeur frontend freelance, stagiaire QA) rejoint le projet, pour éviter les ambiguïtés de responsabilité au moment de l'onboarding.

Tant que le projet reste porté par une seule personne, cette matrice sert surtout de **check-list d'auto-discipline de séquencement** : ne pas coder un écran avant d'avoir validé sa maquette soi-même, ne pas livrer une US frontend avant d'avoir vérifié soi-même l'endpoint dans Postman, etc.

---

## 2. Rôles couverts (un rôle ≠ une personne)

| Rôle | Périmètre | Porté aujourd'hui par |
|---|---|---|
| **BA** (Business Analyst) | Analyse fonctionnelle, arbitrage des règles métier, rédaction CDC/US, validation des décisions (GS-CDA-2026-0x) | Ulrich |
| **Design** | Design system, wireframes, UI, prototypes cliquables (GS-DESIGN-BACKLOG) | Non identifié — risque bloquant signalé |
| **Backend** | Implémentation Spring Boot, endpoints, migrations Flyway, tests unitaires/intégration | Ulrich |
| **Frontend** | Implémentation React, intégration API, routing, état | Non identifié — risque bloquant signalé |
| **DevOps** | CI/CD, environnements Dev/Staging/Prod, monitoring | Ulrich (via `guideconfiguration.md`, GitHub Actions) |
| **QA** | Tests automatisés (Postman, JUnit), recette fonctionnelle (UAT) | Ulrich (partiel — voir GS-PLAN-2026-01, item UAT ❌) |

---

## 3. Règles de dépendance dure (gates) entre équipes

Ces règles sont des **portes bloquantes**, pas de simples recommandations — une US ne doit pas passer "En cours" si sa porte d'entrée n'est pas remplie.

| # | Porte | Condition d'entrée | Qui vérifie | Qui est bloqué si non respecté |
|---|---|---|---|---|
| G1 | BA → Design | La US fonctionnelle correspondante (GS-CDA / backlog backend) a ses critères d'acceptation figés, sans point ouvert non tranché | BA | Design |
| G2 | BA → Backend | Idem G1 | BA | Backend |
| G3 | Design → Frontend (implémentation réelle) | Maquette Figma au statut **"Validée"** (pas "en revue") avec spécification responsive + annotations Dev Mode | Design (auto-déclaré) puis BA (validation finale) | Frontend |
| G4 | Backend → Frontend (intégration réelle) | Endpoint livré, testé (tests d'intégration verts) **et documenté dans `test_postman.md`** avec un exemple de requête/réponse réel | Backend | Frontend |
| G5 | Frontend + Backend → QA (recette) | Fonctionnalité déployée sur Staging, US marquée "Terminée" côté dev des deux côtés | Backend + Frontend | QA |
| G6 | QA → DevOps (release prod) | Suite Postman verte sur Staging, UAT signée pour l'EPIC concerné (voir §6) | QA | DevOps |
| G7 | Toute équipe → DevOps (CI) | Le code ne peut pas être mergé sur `main`/`develop` sans pipeline CI vert (lint, build, tests, SonarCloud quality gate) | DevOps (automatique) | Tous |

> **Nuance sur G3/G4 : le frontend n'est pas obligé d'attendre G3 ET G4 pour commencer à coder.** Voir §5 (stratégie de parallélisation par contrat d'API mocké) — la porte dure s'applique à l'**intégration réelle et à la mise en Staging**, pas à l'écriture du code frontend en local contre des données mockées.

---

## 4. Séquencement inter-équipes par EPIC

Table de correspondance entre les 3 backlogs (Backend, Design, Frontend), avec le sprint indicatif et la porte qui s'applique. C'est la vue d'ensemble qui manquait pour piloter le projet comme un tout plutôt que 3 backlogs isolés.

| Sprint | EPIC Backend | EPIC Design | EPIC Frontend | Porte critique | Parallélisable ? |
|---|---|---|---|---|---|
| 0 | EPIC 1 — Fondations techniques | EPIC-D00 — Design System | EPIC-F00 — Fondations techniques | G3 (F00 a besoin des tokens D00 — US-F002) | Backend et Design **en parallèle** ; Frontend attend la fin de D00 pour F002/F003 uniquement (F001, F004-F006 ne dépendent pas du design) |
| 1 | EPIC 2 — Authentification & Accès | EPIC-D01 — Onboarding public | EPIC-F01 — Onboarding public | G3 + G4 | Design peut commencer dès que G1 (CDA figé) — en parallèle du Backend. Frontend commence le code contre mock dès D01 en revue, intègre réellement seulement après G3+G4 |
| 1-2 | EPIC 3 — Groupe & Filiales / EPIC 4 — Utilisateurs | EPIC-D08 — Groupe/Filiales/Utilisateurs | EPIC-F08 — Groupe/Filiales/Utilisateurs | G3 + G4 | idem |
| 2-3 | EPIC 5 — Catalogue | EPIC-D03 — Catalogue | EPIC-F03 — Catalogue | G3 + G4 | idem |
| 3 | EPIC 6 — Tiers | EPIC-D04 — Tiers | EPIC-F04 — Tiers | G3 + G4 | idem |
| 4 | EPIC 7 — Commandes Fournisseur | EPIC-D05 — Commandes Fournisseur | EPIC-F05 — Commandes Fournisseur | G3 + G4 | idem |
| 4-5 | EPIC 9 — Commandes Client / EPIC 10 — Vente Directe | EPIC-D06 — Commandes Client & Vente Directe | EPIC-F06 — Commandes Client & Vente Directe | G3 + G4 (**+ GS-SEQ-2026-01 §3 comme contrat d'erreur exact**, pas seulement le endpoint nominal) | Le flow d'erreur "stock insuffisant" (US-F061) ne peut pas être mocké fidèlement sans le contrat exact de `InsufficientStockException` (structure `articlesEnRupture[]`) — **priorité à figer ce contrat tôt**, avant même que l'écran ne soit codé, pour éviter un rework |
| 5-6 | EPIC 8 — Gestion du Stock / EPIC 11 — Transferts | EPIC-D07 — Stock | EPIC-F07 — Stock | G3 + G4 | idem — voir aussi correction d'endpoint `/api/v1/groupe/transferts` (GS-FRONTEND-BACKLOG §18) à propager dans GS-SEQ-2026-01 avant le début réel de F072 |
| — | EPIC 12 — Notifications & Alertes | (transverse — alimente D02, D07, D09) | (transverse — alimente F02, F07, F09) | G4 | Backend peut développer ce module en continu ; il n'a pas d'écran dédié, seulement des points d'intégration dans d'autres écrans |
| 2 (en parallèle) | — | EPIC-D02 — Dashboards | EPIC-F02 — Dashboards | Dépend des données réelles de EPIC 8, 9, 10, 13 | Les dashboards peuvent être **maquettés** tôt (Design) mais leur intégration réelle (Frontend) doit attendre que les modules qu'ils agrègent (stock, ventes, stats) soient au moins partiellement livrés — sinon on intègre contre des données qui n'existent pas encore |
| 7+ | EPIC 13 — Statistiques & Reporting | EPIC-D09 — Statistiques | EPIC-F09 — Statistiques | G3 + G4 | idem |
| 8+ | (transverse — RBAC déjà en place) | EPIC-D10 — Back-office Super Admin | EPIC-F10 — Back-office Super Admin | G3 + G4 | Indépendant du reste — peut être décalé sans bloquer le produit client, comme déjà noté dans les deux backlogs |

---

## 5. Stratégie de parallélisation (contrats d'API mockés)

Recommandation actée dans `GS-FRONTEND-BACKLOG-2026-01` §17 (237 SP frontend à absorber — attendre systématiquement G4 avant de commencer à coder ferait doubler le délai total du projet) :

1. **Le contrat d'API (endpoint, DTO de requête/réponse, codes d'erreur) est figé et publié dès qu'une US backend passe "En cours"**, pas seulement à la livraison — via le fichier OpenAPI généré par SpringDoc (`/v3/api-docs`), déjà en place dans le projet.
2. Le Frontend démarre le développement d'un écran dès que **G3 est rempli (design validé)**, contre un serveur de mock généré depuis ce contrat OpenAPI (ex. Prism, MSW) — sans attendre G4.
3. **G4 devient une porte d'intégration, pas de démarrage** : quand le vrai endpoint est livré et documenté dans `test_postman.md`, le Frontend bascule du mock vers l'API réelle et referme le ticket d'intégration.
4. Tout écart entre le contrat mocké et la réalité livrée (champ manquant, code d'erreur différent) est un **bug de contrat**, pas un simple bug — il doit remonter en priorité au Backend, car il signifie que le contrat a changé sans être republié.

**Condition de succès de cette stratégie :** le contrat OpenAPI doit être stable une fois une US backend démarrée. Un changement de contrat en cours de route sans re-synchronisation explicite invalide tout le travail frontend fait contre l'ancien mock — c'est le risque principal de la parallélisation, à surveiller activement (voir §7).

---

## 6. RACI par activité macro

R = Responsable (fait le travail) · A = Approbateur (valide, un seul par ligne) · C = Consulté · I = Informé

| Activité | BA | Design | Backend | Frontend | DevOps | QA |
|---|---|---|---|---|---|---|
| Rédaction analyse fonctionnelle / décisions métier (GS-CDA) | R/A | C | C | C | — | — |
| Rédaction backlog backend | R/A | — | C | — | — | C |
| Wireframes / UI / prototype (GS-DESIGN-BACKLOG) | C | R/A | — | C | — | — |
| Architecture de l'information (GS-IA) | C | R | C | C | — | — |
| Rédaction backlog frontend | C | C | C | R/A | — | C |
| Modèle de données (GS-DATA) / migrations Flyway | C | — | R/A | I | — | C |
| Diagrammes de séquence (GS-SEQ) | C | — | R/A | C | — | C |
| Implémentation endpoint backend | I | — | R/A | I | — | C |
| Documentation `test_postman.md` (contrat API réel) | — | — | R/A | I | — | C |
| Implémentation écran frontend | I | C | I | R/A | — | I |
| Tests unitaires / intégration (JUnit, Testcontainers) | — | — | R/A | — | — | C |
| Tests API automatisés (collection Postman) | — | — | C | — | — | R/A |
| Recette fonctionnelle (UAT) — voir GS-PLAN-2026-01 (❌ non formalisée) | R/A | I | C | C | — | R |
| Pipeline CI (lint, build, tests, quality gate) | — | — | C | C | R/A | C |
| Déploiement Staging | I | — | I | I | R/A | C |
| Déploiement Production (release) | A | — | C | C | R | C |
| Monitoring post-déploiement / alertes techniques | I | — | C | — | R/A | I |
| Revue de code (Pull Request) | — | — | R (pair) | R (pair) | — | — |

---

## 7. Risques de séquencement identifiés

| Risque | Impact | Mitigation |
|---|---|---|
| Aucun designer ni développeur frontend identifié à ce jour | Tout le séquencement de ce document reste théorique tant que ces ressources ne sont pas confirmées | Confirmer les ressources avant le Sprint 0 réel — sinon prioriser le développement backend seul et différer G3/G4 |
| Contrat OpenAPI qui change après le démarrage du mock frontend (§5) | Rework frontend, perte de la parallélisation | Geler le contrat dès la US backend "En cours" ; tout changement ultérieur = ticket de resynchronisation explicite, pas un simple correctif silencieux |
| UAT non formalisée (GS-PLAN-2026-01 l'a déjà signalé comme ❌) | Risque de livrer en production des écrans qui ne correspondent pas à l'usage réel (contexte terrain camerounais : réseau, mobile) sans validation finale | Ce document pose la porte G6 (UAT signée avant release) — mais la procédure UAT elle-même reste à écrire (action de suivi, non couverte ici) |
| Dashboards (F02/D02) intégrés avant que les modules sources (Stock, Ventes, Stats) soient suffisamment avancés | Intégration contre des données incomplètes, rework | Respecter l'ordre du tableau §4 : maquetter tôt, intégrer tard |
| Une seule personne porte BA + Backend + DevOps aujourd'hui | Les portes G1/G2/G7 sont auto-vérifiées par la même personne qui produit le travail — risque de biais de complaisance (on ne se bloque pas soi-même aussi strictement qu'un tiers le ferait) | Utiliser la checklist des CA du backlog comme porte objective, pas un jugement subjectif ; envisager une revue croisée même informelle (relecture à froid) avant de considérer une porte franchie |

---

## 8. Prochaines étapes

1. Confirmer si des ressources Design/Frontend/QA dédiées rejoignent le projet — si non, adapter le rythme de `GS-PLAN-2026-01` en conséquence (le développement restera séquentiel, pas parallèle, tant qu'une seule personne porte tout).
2. ✅ ~~Propager la correction d'endpoint `/api/v1/groupe/transferts` dans `GS-SEQ-2026-01` §4~~ — effectué dans la session de correction documentaire du 10/07/2026.
3. Écrire la procédure UAT (qui l'a signée, sur quel environnement, avec quel gabarit de procès-verbal) — c'est le seul item encore purement à l'état de porte (G6) sans procédure décrite.
