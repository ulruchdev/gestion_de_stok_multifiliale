# 02 — Authentification & Acces (EPIC 2)

## Flux d'Inscription

```mermaid
graph TD
    Start[Visiteur arrive] --> Choice{Type d'inscription ?}

    Choice -->|Boutique unique| Unique[Formulaire Entreprise Unique]
    Choice -->|Multi-sites| Groupe[Formulaire Groupe Multi-sites]

    Unique --> Validate[Validation Jakarta<br/>Email / Mot de passe / Nom]
    Groupe --> Validate

    Validate --> CheckEmail{Email deja utilise ?}

    CheckEmail -->|Oui| Error409[409 CONFLICT<br/>EMAIL_ALREADY_EXISTS]
    CheckEmail -->|Non| Transaction[DEBUT TRANSACTION]

    Transaction --> CreateGroup[Creer TenantGroup]
    CreateGroup --> CreateEnt[Creer Entreprise type MERE]
    CreateEnt --> CreateUser[Creer Utilisateur<br/>role ADMIN_GROUPE<br/>MDP hache BCrypt]
    CreateUser --> Commit[FIN TRANSACTION]

    Commit --> Event[Publier InscriptionSuccessEvent<br/>async non bloquant]
    Event --> Email[Envoyer email de bienvenue]
    Event --> Response[201 CREATED<br/>groupId + email]

    Error409 --> End1[Fin]
    Response --> End2[Fin]

    style Start fill:#4CAF50,color:white
    style Error409 fill:#f44336,color:white
    style Response fill:#2196F3,color:white
    style Transaction fill:#FF9800,color:white
    style Commit fill:#4CAF50,color:white
```

## Flux de Connexion

```mermaid
graph TD
    Login[Utilisateur soumet email + mot de passe] --> CheckEmail{Email existe ?}

    CheckEmail -->|Non| Retour401_1[401 UNAUTHORIZED<br/>Message generique]
    CheckEmail -->|Oui| CheckActive{Compte actif ?}

    CheckActive -->|Non| Retour403_1[403 FORBIDDEN<br/>ACCOUNT_DISABLED]
    CheckActive -->|Oui| CheckGroup{TenantGroup actif ?}

    CheckGroup -->|Non| Retour403_2[403 FORBIDDEN<br/>TENANT_SUSPENDED]
    CheckGroup -->|Oui| CheckRate{Rate limit OK ?<br/>Moins de 5 tentatives par 15 min}

    CheckRate -->|Non| Retour429[429 TOO MANY REQUESTS]
    CheckRate -->|Oui| Verify[Verifier mot de passe BCrypt]

    Verify -->|Invalide| Retour401_2[401 UNAUTHORIZED<br/>Credentials invalides]
    Verify -->|Valide| Generate[Generer les tokens JWT]

    Retour401_1 --> Log1[Log: auth.login_failed<br/>email inexistant]
    Retour401_2 --> Log2[Log: auth.login_failed<br/>MDP invalide + increment rate]

    Generate --> Access[Access Token JWT<br/>15 min<br/>claims: userId, role,<br/>entrepriseId, scope, jti]
    Generate --> Refresh[Refresh Token<br/>7 jours stocke Redis<br/>refresh + userId]

    Access --> Reponse200[200 OK<br/>accessToken + refreshToken<br/>+ expiresIn + role + scope]
    Refresh --> Reponse200

    Log1 --> End1[Fin]
    Log2 --> End2[Fin]
    Retour403_1 --> End3[Fin]
    Retour403_2 --> End4[Fin]
    Retour429 --> End5[Fin]
    Reponse200 --> End6[Succes]

    style Login fill:#2196F3,color:white
    style Retour401_1 fill:#f44336,color:white
    style Retour401_2 fill:#f44336,color:white
    style Retour403_1 fill:#f44336,color:white
    style Retour403_2 fill:#f44336,color:white
    style Retour429 fill:#f44336,color:white
    style Reponse200 fill:#4CAF50,color:white
    style Verify fill:#FF9800,color:white
```

