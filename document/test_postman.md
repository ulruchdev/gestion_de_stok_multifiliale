# StockMaster CM — Collection Postman

> **Référence :** GS-POSTMAN-2026-01
> **Version :** 1.0 — Juin 2026
> **Statut :** ✅ Document actif — doit être mis à jour à chaque nouvel endpoint ou modification d'API
>
> **⚠️ CE FICHIER DOIT ÊTRE MAINTENU COMME `implementation.md`** — toute modification d'API doit être reflétée ici dans la même PR.

---

## 📋 À propos de ce document

Ce fichier constitue la **collection Postman officielle** du projet StockMaster CM. Il est versionné avec le code et doit être mis à jour à chaque évolution de l'API.

**Trois façons d'obtenir la collection :**
1. **Ce fichier Markdown** — source de vérité, lisible directement, versionné avec le code
2. **Import automatique depuis Swagger/OpenAPI** (recommandé) :
   - Lancer l'application (`mvn spring-boot:run -pl stockmaster-shared`)
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
- [Scripts de test Postman](#scripts-de-test-postman)
- [Journal des modifications](#journal-des-modifications)

---

## Environnement Postman

### Variables d'environnement

Créer un environnement Postman avec les variables suivantes :

| Variable | Valeur initiale (dev) | Description |
|---|---|---|
| `base_url` | `http://localhost:8080` | URL de base de l'API |
| `access_token` | *(vide)* | Token JWT récupéré après login |
| `refresh_token` | *(vide)* | Token de rafraîchissement |
| `entreprise_id` | *(vide)* | ID de l'entreprise connectée |
| `group_id` | *(vide)* | ID du groupe connecté |
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
// Récupérer le token depuis la variable d'environnement
const token = pm.environment.get("access_token");
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
    pm.environment.set("access_token", json.data.accessToken);
    pm.environment.set("refresh_token", json.data.refreshToken);
    pm.environment.set("entreprise_id", json.data.entrepriseId);
}
```

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
    "nomBoutique": "Épicerie Centrale",
    "ville": "Douala",
    "quartier": "Akwa",
    "prenom": "Jean",
    "nom": "Kamga",
    "email": "jean.kamga@test.cm",
    "motDePasse": "Test@2026"
}
```

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

### 1.4 POST — Refresh token

> **US :** US-009
> **Statut :** ✅ Implémenté
> **Branche :** `feature/GS-009-refresh-token` (PR en attente)
> **Endpoint :** `POST /api/v1/auth/refresh`
> **Authentification :** ❌ Non

#### Requête

```http
POST {{base_url}}/api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Réponse — Succès (200 OK)

```json
{
    "success": true,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
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

> **Statut :** 🔜 Non implémenté (EPIC 3, US-014 à US-020, Sprint 3-4)

### 2.1 PUT — Modifier le groupe (futur)
### 2.2 GET — Consulter le groupe (futur)
### 2.3 POST — Créer une filiale (futur)
### 2.4 GET — Lister les filiales (futur)
### 2.5 PUT — Modifier une filiale (futur)
### 2.6 PATCH — Activer/Désactiver une filiale (futur)
### 2.7 GET — Dashboard consolidé (futur)

---

## 3. Utilisateurs — `/api/v1/utilisateurs`

> **Statut :** 🔜 Non implémenté (EPIC 4, US-021 à US-026, Sprint 3-4)

### 3.1 POST — Créer un Admin Filiale (futur)
### 3.2 POST — Créer un employé (futur)
### 3.3 GET — Lister les utilisateurs (futur)
### 3.4 PUT — Modifier un utilisateur (futur)
### 3.5 PATCH — Désactiver un utilisateur (futur)
### 3.6 GET/PUT — Profil utilisateur (futur)

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

> **Statut :** 🔜 Non implémenté (EPIC 6, US-036 à US-043, Sprint 5)

### 5.1 POST — Créer un fournisseur (futur)
### 5.2 GET — Lister les fournisseurs (futur)
### 5.3 PUT — Modifier un fournisseur (futur)
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
### 10.2 PATCH — Marquer une alerte comme lue (futur)

---

## 11. Reporting — `/api/v1/reporting`

> **Statut :** 🔜 Non implémenté (EPIC 13, US-076 à US-080, Sprint 10-11)

### 11.1 GET — Top articles (futur)
### 11.2 GET — Top clients (futur)
### 11.3 GET — Évolution CA (futur)
### 11.4 GET — Ruptures imminentes (futur)
### 11.5 GET — Export CSV (futur)

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
        pm.environment.set("article_id", json.data.id);
    }
}
```

---

## Journal des modifications

| Date | Auteur | Changement | Raison |
|---|---|---|---|
| Juin 2026 | Codebuff | Création initiale | Documenter les endpoints pour Postman |
| Juin 2026 | Codebuff | Ajout fichier JSON collection v2.1 + maintenance workflow | Création de `postman_collection.json` importable directement |

---

> **Règle de gestion — Les deux fichiers :**
>
> Ce document ET le fichier `document/postman_collection.json` sont versionnés avec le code et doivent être traités avec le même sérieux que `implementation.md`.
>
> - **Toute modification d'API** (nouvel endpoint, nouveau champ, changement de réponse) DOIT être reflétée dans les **deux fichiers** dans la **même PR**.
> - **Tout nouvel endpoint** DOIT inclure dans `test_postman.md` : méthode HTTP, URL, corps de requête (JSON), réponse succès, réponse erreur, tests Postman.
> - **Tout nouvel endpoint** DOIT être ajouté dans `postman_collection.json` avec la requête, les tests et l'authentification appropriée.
> - **Les deux fichiers doivent toujours être cohérents** — une modification dans l'un doit être répercutée dans l'autre.
> - **Les endpoints non encore implémentés** sont listés avec le tag 🔜 et leur US associée.
