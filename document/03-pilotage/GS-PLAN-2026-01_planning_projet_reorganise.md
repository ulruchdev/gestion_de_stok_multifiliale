# Planning Projet Réorganisé — StockMaster CM
### Référence : GS-PLAN-2026-01 | Version : 1.0 | Date : Juillet 2026 | Statut : Proposé

---

## Objet

Ce document reprend le programme en 5 phases que tu as fourni et l'applique concrètement à StockMaster CM : pour chaque item, on indique s'il est **✅ déjà couvert** (avec la preuve — quel document/quel fichier), **⚠️ partiellement couvert**, ou **❌ manquant**, et on propose une action concrète pour combler chaque trou. Rien n'est réinventé de zéro : on part de ce qui existe (`document/`, `stockmaster-*`) et on complète.

---

## Phase 1 — Cadrage & Stratégie

| Item | Statut | Preuve / Constat | Action proposée |
|---|---|---|---|
| Analyse des besoins client et étude de faisabilité | ✅ | GS-CDA-2026-01 §1.1-1.3 | — |
| Benchmark concurrentiel et étude du marché | ❌ | Aucune trace dans `document/` | Créer `GS-BENCH-2026-01` : comparer StockMaster CM à 2-3 alternatives réellement utilisées par les PME camerounaises aujourd'hui (Excel/cahier — le vrai concurrent selon GS-CDA §1.2 —, puis solutions payantes existantes type Odoo/Sage si accessibles localement). Objectif : justifier le prix d'abonnement et les fonctionnalités différenciantes (multi-filiales, TVA camerounaise, XAF) plutôt que de le supposer. |
| Rédaction du Cahier des Charges (CDC) fonctionnel | ✅ | GS-CDA-2026-01 (docx complet) | — |
| Spécifications fonctionnelles globales et cadrage | ✅ | GS-CDA-2026-01 §3, §10 (périmètre P0/P1/P2) | — |
| Définition des KPI (indicateurs de succès du projet) | ❌ | Aucun indicateur de succès produit défini — seuls des indicateurs techniques (coverage 80%, quality gate) existent | Définir dans un nouveau §11 de GS-CDA-2026-01 ou document séparé `GS-KPI-2026-01` : ex. **taux d'activation** (% de groupes inscrits qui créent au moins 1 article + 1 vente sous 7 jours), **taux de rétention à 30/90 jours**, **nombre de ruptures de stock détectées vs subies avant migration**, **temps moyen de saisie d'une commande fournisseur**. Ces KPI doivent être mesurables via les tables existantes (`mouvement_stock`, `utilisateur`, dates de création) sans instrumentation supplémentaire lourde. |

---

## Phase 2 — Design & Spécifications Fonctionnelles

| Item | Statut | Preuve / Constat | Action proposée |
|---|---|---|---|
| Use Cases et Personas | ✅ | GS-CDA-2026-01 §2 (5 acteurs détaillés), §5 (UC-01 à UC-05) | — |
| User Stories avec critères d'acceptation | ✅ | GS-BACKLOG-2026-01 v1.1 — 80 US, toutes avec CA | — |
| Architecture de l'information (arborescence de l'app) | ❌ | Les flows (GS-CDA §4) décrivent des actions ("il va dans Achats → Nouvelle commande") mais aucune arborescence de navigation formalisée (menus, sections par rôle) | Produire un schéma d'arborescence par rôle (Admin Groupe / Admin Filiale / Gestionnaire Stock / Resp. Achats / Commercial / Caissier) — 1 diagramme par rôle suffit vu que chaque rôle a un périmètre de menu restreint (cohérent avec §7.3 RBAC). Peut être fait en Mermaid, léger et versionnable avec le code. |
| Maquettes UX/UI, prototypage | ❌ | Rien dans `document/` | Hors périmètre backend — à cadrer séparément si un design/UX existe ou est prévu ailleurs (outil externe type Figma). À ne pas bloquer le développement backend dessus, mais à ne pas oublier avant le développement frontend. |
| Diagrammes de flow (workflows) | ✅ | GS-CDA-2026-01 §4 (8 flows détaillés), §7 (interactions), `representation_graphique_structure_et_flow.pdf` | — |

---

## Phase 3 — Architecture & Conception Technique

