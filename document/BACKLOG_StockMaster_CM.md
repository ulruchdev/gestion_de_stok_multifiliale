# Backlog Produit — StockMaster CM
### Référence : GS-BACKLOG-2026-01 | Version : 1.0 | Date : Juin 2026 | Statut : Validé

---

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
- [ ] `V1__init_schema.sql` crée toutes les tables avec contraintes CHECK, FK et UNIQUE (conforme CDCT section 23.3)
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
- [ ] Email de confirmation envoyé après inscription (async)
- [ ] Rollback complet si l'envoi d'email échoue (transaction annulée)
- [ ] Validation Jakarta sur tous les champs obligatoires

**Endpoint :** `POST /api/v1/auth/inscription/entreprise-unique`

**Corps de la requête :**
```json
{
  "nomBoutique": "Épicerie Centrale",
  "ville": "Douala",
  "quartier": "Akwa",
  "prenom": "Jean",
  "nom": "Kamga",
  "email": "jean.kamga@epicerie.cm",
  "motDePasse": "MotDePasse@2026"
}
```

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
- [ ] Rate limiting : 5 tentatives max en 15 minutes par IP (Redis)
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
- [ ] Retourne : tous les champs article + `stockActuel` + `margeBrutePct` + `statutAlerte` (NORMAL / BAS / RUPTURE)
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
- [ ] Stock calculé en temps réel : `Σ(ENTREE + CORRECTION_POS + TRANSFERT_ENTREE) - Σ(SORTIE + CORRECTION_NEG + TRANSFERT_SORTIE)`
- [ ] Double filtre obligatoire : `article_id` ET `entreprise_id`
- [ ] Retourne : `stockActuel`, `seuilAlerte`, `statutAlerte` (NORMAL / BAS / RUPTURE)
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

## EPIC 10 — Vente Directe (Caisse)

> **Objectif :** Permettre au Caissier d'enregistrer des ventes comptoir sans client identifié, avec mise à jour immédiate du stock.

**Dépendance :** EPIC 5 + EPIC 8 complétés.

---

### US-064 — Enregistrer une vente directe

**Priorité :** P0 | **Sprint :** 8 | **Points :** 5

**En tant que** Caissier,
**je veux** enregistrer rapidement une vente comptoir sans saisir de client,
**afin de** traiter les transactions au point de vente sans délai.

**Critères d'acceptation :**
- [ ] `@PreAuthorize("hasAnyRole('CAISSIER','COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")`
- [ ] Pas de client requis (vente anonyme)
- [ ] Vérification stock pour chaque article → blocage si insuffisant
- [ ] Code vente généré : `VNT-{DATE}-{SEQUENCE}`
- [ ] **Opération atomique** : 1 mouvement `SORTIE` par ligne
- [ ] Totaux HT / TVA / TTC calculés côté serveur
- [ ] `StockUpdatedEvent` publié

**Endpoint :** `POST /api/v1/ventes`

**Corps :**
```json
{
  "lignes": [
    { "articleId": 10, "quantite": 2, "prixUnitaire": 18500 },
    { "articleId": 15, "quantite": 1, "prixUnitaire": 5000 }
  ],
  "commentaire": "Vente comptoir"
}
```

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

**Priorité :** P1 | **Sprint :** 9 | **Points :** 3

**En tant que** Caissier ou Admin Filiale,
**je veux** annuler une vente du jour en cas d'erreur,
**afin de** corriger une saisie fautive avant la clôture de caisse.

**Critères d'acceptation :**
- [ ] Annulation possible uniquement si vente du jour même (`date_vente::date = CURRENT_DATE`)
- [ ] Annulation crée des mouvements `CORRECTION_POS` de restitution pour chaque ligne
- [ ] Vente marquée `annulee = true` (jamais supprimée physiquement)
- [ ] `motif` obligatoire

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
- [ ] Condition : `stock_réel ≤ seuil_alerte` ET `seuil_alerte > 0`
- [ ] `NotificationAlerte` créée en base (type `STOCK_BAS` ou `RUPTURE` si stock = 0)
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
- [ ] Retourne alertes non lues (`lue = false`) de l'entreprise connectée
- [ ] Tri par date DESC

**Endpoint :** `GET /api/v1/alertes?lue=false&page=0&size=20`

---

### US-073 — Marquer une alerte comme lue

**Priorité :** P1 | **Sprint :** 9 | **Points :** 1

**En tant que** Gestionnaire de stock,
**je veux** marquer une alerte comme traitée,
**afin de** nettoyer mon tableau de bord des alertes résolues.

**Endpoint :** `PATCH /api/v1/alertes/{id}/lire`

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

## Récapitulatif par Sprint

| Sprint | Durée | US incluses | Points | Objectif |
|--------|-------|-------------|--------|----------|
| **Sprint 1** | 2 sem. | US-001 à US-005 | 19 | Fondations techniques, CI/CD, Docker |
| **Sprint 2** | 2 sem. | US-006 à US-012 | 21 | Authentification complète P0 |
| **Sprint 3** | 2 sem. | US-013 à US-021, US-074 | 22 | Groupe, Filiales, Utilisateurs, Email bienvenue |
| **Sprint 4** | 2 sem. | US-019, US-022 à US-026, US-075 | 20 | Employés, Profil, Activation |
| **Sprint 5** | 2 sem. | US-027 à US-043 | 24 | Catalogue complet, Clients, Fournisseurs |
| **Sprint 6** | 2 sem. | US-044 à US-050 | 23 | Cycle d'achat complet avec mouvements ENTREE |
| **Sprint 7** | 2 sem. | US-051 à US-054, US-056 à US-062 | 34 | Stock réel, Corrections, Ventes B2B |
| **Sprint 8** | 2 sem. | US-055, US-064 à US-070, US-071 | 31 | Stock consolidé, Caisse, Transferts, Alertes |
| **Sprint 9** | 2 sem. | US-063, US-067, US-072 à US-073, US-075 | 14 | Facture PDF, Annulation vente, Centre alertes |
| **Sprint 10** | 2 sem. | US-076 à US-079 | 14 | Reporting et statistiques P1 |
| **Sprint 11** | 2 sem. | US-080 | 3 | Export CSV P2 |

**Total P0 :** 49 user stories — 175 points estimés
**Total P1 :** 22 user stories — 80 points estimés
**Total P2 :** 4 user stories — 12 points estimés
**Total backlog :** **75 user stories** | **267 story points**

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
