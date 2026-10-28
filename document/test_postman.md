# StockMaster CM — Collection Postman

> **Référence :** GS-POSTMAN-2026-01
> **Version :** 1.0 — Juin 2026
> **Statut :** ✅ Document actif — doit être mis à jour à chaque nouvel endpoint ou modification d'API
>
> **⚠️ CE FICHIER EST LE CONTRAT D'API OPPOSABLE (porte G4)** — toute modification d'API doit être reflétée ici dans la même PR.

---

## 📋 À propos de ce document

Ce fichier constitue la **collection Postman officielle** du projet StockMaster CM. Il est versionné avec le code et doit être mis à jour à chaque évolution de l'API.

**Trois façons d'obtenir la collection :**
1. **Ce fichier Markdown** — source de vérité, lisible directement, versionné avec le code
2. **Import automatique depuis Swagger/OpenAPI** (recommandé) :
   - Lancer l'application (`mvn spring-boot:run -pl stockmaster-bootstrap`)
   - Aller sur `http://localhost:8080/v3/api-docs` → Copier le JSON
   - Postman → **Import** → **Raw text** → Coller le JSON → **Import**
   - Tous les endpoints sont automatiquement créés avec leurs paramètres
3. **Fichier JSON officiel** (`document/postman_collection.json`) — format Collection v2.1, prêt à importer directement dans Postman.
   - Postman → **Import** → **File** → Sélectionner `document/postman_collection.json`
   - Alternative : glisser-déposer le fichier dans Postman
   - La collection inclut : variables d'environnement, authentification Bearer automatique, scripts de test pour chaque endpoint

---

## 🔗 Table des matières