## Flux Refresh Token avec Rotation (US-014)

```mermaid
graph TD
    Start[Client envoie refreshToken] --> CheckRedis{Verifier en Redis<br/>refresh + userId existe ?}

    CheckRedis -->|Token invalide ou expire| Rejet[401 UNAUTHORIZED<br/>Client doit se reconnecter]
    CheckRedis -->|Token valide| Rotation[Rotation du token]

    Rotation --> InvaliderAncien[Invalider l'ancien token<br/>le supprimer de Redis]

    InvaliderAncien --> DetectionRejeu{Token soumis a nouveau ?}

    DetectionRejeu -->|Non: 1ere utilisation| GenererNouveau[Generer NOUVEAU refresh token<br/>+ NOUVEAU access token]
    DetectionRejeu -->|Oui: token reutilise| Revocation[REVOCATION TOTALE<br/>Toute la famille revoquee<br/>Deconnexion forcee]

    Revocation --> Alerte[Email + Notification securite<br/>Activite suspecte detectee]
    Revocation --> WarnLog[Log WARN<br/>event: auth.refresh_reuse_detected]

    GenererNouveau --> Reponse[200 OK<br/>Nouveaux access + refresh tokens]

    Rejet --> FinRejet[Fin - Rejet]
    Reponse --> FinSuccess[Succes - Rotation]
    Alerte --> FinAlerte[Fin - Revocation]
    WarnLog --> FinAlerte

    style Start fill:#2196F3,color:white
    style Rejet fill:#f44336,color:white
    style Revocation fill:#f44336,color:white
    style Reponse fill:#4CAF50,color:white
    style Rotation fill:#FF9800,color:white
```

## Flux Mot de Passe Oublie

```mermaid
graph TD
    Forgot[Utilisateur saisit email] --> CheckEmail{Email existe ?}

    CheckEmail -->|Oui| GenToken[Generer token UUID<br/>Expiration: 1 heure<br/>Stocker en Redis: reset + token]
    CheckEmail -->|Non| ReponseGenerique[200 OK<br/>Message generique<br/>independant de l existence]

    GenToken --> SendEmail[Envoyer email<br/>lien: app.stockmaster.cm<br/>avec token]
    SendEmail --> ReponseGenerique

    ReponseGenerique --> Fin1[Fin envoi]

    style Forgot fill:#9C27B0,color:white
    style ReponseGenerique fill:#4CAF50,color:white
```

## Flux Reinitialisation du Mot de Passe

```mermaid
graph TD
    Reset[Utilisateur clique lien] --> ValidateToken{Token valide ?<br/>Present en Redis ?<br/>Non expire ?}

    ValidateToken -->|Invalide ou expire| Error[400 BAD REQUEST<br/>RESET_TOKEN_INVALID]
    ValidateToken -->|Valide| CheckPassword{Mot de passe respecte<br/>les criteres de robustesse ?<br/>8+ car, 1 maj, 1 chiffre,<br/>1 car special}

    CheckPassword -->|Non| ErrorValid[400 BAD REQUEST<br/>Erreur de validation]
    CheckPassword -->|Oui| HashNew[Hacher nouveau MDP avec BCrypt<br/>jamais stocke en clair]

    HashNew --> Save[Sauvegarder nouveau hash<br/>Supprimer token reset de Redis]
    Save --> RevokeRefresh[Revoguer TOUS les refresh tokens<br/>de l utilisateur dans Redis]
    RevokeRefresh --> Success[200 OK<br/>Mot de passe reinitialise]

    Error --> FinError[Fin - Erreur]
    ErrorValid --> FinError
    Success --> FinSuccess[Succes]

    style Reset fill:#9C27B0,color:white
    style Error fill:#f44336,color:white
    style ErrorValid fill:#f44336,color:white
    style Success fill:#4CAF50,color:white
```
