# Backlog Produit — StockMaster CM
### Référence : GS-BACKLOG-2026-01 | Version : 1.2 | Date : 8 septembre 2026 | Statut : Validé
### Changements v1.2 (8 sept. 2026) : rédaction des 20 US manquantes (`US-087` à `US-106`) pour les décisions `DEC-002/007/009/010/011/012/013/015/027/036/038` — 4 EPICs nouveaux (caisse, inventaire, import, idempotence) ; totaux recalculés (108 US / 354 SP)
### Changements v1.1 : intégration des décisions GS-CDA-2026-02 (mouvement `ANNULATION_VENTE`, `client_id` nullable, durcissement sécurité auth) — **révisé le 8 sept. 2026** par le référentiel `GS-REF-2026-01` : la vente directe **bloque** en 409 sur stock insuffisant (`DEC-023`), l'état de la vente est `PAYEE`/`ANNULEE`/`REMBOURSEE` (`DEC-006/010/018`), le point de fidélité est abandonné (`DEC-034`)

---

> ✅ **Périmètre décidé — couverture complète depuis le 8 septembre 2026.**
>
> Ce backlog couvrait 88 US / 250 SP et laissait **10 décisions actées sans aucune US**.
> Ces 20 US manquantes ont été rédigées (`US-087` à `US-106`) : le fichier décrit désormais
> l'intégralité du périmètre acté au journal des décisions.
>
> | Décision | Objet | US |
> |---|---|---|
> | `DEC-009` / `DEC-010` | sessions de caisse, paiement mixte, clôture, remboursement | EPIC 14 — `US-087` à `US-091` |
> | `DEC-036` | campagnes d'inventaire (sans gel) | EPIC 15 — `US-092` à `US-095` |
> | `DEC-038` | import CSV de mise en service | EPIC 16 — `US-096` à `US-099` |
> | `DEC-027` | clés d'idempotence sur les écritures de stock | EPIC 17 — `US-100` |
> | `DEC-015` | limite d'**utilisateurs** par plan | `US-101` (EPIC 4) |
> | `DEC-002` | catalogue au niveau groupe | `US-102` (EPIC 5) |
> | `DEC-013` | unité d'achat / gestion + facteur | `US-103` (EPIC 5) |
> | `DEC-012` | lot et date de péremption (schéma lot-ready) | `US-104` (EPIC 5) |
> | `DEC-011` | règlement et échéance B2B | `US-105` (EPIC 9) |
> | `DEC-007` | transfert multi-lignes à états | `US-106` (EPIC 11) |
>
> **Le périmètre est couvert, le planning ne l'est pas** : la colonne « Sprint » est
> sur-souscrite (voir le récapitulatif en fin de fichier). L'autorité sur l'ordre et la
> charge reste `REF §13` + `GS-PLAN` + `progress-ledger`.

> **Convention de lecture**
>
> - **US** = User Story — format : `En tant que [acteur], je veux [action] afin de [bénéfice]`
> - **Critères d'acceptation (CA)** = conditions mesurables pour considérer la story comme DONE
> - **Endpoint** = contrat HTTP de l'API associée
> - **Priorité** = P0 (MVP bloquant) / P1 (valeur forte V1) / P2 (différenciateur V2)
> - **Sprint** = estimation de placement (1 sprint = 2 semaines)
> - **Points** = estimation en story points (suite de Fibonacci : 1, 2, 3, 5, 8, 13)
>
> **Définition of Done (DoD) globale :**
> - Code review approuvé par un pair
> - Tests unitaires + tests contrôleur écrits et passants
> - Coverage ≥ 80% sur le nouveau code
> - Isolation multi-tenant vérifiée (filtre `entreprise_id` systématique)
> - Pipeline CI vert (build + tests + quality gate SonarCloud)
> - Documentation OpenAPI à jour

---

## Table des matières