| Item | Statut | Preuve / Constat | Action proposée |
|---|---|---|---|
| Spécifications techniques détaillées (stack, protocoles API) | ✅ | CDCT_StockMaster_CM sections 22-30, `knowledge.md` | — |
| Modèle de données (MCD/MLD ou diagramme de classe) | ✅ | GS-CDA-2026-01 §6 (entités, relations, intégrité référentielle) | À jour après GS-CDA-2026-02 (§`Vente.statut`, `Vente.client_id`, `ANNULATION_VENTE`) — voir actions de suivi de l'addendum |
| Diagrammes de séquence (flux Frontend/Backend/API) | ⚠️ | GS-CDA §4 décrit les flows en texte/ASCII, mais pas de vrais diagrammes de séquence formels (UML/Mermaid) | Suffisant pour un backend solo/petite équipe — à ne renforcer que si un frontend externe (autre prestataire) a besoin de contrats plus formels que les endpoints du backlog |
| Politique de sécurité et conformité (RGPD, chiffrement) | ⚠️ | CDCT mentionne A05 OWASP (config Spring), le backlog couvre JWT/BCrypt/rate limiting, mais **aucune politique RGPD/protection des données personnelles formalisée** (durée de conservation, droit à l'oubli, export des données d'un tenant, notification de fuite) | Voir section dédiée ci-dessous « Sécurité & conformité » — c'est le point le plus important des 3 « plus gros ajouts » que tu identifies toi-même |

### Sécurité & conformité — application concrète à StockMaster CM