- [Environnement Postman](#environnement-postman)
- [Variables d'environnement](#variables-denvironnement)
- [Endpoints disponibles](#endpoints-disponibles)
  - [Auth — `/api/v1/auth`](#1-auth--apiv1auth)
  - [Groupe — `/api/v1/groupe`](#2-groupe--apiv1groupe)
  - [Utilisateurs — `/api/v1/utilisateurs`](#3-utilisateurs--apiv1utilisateurs)
  - [Catalogue — `/api/v1/categories` et `/api/v1/articles`](#4-catalogue--apiv1categories-et-apiv1articles)
  - [Tiers — `/api/v1/clients` et `/api/v1/fournisseurs`](#5-tiers--apiv1clients-et-apiv1fournisseurs)
  - [Achats — `/api/v1/commandes-fournisseur`](#6-achats--apiv1commandes-fournisseur)
  - [Stock — `/api/v1/stock`](#7-stock--apiv1stock)
  - [Ventes — `/api/v1/commandes-client` et `/api/v1/ventes`](#8-ventes--apiv1commandes-client-et-apiv1ventes)
  - [Groupes — Transferts — `/api/v1/groupe/transferts`](#9-groupes--transferts--apiv1groupetransferts)
  - [Notifications — `/api/v1/alertes`](#10-notifications--apiv1alertes)
  - [Reporting — `/api/v1/reporting`](#11-reporting--apiv1reporting)
  - [Caisse — `/api/v1/caisse`](#12-caisse--apiv1caisse)
  - [Inventaire — `/api/v1/inventaires`](#13-inventaire--apiv1inventaires)
  - [Imports — `/api/v1/imports`](#14-imports--apiv1imports)
- [Scripts de test Postman](#scripts-de-test-postman)
- [Journal des modifications](#journal-des-modifications)

---

## Environnement Postman

### Variables de collection

> ⚠️ **Convention : `pm.collectionVariables`** (variables déclarées au niveau **Collection**, pas dans un Environnement Postman séparé). C'est ce que `postman_collection.json` utilise réellement (18 occurrences). `pm.environment.*` ne fonctionne pas avec cette collection — voir item C-04 de la checklist `DEC-031`.

Déclarer les variables suivantes dans **Collection → Variables** :

| Variable | Valeur initiale (dev) | Description |
|---|---|---|
| `base_url` | `http://localhost:8080` | URL de base de l'API |
| `access_token` | *(vide)* | Token JWT récupéré après login |
| `refresh_token` | *(vide)* | Token de rafraîchissement |
| `current_group_id` | *(vide)* | ID du groupe — renseigné par l'inscription |
| `article_id` | *(vide)* | ID d'article pour les tests |
| `categorie_id` | *(vide)* | ID de catégorie pour les tests |
| `client_id` | *(vide)* | ID de client pour les tests |
| `fournisseur_id` | *(vide)* | ID de fournisseur pour les tests |
| `commande_fourn_id` | *(vide)* | ID de commande fournisseur |
| `commande_client_id` | *(vide)* | ID de commande client |
| `vente_id` | *(vide)* | ID de vente directe |
| `transfert_id` | *(vide)* | ID de transfert stock |

### Scripts d'authentification automatique

Dans Postman, ajouter ce script dans **Collection → Pre-request Script** pour injecter automatiquement le token :

```javascript
// Récupérer le token depuis les variables de collection
const token = pm.collectionVariables.get("access_token");
if (token) {
    pm.request.headers.add({
        key: "Authorization",
        value: `Bearer ${token}`
    });
}
```

Et ce script dans la requête **Login (POST)** → **Tests** pour sauvegarder automatiquement le token :

```javascript
if (pm.response.code === 200) {
    const json = pm.response.json();
    pm.collectionVariables.set("access_token", json.data.accessToken);
    pm.collectionVariables.set("refresh_token", json.data.refreshToken);
}
```

> **Note :** `LoginResponse` ne contient que `accessToken`, `refreshToken`, `expiresIn`, `role`, `scope` — il n'y a **pas** de `entrepriseId` ni `groupId` dans la réponse de connexion (ils sont dans le JWT). Le `groupId` est récupéré à l'inscription (`InscriptionResponse`).

---

## Endpoints disponibles

---

## 1. Auth — `/api/v1/auth`

### 1.1 POST — Inscription entreprise unique

> **US :** US-006
> **Statut :** ✅ Implémenté
> **Authentification :** ❌ Non

#### Requête

```http
POST {{base_url}}/api/v1/auth/inscription/entreprise-unique
Content-Type: application/json

{
    "nomEntreprise": "Épicerie Centrale",
    "nif": "M071234567890Z",
    "email": "jean.kamga@test.cm",
    "telephone": "+237691234567",
    "adminNom": "Kamga",
    "adminPrenom": "Jean",
    "motDePasse": "Test@2026"
}
```

> **Contraintes (`InscriptionEntrepriseUniqueRequest`) :** `nif` **optionnel** (max 20) · `telephone` **obligatoire**, format E.164 `^\+[1-9]\d{1,14}$` · `adminNom`/`adminPrenom` obligatoires · mot de passe : 8-50 caractères, 1 minuscule + 1 majuscule + 1 chiffre + 1 caractère spécial. `ville` et `quartier` **n'existent plus** (rattachés au profil, US-026).

#### Réponse — Succès (201 Created)

```json
{
    "success": true,
    "message": "Votre espace a été créé. Vérifiez votre email pour activer votre compte.",
    "data": {
        "email": "jean.kamga@test.cm",
        "groupId": 1
    }
}
```

#### Réponse — Email déjà existant (409 Conflict)

```json
{
    "type": "/errors/auth-002",
    "title": "Conflit",
    "status": 409,
    "detail": "Un compte avec cet email existe déjà",
    "instance": "/api/v1/auth/inscription/entreprise-unique",
    "errorCode": "AUTH_002",
    "timestamp": "2026-06-14T10:00:00Z"
}
```

#### Tests Postman (onglet Tests)

```javascript
pm.test("Statut 201 Created", () => {
    pm.response.to.have.status(201);
});
pm.test("Réponse contient success = true", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data).to.have.property("groupId");
});
```

---

### 1.2 POST — Connexion

> **US :** US-008
> **Statut :** ✅ Implémenté
> **Authentification :** ❌ Non

#### Requête

```http
POST {{base_url}}/api/v1/auth/login
Content-Type: application/json

{
    "email": "jean.kamga@test.cm",
    "motDePasse": "Test@2026"
}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "expiresIn": 900,
        "role": "ADMIN_GROUPE",
        "scope": "GROUPE"
    }
}
```

#### Réponse — Credentials invalides (401 Unauthorized)

```json
{
    "type": "/errors/auth-001",
    "title": "Non autorisé",
    "status": 401,
    "detail": "Email ou mot de passe incorrect",
    "instance": "/api/v1/auth/login",
    "errorCode": "AUTH_001",
    "timestamp": "2026-06-14T10:00:00Z"
}
```

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Token JWT présent et valide", () => {
    const json = pm.response.json();
    pm.expect(json.data.accessToken).to.match(/^eyJ/);
    pm.expect(json.data.expiresIn).to.eql(900);
});
```

---

### 1.3 POST — Inscription groupe multi-sites

> **US :** US-007
> **Statut :** ✅ Implémenté
> **Branche :** `feature/GS-007-inscription-groupe`
> **Endpoint :** `POST /api/v1/auth/inscription/groupe`
> **Authentification :** ❌ Non

#### Requête

```http
POST {{base_url}}/api/v1/auth/inscription/groupe
Content-Type: application/json

{
    "nomGroupe": "Distribo Sarl",
    "villeSiege": "Yaoundé",
    "nif": "M123456789",
    "telephone": "+237699000001",
    "emailEntreprise": "contact@distribo.cm",
    "prenom": "Paul",
    "nom": "Biya Jr",
    "emailAdmin": "paul@distribo.cm",
    "motDePasse": "MotDePasse@2026"
}
```

> **Contraintes (`InscriptionGroupeRequest`) :** `villeSiege` (et non `villesiege` — Jackson est sensible à la casse) · `telephone` **au format E.164** `^\+[1-9]\d{1,14}$`, un numéro nu comme `699000001` est **refusé en 422** · `nif` optionnel (format `[A-Za-z0-9/-]`).

#### Réponse — Succès (201 Created)

```json
{
    "success": true,
    "message": "Votre groupe a été créé. Créez votre première filiale depuis le tableau de bord.",
    "data": {
        "email": "paul@distribo.cm",
        "groupId": 2
    }
}
```

---

### 1.4 POST — Refresh token (avec rotation, US-083)

> **US :** US-009, US-083
> **Statut :** ✅ Implémenté
> **Branche :** `feature/GS-083-rotation-refresh-token`
> **Endpoint :** `POST /api/v1/auth/refresh`
> **Authentification :** ❌ Non

> ⚠️ **Rotation à usage unique (US-083) :** chaque appel réussi invalide immédiatement le refresh token présenté et en émet un nouveau. Le client **doit** remplacer sa copie du refresh token par celui reçu dans la réponse à chaque appel — réutiliser un ancien refresh token (même valide auparavant) déclenche la révocation de **toute** la session (tous les appareils déconnectés).

#### Requête

```http
POST {{base_url}}/api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Réponse — Succès, rotation effectuée (200 OK)

```json
{
    "success": true,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "expiresIn": 900
    }
}
```

#### Réponse — Token invalide/expiré (401 Unauthorized)

```json
{
    "type": "/errors/auth-001",
    "title": "Non autorisé",
    "status": 401,
    "detail": "Email ou mot de passe incorrect",
    "instance": "/api/v1/auth/refresh",
    "errorCode": "AUTH_001"
}
```

#### Réponse — Rejeu détecté (401 Unauthorized, US-083)

Le refresh token présenté a déjà été utilisé pour une rotation précédente — toute la session est révoquée.

```json
{
    "type": "/errors/auth-006",
    "title": "Refresh token invalide ou expiré",
    "status": 401,
    "detail": "Refresh token invalide ou expiré",
    "instance": "/api/v1/auth/refresh",
    "errorCode": "AUTH_006"
}
```

---

### 1.5 POST — Déconnexion

> **US :** US-010
> **Statut :** ✅ Implémenté
> **Branche :** `feature/GS-010-logout` (PR en attente)
> **Endpoint :** `POST /api/v1/auth/logout`
> **Authentification :** ✅ Oui (Bearer token)

#### Requête

```http
POST {{base_url}}/api/v1/auth/logout
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "message": "Déconnexion réussie"
}
```

---

### 1.6 POST — Mot de passe oublié

> **US :** US-011
> **Statut :** ✅ Implémenté
> **Branche :** `feature/GS-011-forgot-password` (PR en attente)
> **Endpoint :** `POST /api/v1/auth/forgot-password`
> **Authentification :** ❌ Non

#### Requête

```http
POST {{base_url}}/api/v1/auth/forgot-password
Content-Type: application/json

{
    "email": "jean.kamga@test.cm"
}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "message": "Si cet email existe, un lien de réinitialisation vous a été envoyé."
}
```

---

### 1.7 POST — Réinitialisation mot de passe

> **US :** US-012
> **Statut :** ✅ Implémenté (PR #11 mergée dans `main`)
> **Endpoint :** `POST /api/v1/auth/reset-password`
> **Authentification :** ❌ Non

#### Requête

```http
POST {{base_url}}/api/v1/auth/reset-password
Content-Type: application/json

{
    "token": "uuid-reset-token",
    "nouveauMotDePasse": "NewPass@2026"
}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "message": "Mot de passe réinitialisé avec succès."
}
```

#### Réponse — Token invalide/expiré (400 Bad Request)

```json
{
    "type": "/errors/auth-009",
    "title": "Requête invalide",
    "status": 400,
    "detail": "Token de réinitialisation invalide ou expiré",
    "instance": "/api/v1/auth/reset-password",
    "errorCode": "AUTH_009",
    "timestamp": "2026-06-14T10:00:00Z"
}
```

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Message de succès", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.message).to.include("réinitialisé");
});
```

---

### 1.8 PUT — Changement de mot de passe

> **US :** US-013
> **Statut :** 🚧 Implémenté — En attente de PR
> **Branche :** `feature/GS-013-change-password`
> **Endpoint :** `PUT /api/v1/auth/change-password`
> **Authentification :** ✅ Oui (Bearer token)

#### Requête

```http
PUT {{base_url}}/api/v1/auth/change-password
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "ancienMotDePasse": "MotDePasse@2026",
    "nouveauMotDePasse": "NewPass@2026"
}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "message": "Mot de passe modifié avec succès."
}
```

#### Réponse — Ancien mot de passe incorrect (400 Bad Request)

```json
{
    "type": "/errors/sec-002",
    "title": "Requête invalide",
    "status": 400,
    "detail": "Mot de passe incorrect",
    "instance": "/api/v1/auth/change-password",
    "errorCode": "SEC_002",
    "timestamp": "2026-06-14T10:00:00Z"
}
```

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Message de succès", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.message).to.include("modifié");
});
```

---

## 2. Groupe — `/api/v1/groupe`

> **Statut :** 🔜 EPIC 3 en cours (US-020, Sprint 3-4) — US-014 à US-019 ✅ implémentées

### 2.1 PUT — Modifier le groupe

> **US :** US-014
> **Statut :** ✅ Implémenté
> **Authentification :** ✅ Oui — rôle `ADMIN_GROUPE` uniquement (`@PreAuthorize`)
> **Content-Type :** `multipart/form-data` (upload de logo possible)

Modification **partielle** (sémantique PATCH) : seuls les champs transmis sont appliqués. Le groupe modifié est toujours celui du JWT (`groupId` du principal) — aucun identifiant en entrée.

#### Requête

```http
PUT {{base_url}}/api/v1/groupe
Authorization: Bearer {{access_token}}
Content-Type: multipart/form-data

nomGroupe: Distribo Sarl Cameroun
raisonSociale: Distribo Sarl Cameroun SA
nif: M012345678901X
logo: (fichier PNG/JPEG/WebP, 2 Mo max)
```

Tous les champs sont optionnels et indépendants — envoyer uniquement `logo` remplace le logo sans toucher au nom, par exemple.

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "id": 1,
        "nomGroupe": "Distribo Sarl Cameroun",
        "planAbonnement": "PRO",
        "limiteFiliales": 15,
        "nombreFiliales": 7,
        "dateExpirationPlan": "2027-06-30",
        "logo": "http://localhost:9000/stockmaster/groupe/1/3f2504e0-....png",
        "nif": "M012345678901X",
        "raisonSociale": "Distribo Sarl Cameroun SA"
    }
}
```

#### Réponse — Nom déjà utilisé (409 Conflict)

```json
{
    "type": "/errors/grp-004",
    "title": "Conflit",
    "status": 409,
    "detail": "Ce nom de groupe est déjà utilisé",
    "instance": "/api/v1/groupe",
    "errorCode": "GRP_004",
    "timestamp": "2026-09-14T10:00:00Z"
}
```

#### Réponse — NIF déjà utilisé (409 Conflict)

```json
{
    "errorCode": "RES_006",
    "detail": "Ce NIF est déjà utilisé",
    "status": 409
}
```

#### Réponse — Fichier logo invalide

| Cas | Code | HTTP |
|---|---|---|
| Fichier vide | `SYS_002` | 400 |
| Fichier > 2 Mo | `SYS_006` | 413 |
| Type non autorisé (hors PNG/JPEG/WebP) | `SYS_004` | 415 |

#### Réponse — Rôle non autorisé (403 Forbidden)

Tout rôle autre que `ADMIN_GROUPE` (ex. `CAISSIER`) reçoit un `403` via `@PreAuthorize`.

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Groupe mis à jour", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.id).to.be.a('number');
});
```

---

### 2.2 GET — Consulter le groupe

> **US :** US-015
> **Statut :** ✅ Implémenté
> **Authentification :** ✅ Oui — rôle `ADMIN_GROUPE` uniquement (`@PreAuthorize`)

Le groupe consulté est **toujours** celui du JWT (`groupId` du principal) — l'endpoint ne prend aucun identifiant, l'isolation multi-tenant est structurelle (pas de risque de consulter le groupe d'un autre tenant).

#### Requête

```http
GET {{base_url}}/api/v1/groupe
Authorization: Bearer {{access_token}}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "id": 1,
        "nomGroupe": "Distribo Sarl",
        "planAbonnement": "PRO",
        "limiteFiliales": 15,
        "nombreFiliales": 7,
        "dateExpirationPlan": "2027-06-30"
    }
}
```

#### Réponse — Rôle non autorisé (403 Forbidden)

Tout rôle autre que `ADMIN_GROUPE` (ex. `CAISSIER`) reçoit un `403` via `@PreAuthorize`.

#### Réponse — Groupe introuvable (404 Not Found)

```json
{
    "type": "/errors/res-001",
    "title": "Ressource non trouvée",
    "status": 404,
    "detail": "Ressource non trouvée",
    "instance": "/api/v1/groupe",
    "errorCode": "RES_001",
    "timestamp": "2026-09-14T10:00:00Z"
}
```

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Informations du groupe présentes", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.id).to.be.a('number');
    pm.expect(json.data.nomGroupe).to.be.a('string');
    pm.expect(json.data.planAbonnement).to.be.oneOf(['GRATUIT', 'PRO', 'PERSONNALISE']);
    pm.expect(json.data.limiteFiliales).to.be.a('number');
    pm.expect(json.data.nombreFiliales).to.be.a('number');
});
```

---

### 2.3 POST — Créer une filiale

> **US :** US-016
> **Statut :** ✅ Implémenté
> **Authentification :** ✅ Oui — rôle `ADMIN_GROUPE` uniquement (`@PreAuthorize`)

La filiale est créée dans le groupe du JWT (`groupId` du principal), rattachée à la maison mère du groupe (`parent_id`). Refusée si la limite de filiales du plan (`tenant_group.limite_filiales`, `DEC-015`) est atteinte, ou si `codeFiliale` est déjà utilisé dans le groupe.

#### Requête

```http
POST {{base_url}}/api/v1/groupe/filiales
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "nom": "Boutique Akwa",
    "ville": "Douala",
    "quartier": "Akwa",
    "codeFiliale": "DLA01"
}
```

#### Réponse — Succès (201 Created)

```json
{
    "success": true,
    "data": {
        "id": 42,
        "nom": "Boutique Akwa",
        "codeFiliale": "DLA01",
        "ville": "Douala",
        "quartier": "Akwa",
        "actif": true,
        "siteOperationnel": true,
        "parentId": 10
    }
}
```

#### Réponse — Limite de filiales atteinte (403 Forbidden)

```json
{
    "errorCode": "GRP_001",
    "detail": "Limite de filiales atteinte pour votre plan d'abonnement",
    "status": 403
}
```

#### Réponse — Code filiale déjà utilisé (409 Conflict)

```json
{
    "errorCode": "RES_004",
    "detail": "Ce code filiale existe déjà dans le groupe",
    "status": 409
}
```

#### Réponse — Rôle non autorisé (403 Forbidden)

Tout rôle autre que `ADMIN_GROUPE` (ex. `CAISSIER`) reçoit un `403` via `@PreAuthorize`.

#### Tests Postman

```javascript
pm.test("Statut 201 Created", () => {
    pm.response.to.have.status(201);
});
pm.test("Filiale créée avec parentId", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.id).to.be.a('number');
    pm.expect(json.data.parentId).to.be.a('number');
    pm.expect(json.data.siteOperationnel).to.be.true;
});
```

---

### 2.4 GET — Lister les filiales

> **US :** US-017
> **Statut :** ✅ Implémenté
> **Authentification :** ✅ Oui — rôle `ADMIN_GROUPE` uniquement (`@PreAuthorize`)

Retourne uniquement les filiales du groupe du JWT (`groupId` du principal). Pagination + filtres `actif`/`ville` optionnels.

#### Requête

```http
GET {{base_url}}/api/v1/groupe/filiales?page=0&size=20&actif=true&ville=Douala
Authorization: Bearer {{access_token}}
```

Tous les paramètres sont optionnels : `page` (défaut 0), `size` (défaut 20, borné à `stockmaster.pagination.max-page-size` = 100), `actif`, `ville`.

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": 42,
                "nom": "Boutique Akwa",
                "codeFiliale": "DLA01",
                "ville": "Douala",
                "quartier": "Akwa",
                "actif": true,
                "siteOperationnel": true,
                "parentId": 10,
                "nombreEmployes": 3
            }
        ],
        "page": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
    }
}
```

#### Réponse — Rôle non autorisé (403 Forbidden)

Tout rôle autre que `ADMIN_GROUPE` (ex. `CAISSIER`) reçoit un `403` via `@PreAuthorize`.

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Page de filiales bien formee", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.content).to.be.an('array');
    pm.expect(json.data.totalElements).to.be.a('number');
});
```

---

### 2.5 PUT — Modifier une filiale

> **US :** US-018
> **Statut :** ✅ Implémenté
> **Authentification :** ✅ Oui — rôle `ADMIN_GROUPE` uniquement (`@PreAuthorize`)

Modification **partielle** (sémantique PATCH, même contrat que US-014/016) — seuls les champs transmis sont appliqués. La filiale doit appartenir au groupe du JWT.

#### Requête

```http
PUT {{base_url}}/api/v1/groupe/filiales/42
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "nom": "Boutique Bonanjo",
    "codeFiliale": "DLA02",
    "ville": "Douala",
    "quartier": "Bonanjo"
}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "id": 42,
        "nom": "Boutique Bonanjo",
        "codeFiliale": "DLA02",
        "ville": "Douala",
        "quartier": "Bonanjo",
        "actif": true,
        "siteOperationnel": true,
        "parentId": 10,
        "nombreEmployes": 2
    }
}
```

#### Réponse — Filiale introuvable / autre groupe (404 Not Found)

Ne révèle jamais l'existence d'une filiale d'un autre groupe — même code d'erreur que pour un id inexistant.

```json
{
    "errorCode": "RES_001",
    "detail": "Ressource non trouvée",
    "status": 404
}
```

#### Réponse — Code filiale déjà utilisé (409 Conflict)

```json
{
    "errorCode": "RES_004",
    "detail": "Ce code filiale existe déjà dans le groupe",
    "status": 409
}
```

#### Réponse — Rôle non autorisé (403 Forbidden)

Tout rôle autre que `ADMIN_GROUPE` (ex. `CAISSIER`) reçoit un `403` via `@PreAuthorize`.

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Filiale mise a jour", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.id).to.be.a('number');
});
```

---

### 2.6 PATCH — Activer/Désactiver une filiale

> **US :** US-019
> **Statut :** ✅ Implémenté
> **Authentification :** ✅ Oui — rôle `ADMIN_GROUPE` uniquement (`@PreAuthorize`)

Bascule uniquement le champ `actif` — les données de la filiale sont conservées et consultables. La filiale doit appartenir au groupe du JWT.

> ⚠️ Le blocage effectif des mouvements de stock/commandes sur un site désactivé n'est **pas encore appliqué** : les modules stock/vente (EPICs 5-6) n'existent pas encore. Cet endpoint pose uniquement le statut ; l'application de la règle métier viendra avec ces modules.

#### Requête

```http
PATCH {{base_url}}/api/v1/groupe/filiales/42/statut
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "actif": false
}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "id": 42,
        "nom": "Boutique Akwa",
        "codeFiliale": "DLA01",
        "ville": "Douala",
        "quartier": "Akwa",
        "actif": false,
        "siteOperationnel": true,
        "parentId": 10,
        "nombreEmployes": 5
    }
}
```

#### Réponse — Filiale introuvable / autre groupe (404 Not Found)

Même comportement que US-018 : ne révèle jamais l'existence d'une filiale d'un autre groupe.

#### Réponse — Rôle non autorisé (403 Forbidden)

Tout rôle autre que `ADMIN_GROUPE` (ex. `CAISSIER`) reçoit un `403` via `@PreAuthorize`.

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Statut actif mis a jour", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.actif).to.be.a('boolean');
});
```

---

### 2.7 GET — Dashboard consolidé (futur)

---

## 3. Utilisateurs — `/api/v1/utilisateurs`

> **Statut :** ✅ **Implémenté** (EPIC 4 — US-021 à US-026 livrées, PR #23 à #29)
> **Enveloppe :** `ApiResponse<T>` sur tous les endpoints · `PageResponse<T>` pour la liste
> **Identité :** `entreprise_id` / `group_id` **toujours issus du JWT**, jamais du corps

### 3.1 POST — Créer un Admin Filiale (US-021)

> `@PreAuthorize("hasRole('ADMIN_GROUPE')")` · **201 Created**

```http
POST {{base_url}}/api/v1/utilisateurs/admin-filiale
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "filialeId": 3,
    "prenom": "Aïcha",
    "nom": "Nguemo",
    "email": "aicha.nguemo@distribo.cm"
}
```

> Rôle **forcé `ADMIN_FILIALE`** / scope `FILIALE` côté serveur — jamais lu de la requête. Le compte naît `actif=false` + `emailVerifie=false`, mot de passe aléatoire haché, invitation envoyée par e-mail (token Redis 48 h). Hors groupe → **404 uniforme** ; email déjà pris → **409 `AUTH_008`** ; plan dépassé → **403 `USR_001`**.

### 3.2 POST — Créer un employé (US-022)

> `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")` · **201 Created**

