# StockMaster CM — Auth (Authentification & Accès)

> **Artefact :** `stockmaster-auth`
> **Statut :** ✅ Actif (code implémenté)
> **Dépendance :** `stockmaster-shared`
> **Package :** `com.stockmaster.auth`

---

## Rôle

Module d'authentification et de gestion des accès. Il gère :

- ✅ Inscription d'une entreprise unique (US-006)
- ✅ Connexion JWT avec access + refresh tokens (US-008)
- ✅ Rate limiting sur `/login` (Redis)
- 🔜 Inscription groupe multi-sites (US-007)
- 🔜 Refresh token / Déconnexion (US-009, US-010)
- 🔜 Mot de passe oublié / Réinitialisation (US-011, US-012)
- 🔜 Changement de mot de passe (US-013)

---

## Structure du package

```
com.stockmaster.auth/
├── config/
│   ├── JwtAuthenticationFilter.java    ← Filtre : extrait token → UsernamePasswordAuthenticationToken
│   ├── JwtTokenProvider.java           ← Génération/validation JWT (HS256, jjwt 0.12.6)
│   ├── RateLimitFilter.java            ← 5 tentatives / 15 min par IP (Redis)
│   ├── SecurityConfig.java             ← SecurityFilterChain, BCrypt, endpoints publics
│   └── StockMasterPrincipal.java       ← UserPrincipal (userId, entrepriseId, groupId, role, scope)
├── controller/
│   └── AuthController.java             ← POST /inscription/entreprise-unique, POST /login
├── domain/
│   ├── entity/
│   │   ├── Entreprise.java             ← nom, type (MERE/FILIALE), adresse, NIF
│   │   ├── TenantGroup.java            ← nom, plan, limite filiales
│   │   └── Utilisateur.java            ← email, motDePasse (BCrypt), rôle, scope
│   └── enums/
│       ├── PlanAbonnement.java         ← GRATUIT, STARTER, PRO, ENTERPRISE
│       ├── RoleUtilisateur.java        ← 7 rôles (SUPER_ADMIN à CAISSIER)
│       ├── ScopeUtilisateur.java       ← GROUPE, FILIALE
│       └── TypeEntreprise.java         ← MERE, FILIALE
├── dto/
│   ├── request/
│   │   ├── InscriptionEntrepriseUniqueRequest.java ← nomBoutique, ville, prenom, nom, email, motDePasse
│   │   └── LoginRequest.java           ← email, motDePasse
│   └── response/
│       ├── InscriptionResponse.java    ← email, groupId, message
│       └── LoginResponse.java          ← accessToken, refreshToken, expiresIn, role, scope
├── event/
│   └── InscriptionSuccessEvent.java    ← Événement async pour email de bienvenue
├── mapper/
│   └── AuthMapper.java                 ← MapStruct : Request → Entreprise
├── repository/
│   ├── EntrepriseRepository.java       ← JPA Repository
│   ├── TenantGroupRepository.java      ← JPA Repository
│   └── UtilisateurRepository.java      ← existsByEmail(), findByEmail()
└── service/
    ├── AuthService.java                ← Interface
    └── impl/
        └── AuthServiceImpl.java        ← Inscription atomique + Login (161 lignes)
```

---

## API exposées

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `/api/v1/auth/inscription/entreprise-unique` | POST | ❌ Non | Crée TenantGroup + Entreprise + Utilisateur |
| `/api/v1/auth/login` | POST | ❌ Non | Connecte et retourne JWT |

### Inscription — Requête
```json
{
  "nomBoutique": "Épicerie Centrale",
  "ville": "Douala",
  "prenom": "Jean",
  "nom": "Kamga",
  "email": "jean.kamga@epicerie.cm",
  "motDePasse": "MotDePasse@2026"
}
```
→ `201 CREATED`

### Connexion — Requête
```json
{ "email": "jean.kamga@epicerie.cm", "motDePasse": "MotDePasse@2026" }
```
→ `200 OK` avec `accessToken`, `refreshToken`, `expiresIn`, `role`, `scope`

---

## US associées

| US | Description | Statut |
|---|---|---|
| **US-006** | Inscription entreprise unique | ✅ Terminé |
| **US-007** | Inscription groupe multi-sites | 🔜 Non commencé |
| **US-008** | Connexion JWT | ✅ Terminé |
| **US-009** | Refresh token | 🔜 Non commencé |
| **US-010** | Déconnexion | 🔜 Non commencé |
| **US-011** | Mot de passe oublié | 🔜 Non commencé |
| **US-012** | Réinitialisation mot de passe | 🔜 Non commencé |
| **US-013** | Changement mot de passe | 🔜 Non commencé |

---

## Sécurité

- **Rate limiting :** 5 tentatives max / 15 min par IP (Redis)
- **JWT :** HS256 (jjwt 0.12.6), access token 15 min, refresh token 7 jours
- **Mots de passe :** BCrypt, jamais en clair
- **RBAC :** 7 rôles métier avec `@PreAuthorize`
- **Compte inactif :** `403 ACCOUNT_DISABLED`
- **Groupe suspendu :** `403 TENANT_SUSPENDED`

---

## Tests

Aucun test unitaire pour l'instant (à implémenter avec US-009 à US-012).
