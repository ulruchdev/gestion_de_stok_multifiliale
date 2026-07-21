# StockMaster CM — Frontend

Interface utilisateur React de la solution SaaS de gestion de stock multi-filiales pour PME camerounaises.

## Stack technique

| Couche | Technologie |
|---|---|
| Framework | React 18 + TypeScript |
| Build | Vite 5 |
| Routing | React Router v6 |
| État serveur | TanStack Query |
| État client | Zustand |
| UI Kit | Tailwind CSS + shadcn/ui |
| Formulaires | React Hook Form + Zod |
| Requêtes HTTP | Axios (+ intercepteur JWT) |
| Graphiques | Recharts |
| Mock API | MSW (Mock Service Worker) |

## Prérequis

- Node.js 20+
- npm 10+

## Installation

```bash
cd frontend
npm install
```

## Développement

```bash
# Démarrer le serveur de développement (port 5173)
npm run dev

# Activer le mode mock API (sans backend)
VITE_ENABLE_MOCKS=true npm run dev
```

Le proxy Vite redirige `/api/*` vers `http://localhost:8080` (backend Spring Boot).

## Scripts disponibles

| Commande | Action |
|---|---|
| `npm run dev` | Serveur de développement |
| `npm run build` | Build de production |
| `npm run preview` | Prévisualisation du build |
| `npm run lint` | Vérification ESLint |
| `npm run format` | Formatage Prettier |
| `npm run typecheck` | Vérification TypeScript |

## Structure du projet

```
frontend/
├── src/
│   ├── app/
│   │   ├── routes.tsx              # Routage applicatif + guards RBAC
│   │   ├── providers.tsx           # Providers (QueryClient, Auth)
│   │   └── layouts/                # Layouts par contexte (auth, dashboard)
│   ├── features/                   # Modules fonctionnels
│   │   ├── auth/                   # Authentification (store, composants)
│   │   ├── catalogue/              # Articles & Catégories
│   │   ├── achats/                 # Commandes fournisseur
│   │   ├── ventes/                 # Commandes client & Vente directe
│   │   ├── stock/                  # Mouvements, corrections, transferts
│   │   ├── tiers/                  # Clients & Fournisseurs
│   │   └── notifications/          # Alertes & notifications
│   ├── shared/
│   │   ├── ui/                     # Composants partagés (Button, Input, Table...)
│   │   ├── lib/                    # Utilitaires (API client, formatters)
│   │   └── types/                  # Types TypeScript (miroir du backend)
│   └── mocks/                      # MSW handlers pour le développement sans backend
├── .env.example
├── index.html
├── package.json
├── tailwind.config.ts
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## Règles importantes

1. **RBAC frontend = reflet, jamais source de vérité** — chaque garde de route a son pendant `@PreAuthorize` côté backend.
2. **Le TTC est calculé côté serveur** — l'aperçu client est indicatif, la valeur persistée vient du backend.
3. **Vérification de stock** — le frontend ne décide jamais localement qu'un stock est suffisant ; il affiche le résultat du backend.
4. **Mock API** — activer `VITE_ENABLE_MOCKS=true` pour développer sans backend. Basculer sur l'API réelle une fois les endpoints livrés.

## En savoir plus

Voir les documents dans `document/02-backlogs/` et `document/01-architecture/` pour les spécifications complètes.
