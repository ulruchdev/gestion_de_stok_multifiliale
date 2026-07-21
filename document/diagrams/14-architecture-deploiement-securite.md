# 14 — Architecture Deploiement & Securite

## Architecture de Deploiement

```mermaid
graph TB
    subgraph Dev["Developpement Local"]
        FE[Frontend Vite<br/>Port: 5173]
        API[API Spring Boot<br/>Port: 8080]
        PG[(PostgreSQL 16<br/>Port: 5432)]
        RD[(Redis 7<br/>Port: 6379)]
        MO[(MinIO<br/>Port: 9000)]
        MH[MailHog<br/>SMTP: 1025 / UI: 8025]
    end

    subgraph CI["CI - GitHub Actions"]
        Compile[1. Maven Compile]
        Test[2. Tests unitaires + integration<br/>PostgreSQL + Redis via services]
        Coverage[3. JaCoCo Coverage 80%+]
        Sonar[4. SonarCloud Quality Gate]
        Build[5. Build JAR + Image Docker]
    end

    subgraph Prod["Production"]
        LB[Load Balancer]
        API_Prod[API Spring Boot<br/>Multi-instances scalables]
        PG_Prod[(PostgreSQL 16<br/>Haute disponibilite)]
        RD_Prod[(Redis 7<br/>Cluster)]
        MO_Prod[(MinIO<br/>Stockage objets)]
    end

    Dev -->|git push| CI
    CI -->|docker push and deploy| Prod
    LB --> API_Prod
    API_Prod --> PG_Prod
    API_Prod --> RD_Prod
    API_Prod --> MO_Prod

    style Dev fill:#2196F3,color:white
    style CI fill:#FF9800,color:white
    style Prod fill:#4CAF50,color:white
```

## Architecture de Securite

```mermaid
graph TD
    subgraph AuthFlow["1. Flux d authentification"]
        User[Utilisateur] -->|Credentials| Login[POST /auth/login]
        Login --> RateLimit{Rate Limiting Redis}
        RateLimit -->|<= 5 tentatives| VerifyAuth[Verifier credentials]
        VerifyAuth -->|Succes| JWT[Generer JWT - jjwt 0.12.6 HS256]
        JWT --> AccessToken[Access Token<br/>15 min<br/>claims: userId, role,<br/>entrepriseId, scope, jti]
        JWT --> RefreshToken[Refresh Token<br/>7 jours<br/>stocke Redis<br/>refresh + userId]
    end

    subgraph Protection["2. Protection des requetes"]
        Req[Requete entrante] --> JwtFilter[JwtAuthenticationFilter]
        JwtFilter --> Extract[Extraire Bearer token]
        Extract --> ValidateJWT{Verifier signature<br/>+ expiration JWT}
        ValidateJWT -->|Invalide| Reject[401 Unauthorized]
        ValidateJWT -->|Valide| CheckBlacklist{Token blackliste ?<br/>blacklist + jti + jti}
        CheckBlacklist -->|Oui| Reject
        CheckBlacklist -->|Non| SetContext[Set SecurityContext<br/>StockMasterPrincipal]
        SetContext --> Controller[Endpoint protege]
        Controller --> CheckAuth{@PreAuthorize OK ?}
        CheckAuth -->|Non| AccessDenied[403 Forbidden]
        CheckAuth -->|Oui| MultiTenant[Verifier isolation tenant<br/>entreprise_id du JWT]
    end

    subgraph Isolation["3. Isolation multi-tenant"]
        Rule[Regle ABSOLUE:<br/>entreprise_id vient TOUJOURS du JWT<br/>JAMAIS du body de la requete]
        Violation[Violation = 404 Not Found<br/>ne jamais reveler l existence]
    end

    subgraph FailClosed["4. Comportement fail-closed"]
        FC[Si Redis est DOWN:]
        FC1[Login -> 503 SERVICE_UNAVAILABLE]
        FC2[Refresh -> 503 SERVICE_UNAVAILABLE]
        FC3[Logout -> 503 SERVICE_UNAVAILABLE]
        Never[JAMAIS de fail-open<br/>sur les operations de securite]
    end

    style AuthFlow fill:#2196F3,color:white
    style Protection fill:#FF9800,color:white
    style Isolation fill:#f44336,color:white
    style FailClosed fill:#9C27B0,color:white
```

## Pipeline CI/CD

```mermaid
graph LR
    Push[Git Push sur main] --> Checkout[Checkout code]

    Checkout --> CompileStep[1. Maven Compile]
    CompileStep --> TestStep[2. Maven Test<br/>Avec PostgreSQL + Redis]

    TestStep --> CoverageStep[3. JaCoCo 80%+]
    TestStep -->|FAIL| StopBuild[BUILD STOP - Corriger les tests]

    CoverageStep -->|OK| SonarStep[4. SonarCloud Analyze]
    CoverageStep -->|Coverage moins de 80%| StopBuild

    SonarStep -->|Quality Gate PASS| PackageStep[5. Maven Package]
    SonarStep -->|Quality Gate FAIL| StopBuild

    PackageStep --> DockerBuild[6. Docker Build Multi-stage]
    DockerBuild --> PushRegistry[7. Push vers Registry<br/>Docker Hub / GHCR]

    style Push fill:#4CAF50,color:white
    style StopBuild fill:#f44336,color:white
    style PushRegistry fill:#FF9800,color:white
```