Le document de bonnes pratiques que tu as fourni (OAuth2/OIDC via IdP externe, Vault, Kong, Datadog/SIEM) décrit l'architecture d'une grande entreprise avec une équipe SRE dédiée. StockMaster CM est un backend Spring Boot monolithe modulaire pour une PME SaaS — appliquer ces pratiques **telles quelles** serait disproportionné (Keycloak/Vault/Kong ajouteraient une charge opérationnelle que le projet n'a pas les moyens d'absorber aujourd'hui). Voici la traduction **pragmatique** de chaque principe, déjà intégrée au backlog (EPIC 2, US-014 à US-017 — GS-CDA-2026-02) :

| Principe (doc fourni) | Équivalent StockMaster CM | Statut |
|---|---|---|
| IdP externe (Keycloak/Okta) + JWKS | JWT interne signé HS256 (`jjwt`), suffisant tant qu'il n'y a pas de besoin de SSO multi-produits | ✅ décision assumée, pas un gap |
| Argon2id pour le hachage | US-015 — migration BCrypt → Argon2id avec `DelegatingPasswordEncoder` | ⚠️ à développer |
| Rotation des Refresh Tokens (RTR) + détection de rejeu | US-014 | ⚠️ à développer (c'était le vrai trou identifié) |
| Vault / Secrets Manager externe | Variables d'environnement `.env` (déjà en place) — un vrai secret manager (Doppler, Infisical, ou AWS Secrets Manager si migration cloud) est une amélioration V2, pas un blocage MVP | ⚠️ acceptable pour le MVP, à revisiter à la croissance |
| API Gateway + WAF (Kong/Cloudflare) | Rate limiting applicatif Redis déjà en place (login) — étendre aux autres endpoints publics (US-016 pose les bases fail-closed). Un WAF gratuit (Cloudflare, si le domaine y est proxifié) est un ajout peu coûteux en défense en profondeur, recommandé mais non bloquant | ⚠️ recommandé, non bloquant |
| SIEM (Splunk/Datadog/ELK) | US-017 — logs structurés JSON, sans données sensibles, prêts à être branchés sur un ELK/Datadog plus tard sans réinstrumentation | ⚠️ à développer (préparation seulement, pas de SIEM complet nécessaire au MVP) |
| Fail-closed sur les composants de sécurité | US-016 | ⚠️ à développer |
| Zéro Trust / moindre privilège | Déjà appliqué via `@PreAuthorize` par rôle sur chaque endpoint (backlog) + isolation multi-tenant stricte (§8.6 GS-CDA-2026-01) | ✅ |

**RGPD / protection des données — à formaliser (nouveau, non couvert actuellement) :**
- Durée de conservation des données d'un tenant après désabonnement (proposition : 90 jours en soft-delete avant purge définitive, à valider).
- Endpoint d'export des données d'une entreprise (obligation de portabilité) — à ajouter au backlog comme US future si le marché camerounais/la clientèle l'exige contractuellement.
- Procédure de notification en cas de fuite de données (qui, sous quel délai) — document interne, pas du code.
- Anonymisation des données d'un utilisateur supprimé plutôt que soft-delete brut si des obligations légales l'exigent (à trancher selon le cadre légal camerounais applicable — point à vérifier, ce n'est pas une compétence que je peux garantir avec certitude).

---

## Phase 4 — Planification & Initialisation

| Item | Statut | Preuve / Constat | Action |
|---|---|---|---|
| Backlog (slicing US → tâches techniques) | ✅ | GS-BACKLOG-2026-01 v1.1 | — |
| Planification (assignation, charges, Gantt) | ⚠️ | Sprints définis (1 à 11, 2 semaines chacun) mais pas de Gantt visuel ni d'assignation nominative (normal si équipe solo/réduite) | Le tableau « Récapitulatif par Sprint » du backlog fait déjà office de planning séquentiel — suffisant tant que l'équipe reste petite. À enrichir d'un Gantt uniquement si plusieurs développeurs travaillent en parallèle sur des EPICs différents. |
| Infrastructure DevOps (Dev/Staging/Prod, CI/CD) | ✅ | `guideconfiguration.md`, `docker-compose.yml`, `Dockerfile`, CI GitHub Actions (build, tests, SonarCloud, deploy staging) | — |
| Initialisation des dépôts (Frontend/Backend) | ✅ | Repo backend structuré en 11 modules Maven | Repo frontend séparé à cadrer si pas encore fait |
| Configuration de base (linters, env, boilerplate) | ✅ | `application.yml` multi-profils, `AbstractEntity`, conventions documentées dans `knowledge.md` | — |

---

## Phase 5 — Cycle de Développement, Tests et Revue (itératif)

| Item | Statut | Preuve / Constat | Action |
|---|---|---|---|
| Développement par sprints | ✅ | Structure de sprints dans le backlog | — |
| Revues de code (Code Review / PR) systématiques | ✅ | DoD backlog : "Code review approuvé par un pair" ; A_JIRA_ET_GIT_FLOW.md : MR avec 1 reviewer minimum obligatoire | — |
| Tests automatisés (unitaires, intégration) | ⚠️ | Stratégie définie (`strategie_test.md`, JUnit5+Mockito, cible 80% coverage) — mais la collection Postman (tests API bout-en-bout) était **cassée** (bug `pm.environment` vs `pm.collectionVariables`, voir correctif ci-dessous) | ✅ Corrigé dans cette session — `postman_collection.json` mis à jour |
| Recette fonctionnelle (UAT) | ❌ | Aucune procédure de recette client formalisée dans `document/` — le projet a une Definition of Done technique (coverage, CI vert) mais pas de validation métier finale par un utilisateur réel avant mise en prod | Ajouter une checklist UAT par EPIC : avant bascule en prod d'un module (ex. EPIC 10 Vente Directe), faire valider par un vrai gérant de boutique (ou à défaut le porteur de projet jouant ce rôle) les scénarios nominaux du GS-CDA §4 correspondants, sur l'environnement staging, avec un procès-verbal signé/daté. |
| Documentation technique (Swagger/Postman, Readme) | ✅ | SpringDoc OpenAPI configuré, `postman_collection.json`, `test_postman.md`, README par module | — |
| Déploiement et Release (prod, monitoring des bugs) | ⚠️ | CI/CD déploie en staging (`guideconfiguration.md`), Actuator expose `/health`, `/metrics` — mais **pas de release en production documentée**, ni d'outil de suivi d'erreurs en prod (Sentry ou équivalent) | Ajouter un job `cd-prod.yml` distinct du staging avec étape de validation manuelle (approval GitHub Actions) avant déploiement prod ; brancher un outil léger de suivi d'erreurs (Sentry a un tier gratuit largement suffisant à ce stade) plutôt qu'un SIEM complet. |

---

## Correctifs déjà appliqués dans cette session

| Correctif | Détail |
|---|---|
| **Postman** | 16 occurrences de `pm.environment.*` remplacées par `pm.collectionVariables.*` dans `postman_collection.json` — les variables (`current_email`, `access_token`, `refresh_token`, etc.) sont déclarées au niveau collection, pas dans un environnement Postman séparé. Chaîne de tests automatisée désormais fonctionnelle. |
| **Backlog** | US-064, US-064b (nouveau), US-067 mis à jour ; US-014 à US-017 (sécurité) ajoutées à l'EPIC 2 — voir `GS-BACKLOG-2026-01` v1.1 |
| **Décisions métier** | Formalisées dans `GS-CDA-2026-02_addendum_decisions_validees.md` |

## Prochaines étapes suggérées

1. Valider ou ajuster les KPI proposés en Phase 1 (le seul point qui demande vraiment ton arbitrage métier, le reste est actionnable directement).
2. Développer US-014 à US-017 (sécurité auth) — priorité P0/P1, à placer en Sprint 3 comme indiqué dans le backlog révisé.
3. Développer US-064, US-064b, US-067 revus.
4. Trancher les points 3 et 4 encore ouverts de notre revue précédente (RBAC Commercial sur Vente Directe, commande fournisseur centralisée groupe) — je peux les traiter avec le même format dès que tu es prêt.