```http
POST {{base_url}}/api/v1/utilisateurs/employes
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "prenom": "Blaise",
    "nom": "Mballa",
    "email": "blaise.mballa@distribo.cm",
    "role": "GESTIONNAIRE_STOCK",
    "motDePasseProvisoire": "Bienvenue@2026"
}
```

> `role` ∈ `GESTIONNAIRE_STOCK` \| `RESP_ACHATS` \| `COMMERCIAL` \| `CAISSIER` — tout rôle `ADMIN_*` → **403 `SEC_001`** (contrôle fail-fast avant toute I/O). Compte né `actif=true` + `emailVerifie=true` : connexion immédiate (DEC-016 ne s'applique qu'à l'inscription publique). `motDePasseProvisoire` : ≥ 8 car, 1 majuscule, 1 chiffre, 1 spécial.

### 3.3 GET — Lister les utilisateurs du périmètre (US-023)

> `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")` · **200 OK**

```http
GET {{base_url}}/api/v1/utilisateurs?role=CAISSIER&actif=true&filialeId=3&page=0&size=20
Authorization: Bearer {{access_token}}
```

| Paramètre | Type | Obligatoire | Remarque |
|---|---|---|---|
| `role` | enum | ❌ | filtre `(:x IS NULL OR …)` |
| `actif` | boolean | ❌ | idem |
| `filialeId` | long | ❌ | **Admin Groupe uniquement** ; hors de son groupe → **403 `SEC_001`** |
| `page` / `size` | int | ❌ | `size` borné par `stockmaster.pagination.max-page-size` |

```json
{
  "success": true,
  "data": {
    "content": [{ "id": 12, "prenom": "Blaise", "nom": "Mballa", "email": "blaise.mballa@distribo.cm", "role": "GESTIONNAIRE_STOCK", "actif": true, "filialeId": 3 }],
    "page": 0, "size": 20, "totalElements": 7, "totalPages": 1
  }
}
```

> ⚠️ Un Admin Filiale qui passe le `filialeId` d'une **autre** filiale reçoit **403**, jamais 404. Le champ `motDePasse` **n'apparaît jamais** dans la réponse.

### 3.4 PUT — Modifier un utilisateur (US-024)

> `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")` · **200 OK** · sémantique **PATCH** (champ absent = inchangé)

```http
PUT {{base_url}}/api/v1/utilisateurs/12
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "prenom": "Blaise",
    "nom": "Mballa",
    "email": "blaise.mballa@distribo.cm",
    "role": "COMMERCIAL"
}
```

> Tous les champs **optionnels**. **Mot de passe jamais modifiable ici** (flux US-011/012). Cible hors périmètre / inexistante / soft-supprimée → **404 `RES_001` uniforme** ; cible ou nouveau rôle `ADMIN_*` → **403 `SEC_001`** (pas d'auto-promotion) ; email déjà pris → **409 `AUTH_008`**.

### 3.5 PATCH — Activer / Désactiver un utilisateur (US-025)

> `@PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")` · **200 OK**

```http
PATCH {{base_url}}/api/v1/utilisateurs/12/statut
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "actif": false
}
```

> `actif` **obligatoire** (aucune bascule implicite). Désactivation = **révocation immédiate** : blacklist Redis `blacklist:user:{id}` + suppression de `refresh:{id}` → les tokens en cours sont refusés à la requête suivante. Réactivation → aucune révocation. Cible `ADMIN_*` → **403 `SEC_001`** (cycle de vie admin distinct).

### 3.6 GET / PUT — Profil utilisateur (US-026)

```http
GET {{base_url}}/api/v1/utilisateurs/profil
Authorization: Bearer {{access_token}}
```

**Modification — `multipart/form-data`** (les deux parts sont optionnels, sémantique PATCH) :

| Part | Type | Contenu |
|---|---|---|
| `profil` | JSON | `{"prenom":…, "nom":…, "adresseVille":…}` |
| `photo` | fichier | image PNG/JPEG/WebP, **2 Mo max** → upload MinIO (`utilisateur/{id}`) |

```http
PUT {{base_url}}/api/v1/utilisateurs/profil
Authorization: Bearer {{access_token}}
Content-Type: multipart/form-data

profil  = {"prenom":"Blaise","adresseVille":"Douala"}
photo   = (fichier)
```

> `email` et `role` **absents du DTO** → non modifiables (passe par US-024). Le profil est résolu **depuis le JWT**, jamais depuis un id passé en paramètre.

---

## 4. Catalogue — `/api/v1/categories` et `/api/v1/articles`

> **Statut :** 🔜 Non implémenté (EPIC 5, US-027 à US-035, Sprint 4-5)

### 4.1 POST — Créer une catégorie (futur)
### 4.2 GET — Lister les catégories (futur)
### 4.3 PUT — Modifier une catégorie (futur)
### 4.4 DELETE — Supprimer une catégorie (futur)
### 4.5 POST — Créer un article (futur)
### 4.6 GET — Lister les articles (futur)
### 4.7 GET — Consulter un article (futur)
### 4.8 PUT — Modifier un article (futur)
### 4.9 DELETE — Archiver un article (futur)

---

## 5. Tiers — `/api/v1/clients` et `/api/v1/fournisseurs`

> **Statut :** 🟡 EPIC 6 — **US-036 ✅ et US-037 ✅ implémentées** ; US-038 à US-043 🔜 non implémentées

### 5.1 POST — Créer un fournisseur

> **US :** US-036
> **Statut :** ✅ Implémenté (branche `feature/GS-036-creer-fournisseur`)
> **Authentification :** ✅ Oui — `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")`

La fiche est rattachée à l'`entreprise_id` **extrait du JWT** — jamais lu du corps. Un champ `entrepriseId` ajouté dans le payload est **ignoré** (le DTO n'en possède aucun).

#### Requête

```http
POST {{base_url}}/api/v1/fournisseurs
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "raisonSociale": "SARL Import Golfe",
    "telephone": "+237691234567",
    "nif": "M0126000001",
    "contact": "Jean Etoundi",
    "email": "achats@importgolfe.cm",
    "adresseVille": "Douala"
}
```

> **Contraintes (`CreerFournisseurRequest`) :** `raisonSociale` **obligatoire**, max 200 · `telephone` **optionnel** mais, si fourni, format E.164 `^\\+[1-9]\\d{1,14}$` (même regex que l'inscription) · `nif` (20), `contact` (100), `email` (150, format validé), `adresseVille` (100) optionnels. **Aucun champ tenant** dans le corps.

#### Réponse — Succès (201 Created)

```json
{
    "success": true,
    "data": {
        "id": 300,
        "raisonSociale": "SARL Import Golfe",
        "nif": "M0126000001",
        "contact": "Jean Etoundi",
        "telephone": "+237691234567",
        "email": "achats@importgolfe.cm",
        "adresseVille": "Douala",
        "entrepriseId": 10
    }
}
```

#### Réponses d'erreur

| Cas | Code | HTTP |
|---|---|---|
| `raisonSociale` vide / trop longue, `telephone` hors format | `SYS_005` | 400 |
| Rôle hors périmètre (`CAISSIER`, `COMMERCIAL`, `GESTIONNAIRE_STOCK`) | `SEC_001` | 403 |
| JWT sans `groupId` | `GRP_002` | 403 |
| Entreprise introuvable / soft-supprimée / hors groupe | `RES_001` | 404 |

#### Tests Postman

```javascript
pm.test("Statut 201 Created", () => {
    pm.response.to.have.status(201);
});
pm.test("Fournisseur rattache au tenant du JWT", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.raisonSociale).to.eql("SARL Import Golfe");
    pm.expect(json.data).to.have.property("entrepriseId");
});
```

---

### 5.2 GET — Lister / Rechercher les fournisseurs

> **US :** US-037
> **Statut :** ✅ Implémenté (branche `feature/GS-037-lister-fournisseurs`)
> **Authentification :** ✅ Oui — `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")`

Listing **strictement limité aux fournisseurs de l'entreprise du JWT** — aucun paramètre de tenant n'est accepté (un `entrepriseId` ajouté à la query string est ignoré).

#### Requête

```http
GET {{base_url}}/api/v1/fournisseurs?search=somiref&page=0&size=20
Authorization: Bearer {{access_token}}
```

| Paramètre | Type | Obligatoire | Défaut | Comportement |
|---|---|---|---|---|
| `search` | string | ❌ | — | Recherche sur **raison sociale OU ville** (`LIKE %x%`, insensible à la casse, accents non normalisés), trimé. **Absent ou blanc → tous les fournisseurs du tenant** |
| `page` | int | ❌ | `0` | négatif borné à 0 |
| `size` | int | ❌ | `20` | **borné à 100** (`stockmaster.pagination.max-page-size`) — clamp silencieux, **jamais de 400** |

Tri **fixe** : `raisonSociale ASC` (aucun tri dynamique exposé).

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": 300,
                "raisonSociale": "SOMIREF Distribution",
                "nif": "M0126000001",
                "contact": "Jean Etoundi",
                "telephone": "+237691234567",
                "email": "achats@importgolfe.cm",
                "adresseVille": "Douala",
                "entrepriseId": 10
            }
        ],
        "page": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
    }
}
```

#### Réponse — Recherche sans résultat (200 OK, **jamais 404**)

```json
{
    "success": true,
    "data": { "content": [], "page": 0, "size": 20, "totalElements": 0, "totalPages": 0 }
}
```

#### Réponses d'erreur

| Cas | Code | HTTP |
|---|---|---|
| Rôle hors périmètre (`CAISSIER`, `COMMERCIAL`, `GESTIONNAIRE_STOCK`) | `SEC_001` | 403 |
| JWT sans `groupId` | `GRP_002` | 403 |
| Entreprise du JWT introuvable / soft-supprimée / hors groupe | `RES_001` | 404 |

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Page de fournisseurs bien formee", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.content).to.be.an("array");
    pm.expect(json.data.totalElements).to.be.a("number");
});
```