- [EPIC 1 — Fondations techniques](#epic-1--fondations-techniques)
- [EPIC 2 — Authentification & Accès](#epic-2--authentification--accès)
- [EPIC 3 — Groupe & Filiales](#epic-3--groupe--filiales)
- [EPIC 4 — Gestion des Utilisateurs](#epic-4--gestion-des-utilisateurs)
- [EPIC 5 — Catalogue (Catégories & Articles)](#epic-5--catalogue-catégories--articles)
- [EPIC 6 — Tiers (Clients & Fournisseurs)](#epic-6--tiers-clients--fournisseurs)
- [EPIC 7 — Commandes Fournisseur (Achats)](#epic-7--commandes-fournisseur-achats)
- [EPIC 8 — Gestion du Stock](#epic-8--gestion-du-stock)
- [EPIC 9 — Commandes Client (Ventes B2B)](#epic-9--commandes-client-ventes-b2b)
- [EPIC 10 — Vente Directe (Caisse)](#epic-10--vente-directe-caisse)
- [EPIC 11 — Transferts Inter-Filiales](#epic-11--transferts-inter-filiales)
- [EPIC 12 — Notifications & Alertes](#epic-12--notifications--alertes)
- [EPIC 13 — Statistiques & Reporting](#epic-13--statistiques--reporting)
- [EPIC 14 — Caisse : sessions, paiements et remboursement](#epic-14--caisse--sessions-paiements-et-remboursement)
- [EPIC 15 — Campagnes d'inventaire](#epic-15--campagnes-dinventaire)
- [EPIC 16 — Import de mise en service (CSV)](#epic-16--import-de-mise-en-service-csv)
- [EPIC 17 — Robustesse des écritures de stock](#epic-17--robustesse-des-écritures-de-stock)
- [Récapitulatif par Sprint](#récapitulatif-par-sprint)
- [Matrice de dépendances](#matrice-de-dépendances)

---

## EPIC 1 — Fondations techniques

> **Objectif :** Mettre en place l'infrastructure de base sans laquelle aucune fonctionnalité métier ne peut être développée. Cet epic est entièrement technique — il ne produit pas d'endpoints visibles, mais conditionne la qualité et la vitesse de tout ce qui suit.

---

### US-001 — Initialisation du projet Spring Boot

**Priorité :** P0 | **Sprint :** 1 | **Points :** 3

**En tant que** développeur,
**je veux** disposer d'un projet Spring Boot 3.3.x correctement structuré avec tous les modules Maven configurés,
**afin de** pouvoir démarrer le développement fonctionnel sur une base saine et reproductible.

**Critères d'acceptation :**
- [ ] `pom.xml` conforme au CDCT section 21 (Java 21, toutes les dépendances versionnées)
- [ ] Structure de packages `com.stockmaster.{module}` créée pour les 11 modules fonctionnels
- [ ] `AbstractEntity` avec `id`, `dateCreation`, `dateModification`, `supprime` implémentée
- [ ] `ApiResponse<T>` et `ProblemResponse` (RFC 7807) implémentés dans `shared/`
- [ ] `application.yml` + profils `dev`, `test`, `prod` configurés
- [ ] `@ConfigurationProperties` pour JWT, CORS, pagination
- [ ] `open-in-view: false` explicitement désactivé
- [ ] Application démarre sans erreur : `mvn spring-boot:run`

**Endpoint :** aucun (fondation technique)

---

### US-002 — Configuration Flyway et schéma initial

**Priorité :** P0 | **Sprint :** 1 | **Points :** 5

**En tant que** développeur,
**je veux** que Flyway gère les migrations de schéma de façon versionnée et automatique,
**afin de** garantir la cohérence du schéma entre tous les environnements sans jamais utiliser `ddl-auto=update`.

**Critères d'acceptation :**
- [ ] Flyway activé et configuré pour tous les profils
- [ ] `V1__init_schema.sql` crée toutes les tables avec contraintes CHECK, FK et UNIQUE — **état de départ** ; le schéma cible (CDCT §23.3, REF §6.2) s'en écarte et sera appliqué par la migration `V5`
- [ ] `V2__create_indexes.sql` crée tous les index de performance critiques
- [ ] `V3__functions_and_triggers.sql` crée le trigger `update_date_modification`
- [ ] `clean-disabled: true` en profil `prod`
- [ ] `ddl-auto=none` en profil `prod` et `test`, `ddl-auto=validate` en profil `dev`
- [ ] La migration s'applique automatiquement au démarrage sans erreur
- [ ] Chaque script de migration est accompagné de son script de rollback documenté

**Endpoint :** aucun (fondation technique)

---

### US-003 — Gestion centralisée des erreurs

**Priorité :** P0 | **Sprint :** 1 | **Points :** 3

**En tant que** développeur,
**je veux** un `GlobalExceptionHandler` qui intercepte toutes les exceptions et retourne un `ProblemResponse` formaté,
**afin que** les erreurs soient cohérentes, exploitables par le frontend et jamais exposées en stack trace brute.

**Critères d'acceptation :**
- [ ] `ErrorCode` enum complet (AUTH_*, RES_*, CMD_*, GRP_*, STK_*, SEC_*, SYS_*)
- [ ] `BusinessException`, `EntityNotFoundException`, `InsufficientStockException` implémentées
- [ ] `GlobalExceptionHandler` couvre : `EntityNotFoundException`, `BusinessException`, `InsufficientStockException`, `MethodArgumentNotValidException`, `AccessDeniedException`, `Exception` (fallback)
- [ ] Format RFC 7807 respecté : `type`, `title`, `status`, `detail`, `instance`, `errorCode`, `timestamp`, `errors[]`
- [ ] Stack trace JAMAIS exposée au client (log uniquement)
- [ ] Tests unitaires pour chaque cas d'erreur du handler

**Endpoint :** aucun (transversal)

---

### US-004 — Pipeline CI/CD GitHub Actions

**Priorité :** P0 | **Sprint :** 1 | **Points :** 5

**En tant que** tech lead,
**je veux** un pipeline CI automatisé qui s'exécute à chaque push et pull request,
**afin de** détecter les régressions immédiatement et garantir la qualité du code de façon continue.

**Critères d'acceptation :**
- [ ] Workflow CI (`ci.yml`) : compilation → tests unitaires → tests intégration → JaCoCo ≥ 80% → SonarCloud → build JAR
- [ ] Workflow CD (`cd.yml`) : build image Docker → push GHCR → déploiement SSH sur le serveur de staging
- [ ] Services Docker PostgreSQL 16 et Redis 7 disponibles dans le job CI
- [ ] Stratégie de branches documentée : `main` (prod), `develop` (intégration), `feature/*`, `fix/*`
- [ ] `main` protégée : PR + CI obligatoires avant merge
- [ ] Secrets configurés dans GitHub : `SONAR_TOKEN`, `PROD_HOST`, `PROD_SSH_KEY`

**Endpoint :** aucun (DevOps)

---

### US-005 — Conteneurisation Docker

**Priorité :** P0 | **Sprint :** 1 | **Points :** 3

**En tant que** développeur,
**je veux** un `Dockerfile` multi-stage et un `docker-compose.yml` de développement local,
**afin de** pouvoir lancer l'environnement complet (API + PostgreSQL + Redis + MinIO + MailHog) en une seule commande.

**Critères d'acceptation :**
- [ ] `Dockerfile` multi-stage (builder JDK 21 → runtime JRE 21 alpine) avec layered JAR
- [ ] Utilisateur non-root dans le conteneur (`stockmaster`)
- [ ] `HEALTHCHECK` configuré sur `/actuator/health`
- [ ] `docker-compose.yml` : API + PostgreSQL 16 + Redis 7 + MinIO + MailHog
- [ ] `docker-compose up -d` démarre l'environnement complet en moins de 2 minutes
- [ ] Variables d'environnement via `.env` (gitignored)
- [ ] Flags JVM optimisés pour conteneur : `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`

**Endpoint :** `GET /actuator/health` → `200 OK { "status": "UP" }`

---

## EPIC 2 — Authentification & Accès

> **Objectif :** Permettre à tout utilisateur de s'inscrire, se connecter et gérer son accès de façon sécurisée. Cet epic est le prérequis absolu de tous les epics métier — sans token JWT valide, aucun endpoint n'est accessible.

**Dépendance :** EPIC 1 complété.

---

### US-006 — Inscription — Entreprise unique

**Priorité :** P0 | **Sprint :** 2 | **Points :** 5

**En tant que** gérant d'une boutique unique (épicerie, pharmacie, quincaillerie),
**je veux** m'inscrire avec un formulaire simplifié (nom de boutique, ville, mes coordonnées),
**afin de** créer mon espace StockMaster en moins de 2 minutes sans configuration complexe.

**Critères d'acceptation :**
- [ ] Création atomique dans une seule transaction : `TenantGroup` + `Entreprise` (type `MERE`, `parent_id = null`) + `Utilisateur` (rôle `ADMIN_GROUPE`)
- [ ] Si l'email existe déjà → `409 CONFLICT` avec `ErrorCode.EMAIL_ALREADY_EXISTS`
- [ ] Mot de passe haché avec BCrypt (jamais en clair)
- [ ] Email de bienvenue envoyé de façon **asynchrone** après inscription (`InscriptionSuccessEvent` publié via `ApplicationEventPublisher`)
- [ ] L'envoi de l'email **ne bloque pas** la transaction d'inscription — l'utilisateur est créé même si l'email échoue (comportement volontaire : ne pas pénaliser l'utilisateur pour un problème d'email temporaire)
- [ ] En cas d'échec d'email, un mécanisme de retry sera implémenté dans le module Notification (EPIC 12) — voir US-074
- [ ] Validation Jakarta sur tous les champs obligatoires

**Endpoint :** `POST /api/v1/auth/inscription/entreprise-unique`

**Corps de la requête :**
```json
{
  "nomEntreprise": "Épicerie Centrale",
  "nif": "M123456789",
  "email": "jean.kamga@epicerie.cm",
  "telephone": "+237691234567",
  "adminNom": "Kamga",
  "adminPrenom": "Jean",
  "motDePasse": "MotDePasse@2026"
}
```

> **Note (Juillet 2026) :** `ville` et `quartier` ont été retirés de l'inscription (décision UX — allègement du formulaire). Ils seront demandés dans l'étape de complétion du profil entreprise. Le `nif` est optionnel (les petites boutiques n'ont pas de NIF au Cameroun). Le `telephone` est obligatoire au format E.164 (+237XXXXXXXX).

**Réponse succès :** `201 CREATED`
```json
{
  "success": true,
  "message": "Votre espace a été créé. Vérifiez votre email pour activer votre compte.",
  "data": { "email": "jean.kamga@epicerie.cm", "groupId": 1 }
}
```

---

### US-007 — Inscription — Groupe multi-sites

**Priorité :** P0 | **Sprint :** 2 | **Points :** 5

**En tant que** dirigeant d'un groupe multi-sites (distributeur, chaîne, grossiste),
**je veux** m'inscrire avec les informations complètes de mon groupe (nom du groupe, NIF, siège),
**afin de** créer la maison mère de mon groupe et ensuite y rattacher mes filiales.

**Critères d'acceptation :**
- [ ] Création atomique : `TenantGroup` + `Entreprise` (type `MERE`, siège social) + `Utilisateur` (rôle `ADMIN_GROUPE`)
- [ ] NIF facultatif mais validé en format si fourni
- [ ] Dashboard post-connexion affiche le guide "Créez votre première filiale"
- [ ] Mêmes règles de sécurité que US-006 (BCrypt, rollback, email)
- [ ] Le même modèle de données qu'US-006 — seule l'UX diffère

**Endpoint :** `POST /api/v1/auth/inscription/groupe`

**Corps de la requête :**
```json
{
  "nomGroupe": "Distribo Sarl",
  "villesiege": "Yaoundé",
  "nif": "M123456789",
  "telephone": "699000001",
  "emailEntreprise": "contact@distribo.cm",
  "prenom": "Paul",
  "nom": "Biya Jr",
  "emailAdmin": "paul@distribo.cm",
  "motDePasse": "MotDePasse@2026"
}
```

**Réponse succès :** `201 CREATED`

---

### US-082 — Unicité stricte des entreprises à l'inscription (NIF, téléphone, email entreprise, nom groupe)

**Priorité :** P0 | **Sprint :** 11 (add.) | **Points :** 3

> **Ajout post-planification (juillet 2026)** — audité et priorisé P0 lors de l'audit d'unicité.

**En tant que** gérant ou dirigeant d'entreprise,
**je veux** que deux entreprises ne puissent pas être inscrites avec les mêmes informations clés d'identification,
**afin de** garantir qu'aucune structure ne soit dupliquée dans la plateforme (fraude, erreur de saisie, double enregistrement).

> **Contexte (audit juillet 2026) :** Seule l'unicité de l'email admin est aujourd'hui vérifiée (`existsByEmail` + contrainte `uq_utilisateur_email`). Le NIF (clé légale au Cameroun), le téléphone et l'email de l'entreprise n'ont **aucun** contrôle — une même entreprise peut être inscrite deux fois. Le `existsByNomGroupe` de `TenantGroupRepository` existe mais n'est **jamais appelé** dans le service : un doublon de nom lèverait une `DataIntegrityViolationException` → 500 au lieu d'un 409 propre.

**Critères d'acceptation :**
- [ ] `EntrepriseRepository` : ajout de `existsByNif`, `existsByTelephone`, `existsByEmailEntreprise`
- [ ] `AuthServiceImpl.inscrireEntrepriseUnique` et `inscrireGroupe` : vérifier AVANT insertion → 409 `RES_DUPLICATE_*` avec message par champ : NIF déjà utilisé, téléphone déjà utilisé, email entreprise déjà utilisé, nom de groupe déjà utilisé (`tenantGroupRepository.existsByNomGroupe`)
- [ ] Contrainte DB (V4 migration) : index UNIQUE partiel `uq_entreprise_nif_actif` sur `nif` WHERE `supprime = false`, idem téléphone et email — compatible soft-delete
- [ ] `Utilisateur.email` reste unique au niveau plateforme (déjà en place)
- [ ] Le frontend affiche l'erreur 409 par champ (toast + inline) — cf. US-F112
- [ ] Tests unitaires + contrôleur : chaque doublon → 409, pas de 500

**Endpoints :** `POST /api/v1/auth/inscription/entreprise-unique`, `POST /api/v1/auth/inscription/groupe` (durcissement)

---

### US-008 — Connexion JWT

**Priorité :** P0 | **Sprint :** 2 | **Points :** 3

**En tant qu'** utilisateur enregistré,
**je veux** me connecter avec mon email et mon mot de passe et recevoir un token JWT,
**afin d'** accéder aux fonctionnalités de mon espace selon mon rôle.

**Critères d'acceptation :**
- [ ] Vérification email + BCrypt. Credentials invalides → `401 UNAUTHORIZED` avec message générique (ne pas révéler si l'email existe)
- [ ] Compte inactif (`actif = false`) → `403 FORBIDDEN` avec `ErrorCode.ACCOUNT_DISABLED`
- [ ] Groupe suspendu → `403 FORBIDDEN` avec `ErrorCode.TENANT_SUSPENDED`
- [ ] Access token : expiration 15 minutes, claims : `userId`, `entrepriseId`, `groupId`, `role`, `scope`, `jti`
- [ ] Refresh token : expiration 7 jours, stocké en Redis avec clé `refresh:{userId}`
- [ ] Rate limiting : configurable via `stockmaster.rate-limiting` (application.yml). Défauts : 5 tentatives/15 min par IP, compteur global 100 requêtes/min, countdown progressif
- [ ] `jjwt 0.12.x` utilisé — jamais 0.9.x

**Endpoint :** `POST /api/v1/auth/login`

**Corps :**
```json
{ "email": "jean.kamga@epicerie.cm", "motDePasse": "MotDePasse@2026" }
```

**Réponse succès :** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 900,
    "role": "ADMIN_GROUPE",
    "scope": "FILIALE"
  }
}
```

---

### US-009 — Refresh token

**Priorité :** P0 | **Sprint :** 2 | **Points :** 2

**En tant qu'** utilisateur connecté,
**je veux** renouveler mon access token sans me reconnecter,
**afin de** maintenir ma session active pendant mon travail.

**Critères d'acceptation :**
- [ ] Refresh token validé en Redis (existence + non révoqué)
- [ ] Refresh token expiré ou absent → `401 UNAUTHORIZED` → client doit se reconnecter
- [ ] Nouveau access token émis avec les mêmes claims (rôle, scope)
- [ ] L'ancien access token reste valide jusqu'à sa propre expiration (15 min)

**Endpoint :** `POST /api/v1/auth/refresh`

**Corps :** `{ "refreshToken": "eyJ..." }`

**Réponse :** `200 OK` `{ "accessToken": "eyJ...", "expiresIn": 900 }`

---

### US-010 — Déconnexion

**Priorité :** P0 | **Sprint :** 2 | **Points :** 2

**En tant qu'** utilisateur connecté,
**je veux** me déconnecter explicitement,
**afin que** mon token soit immédiatement invalidé côté serveur et non réutilisable.

**Critères d'acceptation :**
- [ ] Le `jti` de l'access token est ajouté à la blacklist Redis (TTL = durée restante du token)
- [ ] Le refresh token est supprimé de Redis
- [ ] Toute requête ultérieure avec ce token → `401 UNAUTHORIZED`
- [ ] Requiert un access token valide (endpoint protégé)

**Endpoint :** `POST /api/v1/auth/logout`

**Réponse :** `200 OK` `{ "success": true, "message": "Déconnexion réussie" }`

---

### US-011 — Mot de passe oublié

**Priorité :** P0 | **Sprint :** 2 | **Points :** 3

**En tant qu'** utilisateur ayant oublié son mot de passe,
**je veux** recevoir un lien de réinitialisation par email,
**afin de** retrouver l'accès à mon compte sans aide manuelle.

**Critères d'acceptation :**
- [ ] Si l'email n'existe pas → réponse identique au cas succès (ne pas révéler l'existence du compte)
- [ ] Token de reset généré (UUID), stocké haché en base, expiration 1 heure
- [ ] Email envoyé avec lien : `https://app.stockmaster.cm/reset-password?token={token}`
- [ ] Un seul token actif par utilisateur (le précédent est révoqué)

**Endpoint :** `POST /api/v1/auth/forgot-password`

**Corps :** `{ "email": "jean.kamga@epicerie.cm" }`

**Réponse :** `200 OK` `{ "success": true, "message": "Si cet email existe, un lien a été envoyé." }`

---

### US-012 — Réinitialisation du mot de passe

**Priorité :** P0 | **Sprint :** 2 | **Points :** 3

**En tant qu'** utilisateur ayant reçu un lien de reset,
**je veux** définir un nouveau mot de passe via ce lien,
**afin de** retrouver l'accès à mon compte.

**Critères d'acceptation :**
- [ ] Token validé : existe en base, non expiré, non déjà utilisé
- [ ] Token invalide ou expiré → `400 BAD REQUEST` avec `ErrorCode.RESET_TOKEN_INVALID`
- [ ] Nouveau mot de passe haché BCrypt et sauvegardé
- [ ] Token supprimé après utilisation (usage unique)
- [ ] Tous les refresh tokens de l'utilisateur révoqués en Redis (sécurité)
- [ ] Critères de robustesse : min 8 chars, 1 majuscule, 1 chiffre, 1 caractère spécial

**Endpoint :** `POST /api/v1/auth/reset-password`

**Corps :** `{ "token": "uuid-token", "nouveauMotDePasse": "NouveauMdp@2026" }`

---

### US-013 — Changement de mot de passe (utilisateur connecté)

**Priorité :** P1 | **Sprint :** 3 | **Points :** 2

**En tant qu'** utilisateur connecté,
**je veux** changer mon mot de passe depuis mon profil,
**afin de** renouveler ma sécurité sans passer par le flow de reset.

**Critères d'acceptation :**
- [ ] Vérification de l'ancien mot de passe avant modification
- [ ] Ancien mot de passe incorrect → `400 BAD REQUEST`
- [ ] Nouveau mot de passe respecte les critères de robustesse
- [ ] Tous les refresh tokens révoqués après changement

**Endpoint :** `PUT /api/v1/auth/change-password`

**Corps :** `{ "ancienMotDePasse": "...", "nouveauMotDePasse": "..." }`

---

### US-083 — Rotation du Refresh Token avec détection de rejeu

**Priorité :** P0 | **Sprint :** 3 | **Points :** 5

**En tant que** responsable sécurité du produit,
**je veux** qu'un refresh token ne soit utilisable qu'une seule fois et que sa réutilisation déclenche une révocation totale,
**afin de** limiter drastiquement l'impact du vol d'un refresh token (téléphone volé, token intercepté).

> **Contexte** : US-009 (refresh) ne fait aujourd'hui que vérifier l'existence du token en Redis — il n'y a pas de rotation. Un attaquant en possession d'un refresh token valide peut l'utiliser indéfiniment jusqu'à son expiration (7 jours), sans que l'utilisateur légitime ne le sache. C'est l'écart le plus important par rapport aux pratiques standard (RTR — Refresh Token Rotation) du secteur.

**Critères d'acceptation :**
- [ ] À chaque appel `POST /api/v1/auth/refresh` réussi : le refresh token présenté est immédiatement invalidé en Redis et un **nouveau** refresh token est émis (usage unique)
- [ ] Les refresh tokens successifs d'un même utilisateur sont chaînés (`family_id` commun stocké en Redis avec chaque token de la famille)
- [ ] Si un refresh token **déjà invalidé** est présenté à nouveau : toute la famille de tokens de cet utilisateur est révoquée immédiatement (déconnexion forcée de tous les appareils), et une `NotificationAlerte` de sécurité est créée pour l'utilisateur (email + in-app : "Activité suspecte détectée, vous avez été déconnecté")
- [ ] Cet événement est loggé en `WARN` avec `event: "auth.refresh_reuse_detected"`, `userId`, `ip` — jamais le token lui-même

**Endpoint :** modification de `POST /api/v1/auth/refresh` (US-009) — pas de nouvel endpoint

---

### US-084 — Hachage des mots de passe en Argon2id

**Priorité :** P1 | **Sprint :** 3 | **Points :** 3

**En tant que** responsable sécurité du produit,
**je veux** que les mots de passe soient hachés avec Argon2id plutôt que BCrypt,
**afin de** rester aligné sur le standard actuel recommandé (ANSSI/OWASP), plus résistant aux attaques par accélération matérielle (GPU/ASIC) que BCrypt.

**Critères d'acceptation :**
- [ ] `Argon2PasswordEncoder` configuré (paramètres recommandés OWASP : mémoire ≥ 19 MiB, itérations ≥ 2, parallélisme ≥ 1 — à ajuster selon charge serveur mesurée)
- [ ] `DelegatingPasswordEncoder` utilisé pour supporter la **migration progressive** : les hachages `{bcrypt}` existants restent vérifiables, tout nouveau mot de passe (inscription, reset, changement) est haché en `{argon2}`
- [ ] Migration transparente au prochain login réussi : si le hash stocké est `{bcrypt}` et le mot de passe fourni est correct, il est ré-haché en `{argon2}` et sauvegardé (upgrade-on-login, aucune action utilisateur requise)
- [ ] Aucune donnée en clair, aucun mot de passe en log, à aucun moment

**Endpoint :** aucun (changement transverse du `PasswordEncoder`)

---

### US-085 — Comportement fail-closed ciblé en cas d'indisponibilité de Redis

**Priorité :** P0 | **Sprint :** 3 | **Points :** 3

**En tant que** responsable sécurité du produit,
**je veux** que les opérations où l'absence de Redis créerait un trou de sécurité concret rejettent la requête, sans pour autant bloquer l'authentification globale de l'entreprise sur un simple incident d'infrastructure,
**afin de** ne jamais transformer une panne Redis en faille de sécurité **ni** en panne totale d'authentification.

> **Révision (juillet 2026, ingénierie) :** la version initiale demandait un fail-closed strict sur les 5 endpoints (`login`, `refresh`, `logout`, `forgot-password`, `reset-password`). Revu après constat que **Redis tourne en instance unique sans HA dans cette architecture** (`docker-compose.yml` : un seul conteneur, pas de Sentinel/cluster) — un redémarrage banal du conteneur (déploiement, OOM, reboot) est un incident opérationnel courant, pas un scénario d'attaque exotique. Un fail-closed strict sur `login` aurait transformé chaque redémarrage Redis en panne totale d'authentification pour toute l'entreprise cliente — un coût disproportionné par rapport au risque réellement couvert (fenêtre d'exploitation étroite : il faut savoir que Redis est en panne ET bruteforcer à cet instant précis). Le fail-closed est donc restreint aux deux endroits où l'absence de Redis crée réellement un nouveau trou de sécurité.

**Critères d'acceptation :**
- [ ] `POST /api/v1/auth/refresh` : si Redis est injoignable, retourne `503 SERVICE_UNAVAILABLE` (`ErrorCode.SEC_STORE_UNAVAILABLE`) — sans Redis, impossible de vérifier la rotation/le rejeu (US-083), accepter un refresh ici annulerait la protection
- [ ] Rate limiting **par endpoint** (les endpoints sensibles configurés, y compris les 5 listés ci-dessus) : si Redis est injoignable, retourne `503 SERVICE_UNAVAILABLE` (`ErrorCode.SEC_STORE_UNAVAILABLE`) — jamais un contournement silencieux du rate limit sur un endpoint sensible
- [ ] `login`, `logout`, `forgot-password` : **tolérants** à une panne Redis (best-effort) — n'échouent pas sur l'utilisateur ; la vérification du mot de passe ne dépend pas de Redis, et bloquer le logout serait contre-productif pour la sécurité. Log `ERROR` en cas d'échec de l'écriture/lecture Redis associée, mais la requête aboutit
- [ ] `reset-password` : comportement inchangé — échoue déjà naturellement si le token Redis est illisible, aucune règle supplémentaire nécessaire
- [ ] Rate limiting **global** (anti-bot, toutes requêtes confondues) et vérification du **blacklist JWT** (`JwtAuthenticationFilter`, sur toute requête authentifiée) : **fail-open** + log `WARN` si Redis est injoignable — un fail-closed ici bloquerait toute l'application (tous modules confondus), pas seulement l'auth
- [ ] Log `ERROR` distinctif si Redis reste injoignable plus de 60 secondes en continu (pas de canal d'alerte réel disponible aujourd'hui — pas de Sentry/module notification — donc pas d'alerte "Super Admin SaaS" fabriquée artificiellement ; le log est le point d'ancrage pour un futur outil de monitoring)
- [ ] Documenté dans le CDCT comme décision d'architecture (ADR) : fail-closed ciblé (refresh + rate-limit par endpoint), fail-open assumé ailleurs, avec la justification ci-dessus

**Endpoint :** aucun (comportement transverse des filtres de sécurité et d'`AuthServiceImpl`)

---

### US-086 — Logs d'audit structurés pour les événements d'authentification

**Priorité :** P1 | **Sprint :** 3 | **Points :** 3

**En tant qu'** Admin Groupe ou Super Admin SaaS,
**je veux** que chaque événement d'authentification génère un log structuré et exploitable,
**afin de** pouvoir investiguer un incident de sécurité et, à terme, brancher un outil de supervision (ELK, Datadog ou équivalent) sans réécrire l'instrumentation.

**Critères d'acceptation :**
- [ ] Chaque connexion réussie/échouée, déconnexion, changement de mot de passe, reset de mot de passe, et détection de rejeu de refresh token (US-083) génère un log JSON structuré : `{ "event": "auth.login_success", "userId", "entrepriseId", "ip", "timestamp" }` (adapter le champ `event` selon le cas)
- [ ] **Jamais** de mot de passe, token complet, ou secret dans un log, à aucun niveau
- [ ] Les logs d'échec de connexion incluent un compteur de tentatives consécutives (aide au diagnostic sans consulter Redis)
- [ ] Format et champs documentés dans le CDCT (section Observabilité) pour préparer un branchement futur vers un SIEM externe

**Endpoint :** aucun (instrumentation transverse, `SLF4J` structuré)

---

## EPIC 3 — Groupe & Filiales

> **Objectif :** Permettre à l'Admin Groupe de configurer son organisation multi-sites et d'avoir une vue consolidée sur l'ensemble de ses filiales.

**Dépendance :** EPIC 2 complété.

---

### US-014 — Modifier les informations du groupe

**Priorité :** P0 | **Sprint :** 3 | **Points :** 2

**En tant qu'** Admin Groupe,
**je veux** modifier le nom, le logo et les informations fiscales de mon groupe,
**afin de** maintenir les informations de mon entreprise à jour.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasRole('ADMIN_GROUPE')")`
- [ ] Seul l'Admin du groupe concerné peut modifier son propre groupe (filtre `groupId` du JWT)
- [ ] Logo uploadé vers MinIO, URL persistante stockée
- [ ] Modification partielle acceptée (PATCH sémantique)

**Endpoint :** `PUT /api/v1/groupe`

---

### US-014b — Compléter les informations de l'entreprise (profil)

**Priorité :** P1 | **Sprint :** 4 | **Points :** 3

**En tant que** Admin Groupe ou Admin Filiale,
**je veux** compléter les informations d'adresse de mon entreprise (ville, quartier, rue, région),
**afin de** finaliser mon profil après une inscription allégée qui n'a demandé que le strict minimum.

> **Contexte :** Lors de l'inscription (US-006, US-007), seuls les champs essentiels ont été demandés. `ville` et `quartier` ont été retirés pour alléger le formulaire. Cette US permet de les renseigner une fois connecté.

**Critères d'acceptation :**
- [ ] L'utilisateur connecté peut modifier les champs suivants de son entreprise : `ville`, `quartier`, `rue`, `région`, `pays`
- [ ] Les champs sont pré-remplis si déjà saisis (modification, pas écrasement)
- [ ] `@PreAuthorize("hasAnyRole('ADMIN_GROUPE','ADMIN_FILIALE')")`
- [ ] Isolation multi-tenant : seul l'admin de l'entreprise concernée peut modifier ses propres infos
- [ ] Une notification in-app "Pensez à compléter l'adresse de votre entreprise" s'affiche tant que `ville` ou `quartier` est vide
- [ ] La modification est partielle (PATCH) : on peut changer seulement la rue sans retaper la ville

**Endpoints :**
- `GET /api/v1/entreprise/profil` — consulter les infos
- `PATCH /api/v1/entreprise/profil` — modifier (partiel)

**Corps de la requête (PATCH) :**
```json
{
  "ville": "Douala",
  "quartier": "Akwa",
  "rue": "123 Rue de la Liberté",
  "region": "Littoral",
  "pays": "Cameroun"
}
```

---

### US-081 — Personnaliser le branding de l'entreprise (logo & titre)

**Priorité :** P1 | **Sprint :** 11 (add.) | **Points :** 3

> **Ajout post-planification (juillet 2026)** — audité et priorisé P1 lors de l'audit branding.

**En tant qu'** Admin Groupe ou Admin Filiale,
**je veux** personnaliser le logo et le titre affichés dans l'interface de mon entreprise,
**afin que** mes utilisateurs voient l'identité visuelle de mon entreprise (et non le branding générique StockMaster) partout dans l'application.

> **Contexte (audit juillet 2026) :** Le schéma DB est prêt (colonne `logo` VARCHAR(500) sur `entreprise`), mais : (1) aucun endpoint ne permet de le mettre à jour, (2) `AuthMapper` ignore le champ `logo` à la création (`@Mapping(target = "logo", ignore = true)`), (3) le composant frontend `Logo.tsx` est statique (icône `Warehouse` + texte "StockMaster" en dur).
>
> **Relation avec US-014 :** US-014 (EPIC 3) couvre le logo au **niveau groupe** (`PUT /api/v1/groupe`). Cette US-081 traite le branding **au niveau entreprise/filiale** (`PATCH /entreprises/{id}/branding`) — les deux se complètent : le logo du groupe sert de fallback, le logo de l'entreprise (ou de la filiale) le surcharge. Ne pas dupliquer la logique d'upload (réutiliser le service MinIO commun).

**Critères d'acceptation :**
- [ ] Endpoint `PATCH /api/v1/entreprises/{id}/branding` — corps : `{ "logo": "<URL MinIO>", "titre": "Ma Boutique" }`, `@PreAuthorize` admin de l'entreprise concernée + isolation multi-tenant (l'admin ne modifie que SON entreprise)
- [ ] `AuthMapper` : retirer `ignore = true` sur `logo` pour mapper le champ à la création (US-006/US-007) — valeur par défaut `null` si absent
- [ ] Upload du fichier logo vers MinIO (bucket dédié `branding`), URL persistante stockée en base — validation type (PNG/JPG/SVG) et taille (max 2 Mo)
- [ ] GET `entreprise/profil` retourne `logo` et `titre` (fallback : `nom` de l'entreprise)
- [ ] Frontend : composant `Logo` dynamique (cf. US-F110) + page Paramètres (cf. US-F111)
- [ ] Tests : controller + service branding, upload fichier, isolation tenant

**Endpoint :** `PATCH /api/v1/entreprises/{id}/branding`

**Corps de la requête :**
```json
{ "logo": "https://minio/branding/12/logo.png", "titre": "Épicerie Centrale" }
```

---

### US-015 — Consulter les informations du groupe

**Priorité :** P0 | **Sprint :** 3 | **Points :** 1

**En tant qu'** Admin Groupe,
**je veux** consulter les informations de mon groupe et le plan d'abonnement actif,
**afin de** connaître ma configuration et mes limites.

**Critères d'acceptation :**
- [ ] Retourne : nom, plan, limite filiales, nombre filiales actives, date expiration plan
- [ ] `@PreAuthorize("hasRole('ADMIN_GROUPE')")`

**Endpoint :** `GET /api/v1/groupe`

---

### US-016 — Créer une filiale

**Priorité :** P0 | **Sprint :** 3 | **Points :** 3

**En tant qu'** Admin Groupe,
**je veux** créer une nouvelle filiale (point de vente ou entrepôt) dans mon groupe,
**afin d'** étendre mon réseau de distribution avec un site autonome.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasRole('ADMIN_GROUPE')")`
- [ ] Vérification limite filiales selon plan d'abonnement → `403` avec `ErrorCode.FILIALE_LIMIT_REACHED` si dépassée
- [ ] Limite lue depuis `tenant_group.limite_filiales` (`DEC-015`), **jamais codée en dur** ; la limite d'utilisateurs, symétrique, est portée par US-101
- [ ] `code_filiale` unique dans le groupe → `409` avec `ErrorCode.DUPLICATE_FILIALE_CODE`
- [ ] Filiale créée avec `parent_id` pointant vers la maison mère du groupe
- [ ] Filiale vide à la création (pas d'articles, pas d'utilisateurs)

**Endpoint :** `POST /api/v1/groupe/filiales`

**Corps :**
```json
{
  "nom": "Boutique Akwa",
  "ville": "Douala",
  "quartier": "Akwa",
  "codeFiliale": "DLA01"
}
```

---

### US-017 — Lister les filiales du groupe

**Priorité :** P0 | **Sprint :** 3 | **Points :** 2

**En tant qu'** Admin Groupe,
**je veux** voir la liste de toutes mes filiales avec leur statut,
**afin de** piloter mon réseau depuis un seul écran.

**Critères d'acceptation :**
- [ ] Retourne uniquement les filiales du groupe de l'utilisateur connecté
- [ ] Pagination + filtre par `actif` et `ville`
- [ ] Chaque filiale affiche : nom, ville, code, statut actif, nombre d'employés

**Endpoint :** `GET /api/v1/groupe/filiales?page=0&size=20&actif=true`

---

### US-018 — Modifier une filiale

**Priorité :** P0 | **Sprint :** 3 | **Points :** 2

**En tant qu'** Admin Groupe,
**je veux** modifier les informations d'une filiale (nom, adresse, code),
**afin de** corriger les données ou adapter l'organisation.

**Critères d'acceptation :**
- [ ] Vérification d'appartenance au groupe (`group_id` du JWT)
- [ ] `code_filiale` modifiable uniquement si unique dans le groupe
- [ ] Filiale appartenant à un autre groupe → `404` (ne jamais révéler l'existence)

**Endpoint :** `PUT /api/v1/groupe/filiales/{id}`

---

### US-019 — Activer / Désactiver une filiale

**Priorité :** P1 | **Sprint :** 4 | **Points :** 2

**En tant qu'** Admin Groupe,
**je veux** suspendre temporairement une filiale sans la supprimer,
**afin de** bloquer toute activité sur un site en cas de fermeture temporaire.

**Critères d'acceptation :**
- [ ] Filiale désactivée → plus aucun mouvement de stock ni commande acceptée sur ce site
- [ ] Les données existantes sont conservées et consultables
- [ ] Vérification appartenance au groupe

**Endpoint :** `PATCH /api/v1/groupe/filiales/{id}/statut`

**Corps :** `{ "actif": false }`

---

### US-020 — Dashboard consolidé groupe

**Priorité :** P0 | **Sprint :** 4 | **Points :** 5

**En tant qu'** Admin Groupe,
**je veux** un tableau de bord consolidé affichant le stock total, le CA et les alertes de toutes mes filiales,
**afin de** piloter mon activité globale depuis une seule vue.

**Critères d'acceptation :**
- [ ] Stock total consolidé par article (somme de toutes les filiales)
- [ ] CA du jour / de la semaine / du mois (somme commandes VALIDÉES + ventes)
- [ ] Nombre d'alertes de rupture actives (toutes filiales)
- [ ] Top 5 articles les plus vendus ce mois (toutes filiales)
- [ ] `@Transactional(readOnly = true)` sur le service
- [ ] Résultat mis en cache Redis (TTL 5 minutes) — `@Cacheable`

**Endpoint :** `GET /api/v1/groupe/dashboard`

---

## EPIC 4 — Gestion des Utilisateurs

> **Objectif :** Permettre la création et la gestion des comptes employés avec leurs rôles métier, assurant que chaque utilisateur n'accède qu'aux fonctionnalités de son périmètre.

**Dépendance :** EPIC 3 complété.

---

### US-021 — Créer un Admin Filiale

**Priorité :** P0 | **Sprint :** 3 | **Points :** 3

**En tant qu'** Admin Groupe,
**je veux** créer un compte Admin Filiale pour gérer un point de vente spécifique,
**afin de** déléguer la gestion opérationnelle d'une filiale à un responsable dédié.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasRole('ADMIN_GROUPE')")`
- [ ] La filiale cible doit appartenir au groupe de l'Admin Groupe
- [ ] Email unique au niveau plateforme → `409` si doublon
- [ ] Invitation par email avec lien d'activation (token UUID, expiration 48h)
- [ ] Utilisateur créé avec `actif = false` jusqu'à activation via lien
- [ ] Rôle forcé à `ADMIN_FILIALE`, scope `FILIALE`

**Endpoint :** `POST /api/v1/utilisateurs/admin-filiale`

**Corps :**
```json
{
  "filialeId": 5,
  "prenom": "Marie",
  "nom": "Ngono",
  "email": "marie.ngono@distribo.cm"
}
```

---

### US-022 — Créer un employé

**Priorité :** P0 | **Sprint :** 3 | **Points :** 3

**En tant qu'** Admin Filiale,
**je veux** créer un compte employé avec son rôle métier (Gestionnaire stock, Responsable achats, Commercial, Caissier),
**afin qu'il** accède uniquement aux fonctionnalités correspondant à ses responsabilités.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Rôle sélectionnable parmi : `GESTIONNAIRE_STOCK`, `RESP_ACHATS`, `COMMERCIAL`, `CAISSIER`
- [ ] Assigné à l'entreprise de l'Admin Filiale connecté (jamais à une autre filiale)
- [ ] Option A : création directe avec mot de passe provisoire
- [ ] Option B : invitation par email avec lien d'activation

**Endpoint :** `POST /api/v1/utilisateurs/employes`

**Corps :**
```json
{
  "prenom": "Claude",
  "nom": "Fotso",
  "email": "claude.fotso@boutique.cm",
  "role": "GESTIONNAIRE_STOCK",
  "motDePasseProvisoire": "Prov@2026"
}
```

---

### US-023 — Lister les utilisateurs

**Priorité :** P0 | **Sprint :** 3 | **Points :** 2

**En tant qu'** Admin Filiale ou Admin Groupe,
**je veux** voir la liste des utilisateurs de mon périmètre,
**afin de** gérer les accès et les rôles de mon équipe.

**Critères d'acceptation :**
- [ ] Admin Groupe : voit tous les utilisateurs de toutes ses filiales (filtre `group_id`)
- [ ] Admin Filiale : voit uniquement les utilisateurs de sa filiale (`entreprise_id`)
- [ ] Pagination + filtre par `role`, `actif`, `filialeId`
- [ ] Le mot de passe n'est JAMAIS retourné dans la réponse

**Endpoint :** `GET /api/v1/utilisateurs?role=COMMERCIAL&actif=true&page=0&size=20`

---

### US-024 — Modifier un utilisateur

**Priorité :** P0 | **Sprint :** 3 | **Points :** 2

**En tant qu'** Admin Filiale,
**je veux** modifier le nom, le rôle ou l'email d'un employé,
**afin de** maintenir les informations et permissions à jour.

**Critères d'acceptation :**
- [ ] Vérification que l'utilisateur cible appartient au périmètre de l'Admin connecté
- [ ] Modification du rôle journalisée (log INFO)
- [ ] Email modifié → vérification unicité

**Endpoint :** `PUT /api/v1/utilisateurs/{id}`

---

### US-025 — Désactiver un utilisateur

**Priorité :** P0 | **Sprint :** 3 | **Points :** 2

**En tant qu'** Admin Filiale,
**je veux** bloquer l'accès d'un employé sans supprimer son compte,
**afin de** gérer les départs sans perdre l'historique de ses actions.

**Critères d'acceptation :**
- [ ] `actif = false` → le token JWT de l'utilisateur cible est révoqué dans Redis
- [ ] L'utilisateur désactivé ne peut plus se connecter → `403 ACCOUNT_DISABLED`
- [ ] Historique des mouvements de stock créés par cet utilisateur conservé

**Endpoint :** `PATCH /api/v1/utilisateurs/{id}/statut`

**Corps :** `{ "actif": false }`

---

### US-026 — Consulter et modifier son profil

**Priorité :** P1 | **Sprint :** 4 | **Points :** 2

**En tant que** tout utilisateur connecté,
**je veux** consulter et modifier mes informations personnelles (nom, ville, photo),
**afin de** maintenir mon profil à jour.

**Critères d'acceptation :**
- [ ] Chaque utilisateur peut modifier uniquement son propre profil
- [ ] `@PreAuthorize("#id == authentication.principal.userId or hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Photo uploadée vers MinIO
- [ ] Email et rôle non modifiables par l'utilisateur lui-même (admin uniquement)

**Endpoints :**
- `GET /api/v1/utilisateurs/profil`
- `PUT /api/v1/utilisateurs/profil`

---

### US-101 — Contrôler la limite d'utilisateurs du plan

**Priorité :** P0 | **Sprint :** 3 | **Points :** 3

**En tant qu'** Admin Groupe,
**je veux** être bloqué quand j'atteins le nombre d'utilisateurs de mon plan,
**afin que** la grille commerciale soit réellement appliquée et non purement déclarative.

**Critères d'acceptation :**
- [ ] Contrôle appliqué à **toute création d'utilisateur** (US-019, US-022) et à toute réactivation d'un compte désactivé
- [ ] Limites issues de `DEC-015` : `GRATUIT` = 10, `PRO` = 50, `PERSONNALISE` = valeur négociée stockée en base
- [ ] La limite est lue depuis `tenant_group.limite_utilisateurs`, **jamais codée en dur** — c'est la faute que `DEC-015` reproche à l'existant (`AuthServiceImpl` avait 5 en dur)
- [ ] Dépassement → `403` avec `ErrorCode.USER_LIMIT_REACHED`, message indiquant le plan courant et sa limite
- [ ] Le décompte porte sur les utilisateurs **actifs et non supprimés** du groupe, toutes filiales confondues
- [ ] Symétrique de `limite_filiales` (US-016) : mêmes règles, même famille d'erreur
- [ ] Un plan expiré (`date_expiration_plan` dépassée) est traité comme `GRATUIT` pour le calcul de la limite

**Endpoint :** transverse (contrôle appliqué sur `POST /api/v1/utilisateurs` et la réactivation)

---

## EPIC 5 — Catalogue (Catégories & Articles)

> **Objectif :** Permettre la création et la gestion du catalogue produits qui sert de référentiel à toutes les opérations de stock, d'achat et de vente.

**Dépendance :** EPIC 4 complété.

---

### US-027 — Créer une catégorie

**Priorité :** P0 | **Sprint :** 4 | **Points :** 2

**En tant que** Gestionnaire de stock,
**je veux** créer une catégorie d'articles avec son taux de TVA,
**afin de** classer mes articles et appliquer le bon taux fiscal automatiquement.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Code unique par entreprise → `409 DUPLICATE_CODE`
- [ ] Taux TVA parmi les valeurs supportées (19.25%, 0%, ou taux réduit configuré)
- [ ] Isolation tenant : `entreprise_id` extrait du JWT, jamais du corps de la requête

**Endpoint :** `POST /api/v1/categories`

**Corps :**
```json
{
  "code": "ALIM",
  "designation": "Alimentation générale",
  "tauxTva": 19.25
}
```

---

### US-028 — Lister les catégories

**Priorité :** P0 | **Sprint :** 4 | **Points :** 1

**En tant que** tout utilisateur authentifié,
**je veux** voir la liste des catégories de mon entreprise,
**afin de** les utiliser lors de la création d'articles.

**Critères d'acceptation :**
- [ ] Résultat mis en cache Redis (`@Cacheable(value = "categories", key = "#principal.entrepriseId")`)
- [ ] Cache évicté à toute modification (`@CacheEvict`)
- [ ] Pagination + recherche par désignation

**Endpoint :** `GET /api/v1/categories?search=alim&page=0&size=20`

---

### US-029 — Modifier une catégorie

**Priorité :** P0 | **Sprint :** 4 | **Points :** 1

**En tant que** Gestionnaire de stock,
**je veux** modifier la désignation ou le taux de TVA d'une catégorie,
**afin de** corriger une erreur ou adapter la configuration fiscale.

**Critères d'acceptation :**
- [ ] Vérification isolation tenant
- [ ] Modification du taux TVA n'affecte pas les commandes existantes (snapshot figé sur les lignes)
- [ ] Cache catégories évicté

**Endpoint :** `PUT /api/v1/categories/{id}`

---

### US-030 — Supprimer une catégorie

**Priorité :** P0 | **Sprint :** 4 | **Points :** 1

**En tant que** Gestionnaire de stock,
**je veux** supprimer une catégorie vide,
**afin de** nettoyer le catalogue.

**Critères d'acceptation :**
- [ ] Soft delete (`supprime = true`)
- [ ] Bloqué si la catégorie contient des articles actifs → `409 ENTITY_HAS_DEPENDENCIES`

**Endpoint :** `DELETE /api/v1/categories/{id}`

---

### US-031 — Créer un article

**Priorité :** P0 | **Sprint :** 4 | **Points :** 5

**En tant que** Gestionnaire de stock,
**je veux** créer un article avec son prix d'achat, son prix de vente et son seuil d'alerte,
**afin qu'il** soit disponible dans le catalogue pour les commandes et les ventes.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] `code_article` unique par entreprise → `409 DUPLICATE_CODE`
- [ ] `prix_vente_ttc` calculé côté serveur : `prix_vente_ht × (1 + taux_tva / 100)` — JAMAIS saisi
- [ ] Taux TVA hérité de la catégorie si non fourni, surchargeable individuellement
- [ ] `seuil_alerte = 0` → alerte désactivée pour cet article
- [ ] Photo uploadée vers MinIO (facultatif)
- [ ] Isolation tenant stricte

**Endpoint :** `POST /api/v1/articles`

**Corps :**
```json
{
  "codeArticle": "RIZ50KG",
  "designation": "Riz parfumé 50 kg",
  "categorieId": 3,
  "prixAchatHt": 15000,
  "prixVenteHt": 18500,
  "tauxTva": 19.25,
  "seuilAlerte": 10
}
```

**Réponse :** inclut `prixVenteTtc` calculé et `margebrute_pct` calculée à la volée

---

### US-032 — Lister les articles

**Priorité :** P0 | **Sprint :** 4 | **Points :** 3

**En tant que** tout utilisateur authentifié,
**je veux** rechercher et filtrer les articles de mon entreprise,
**afin de** trouver rapidement un article lors d'une commande ou d'une vente.

**Critères d'acceptation :**
- [ ] Recherche full-text (désignation + code article) via index GIN PostgreSQL
- [ ] Filtre par `categorieId`, `actif`, `stockBas` (stock ≤ seuil_alerte)
- [ ] Chaque article retourné inclut `stockActuel` calculé en temps réel
- [ ] Pagination obligatoire (max 100 par page)
- [ ] `@Transactional(readOnly = true)`

**Endpoint :** `GET /api/v1/articles?search=riz&categorieId=3&stockBas=true&page=0&size=20`

---

### US-033 — Consulter un article

**Priorité :** P0 | **Sprint :** 4 | **Points :** 2

**En tant que** tout utilisateur authentifié,
**je veux** consulter la fiche complète d'un article,
**afin de** voir son stock actuel, ses prix, son taux de TVA et son statut d'alerte.

**Critères d'acceptation :**
- [ ] Retourne : tous les champs article + `stockActuel` + `margeBrutePct` + `statutAlerte` (NORMAL / BAS / RUPTURE / ANOMALIE — REF §4.2)
- [ ] Article appartenant à une autre entreprise → `404` (ne pas révéler l'existence)

**Endpoint :** `GET /api/v1/articles/{id}`

---

### US-034 — Modifier un article

**Priorité :** P0 | **Sprint :** 4 | **Points :** 2

**En tant que** Gestionnaire de stock,
**je veux** modifier les informations d'un article (prix, seuil d'alerte, photo),
**afin de** maintenir le catalogue à jour.

**Critères d'acceptation :**
- [ ] `prix_vente_ttc` recalculé automatiquement si `prix_vente_ht` ou `taux_tva` modifié
- [ ] Modification du prix n'affecte pas les commandes existantes (snapshot figé)
- [ ] Cache articles évicté

**Endpoint :** `PUT /api/v1/articles/{id}`

---

### US-035 — Supprimer (archiver) un article

**Priorité :** P0 | **Sprint :** 4 | **Points :** 2

**En tant que** Gestionnaire de stock,
**je veux** archiver un article qui n'est plus commercialisé,
**afin de** le retirer du catalogue actif tout en conservant l'historique.

**Critères d'acceptation :**
- [ ] Soft delete si article non référencé dans aucune commande ni vente → `supprime = true`
- [ ] Si référencé → `409 ENTITY_HAS_DEPENDENCIES` avec message explicite "Archivez l'article plutôt que de le supprimer"
- [ ] Article avec stock > 0 → peut être archivé (`actif = false`) mais pas supprimé

**Endpoint :** `DELETE /api/v1/articles/{id}`

---

### US-102 — Rattacher le catalogue au groupe ⭐

**Priorité :** P1 | **Sprint :** 5 | **Points :** 8

**En tant qu'** Admin Groupe,
**je veux** définir mes articles une seule fois pour tout le groupe,
**afin de** ne pas ressaisir le même produit dans chaque filiale et de rendre les transferts possibles.

**Critères d'acceptation :**
- [ ] `article` et `categorie` portent `group_id` et non plus `entreprise_id` (`DEC-002`) — **exception documentée** à la règle générale d'isolation par `entreprise_id`
- [ ] Unicité du code article au niveau **groupe** : index `(group_id, code_article)`
- [ ] **Le prix, le seuil d'alerte et le stock restent par filiale** — seule la définition de l'article est partagée
- [ ] Toute lecture reste filtrée : `group_id` du JWT, jamais du corps de requête
- [ ] Un utilisateur de filiale voit le catalogue du groupe mais ne modifie que ce que son rôle autorise
- [ ] Prérequis explicite du transfert inter-filiales (US-106) : sans article commun, un transfert n'a pas de sens
- [ ] GRP-08 passe de P2 à **P1** et précède le transfert (`DEC-002`)

**Endpoint :** impacte `GET|POST|PUT /api/v1/articles` et `/api/v1/categories`

---

### US-103 — Unité de gestion, unité d'achat et facteur de conversion

**Priorité :** P1 | **Sprint :** 5 | **Points :** 5

**En tant que** Gestionnaire de stock,
**je veux** acheter en carton et vendre à l'unité,
**afin de** ne pas convertir mentalement à chaque réception.

**Critères d'acceptation :**
- [ ] Trois champs sur l'article : `unite_gestion` (unité de stock et de vente), `unite_achat`, `facteur_conversion` (`DEC-013`)
- [ ] `facteur_conversion` en `DECIMAL(12,3)` strictement > 0 ; vaut 1 si les deux unités sont identiques
- [ ] **Conversion appliquée à la réception** : une réception de N unités d'achat crée un mouvement `ENTREE` de `N × facteur` en unité de gestion
- [ ] Le stock, les seuils et les ventes s'expriment **toujours** en unité de gestion — jamais de stock exprimé en deux unités
- [ ] Le mouvement conserve la quantité en unité de gestion ; l'unité d'achat n'apparaît que sur la ligne de commande
- [ ] Le **code-barres est hors périmètre V1** — reporté au jalon V1.5 (`DEC-013`)

**Endpoint :** impacte `POST|PUT /api/v1/articles` et `POST /api/v1/commandes-fournisseur/{id}/livrer`

---

### US-104 — Lot et date de péremption (schéma lot-ready)

**Priorité :** P1 | **Sprint :** 6 | **Points :** 5

**En tant que** Gestionnaire de stock,
**je veux** enregistrer le lot et la date de péremption à la réception,
**afin de** pouvoir tracer un rappel produit et préparer la sortie FEFO.

**Critères d'acceptation :**
- [ ] `lot` et `date_peremption` portés par le mouvement de stock dès la migration V5 (`DEC-012`)
- [ ] Le stock est calculé par **article, lot et filiale** dès l'origine — le découpage n'est pas rétro-ajouté plus tard
- [ ] Champs **optionnels** : un article non périssable les laisse vides, sans friction de saisie
- [ ] Saisie proposée à la réception (US-049) et à la correction positive (US-053)
- [ ] **Hors périmètre V1, livré au jalon V1.5** (`DEC-012`) : la sortie FEFO automatique, l'alerte de péremption et le choix de lot en caisse
- [ ] En V1, la sortie ne choisit pas de lot : le champ est renseigné mais n'arbitre rien

**Endpoint :** impacte `POST /api/v1/commandes-fournisseur/{id}/livrer` et `POST /api/v1/stock/corrections`

---

## EPIC 6 — Tiers (Clients & Fournisseurs)

> **Objectif :** Gérer le référentiel des partenaires commerciaux (clients B2B et fournisseurs) qui sont rattachés aux commandes.

**Dépendance :** EPIC 4 complété.

---

### US-036 — Créer un fournisseur

**Priorité :** P0 | **Sprint :** 5 | **Points :** 2

**En tant que** Responsable achats,
**je veux** créer une fiche fournisseur avec ses coordonnées,
**afin de** l'associer aux commandes d'approvisionnement.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Isolation tenant : fournisseur lié à l'`entreprise_id` du JWT
- [ ] Validation : `raisonSociale` obligatoire, `telephone` format camerounais si fourni

**Endpoint :** `POST /api/v1/fournisseurs`

---

### US-037 — Lister / Rechercher les fournisseurs

**Priorité :** P0 | **Sprint :** 5 | **Points :** 1

**En tant que** Responsable achats,
**je veux** rechercher un fournisseur par nom ou ville,
**afin de** le retrouver rapidement lors d'une commande.

**Endpoint :** `GET /api/v1/fournisseurs?search=somiref&page=0&size=20`

---

### US-038 — Modifier un fournisseur

**Priorité :** P0 | **Sprint :** 5 | **Points :** 1

**En tant que** Responsable achats,
**je veux** mettre à jour les coordonnées d'un fournisseur,
**afin de** maintenir les contacts à jour.

**Endpoint :** `PUT /api/v1/fournisseurs/{id}`

---

### US-039 — Supprimer un fournisseur

**Priorité :** P0 | **Sprint :** 5 | **Points :** 1

**En tant que** Responsable achats,
**je veux** supprimer un fournisseur inactif,
**afin de** nettoyer le référentiel.

**Critères d'acceptation :**
- [ ] Bloqué si des commandes fournisseur lui sont associées → `409 ENTITY_HAS_DEPENDENCIES`
- [ ] Soft delete sinon

**Endpoint :** `DELETE /api/v1/fournisseurs/{id}`

---

### US-040 — Créer un client

**Priorité :** P0 | **Sprint :** 5 | **Points :** 2

**En tant que** Commercial,
**je veux** créer une fiche client avec ses coordonnées,
**afin de** l'associer aux commandes de vente B2B.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Isolation tenant stricte

**Endpoint :** `POST /api/v1/clients`

---

### US-041 — Lister / Rechercher les clients

**Priorité :** P0 | **Sprint :** 5 | **Points :** 1

**En tant que** Commercial,
**je veux** rechercher un client par nom ou téléphone,
**afin de** le retrouver rapidement lors d'une commande.

**Endpoint :** `GET /api/v1/clients?search=martin&page=0&size=20`

---

### US-042 — Modifier un client

**Priorité :** P0 | **Sprint :** 5 | **Points :** 1

**En tant que** Commercial,
**je veux** mettre à jour les coordonnées d'un client.

**Endpoint :** `PUT /api/v1/clients/{id}`

---

### US-043 — Supprimer un client

**Priorité :** P0 | **Sprint :** 5 | **Points :** 1

**En tant que** Commercial,
**je veux** supprimer un client sans historique de commandes.

**Critères d'acceptation :**
- [ ] Bloqué si commandes existantes → `409 ENTITY_HAS_DEPENDENCIES`

**Endpoint :** `DELETE /api/v1/clients/{id}`

---

## EPIC 7 — Commandes Fournisseur (Achats)

> **Objectif :** Gérer le cycle complet d'approvisionnement — de la création de la commande à la réception physique — avec génération automatique des mouvements d'entrée de stock à la validation.

**Dépendance :** EPIC 5 + EPIC 6 complétés.

---

### US-044 — Créer une commande fournisseur

**Priorité :** P0 | **Sprint :** 6 | **Points :** 5

**En tant que** Responsable achats,
**je veux** créer une commande fournisseur en saisissant les articles et quantités commandés,
**afin d'** initier un réapprovisionnement de stock.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Fournisseur appartient à la même entreprise (isolation tenant)
- [ ] Articles appartiennent à la même entreprise (isolation tenant)
- [ ] Commande créée à l'état `EN_PREPARATION`
- [ ] Code commande généré automatiquement : `CF-{ANNEE}-{SEQUENCE}`
- [ ] Taux TVA snapshot figé sur chaque ligne au moment de la saisie
- [ ] Au moins 1 ligne obligatoire
- [ ] `prix_unitaire` snapshot saisi manuellement (prix négocié avec le fournisseur)

**Endpoint :** `POST /api/v1/commandes-fournisseur`

**Corps :**
```json
{
  "fournisseurId": 2,
  "commentaire": "Commande mensuelle",
  "lignes": [
    { "articleId": 10, "quantite": 50, "prixUnitaire": 15000 },
    { "articleId": 11, "quantite": 20, "prixUnitaire": 8500 }
  ]
}
```

---

### US-045 — Lister les commandes fournisseur

**Priorité :** P0 | **Sprint :** 6 | **Points :** 2

**En tant que** Responsable achats,
**je veux** voir la liste de mes commandes fournisseur avec filtres,
**afin de** suivre l'avancement des approvisionnements.

**Critères d'acceptation :**
- [ ] Filtres : `etat` (EN_PREPARATION / VALIDEE / LIVREE), `fournisseurId`, `dateDebut`, `dateFin`
- [ ] Pagination obligatoire
- [ ] Totaux HT et TTC calculés à la volée depuis les lignes (jamais stockés)

**Endpoint :** `GET /api/v1/commandes-fournisseur?etat=EN_PREPARATION&page=0&size=20`

---

### US-046 — Consulter le détail d'une commande fournisseur

**Priorité :** P0 | **Sprint :** 6 | **Points :** 2

**En tant que** tout utilisateur authentifié,
**je veux** voir le détail complet d'une commande fournisseur,
**afin de** vérifier les articles, quantités, prix et l'état de la commande.

**Critères d'acceptation :**
- [ ] Retourne : en-tête + toutes les lignes + totaux HT / TVA / TTC calculés
- [ ] Isolation tenant stricte

**Endpoint :** `GET /api/v1/commandes-fournisseur/{id}`

---

### US-047 — Modifier une commande EN_PREPARATION

**Priorité :** P0 | **Sprint :** 6 | **Points :** 3

**En tant que** Responsable achats,
**je veux** modifier les lignes d'une commande en cours de saisie,
**afin de** corriger les quantités ou les prix avant validation.

**Critères d'acceptation :**
- [ ] Modification impossible si état ≠ `EN_PREPARATION` → `409 ORDER_NOT_MODIFIABLE`
- [ ] Remplacement complet des lignes (PUT sémantique sur les lignes)

**Endpoint :** `PUT /api/v1/commandes-fournisseur/{id}`

---

### US-048 — Valider une commande fournisseur ⭐

**Priorité :** P0 | **Sprint :** 6 | **Points :** 8

**En tant que** Responsable achats,
**je veux** valider une commande fournisseur à la réception des marchandises,
**afin que** le stock soit automatiquement mis à jour avec les quantités reçues.

**Critères d'acceptation :**
- [ ] État = `EN_PREPARATION` obligatoire → sinon `409 ORDER_NOT_MODIFIABLE`
- [ ] Au moins 1 ligne → sinon `400 ORDER_HAS_NO_LINES`
- [ ] **Opération atomique** : pour chaque ligne → 1 mouvement `ENTREE` créé + stock mis à jour
- [ ] Si une création de mouvement échoue → rollback complet (aucun mouvement créé)
- [ ] Commande passe à l'état `VALIDEE` — plus modifiable ni supprimable
- [ ] `StockUpdatedEvent` publié pour chaque article (déclenche vérification alerte)
- [ ] Log INFO : "Commande {code} validée — {N} mouvements ENTREE créés"

**Endpoint :** `POST /api/v1/commandes-fournisseur/{id}/valider`

---

### US-049 — Marquer une commande fournisseur comme livrée

**Priorité :** P0 | **Sprint :** 6 | **Points :** 2

**En tant que** Responsable achats,
**je veux** confirmer la réception physique complète d'une commande,
**afin de** clôturer le dossier d'approvisionnement.

**Critères d'acceptation :**
- [ ] État = `VALIDEE` obligatoire pour passer à `LIVREE`
- [ ] `LIVREE` = état final immuable, aucune transition possible

**Endpoint :** `POST /api/v1/commandes-fournisseur/{id}/livrer`

---

### US-050 — Supprimer une commande fournisseur EN_PREPARATION

**Priorité :** P0 | **Sprint :** 6 | **Points :** 1

**En tant que** Responsable achats,
**je veux** supprimer une commande non encore validée,
**afin de** annuler une saisie erronée.

**Critères d'acceptation :**
- [ ] Soft delete uniquement si état = `EN_PREPARATION`
- [ ] Impossible si état `VALIDEE` ou `LIVREE` → `409 ORDER_CANNOT_BE_DELETED`

**Endpoint :** `DELETE /api/v1/commandes-fournisseur/{id}`

---

## EPIC 8 — Gestion du Stock

> **Objectif :** Permettre la consultation du stock en temps réel et les corrections manuelles pour maintenir la cohérence entre le stock système et le stock physique.

**Dépendance :** EPIC 7 (au moins US-048) pour avoir des données de stock réelles.

---

### US-051 — Consulter le stock réel par article

**Priorité :** P0 | **Sprint :** 7 | **Points :** 3

**En tant que** tout utilisateur authentifié,
**je veux** consulter le stock réel de chaque article de mon entreprise,
**afin de** connaître les disponibilités à tout moment.

**Critères d'acceptation :**
- [ ] Stock calculé en temps réel : `Σ(ENTREE + CORRECTION_POS + TRANSFERT_ENTREE + ANNULATION_VENTE + REMBOURSEMENT) - Σ(SORTIE + CORRECTION_NEG + TRANSFERT_SORTIE)` (`DEC-010`, REF §4.2)
- [ ] Double filtre obligatoire : `article_id` ET `entreprise_id`
- [ ] Retourne : `stockActuel`, `seuilAlerte`, `statutAlerte` (NORMAL / BAS / RUPTURE / ANOMALIE — REF §4.2)
- [ ] Pagination + filtre `statutAlerte`

**Endpoint :** `GET /api/v1/stock?statutAlerte=BAS&page=0&size=20`

---

### US-052 — Historique des mouvements d'un article

**Priorité :** P0 | **Sprint :** 7 | **Points :** 3

**En tant que** Gestionnaire de stock,
**je veux** consulter l'historique complet des mouvements d'un article,
**afin de** retracer toutes les entrées, sorties et corrections avec leur origine.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Retourne : type, quantité, date, utilisateur, origine (commande / vente / correction / transfert)
- [ ] Filtres : `typeMouvement`, `dateDebut`, `dateFin`
- [ ] Pagination obligatoire
- [ ] Tri par date DESC par défaut

**Endpoint :** `GET /api/v1/stock/articles/{articleId}/mouvements?typeMouvement=ENTREE&page=0&size=20`

---

### US-053 — Correction positive de stock

**Priorité :** P0 | **Sprint :** 7 | **Points :** 3

**En tant que** Gestionnaire de stock,
**je veux** ajouter manuellement du stock (suite à un inventaire ou un don),
**afin de** corriger une divergence entre le stock système et le stock physique réel.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] `motif` obligatoire → `400` si absent (`ErrorCode.MOTIF_REQUIRED`)
- [ ] Mouvement `CORRECTION_POS` créé avec `origine_type = 'CORRECTION'`
- [ ] `quantite > 0` validé côté Jakarta + contrainte BDD
- [ ] `StockUpdatedEvent` publié

**Endpoint :** `POST /api/v1/stock/corrections`

**Corps :**
```json
{
  "articleId": 10,
  "type": "POSITIVE",
  "quantite": 15,
  "motif": "Inventaire physique du 10/06/2026 — surplus constaté"
}
```

---

### US-054 — Correction négative de stock

**Priorité :** P0 | **Sprint :** 7 | **Points :** 3

**En tant que** Gestionnaire de stock,
**je veux** retirer manuellement du stock (casse, perte, vol constaté),
**afin de** corriger un stock système supérieur à la réalité physique.

**Critères d'acceptation :**
- [ ] `motif` obligatoire
- [ ] Mouvement `CORRECTION_NEG` créé
- [ ] Warning si correction négative > stock actuel (stock peut devenir négatif — comportement paramétrable)
- [ ] `StockUpdatedEvent` publié → vérification alerte

**Endpoint :** `POST /api/v1/stock/corrections`

**Corps :**
```json
{
  "articleId": 10,
  "type": "NEGATIVE",
  "quantite": 3,
  "motif": "Casse constatée lors du déchargement du 10/06/2026"
}
```

---

### US-055 — Stock consolidé groupe

**Priorité :** P1 | **Sprint :** 8 | **Points :** 5

**En tant qu'** Admin Groupe,
**je veux** voir le stock agrégé de toutes mes filiales par article,
**afin de** prendre des décisions de réapprovisionnement ou de transfert à l'échelle du groupe.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasRole('ADMIN_GROUPE')")`
- [ ] Agrégation par `article_id` sur toutes les `entreprise_id` du groupe
- [ ] Retourne pour chaque article : stock total groupe + détail par filiale
- [ ] `@Transactional(readOnly = true)` + cache Redis (TTL 2 minutes)

**Endpoint :** `GET /api/v1/groupe/stock-consolide?page=0&size=20`

---

## EPIC 9 — Commandes Client (Ventes B2B)

> **Objectif :** Gérer le cycle complet de vente B2B avec vérification de stock avant validation et génération automatique des mouvements de sortie.

**Dépendance :** EPIC 7 + EPIC 8 complétés (stock doit exister avant de vendre).

---

### US-056 — Créer une commande client

**Priorité :** P0 | **Sprint :** 7 | **Points :** 5

**En tant que** Commercial,
**je veux** créer une commande client en saisissant les articles demandés et les prix de vente,
**afin d'** initier une transaction de vente B2B.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Client appartient à la même entreprise (isolation tenant)
- [ ] Articles appartiennent à la même entreprise
- [ ] Code commande généré : `CC-{ANNEE}-{SEQUENCE}`
- [ ] Taux TVA snapshot figé sur chaque ligne
- [ ] Au moins 1 ligne obligatoire
- [ ] État initial : `EN_PREPARATION`

**Endpoint :** `POST /api/v1/commandes-client`

**Corps :**
```json
{
  "clientId": 7,
  "commentaire": "Livraison urgente",
  "lignes": [
    { "articleId": 10, "quantite": 5, "prixUnitaire": 18500 },
    { "articleId": 12, "quantite": 2, "prixUnitaire": 45000 }
  ]
}
```

---

### US-057 — Lister les commandes client

**Priorité :** P0 | **Sprint :** 7 | **Points :** 2

**En tant que** Commercial,
**je veux** voir la liste de mes commandes client avec filtres,
**afin de** suivre les ventes et les livraisons en cours.

**Critères d'acceptation :**
- [ ] Filtres : `etat`, `clientId`, `dateDebut`, `dateFin`
- [ ] Pagination obligatoire

**Endpoint :** `GET /api/v1/commandes-client?etat=VALIDEE&clientId=7&page=0&size=20`

---

### US-058 — Consulter le détail d'une commande client

**Priorité :** P0 | **Sprint :** 7 | **Points :** 2

**En tant que** tout utilisateur authentifié,
**je veux** voir le détail complet d'une commande client avec les totaux,
**afin de** vérifier le bon de commande avant validation ou livraison.

**Endpoint :** `GET /api/v1/commandes-client/{id}`

---

### US-059 — Modifier une commande client EN_PREPARATION

**Priorité :** P0 | **Sprint :** 7 | **Points :** 3

**En tant que** Commercial,
**je veux** modifier les lignes d'une commande avant validation,
**afin de** corriger les quantités ou prix à la demande du client.

**Critères d'acceptation :**
- [ ] Modification impossible si état ≠ `EN_PREPARATION` → `409 ORDER_NOT_MODIFIABLE`

**Endpoint :** `PUT /api/v1/commandes-client/{id}`

---

### US-060 — Valider une commande client ⭐

**Priorité :** P0 | **Sprint :** 7 | **Points :** 8

**En tant que** Commercial,
**je veux** valider une commande client après vérification du stock disponible,
**afin que** les sorties de stock soient enregistrées et la vente confirmée.

**Critères d'acceptation :**
- [ ] État = `EN_PREPARATION` obligatoire
- [ ] **Vérification stock AVANT toute création de mouvement** : pour chaque ligne, `stock_réel ≥ quantité` ?
- [ ] Si au moins 1 article insuffisant → `409 INSUFFICIENT_STOCK` avec liste détaillée des articles en rupture (code, désignation, disponible, demandé). **Aucun mouvement créé.**
- [ ] Si tous les stocks suffisants → **opération atomique** : 1 mouvement `SORTIE` par ligne
- [ ] Commande passe à `VALIDEE`
- [ ] `StockUpdatedEvent` publié pour chaque article (vérification alerte)

**Endpoint :** `POST /api/v1/commandes-client/{id}/valider`

---

### US-061 — Marquer une commande client comme livrée

**Priorité :** P0 | **Sprint :** 7 | **Points :** 2

**En tant que** Commercial,
**je veux** confirmer que les marchandises ont été livrées chez le client,
**afin de** clôturer le dossier de vente.

**Critères d'acceptation :**
- [ ] État = `VALIDEE` obligatoire pour passer à `LIVREE`
- [ ] `LIVREE` = état final immuable

**Endpoint :** `POST /api/v1/commandes-client/{id}/livrer`

---

### US-062 — Supprimer une commande client EN_PREPARATION

**Priorité :** P0 | **Sprint :** 7 | **Points :** 1

**En tant que** Commercial,
**je veux** annuler une commande client non validée,
**afin de** corriger une saisie erronée.

**Critères d'acceptation :**
- [ ] Soft delete uniquement si état = `EN_PREPARATION`

**Endpoint :** `DELETE /api/v1/commandes-client/{id}`

---

### US-063 — Générer la facture PDF

**Priorité :** P1 | **Sprint :** 9 | **Points :** 5

**En tant que** Commercial ou Admin,
**je veux** générer une facture PDF pour une commande validée,
**afin de** la transmettre au client comme justificatif commercial.

**Critères d'acceptation :**
- [ ] Disponible uniquement si état = `VALIDEE` ou `LIVREE`
- [ ] Contient : entête entreprise, coordonnées client, lignes (désignation, qté, PU HT, TVA, TTC), totaux
- [ ] Numéro de facture = code commande
- [ ] Stockée dans MinIO avec URL accessible
- [ ] Format PDF conforme aux standards camerounais (XAF, mention TVA 19,25%)

**Endpoint :** `GET /api/v1/commandes-client/{id}/facture`

---

### US-105 — Règlement et échéance d'une commande client

**Priorité :** P1 | **Sprint :** 7 | **Points :** 3

**En tant que** Commercial,
**je veux** savoir quelles factures B2B sont réglées et lesquelles sont en retard,
**afin de** relancer les clients qui me doivent de l'argent.

**Critères d'acceptation :**
- [ ] Deux champs sur la commande client : `etat_reglement` (`NON_REGLEE` | `REGLEE`) et `date_echeance` (`DEC-011`)
- [ ] `date_reglement` renseignée automatiquement au passage à `REGLEE`
- [ ] Une commande livrée mais non réglée reste `NON_REGLEE` — **livraison et règlement sont deux axes indépendants**
- [ ] Filtre « en retard » : `etat_reglement = NON_REGLEE` ET `date_echeance < aujourd'hui` en `Africa/Douala` (`DEC-021`)
- [ ] Le CA n'est **pas** recalculé au règlement : `DEC-020` retient le CA facturé, jamais rétroactif
- [ ] Aucun encaissement partiel en V1 : l'état est binaire, assumé comme tel

**Endpoint :** `PATCH /api/v1/commandes-client/{id}/reglement`

**Corps :**
```json
{
  "etatReglement": "REGLEE"
}
```

---

## EPIC 10 — Vente Directe (Caisse)

> **Objectif :** Permettre au Caissier d'enregistrer des ventes comptoir sans client identifié, avec mise à jour immédiate du stock.

**Dépendance :** EPIC 5 + EPIC 8 complétés.

---

### US-064 — Enregistrer une vente directe

**Priorité :** P0 | **Sprint :** 8 | **Points :** 5

**En tant que** Caissier,
**je veux** enregistrer rapidement une vente comptoir sans saisir de client,
**afin de** traiter les transactions au point de vente sans délai.

> **Décision `DEC-023` / `DEC-017` (révise GS-CDA-2026-02 §1)** : la Vente Directe **bloque** sur stock insuffisant, comme la Commande Client. L'invariant produit est que le stock d'un article ne peut jamais devenir négatif : une vente qui l'y ferait passer est refusée en `409`. L'ancien raisonnement (« le caissier voit l'article, donc on laisse passer ») est écarté — il traitait un écart de donnée en le propageant dans le journal de mouvements, rendant le stock système définitivement faux. L'écart entre stock physique et stock système se constate et se corrige par les **campagnes d'inventaire** (`DEC-036`), seul organe capable de le découvrir.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('CAISSIER','COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Pas de client requis (vente anonyme) — `clientId` optionnel (voir US-064b)
- [ ] **Vérification bloquante de stock** — si le stock réel passerait sous 0, la vente est **refusée en 409** (`DEC-017`, `DEC-023`) ; le mouvement `SORTIE` n'est créé que si le stock reste ≥ 0
- [ ] Aucune `NotificationAlerte` d'écart n'est créée à la vente (`ECART_STOCK_DETECTE` supprimé par `DEC-037`) — la détection d'écart relève des campagnes d'inventaire (`DEC-036`)
- [ ] Code vente généré : `VNT-{DATE}-{SEQUENCE}`
- [ ] **Opération atomique** : 1 mouvement `SORTIE` par ligne
- [ ] Totaux HT / TVA / TTC calculés côté serveur
- [ ] `StockUpdatedEvent` publié

**Endpoint :** `POST /api/v1/ventes`

**Corps :**
```json
{
  "clientId": null,
  "lignes": [
    { "articleId": 10, "quantite": 2, "prixUnitaire": 18500 },
    { "articleId": 15, "quantite": 1, "prixUnitaire": 5000 }
  ],
  "commentaire": "Vente comptoir"
}
```

---

### US-064b — Associer un client existant à une vente directe

**Priorité :** P1 | **Sprint :** 8 | **Points :** 2

**En tant que** Caissier,
**je veux** pouvoir rattacher optionnellement une vente comptoir à un client déjà enregistré,
**afin de** faire remonter les achats des clients réguliers du comptoir dans leur historique et dans les statistiques de fidélité (STAT-02), sans les faire passer par le cycle complet de Commande Client.

> **Décision validée GS-CDA-2026-02 (§3)** : STAT-02 / CLI-05 ne s'appuyaient que sur `CommandeClient` (B2B), rendant invisibles les clients réguliers achetant au comptoir — majoritaires pour un commerce général/quincaillerie. Ce gap est corrigé par un champ nullable, pas par un changement de workflow.

**Critères d'acceptation :**
- [ ] Champ `client_id` (FK nullable vers `Client`) ajouté à l'entité `Vente`
- [ ] Vente sans `clientId` reste 100% valide (vente anonyme conservée par défaut)
- [ ] Si `clientId` fourni : la vente apparaît dans l'historique du client (CLI-05) et contribue à STAT-02
- [ ] Aucun impact sur les règles US-064 (vérification bloquante de stock, `DEC-023`)

**Endpoint :** inclus dans `POST /api/v1/ventes` (champ `clientId` déjà présent dans le corps ci-dessus) + `GET /api/v1/clients/{id}/historique` mis à jour pour inclure les ventes directes rattachées

---

### US-065 — Lister les ventes directes

**Priorité :** P0 | **Sprint :** 8 | **Points :** 2

**En tant qu'** Admin Filiale,
**je veux** consulter le journal de caisse avec filtres,
**afin de** contrôler l'activité quotidienne de la caisse.

**Critères d'acceptation :**
- [ ] Filtres : `dateDebut`, `dateFin`, `utilisateurId`, `articleId`
- [ ] Pagination obligatoire
- [ ] Total journalier calculé et retourné

**Endpoint :** `GET /api/v1/ventes?dateDebut=2026-06-01&dateFin=2026-06-10&page=0&size=20`

---

### US-066 — Consulter une vente directe

**Priorité :** P0 | **Sprint :** 8 | **Points :** 1

**En tant que** Caissier,
**je veux** consulter le détail d'une vente enregistrée,
**afin de** vérifier ou imprimer le ticket.

**Endpoint :** `GET /api/v1/ventes/{id}`

---

### US-067 — Annuler une vente directe

**Priorité :** P1 | **Sprint :** 9 | **Points :** 5

**En tant que** Caissier ou Admin Filiale,
**je veux** annuler une vente du jour en cas d'erreur,
**afin de** corriger une saisie fautive avant la clôture de caisse, sans jamais modifier le journal de mouvements de stock.

> **Décision validée GS-CDA-2026-02 (§2)** : `CORRECTION_POS` est réservé aux corrections d'inventaire (écart constaté physiquement, motif = "Inventaire du ..."). L'utiliser pour une annulation de vente masquerait la cause réelle du mouvement dans le journal (`origine_type` perdrait son sens) et rendrait impossible de distinguer, a posteriori, "on a vendu et annulé" de "on a fait un inventaire". D'où l'introduction d'un type dédié `ANNULATION_VENTE`, qui reste une entrée compensatoire mais garde une traçabilité exacte de sa cause.

**Modification du modèle (à répercuter sur CDCT + migration Flyway) :**
- [ ] Nouvelle valeur d'enum `type_mouvement` : `ANNULATION_VENTE` (entrée compensatoire — remet la quantité en stock, au même titre que `ENTREE`/`CORRECTION_POS`/`TRANSFERT_ENTREE` dans la formule de stock réel)
- [ ] Nouvel état sur l'entité `Vente` : `statut` (enum `PAYEE` | `ANNULEE` | `REMBOURSEE`) — remplace le booléen `annulee` ; `REMBOURSEE` couvert par `DEC-010` (remboursement en caisse) et confirmé par `DEC-018` (contre `VALIDEE | ANNULEE`)

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('CAISSIER','COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")` — le `COMMERCIAL` est inclus : `DEC-018` a tranché que celui qui encaisse (US-064 l'y autorise) doit pouvoir corriger sa propre erreur
- [ ] Règle citable (`DEC-018`) : *« Quiconque a créé une vente peut l'annuler le jour même, dans sa propre session. »* — le contrôle porte sur la **session de caisse** (`DEC-009`), pas sur le rôle
- [ ] Annulation possible uniquement si la vente appartient à la **session de caisse ouverte de l'utilisateur courant** et `statut = PAYEE`
- [ ] La journée est bornée par la session, pas par `CURRENT_DATE` — la journée comptable court 00:00-23:59 en `Africa/Douala` (`DEC-021`), jamais en UTC
- [ ] Vente déjà `ANNULEE` → `409 CONFLICT` avec `ErrorCode.VENTE_DEJA_ANNULEE`
- [ ] **Le mouvement `SORTIE` original n'est ni modifié ni supprimé** (immuabilité absolue du journal, règle CDA §6.4)
- [ ] Un mouvement `ANNULATION_VENTE` est créé pour chaque ligne de la vente annulée, avec `origine_type = ANNULATION_VENTE` et `origine_id` = id de la vente annulée
- [ ] La vente passe à `statut = ANNULEE` (jamais supprimée physiquement — cohérent avec le soft delete général)
- [ ] `motif` obligatoire (même exigence que pour les corrections manuelles — traçabilité de l'audit trail)
- [ ] **Opération atomique** : tous les mouvements `ANNULATION_VENTE` créés + statut mis à jour, ou aucun (transaction)

**Endpoint :** `POST /api/v1/ventes/{id}/annuler`

**Corps :** `{ "motif": "Erreur de saisie — client a changé de commande" }`

---

## EPIC 11 — Transferts Inter-Filiales

> **Objectif :** Permettre à l'Admin Groupe de déplacer du stock d'une filiale vers une autre avec traçabilité complète via un bon de transfert.

**Dépendance :** EPIC 8 complété. Au moins 2 filiales créées avec du stock.

---

### US-068 — Créer un transfert inter-filiales ⭐

**Priorité :** P1 | **Sprint :** 8 | **Points :** 8

**En tant qu'** Admin Groupe,
**je veux** transférer une quantité d'un article d'une filiale vers une autre,
**afin d'** équilibrer les stocks entre mes points de vente sans passer par un fournisseur.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasRole('ADMIN_GROUPE')")`
- [ ] Source ≠ cible → sinon `400 SAME_SOURCE_AND_TARGET`
- [ ] Les deux filiales appartiennent au même groupe → sinon `403 CROSS_GROUP_FORBIDDEN`
- [ ] Stock source suffisant → sinon `409 INSUFFICIENT_STOCK`
- [ ] **Opération atomique (une seule transaction)** :
  - 1 mouvement `TRANSFERT_SORTIE` sur la filiale source
  - 1 mouvement `TRANSFERT_ENTREE` sur la filiale cible
  - Les deux référencent le même `transfert_id`
- [ ] Bon de transfert généré avec référence `TRF-{ANNEE}-{SEQUENCE}`
- [ ] `StockUpdatedEvent` publié pour les 2 filiales

**Endpoint :** `POST /api/v1/groupe/transferts`

**Corps :**
```json
{
  "filialeSourceId": 3,
  "filialeCibleId": 5,
  "articleId": 10,
  "quantite": 20
}
```

---

### US-069 — Lister les transferts du groupe

**Priorité :** P1 | **Sprint :** 8 | **Points :** 2

**En tant qu'** Admin Groupe,
**je veux** consulter l'historique des transferts entre filiales,
**afin de** tracer tous les déplacements de marchandises.

**Critères d'acceptation :**
- [ ] Filtres : `filialeSourceId`, `filialeCibleId`, `articleId`, `dateDebut`, `dateFin`
- [ ] Pagination obligatoire

**Endpoint :** `GET /api/v1/groupe/transferts?filialeSourceId=3&page=0&size=20`

---

### US-070 — Consulter un bon de transfert

**Priorité :** P1 | **Sprint :** 8 | **Points :** 1

**En tant qu'** Admin Groupe ou Admin Filiale concerné,
**je veux** consulter le détail d'un bon de transfert,
**afin de** vérifier les informations (source, cible, article, quantité, date).

**Critères d'acceptation :**
- [ ] Admin Filiale : visible uniquement si sa filiale est source ou cible
- [ ] Admin Groupe : visible pour tous les transferts de son groupe

**Endpoint :** `GET /api/v1/groupe/transferts/{id}`

---

### US-106 — Transfert multi-lignes à états ⭐

**Priorité :** P1 | **Sprint :** 8 | **Points :** 8

**En tant qu'** Admin Groupe,
**je veux** envoyer plusieurs articles dans un même bon de transfert et suivre sa réception,
**afin de** refléter la réalité d'un camion qui part avec dix références et en arrive neuf.

**Critères d'acceptation :**
- [ ] **Remplace le transfert mono-article d'US-068** : en-tête (`group_id`, source, cible, statut, dates) + **N lignes** (`DEC-007`)
- [ ] Chaque ligne porte trois quantités : **demandée, expédiée, reçue** — en `DECIMAL(12,3)` (`DEC-003`)
- [ ] Machine à états `DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART` (`DEC-002`)
- [ ] `TRANSFERT_SORTIE` créé au passage `EN_TRANSIT` (sur la quantité **expédiée**), `TRANSFERT_ENTREE` au passage `RECU` (sur la quantité **reçue**)
- [ ] **L'écart de réception est la différence expédié / reçu** (`DEC-007`) ; un écart non nul fait passer le bon à `ECART`, jamais à `RECU`
- [ ] Un bon en `ECART` reste ouvert jusqu'à arbitrage : il ne se clôture pas tout seul
- [ ] Stock source suffisant au passage `EN_TRANSIT` → sinon `409 INSUFFICIENT_STOCK` (`DEC-017`)
- [ ] Source ≠ cible et **même groupe** → sinon `400 SAME_SOURCE_AND_TARGET` / `403 CROSS_GROUP_FORBIDDEN`
- [ ] Prérequis : catalogue au niveau groupe (US-102)
- [ ] En-tête `Idempotency-Key` obligatoire sur les transitions créant un mouvement (`DEC-027`)

**Endpoint :** `POST /api/v1/groupe/transferts` puis `POST /api/v1/groupe/transferts/{id}/{transition}`

**Corps :**
```json
{
  "filialeSourceId": 3,
  "filialeCibleId": 5,
  "lignes": [
    { "articleId": 10, "quantiteDemandee": 20.000 },
    { "articleId": 14, "quantiteDemandee": 5.500 }
  ]
}
```

---

## EPIC 12 — Notifications & Alertes

> **Objectif :** Informer automatiquement les utilisateurs des événements critiques (rupture de stock, seuil minimum atteint) pour permettre une réaction proactive.

**Dépendance :** EPIC 8 (les événements `StockUpdatedEvent` doivent exister).

---

### US-071 — Alerte automatique seuil de stock minimum

**Priorité :** P0 | **Sprint :** 8 | **Points :** 5

**En tant que** système automatique,
**je veux** déclencher une alerte quand le stock d'un article passe sous son seuil minimum,
**afin que** le Gestionnaire de stock et l'Admin Filiale soient informés pour réapprovisionner.

**Critères d'acceptation :**
- [ ] Déclenchement asynchrone via `@EventListener` sur `StockUpdatedEvent`
- [ ] Conditions (REF §4.2) : `RUPTURE` si `stock_réel ≤ 0` — **indépendant du seuil**, donc déclenchée même si `seuil_alerte = 0` ; `STOCK_BAS` si `0 < stock_réel ≤ seuil_alerte` ET `seuil_alerte > 0`
- [ ] `seuil_alerte = 0` désactive la seule alerte `STOCK_BAS`, jamais `RUPTURE`
- [ ] `NotificationAlerte` créée en base avec le type correspondant (`STOCK_BAS` ou `RUPTURE`)
- [ ] Email envoyé au Gestionnaire de Stock et à l'Admin Filiale concernés
- [ ] Anti-spam : pas de doublon d'email si alerte déjà envoyée < 24h pour le même article
- [ ] Exécuté dans une transaction indépendante (`REQUIRES_NEW`) — échec n'annule pas le mouvement

**Endpoint :** aucun (acteur Système)

---

### US-072 — Consulter les alertes en cours

**Priorité :** P1 | **Sprint :** 9 | **Points :** 2

**En tant que** Gestionnaire de stock ou Admin,
**je veux** voir la liste des alertes de stock actives non lues,
**afin de** traiter les ruptures et réapprovisionnements urgents.

**Critères d'acceptation :**
- [ ] Retourne les alertes de l'entreprise connectée filtrées par `etat` (`NON_LU` par défaut — `DEC-004`)
- [ ] Tri par date DESC

**Endpoint :** `GET /api/v1/alertes?etat=NON_LU&page=0&size=20`

---

### US-073 — Changer l'état d'une alerte

**Priorité :** P1 | **Sprint :** 9 | **Points :** 1

**En tant que** Gestionnaire de stock,
**je veux** faire passer une alerte à `LU` puis à `RESOLU`,
**afin de** nettoyer mon tableau de bord des alertes traitées.

**Endpoint :** `PATCH /api/v1/alertes/{id}/etat` (corps : `{"etat":"LU"|"RESOLU"}` — `DEC-004`)

---

### US-074 — Email de bienvenue à l'inscription

**Priorité :** P1 | **Sprint :** 3 | **Points :** 2

**En tant que** système,
**je veux** envoyer un email de bienvenue avec lien d'activation à chaque nouvel inscrit,
**afin de** valider l'adresse email et guider l'utilisateur dans la prise en main.

**Critères d'acceptation :**
- [ ] Envoi asynchrone (ne bloque pas la réponse de l'inscription)
- [ ] Lien d'activation valide 48 heures
- [ ] Template HTML avec nom du gérant et nom de l'entreprise

**Endpoint :** aucun (déclenché par US-006 et US-007)

---

### US-075 — Email invitation employé

**Priorité :** P1 | **Sprint :** 4 | **Points :** 2

**En tant que** système,
**je veux** envoyer un email d'invitation avec lien d'activation à chaque nouvel employé créé par invitation,
**afin qu'il** puisse définir son mot de passe et accéder à son espace.

**Critères d'acceptation :**
- [ ] Token d'activation UUID, haché en base, expiration 48h
- [ ] Template HTML avec rôle attribué et nom de l'entreprise

**Endpoint :** `POST /api/v1/auth/activer-compte`

**Corps :** `{ "token": "uuid-activation", "motDePasse": "MonMdp@2026" }`

---

## EPIC 13 — Statistiques & Reporting

> **Objectif :** Fournir aux administrateurs des indicateurs de performance pour piloter l'activité commerciale.

**Dépendance :** EPIC 9 + EPIC 10 complétés (données de ventes nécessaires).

---

### US-076 — Top articles les plus vendus

**Priorité :** P1 | **Sprint :** 10 | **Points :** 3

**En tant qu'** Admin Filiale ou Admin Groupe,
**je veux** voir le classement des articles les plus vendus sur une période,
**afin d'** identifier mes produits phares et optimiser mon stock.

**Critères d'acceptation :**
- [ ] Filtre : `dateDebut`, `dateFin`, `filialeId` (Admin Groupe uniquement)
- [ ] Classement par quantité vendue ou par CA (paramètre `critere`)
- [ ] `@Transactional(readOnly = true)` + cache Redis (TTL 10 minutes)

**Endpoint :** `GET /api/v1/reporting/top-articles?dateDebut=2026-01-01&dateFin=2026-06-30&critere=CA&limit=10`

---

### US-077 — Clients les plus actifs

**Priorité :** P1 | **Sprint :** 10 | **Points :** 3

**En tant qu'** Admin,
**je veux** voir le classement de mes clients par chiffre d'affaires ou fréquence de commandes,
**afin de** prioriser la relation commerciale avec mes meilleurs clients.

**Critères d'acceptation :**
- [ ] Classement par CA total ou nombre de commandes
- [ ] Retourne : client, nombre commandes, CA total, date dernière commande

**Endpoint :** `GET /api/v1/reporting/top-clients?critere=CA&limit=10`

---

### US-078 — Évolution du chiffre d'affaires

**Priorité :** P1 | **Sprint :** 10 | **Points :** 5

**En tant qu'** Admin,
**je veux** visualiser l'évolution du CA journalier ou mensuel,
**afin de** détecter les tendances et les pics d'activité.

**Critères d'acceptation :**
- [ ] Granularité : `JOUR` ou `MOIS` (paramètre `granularite`)
- [ ] CA = somme des commandes `VALIDÉES` + ventes directes de la période
- [ ] Admin Groupe : filtre par filiale ou vue consolidée groupe

**Endpoint :** `GET /api/v1/reporting/ca?dateDebut=2026-01-01&dateFin=2026-06-30&granularite=MOIS`

---

### US-079 — Alertes de rupture imminente

**Priorité :** P1 | **Sprint :** 10 | **Points :** 3

**En tant qu'** Admin,
**je veux** voir les articles dont le stock actuel couvre moins de N jours de vente au rythme actuel,
**afin d'** anticiper les ruptures avant qu'elles se produisent.

**Critères d'acceptation :**
- [ ] Calcul : `stock_réel / (quantité_vendue_30_derniers_jours / 30)` = jours restants
- [ ] Paramètre `joursAlerte` (défaut : 7 jours)
- [ ] Retourne : article, stock actuel, jours restants estimés, seuil d'alerte

**Endpoint :** `GET /api/v1/reporting/ruptures-imminentes?joursAlerte=7`

---

### US-080 — Export CSV des mouvements de stock

**Priorité :** P2 | **Sprint :** 11 | **Points :** 3

**En tant qu'** Admin,
**je veux** exporter l'historique des mouvements de stock en fichier CSV,
**afin de** l'analyser dans Excel ou de l'archiver.

**Critères d'acceptation :**
- [ ] Filtres : `dateDebut`, `dateFin`, `articleId`, `typeMouvement`
- [ ] Limite : 10 000 lignes par export
- [ ] En-têtes CSV en français
- [ ] `Content-Type: text/csv`, `Content-Disposition: attachment`

**Endpoint :** `GET /api/v1/reporting/export/mouvements?format=CSV&dateDebut=2026-01-01`

---

## EPIC 14 — Caisse : sessions, paiements et remboursement

> **Objectif :** rendre la caisse exploitable et contrôlable. Une vente ne flotte plus : elle tombe dans la session de quelqu'un, on sait comment elle a été payée, et la clôture est le seul contrôle anti-perte du produit.
>
> Périmètre issu de `DEC-009` (tables `session_caisse` et `paiement`), `DEC-010` (remboursement) et `DEC-018` (le contrôle porte sur la session, pas sur le rôle).

### US-087 — Ouvrir une session de caisse ⭐

**Priorité :** P0 | **Sprint :** 8 | **Points :** 5

**En tant que** Caissier,
**je veux** ouvrir ma session de caisse en déclarant mon fond de caisse,
**afin de** pouvoir encaisser et répondre de mon tiroir à la clôture.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('CAISSIER','COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] La session est rattachée à l'utilisateur courant **et** à sa filiale (`entreprise_id` du JWT, jamais du corps)
- [ ] Un utilisateur ne peut avoir **qu'une seule session ouverte à la fois** → sinon `409` avec `ErrorCode.CAISSE_SESSION_DEJA_OUVERTE`
- [ ] `fond_caisse` obligatoire, entier XAF ≥ 0 (`DEC-003`)
- [ ] Horodatage d'ouverture en `TIMESTAMPTZ` UTC ; la journée comptable s'entend en `Africa/Douala` (`DEC-021`)
- [ ] Règle citable (`DEC-018`) : *« Quiconque encaisse ouvre une session de caisse à son nom, et en répond à la clôture. »*

**Endpoint :** `POST /api/v1/caisse/sessions`

**Corps :**
```json
{
  "fondCaisse": 50000
}
```

---

### US-088 — Encaisser une vente en paiement mixte ⭐

**Priorité :** P0 | **Sprint :** 8 | **Points :** 8

**En tant que** Caissier,
**je veux** encaisser une vente en combinant plusieurs moyens de paiement,
**afin de** traiter le cas courant « une partie en espèces, le reste en Mobile Money ».

**Critères d'acceptation :**
- [ ] La vente est rattachée à la **session ouverte de l'utilisateur courant** → sinon `409` avec `ErrorCode.CAISSE_AUCUNE_SESSION_OUVERTE`
- [ ] N lignes de paiement autorisées sur une même vente (table `paiement`)
- [ ] Modes acceptés : `ESPECES`, `MOBILE_MONEY`, `CARTE` (`DEC-009`) — aucun autre
- [ ] `reference_transaction` obligatoire pour `MOBILE_MONEY` et `CARTE`, interdite pour `ESPECES`
- [ ] **Σ des paiements = total TTC de la vente** → sinon `422` avec `ErrorCode.PAIEMENT_MONTANT_INCOHERENT`
- [ ] Montants en `INTEGER` XAF (`DEC-003`) ; la monnaie rendue est un calcul d'affichage, jamais une ligne de paiement
- [ ] Écriture transactionnelle : `@Transactional(rollbackFor = Exception.class)` — la vente, ses lignes, ses paiements et le mouvement `SORTIE` tombent ensemble ou pas du tout
- [ ] En-tête `Idempotency-Key` obligatoire (`DEC-027`, voir US-100)

**Endpoint :** `POST /api/v1/caisse/ventes/{venteId}/paiements`

**Corps :**
```json
{
  "paiements": [
    { "mode": "ESPECES", "montant": 20000 },
    { "mode": "MOBILE_MONEY", "montant": 43200, "referenceTransaction": "MTN-8842119" }
  ]
}
```

---

### US-089 — Clôturer une session de caisse ⭐

**Priorité :** P0 | **Sprint :** 8 | **Points :** 5

**En tant que** Caissier,
**je veux** clôturer ma session en déclarant le montant réellement compté dans le tiroir,
**afin que** l'écart de caisse soit constaté et imputable.

**Critères d'acceptation :**
- [ ] Seul le **titulaire de la session** ou un `ADMIN_FILIALE` peut clôturer
- [ ] `montant_compte` (espèces réellement en tiroir) obligatoire
- [ ] **Théorique espèces** = `fond_caisse` + Σ paiements `ESPECES` de la session − Σ remboursements `ESPECES`
- [ ] `ecart = montant_compte − theorique_especes` — **figé** sur la session, jamais recalculé après coup
- [ ] Un écart non nul n'empêche pas la clôture : il est **constaté**, pas bloquant (c'est la trace qui a de la valeur)
- [ ] Une session clôturée est **immuable** ; toute nouvelle vente exige une nouvelle session
- [ ] Clôturer une session déjà clôturée → `409` avec `ErrorCode.CAISSE_SESSION_DEJA_CLOTUREE`
- [ ] Les paiements `MOBILE_MONEY` et `CARTE` sont totalisés séparément (rapprochement opérateur), hors écart espèces

**Endpoint :** `POST /api/v1/caisse/sessions/{id}/cloturer`

**Corps :**
```json
{
  "montantCompte": 112400
}
```

---

### US-090 — Consulter l'historique des sessions de caisse

**Priorité :** P1 | **Sprint :** 9 | **Points :** 2

**En tant qu'** Admin Filiale,
**je veux** consulter les sessions de caisse passées avec leurs écarts,
**afin de** suivre les pertes et identifier les postes à problème.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Filtres : `utilisateurId`, `dateDebut`, `dateFin`, `avecEcart` (booléen)
- [ ] Retourne : titulaire, ouverture, clôture, fond, théorique, compté, écart, totaux par mode de paiement
- [ ] Filtrage `entreprise_id` obligatoire ; l'Admin Groupe voit ses filiales via `group_id`
- [ ] Pagination obligatoire

**Endpoint :** `GET /api/v1/caisse/sessions?avecEcart=true&dateDebut=2026-09-01&page=0&size=20`

---

### US-091 — Rembourser une vente en caisse

**Priorité :** P1 | **Sprint :** 9 | **Points :** 5

**En tant que** Caissier,
**je veux** rembourser un article rapporté par un client,
**afin de** traiter un retour sans annuler toute la vente.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('CAISSIER','COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Remboursement **en caisse, sans avoir** (`DEC-010`) — aucun bon d'achat, aucune table d'avoirs
- [ ] Le remboursement tombe dans une **session ouverte** et pèse sur son écart (`DEC-009`)
- [ ] Remboursement **partiel autorisé** : ligne par ligne, quantité par quantité
- [ ] Un mouvement `REMBOURSEMENT` est créé par ligne remboursée — **entrée compensatoire (signe +)**, jamais une sortie (REF §4.2)
- [ ] **Le mouvement `SORTIE` original n'est ni modifié ni supprimé** (immuabilité du journal)
- [ ] La vente passe à `statut = REMBOURSEE` si tout est rendu ; elle reste `PAYEE` si le remboursement est partiel
- [ ] Une vente `ANNULEE` ne peut pas être remboursée → `409` avec `ErrorCode.VENTE_DEJA_ANNULEE`
- [ ] En-tête `Idempotency-Key` obligatoire (`DEC-027`)

**Endpoint :** `POST /api/v1/caisse/ventes/{venteId}/rembourser`

**Corps :**
```json
{
  "lignes": [
    { "ligneVenteId": 812, "quantite": 1.000 }
  ],
  "modeRemboursement": "ESPECES",
  "motif": "Article défectueux"
}
```

---

## EPIC 15 — Campagnes d'inventaire

> **Objectif :** donner au produit le **seul organe par lequel il peut découvrir qu'un stock est faux**. `DEC-037` ayant supprimé la détection automatique d'écart, l'inventaire n'est plus un confort de saisie : c'est la contrepartie de l'invariant « stock jamais négatif » (`DEC-023`).
>
> Périmètre issu de `DEC-036` — campagne **sans gel du stock** : les ventes continuent pendant le comptage.

### US-092 — Ouvrir une campagne d'inventaire ⭐

**Priorité :** P0 | **Sprint :** 9 | **Points :** 5

**En tant que** Gestionnaire de stock,
**je veux** ouvrir une campagne d'inventaire datée sur ma filiale,
**afin de** confronter le stock système au stock physique de façon traçable.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE')")`
- [ ] Session rattachée à **une filiale** (`entreprise_id`), **jamais au groupe** (`DEC-036`)
- [ ] Périmètre au choix : tout le catalogue actif, ou une catégorie
- [ ] Génère la **liste de comptage** : une ligne par article du périmètre, quantité constatée vide
- [ ] Numéro de campagne `INV-{ANNEE}-{SEQUENCE}`
- [ ] Une seule campagne ouverte par filiale à la fois → sinon `409` avec `ErrorCode.INVENTAIRE_DEJA_OUVERT`
- [ ] **Aucun gel du stock** : les ventes, réceptions et transferts continuent normalement pendant la campagne

**Endpoint :** `POST /api/v1/inventaires`

**Corps :**
```json
{
  "perimetre": "CATEGORIE",
  "categorieId": 4
}
```

---

### US-093 — Saisir les quantités constatées ⭐

**Priorité :** P0 | **Sprint :** 9 | **Points :** 8

**En tant que** Gestionnaire de stock,
**je veux** saisir la quantité physiquement comptée pour chaque article,
**afin de** matérialiser l'écart avec le stock système.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE')")`
- [ ] `quantite_constatee` en `DECIMAL(12,3)` (`DEC-003`), ≥ 0
- [ ] À la saisie, la ligne **fige trois valeurs** : l'horodatage, le **stock système à cet instant**, et l'écart
- [ ] `ecart = quantite_constatee − stock_systeme_au_comptage` — figé, jamais recalculé (`DEC-036`)
- [ ] Un mouvement survenu **après** le comptage est réel et ne doit pas être annulé : le delta reste arithmétiquement juste
- [ ] Saisie possible en plusieurs fois ; une ligne peut être recomptée tant que la campagne est ouverte
- [ ] Saisie interdite sur une campagne validée → `409` avec `ErrorCode.INVENTAIRE_DEJA_VALIDE`

**Endpoint :** `PUT /api/v1/inventaires/{id}/lignes/{ligneId}`

**Corps :**
```json
{
  "quantiteConstatee": 47.500
}
```

---

### US-094 — Consulter la feuille d'écarts

**Priorité :** P0 | **Sprint :** 9 | **Points :** 3

**En tant que** Gestionnaire de stock ou Admin Filiale,
**je veux** voir la liste des écarts constatés avant validation,
**afin de** décider en connaissance de cause et repérer les erreurs de comptage.

**Critères d'acceptation :**
- [ ] Retourne par ligne : article, stock système figé, quantité constatée, écart, horodatage du comptage
- [ ] Filtre `ecartNonNul` (booléen) pour n'afficher que ce qui bouge
- [ ] Totaux : nombre de lignes comptées / non comptées, écart positif cumulé, écart négatif cumulé
- [ ] Valorisation indicative de l'écart au prix d'achat (`INTEGER` XAF) — **indicative**, la comptabilité ne dérive pas de cet écran
- [ ] Pagination obligatoire

**Endpoint :** `GET /api/v1/inventaires/{id}/ecarts?ecartNonNul=true&page=0&size=20`

---

### US-095 — Valider une campagne et générer les corrections ⭐

**Priorité :** P0 | **Sprint :** 10 | **Points :** 8

**En tant qu'** Admin Filiale,
**je veux** valider la campagne pour que les écarts deviennent des mouvements de stock,
**afin d'** aligner le stock système sur le réel, de façon tracée et justifiable.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasRole('ADMIN_FILIALE')")` — **celui qui compte ne valide pas son propre comptage** (`DEC-036`) ; le `GESTIONNAIRE_STOCK` est explicitement exclu
- [ ] Chaque ligne à écart non nul produit une `CORRECTION_POS` (écart > 0) ou `CORRECTION_NEG` (écart < 0)
- [ ] `motif = "Inventaire n° INV-{ANNEE}-{SEQUENCE}"` sur chaque mouvement généré
- [ ] Les lignes à écart nul ne produisent **aucun mouvement**
- [ ] **Cas limite `DEC-023`** : si appliquer l'écart ferait passer le stock sous zéro, la ligne est **refusée et marquée `A_RECOMPTER`** — elle n'est **jamais** appliquée partiellement (« le comptage est périmé, pas approximatif »)
- [ ] Les lignes refusées n'empêchent pas la validation des autres ; elles sont listées dans la réponse
- [ ] Opération transactionnelle : `@Transactional(rollbackFor = Exception.class)`
- [ ] Campagne passe à `VALIDEE` et devient immuable ; `StockUpdatedEvent` publié par article corrigé
- [ ] Une campagne s'ouvre et se valide **le jour même** (`DEC-036`) — au-delà, les lignes périment au sens du cas limite ci-dessus

**Endpoint :** `POST /api/v1/inventaires/{id}/valider`

---

## EPIC 16 — Import de mise en service (CSV)

> **Objectif :** lever la friction d'entrée qui rendait l'essai de 90 jours (`DEC-015`) inexploitable — saisir 2 000 références à la main n'est pas un onboarding.
>
> Périmètre issu de `DEC-038` : trois imports, gabarit téléchargeable, rapport d'erreurs **ligne à ligne**, import **transactionnel** (tout ou rien par lot validé). **Aucune reprise d'historique** (ventes, transferts, commandes) en V1.

### US-096 — Télécharger les gabarits CSV

**Priorité :** P0 | **Sprint :** 5 | **Points :** 2

**En tant qu'** Admin,
**je veux** télécharger le fichier modèle de chaque import,
**afin de** préparer mes données au bon format sans deviner les colonnes.

**Critères d'acceptation :**
- [ ] Trois gabarits : `catalogue`, `stock-initial`, `tiers`
- [ ] En-têtes en français, une ligne d'exemple commentée
- [ ] Encodage **UTF-8 avec BOM** (ouverture correcte dans Excel en environnement francophone)
- [ ] Séparateur `;` (convention Excel FR), documenté dans le gabarit
- [ ] `Content-Type: text/csv`, `Content-Disposition: attachment`

**Endpoint :** `GET /api/v1/imports/gabarits/{type}` (`type` = `catalogue` | `stock-initial` | `tiers`)

---

### US-097 — Importer le catalogue (catégories + articles) ⭐

**Priorité :** P0 | **Sprint :** 5 | **Points :** 8

**En tant qu'** Admin,
**je veux** importer mes catégories et mes articles depuis un fichier CSV,
**afin de** mettre le produit en service sans ressaisie manuelle.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Les catégories absentes sont créées à la volée depuis la colonne catégorie
- [ ] Validation ligne à ligne : `code_article` unique, prix `INTEGER` ≥ 0, `taux_tva` valide, quantités `DECIMAL(12,3)` (`DEC-003`)
- [ ] `prix_vente_ttc` **calculé côté serveur**, jamais lu depuis le fichier
- [ ] **Rapport d'erreurs ligne à ligne** : numéro de ligne, colonne, valeur reçue, motif du rejet
- [ ] Import **transactionnel** : une seule erreur ⇒ **aucune ligne n'est écrite** (`DEC-038`)
- [ ] Mode `dryRun` : valide et retourne le rapport sans rien écrire
- [ ] Taille maximale 5 Mo / 10 000 lignes par lot
- [ ] Le fichier importé n'est pas conservé au-delà du traitement

**Endpoint :** `POST /api/v1/imports/catalogue?dryRun=false` (multipart/form-data)

---

### US-098 — Importer le stock initial par filiale ⭐

**Priorité :** P0 | **Sprint :** 6 | **Points :** 5

**En tant qu'** Admin,
**je veux** charger le stock de départ de chaque filiale,
**afin de** partir d'un stock juste au jour 1 sans inventaire manuel.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Chaque ligne génère un mouvement d'ouverture `CORRECTION_POS` avec `motif = "Import mise en service"` (`DEC-038`)
- [ ] Quantités en `DECIMAL(12,3)` (`DEC-003`), strictement > 0
- [ ] L'article doit exister → sinon la ligne est rejetée avec son numéro (pas de création implicite)
- [ ] La filiale cible doit appartenir au groupe de l'appelant → sinon `403 CROSS_GROUP_FORBIDDEN`
- [ ] **Refus si un stock d'ouverture existe déjà** pour le couple article/filiale → `409` avec `ErrorCode.STOCK_INITIAL_DEJA_IMPORTE` (un import d'ouverture ne se rejoue pas)
- [ ] Import transactionnel + rapport ligne à ligne + `dryRun`, comme US-097
- [ ] Lève la conséquence assumée de `DEC-036` : l'inventaire initial passe par cet import, pas à la main

**Endpoint :** `POST /api/v1/imports/stock-initial?filialeId=3&dryRun=false` (multipart/form-data)

---

### US-099 — Importer les tiers (clients + fournisseurs)

**Priorité :** P0 | **Sprint :** 5 | **Points :** 3

**En tant qu'** Admin,
**je veux** importer mon fichier clients et fournisseurs,
**afin de** retrouver mes contacts existants dès la mise en service.

**Critères d'acceptation :**
- [ ] Colonne `type` obligatoire : `CLIENT` ou `FOURNISSEUR`
- [ ] Téléphone au format camerounais validé ; e-mail optionnel mais validé s'il est présent
- [ ] Doublon détecté sur (nom + téléphone) dans l'entreprise → ligne rejetée, jamais écrasée silencieusement
- [ ] Import transactionnel + rapport ligne à ligne + `dryRun`, comme US-097

**Endpoint :** `POST /api/v1/imports/tiers?dryRun=false` (multipart/form-data)

---

## EPIC 17 — Robustesse des écritures de stock

> **Objectif :** empêcher qu'une connexion mobile instable ne crée un double mouvement de stock — une faute **indétectable a posteriori**, puisque le stock est calculé depuis le journal (ADR-003).
>
> Périmètre issu de `DEC-027`.

### US-100 — Clé d'idempotence sur les écritures de stock ⭐

**Priorité :** P0 | **Sprint :** 6 | **Points :** 5

**En tant que** système,
**je veux** rejeter la seconde soumission identique d'une écriture de stock,
**afin qu'** une double soumission réseau ne crée jamais deux mouvements.

**Critères d'acceptation :**
- [ ] En-tête `Idempotency-Key` **obligatoire** sur toute écriture créant un `mouvement_stock` : vente (US-064), réception (US-049), transfert (US-068), correction (US-053/054), remboursement (US-091), validation d'inventaire (US-095)
- [ ] Absence d'en-tête → `400` avec `ErrorCode.IDEMPOTENCY_KEY_REQUISE`
- [ ] La clé est stockée **avec la réponse produite** (`DEC-027`)
- [ ] Rejeu de la **même clé** avec le **même corps** → la réponse mémorisée est renvoyée telle quelle, **sans réexécuter** l'écriture (même code HTTP, même corps)
- [ ] Rejeu de la même clé avec un corps **différent** → `409` avec `ErrorCode.IDEMPOTENCY_KEY_REUTILISEE`
- [ ] Portée de la clé : `entreprise_id` + endpoint — deux entreprises peuvent employer la même clé sans se percuter
- [ ] Rétention : 24 h glissantes, purge automatique (`DEC-032`)
- [ ] L'enregistrement de la clé et l'écriture métier sont dans **la même transaction** — sinon la protection ne vaut rien

**Endpoint :** transverse (en-tête HTTP sur les endpoints listés ci-dessus)

---

## Récapitulatif par Sprint

| Sprint | Durée | US incluses | Points | Objectif |
|--------|-------|-------------|--------|----------|
| **Sprint 1** | 2 sem. | US-001 à US-005 | 19 | Fondations techniques, CI/CD, Docker |
| **Sprint 2** | 2 sem. | US-006 à US-012 | 21 | Authentification complète P0 |
| **Sprint 3** | 2 sem. | US-013 à US-021, US-074 | 22 | Groupe, Filiales, Utilisateurs, Email bienvenue |
| **Sprint 3 (sécu)** | inclus | US-083 à US-086 *(nouveau — GS-CDA-2026-02)* | +14 | Rotation refresh token, Argon2id, fail-closed, audit logs |
| **Sprint 4** | 2 sem. | US-019, US-022 à US-026, US-075 | 20 | Employés, Profil, Activation |
| **Sprint 3 (plan)** | inclus | US-101 *(nouveau — `DEC-015`)* | +3 | Limite d'utilisateurs par plan |
| **Sprint 5** | 2 sem. | US-027 à US-043 | 24 | Catalogue complet, Clients, Fournisseurs |
| **Sprint 5 (cat./import)** | inclus | US-096, US-097, US-099, US-102, US-103 *(nouveaux)* | +26 | Catalogue groupe, unités d'achat, imports CSV catalogue et tiers |
| **Sprint 6** | 2 sem. | US-044 à US-050 | 23 | Cycle d'achat complet avec mouvements ENTREE |
| **Sprint 6 (add.)** | inclus | US-098, US-100, US-104 *(nouveaux)* | +15 | Import du stock initial, idempotence, lot / péremption |
| **Sprint 7** | 2 sem. | US-051 à US-054, US-056 à US-062 | 34 | Stock réel, Corrections, Ventes B2B |
| **Sprint 7 (add.)** | inclus | US-105 *(nouveau — `DEC-011`)* | +3 | Règlement et échéance B2B |
| **Sprint 8** | 2 sem. | US-055, US-064, US-064b, US-065 à US-070, US-071 | 33 | Stock consolidé, Caisse (bloquante 409), Transferts, Alertes |
| **Sprint 8 (caisse)** | inclus | US-087 à US-089, US-106 *(nouveaux)* | +26 | Sessions de caisse, paiement mixte, clôture, transfert multi-lignes |
| **Sprint 9** | 2 sem. | US-063, US-067 *(revu, 3→5 pts)*, US-072 à US-073, US-075 | 16 | Facture PDF, Annulation vente, Centre alertes |
| **Sprint 9 (add.)** | inclus | US-090 à US-094 *(nouveaux)* | +23 | Historique de caisse, remboursement, campagnes d'inventaire |
| **Sprint 10** | 2 sem. | US-076 à US-079, US-095 *(nouveau)* | 22 | Reporting, validation d'inventaire |
| **Sprint 11** | 2 sem. | US-080 | 3 | Export CSV P2 |
| **Sprint 11 (add.)** | inclus | US-081, US-082 *(GS-UNICITE-2026-07)* | 6 | Branding entreprise, Unicité stricte |

**Total P0 :** 78 user stories — 248 points
**Total P1 :** 29 user stories — 103 points
**Total P2 :** 1 user story — 3 points
**Total backlog :** **108 user stories** | **354 story points**

> ⚠️ **La colonne « Sprint » est désormais sur-souscrite et n'est plus un plan.**
>
> Les 20 US ajoutées le 8 sept. 2026 (`DEC-002`, `007`, `009`, `010`, `011`, `012`, `013`, `015`,
> `027`, `036`, `038`) ont été rattachées au sprint le plus proche **fonctionnellement**, sans
> rééquilibrage de charge : le sprint 8 porte 57 points et le sprint 3 en porte 43, pour une
> vélocité observée de l'ordre de 20-25. À ~22 points par sprint, 354 points représentent
> **≈ 16 sprints**, et `REF §13.3` chiffre le périmètre acté à **≈ 24,5 sprints** une fois
> comptés les coûts non exprimés en US.
>
> **Le rééquilibrage et l'ordre de livraison relèvent de `GS-PLAN-2026-01`**, autorité du
> planning (`REF §13`) — pas de ce fichier, qui décrit *quoi* faire et non *quand*.

> 📌 **Corrections de comptage (8 sept. 2026).** Les totaux précédents (« 82 US / 291 SP »)
> ne correspondaient pas au contenu réel du fichier, qui comptait **88 US pour 250 SP**
> avant les ajouts. Les totaux ci-dessus sont recalculés depuis les en-têtes des US.

> **Changelog GS-CDA-2026-02 (voir addendum dédié)** : +5 US, +18 points par rapport à la version 1.0 — US-083 à US-086 (durcissement sécurité auth) et US-064b (association d'un client existant à une vente directe) ajoutées ; US-064 et US-067 revues en profondeur (comportement bloquant en `409` — `DEC-023`/`DEC-017` — + mouvement `ANNULATION_VENTE` dédié).

---

## Matrice de dépendances

```
EPIC 1 (Fondations)
    └── EPIC 2 (Auth)
            ├── EPIC 3 (Groupe/Filiales)
            │       └── EPIC 4 (Utilisateurs)
            │               ├── EPIC 5 (Catalogue)
            │               │       ├── EPIC 6 (Tiers)
            │               │       │       ├── EPIC 7 (Commandes Fournisseur) ──┐
            │               │       │       └── EPIC 9 (Commandes Client) ──────┤
            │               │       │                                             │
            │               │       └── EPIC 10 (Vente Directe) ────────────────┤
            │               │                                                     ▼
            │               └── EPIC 8 (Gestion Stock) ◄─────────────── (alimenté par 7, 9, 10)
            │                       ├── EPIC 11 (Transferts) ← dépend de EPIC 8
            │                       └── EPIC 12 (Notifications) ← dépend de EPIC 8
            └── EPIC 13 (Reporting) ← dépend de EPIC 9 + EPIC 10
```

---

> **Source de vérité :** Ce backlog dérive de l'Analyse Fonctionnelle (GS-CDA-2026-01)
> et du Cahier des Charges Techniques (GS-CDCT-2026-01).
> En cas de conflit, les Use Cases de la section 5 de l'Analyse Fonctionnelle font autorité.
>
> **Révision :** À mettre à jour à chaque fin de sprint lors de la cérémonie de sprint review.
