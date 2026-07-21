# 01 — Architecture Globale du Systeme

```mermaid
graph TB
    subgraph Frontend["Frontend React (Vite)"]
        FE_Auth[Auth Pages<br/>Login / Register]
        FE_Dash[Dashboard<br/>Admin Groupe]
        FE_Filiale[Dashboard<br/>Admin Filiale]
        FE_Employe[Dashboard<br/>Employe]
    end

    subgraph API["API Gateway (Spring Boot)"]
        GW[API REST<br/>Port 8080]
    end

    subgraph Modules["Modules Spring Boot"]
        Auth[Auth Module<br/>Inscription / Login / JWT]
        Groupe[Groupe Module<br/>Filiales / Dashboard]
        Users[Utilisateurs Module<br/>RBAC / Profils]
        Catalog[Catalogue Module<br/>Categories / Articles]
        Tiers[Tiers Module<br/>Clients / Fournisseurs]
        Achat[Achats Module<br/>Commandes Fournisseur]
        Stock[Stock Module<br/>Mouvements / Corrections]
        Vente[Ventes Module<br/>Commandes Client / Caisse]
        Transfer[Transferts Module<br/>Inter-Filiales]
        Notif[Notifications Module<br/>Alertes / Emails]
        Report[Reporting Module<br/>Statistiques]
    end

    subgraph Shared["Shared Library"]
        Entity[AbstractEntity<br/>Soft Delete]
        Exception[GlobalExceptionHandler<br/>RFC 7807]
        DTO[ApiResponse / ProblemResponse]
        Config[JWT / CORS / Pagination]
    end

    subgraph Infra["Infrastructure"]
        PG[(PostgreSQL 16)]
        Redis[(Redis 7<br/>Refresh Tokens / Cache)]
        MinIO[(MinIO (S3)<br/>Photos / Logos)]
        Mail[(MailHog<br/>Emails dev)]
    end

    subgraph CI_CD["CI/CD Pipeline"]
        GHA[GitHub Actions]
        Sonar[SonarCloud<br/>Quality Gate]
        Docker[Docker Hub / GHCR]
    end

    Frontend -->|HTTP/HTTPS| API
    API --> Auth
    API --> Groupe
    API --> Users
    API --> Catalog
    API --> Tiers
    API --> Achat
    API --> Stock
    API --> Vente
    API --> Transfer
    API --> Notif
    API --> Report

    Auth --> Shared
    Groupe --> Shared
    Users --> Shared
    Catalog --> Shared
    Tiers --> Shared
    Achat --> Shared
    Stock --> Shared
    Vente --> Shared
    Transfer --> Shared
    Notif --> Shared
    Report --> Shared

    Auth --> PG
    Auth --> Redis
    Groupe --> PG
    Users --> PG
    Catalog --> PG
    Tiers --> PG
    Achat --> PG
    Stock --> PG
    Vente --> PG
    Transfer --> PG
    Notif --> PG
    Report --> PG

    Catalog --> Redis
    Groupe --> Redis

    Auth --> Mail
    Notif --> Mail
    Users --> MinIO
    Catalog --> MinIO

    GHA -->|Build + Test| CI_CD
    GHA -->|Analyze| Sonar
    GHA -->|Package + Push| Docker
```