---

### 5.3 PUT — Modifier un fournisseur

> **US :** US-038
> **Statut :** ✅ Implémenté (branche `feature/GS-038-modifier-fournisseur`)
> **Authentification :** ✅ Oui — `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")`

Modifie les coordonnées d'un fournisseur **de l'entreprise du JWT** uniquement.

**Sémantique PATCH sur un endpoint PUT** (convention US-014 / US-018 / US-024) : un champ absent **ou blanc** laisse la valeur actuelle en place. Un corps vide `{}` est donc valide — il ne modifie rien et renvoie l'état courant (200).

#### Requête

```http
PUT {{base_url}}/api/v1/fournisseurs/300
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
    "raisonSociale": "SARL Import Golfe SA",
    "telephone": "+237677445566",
    "nif": "M0126000099",
    "contact": "Clarisse Mballa",
    "email": "commandes@importgolfe.cm",
    "adresseVille": "Yaounde"
}
```

| Champ | Obligatoire | Contrainte | Comportement |
|---|---|---|---|
| `raisonSociale` | ❌ | max 200, **non blanc si fourni** | blanc → 400 `SYS_005` (la colonne est `NOT NULL`) ; absent → inchangé |
| `telephone` | ❌ | E.164 `^\+[1-9]\d{1,14}$`, max 20 | `691234567` (sans `+`) → 400 `SYS_005` |
| `nif` | ❌ | max 20 | **aucune unicité** (pas d'index UNIQUE en base, aucun CA) |
| `contact` | ❌ | max 100 | — |
| `email` | ❌ | format email, max 150 | — |
| `adresseVille` | ❌ | max 100 | — |

**Non modifiables** : `id` (dans le chemin), `entreprise_id` (JWT — aucun champ dans le DTO), `dateCreation`, `dateModification` (trigger V3), `supprime`.

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "id": 300,
        "raisonSociale": "SARL Import Golfe SA",
        "nif": "M0126000099",
        "contact": "Clarisse Mballa",
        "telephone": "+237677445566",
        "email": "commandes@importgolfe.cm",
        "adresseVille": "Yaounde",
        "entrepriseId": 10
    }
}
```

#### Réponses d'erreur

| Cas | Code | HTTP |
|---|---|---|
| `raisonSociale` blanche, `telephone` hors E.164, email invalide, champ trop long | `SYS_005` | 400 |
| Rôle hors périmètre (`CAISSIER`, `COMMERCIAL`, `GESTIONNAIRE_STOCK`) | `SEC_001` | 403 |
| JWT sans `groupId` | `GRP_002` | 403 |
| Fournisseur inexistant, **soft-supprimé** ou **appartenant à un autre tenant** (IDOR) | `RES_001` | 404 |
| Entreprise du JWT introuvable / soft-supprimée / hors groupe | `RES_001` | 404 |

> **Isolation tenant :** un fournisseur d'un autre tenant renvoie **404**, jamais 403 — l'existence de la fiche n'est pas révélée, et **rien n'est modifié** (test dédié).

#### Tests Postman

```javascript
pm.test("Statut 200 OK", () => {
    pm.response.to.have.status(200);
});
pm.test("Fournisseur mis a jour et scope au tenant", () => {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
    pm.expect(json.data.id).to.eql(300);
    pm.expect(json.data.raisonSociale).to.eql("SARL Import Golfe SA");
    pm.expect(json.data.entrepriseId).to.eql(10);
});
```

---

### 5.4 DELETE — Supprimer un fournisseur (futur)
### 5.5 POST — Créer un client (futur)
### 5.6 GET — Lister les clients (futur)
### 5.7 PUT — Modifier un client (futur)
### 5.8 DELETE — Supprimer un client (futur)

---

## 6. Achats — `/api/v1/commandes-fournisseur`

> **Statut :** 🔜 Non implémenté (EPIC 7, US-044 à US-050, Sprint 6)

### 6.1 POST — Créer une commande fournisseur (futur)
### 6.2 GET — Lister les commandes fournisseur (futur)
### 6.3 GET — Consulter une commande fournisseur (futur)
### 6.4 PUT — Modifier une commande (futur)
### 6.5 POST — Valider une commande (futur)
### 6.6 POST — Marquer comme livrée (futur)
### 6.7 DELETE — Supprimer une commande (futur)

---

## 7. Stock — `/api/v1/stock`

> **Statut :** 🔜 Non implémenté (EPIC 8, US-051 à US-054, Sprint 7)

### 7.1 GET — Consulter le stock réel (futur)
### 7.2 GET — Historique des mouvements (futur)
### 7.3 POST — Correction positive (futur)
### 7.4 POST — Correction négative (futur)

---

## 8. Ventes — `/api/v1/commandes-client` et `/api/v1/ventes`

> **Statut :** 🔜 Non implémenté (EPIC 9-10, US-056 à US-067, Sprint 7-8)

### 8.1 POST — Créer une commande client (futur)
### 8.2 GET — Lister les commandes client (futur)
### 8.3 GET — Consulter une commande client (futur)
### 8.4 PUT — Modifier une commande client (futur)
### 8.5 POST — Valider une commande client (futur)
### 8.6 POST — Marquer comme livrée (futur)
### 8.7 DELETE — Supprimer une commande client (futur)
### 8.8 GET — Générer facture PDF (futur)
### 8.9 POST — Enregistrer une vente directe (futur)
### 8.10 GET — Lister les ventes directes (futur)
### 8.11 GET — Consulter une vente directe (futur)
### 8.12 POST — Annuler une vente directe (futur)

---

## 9. Groupes — Transferts — `/api/v1/groupe/transferts`

> **Statut :** 🔜 Non implémenté (EPIC 11, US-068 à US-070, Sprint 8)

### 9.1 POST — Créer un transfert inter-filiales (futur)
### 9.2 GET — Lister les transferts (futur)
### 9.3 GET — Consulter un bon de transfert (futur)

---

## 10. Notifications — `/api/v1/alertes`

> **Statut :** 🔜 Non implémenté (EPIC 12, US-071 à US-075, Sprint 8-9)

### 10.1 GET — Consulter les alertes (futur)
### 10.2 PATCH — Changer l'état d'une alerte : `NON_LU`/`LU`/`RESOLU` (futur)

---

## 11. Reporting — `/api/v1/reporting`

> **Statut :** 🔜 Non implémenté (EPIC 13, US-076 à US-080, Sprint 10-11)

### 11.1 GET — Top articles (futur)
### 11.2 GET — Top clients (futur)
### 11.3 GET — Évolution CA (futur)
### 11.4 GET — Ruptures imminentes (futur)
### 11.5 GET — Export CSV (futur)

---

## 12. Caisse — `/api/v1/caisse`

> **Statut :** 🔜 Non implémenté (EPIC 14, US-087 à US-091, Sprint 8-9 — `DEC-009`, `DEC-010`, `DEC-018`)

### 12.1 POST — Ouvrir une session de caisse (futur)
### 12.2 POST — Encaisser une vente en paiement mixte (futur) — `Idempotency-Key` requis
### 12.3 POST — Clôturer une session de caisse (futur)
### 12.4 GET — Historique des sessions de caisse (futur)
### 12.5 POST — Rembourser une vente (futur) — `Idempotency-Key` requis

---

## 13. Inventaire — `/api/v1/inventaires`

> **Statut :** 🔜 Non implémenté (EPIC 15, US-092 à US-095, Sprint 9-10 — `DEC-036`)

### 13.1 POST — Ouvrir une campagne d'inventaire (futur)
### 13.2 PUT — Saisir une quantité constatée (futur)
### 13.3 GET — Feuille d'écarts (futur)
### 13.4 POST — Valider la campagne et générer les corrections (futur) — `Idempotency-Key` requis

---

## 14. Imports — `/api/v1/imports`

> **Statut :** 🔜 Non implémenté (EPIC 16, US-096 à US-099, Sprint 5-6 — `DEC-038`)

### 14.1 GET — Télécharger un gabarit CSV (futur)
### 14.2 POST — Importer le catalogue (futur) — multipart, `dryRun` supporté
### 14.3 POST — Importer le stock initial d'une filiale (futur) — multipart, `dryRun` supporté
### 14.4 POST — Importer les tiers (futur) — multipart, `dryRun` supporté

---

> ### ⚙️ Règle transverse — en-tête `Idempotency-Key` (`DEC-027`)
>
> Toute écriture créant un `mouvement_stock` **exige** l'en-tête `Idempotency-Key` :
> vente, réception, transfert, correction, remboursement, validation d'inventaire.
> Absence → `400 IDEMPOTENCY_KEY_REQUISE`. Rejeu à corps identique → la réponse mémorisée
> est renvoyée sans réexécution. Rejeu à corps différent → `409 IDEMPOTENCY_KEY_REUTILISEE`.
> Voir `US-100`.

---

## 15. Catalogue — `/api/v1/articles`

> **Statut :** ✅ Implémenté (EPIC 5, US-031 — livré le 21 oct. 2026)

### 15.1 POST — Créer un article

- **Rôles :** `GESTIONNAIRE_STOCK` / `ADMIN_FILIALE` / `ADMIN_GROUPE`
- **Corps :** `codeArticle` (unique par groupe), `designation`, `categorieId`, `prixAchatHt`, `prixVenteHt`, `tauxTva` (nullable = hérité de la catégorie), `seuilAlerte` (nullable = 0, alerte désactivée — DEC-023)
- **Calculs serveur :** `prixVenteTtc` (DEC-003, jamais saisi) + `margeBrutePct` (à la volée, non persistée)
- **Réponses :** `201` · `400` CAT_001 (taux non supporté / hors [0;100]) · `403` SEC_001 / GRP_002 · `404` RES_001 (catégorie absente/hors groupe) · `409` RES_002 (code article dupliqué)

---

## Scripts de test Postman

### Scripts de test génériques (à ajouter à chaque requête)

#### Vérification du format ApiResponse

```javascript
// Tester le format standard ApiResponse
pm.test("Réponse au format ApiResponse", () => {
    const json = pm.response.json();
    pm.expect(json).to.have.property("success");
    pm.expect(json).to.have.property("data");
});
```

#### Vérification du format ProblemResponse (RFC 7807)

```javascript
// Tester le format d'erreur RFC 7807
pm.test("Erreur au format ProblemResponse (RFC 7807)", () => {
    const json = pm.response.json();
    pm.expect(json).to.have.all.keys(
        "type", "title", "status", "detail",
        "instance", "errorCode", "timestamp"
    );
});
```

#### Extraction et sauvegarde d'un ID depuis la réponse

```javascript
// Après une création, sauvegarder l'ID
if (pm.response.code === 201) {
    const json = pm.response.json();
    if (json.data && json.data.id) {
        pm.collectionVariables.set("article_id", json.data.id);
    }
}
```

---

## Journal des modifications

| Date | Auteur | Changement | Raison |
|---|---|---|---|
| Juin 2026 | Codebuff | Création initiale | Documenter les endpoints pour Postman |
| Juin 2026 | Codebuff | Ajout fichier JSON collection v2.1 + maintenance workflow | Création de `postman_collection.json` importable directement |
| 8 sept. 2026 | Claude Code | Sections 12-14 (caisse, inventaire, imports) + règle `Idempotency-Key` ; §10.2 aligné sur `etat` | Rédaction des US `US-087` à `US-106` couvrant `DEC-009/010/027/036/038` |
| 21 oct. 2026 | Codebuff | Section 15 (`POST /api/v1/articles`) + requête dans `postman_collection.json` | Livraison US-031 (EPIC 5) |
| 27 oct. 2026 | Codebuff | Section 5.1 (`POST /api/v1/fournisseurs`) + requête dans `postman_collection.json` | Livraison US-036 (EPIC 6) |
| 27 oct. 2026 | Codebuff | Section 5.2 (`GET /api/v1/fournisseurs`) + requête dans `postman_collection.json` | Livraison US-037 (EPIC 6) |

---

> **Règle de gestion — Les deux fichiers :**
>
> Ce document ET le fichier `document/postman_collection.json` sont versionnés avec le code et doivent être traités avec le même sérieux que le code et que `progress-ledger.md`.
>
> - **Toute modification d'API** (nouvel endpoint, nouveau champ, changement de réponse) DOIT être reflétée dans les **deux fichiers** dans la **même PR**.
> - **Tout nouvel endpoint** DOIT inclure dans `test_postman.md` : méthode HTTP, URL, corps de requête (JSON), réponse succès, réponse erreur, tests Postman.
> - **Tout nouvel endpoint** DOIT être ajouté dans `postman_collection.json` avec la requête, les tests et l'authentification appropriée.
> - **Les deux fichiers doivent toujours être cohérents** — une modification dans l'un doit être répercutée dans l'autre.
> - **Les endpoints non encore implémentés** sont listés avec le tag 🔜 et leur US associée.
