# Formation Frontend Complete — StockMaster CM
> Couvre TOUTES les User Stories : US-F001 a F103, US-D001 a D102, US-001 a 080
> Supprime ce dossier quand tu maitrises — formation/ a ete concu pour ca

## Architecture de la formation
Chaque section = 1 concept cle. Chaque concept est explique avec :
- **Definition** : c'est quoi ?
- **Syntaxe** : comment ca s'ecrit ?
- **Cas d'usage StockMaster** : ou et pourquoi on l'utilise dans le projet ?
- **Exemple concret** : extrait du code du projet
- **Lecon** : l'idee a retenir

---

## INDEX DES CONCEPTS (80 concepts couverts)

### EPIC-F00 : Fondations (41 SP)
1. Vite — Build tool et HMR
2. TypeScript strict — Les types
3. Path aliases — Les raccourcis d'import
4. ESLint + Prettier — Qualite du code
5. Husky — Pre-commit hooks
6. Package.json — Dependances
7. Proxy API — CORS
8. TanStack Query — Cache serveur
9. Zustand — Store
10. Tailwind CSS — Design tokens
11. shadcn/ui — Composants
12. Feature-based architecture
13. Barrel exports
14. Axios interceptor — JWT
15. Refresh token automatique
16. React Router — Routing
17. Route guards — RBAC
18. Layouts — Composition
19. Composants UI — Atomic design
20. CVA — Variants de composants
21. MSW — Mock Service Worker
22. Lazy loading — Code splitting
23. Skeleton loaders
24. Error boundaries
25. State management patterns

### EPIC-F01 : Onboarding (24 SP)
26. React Hook Form
27. Zod validation
28. Validation miroir
29. Formulaire multi-etapes
30. Gestion erreur 409
31. LCP performance
32. Expiration lien (1h)
33. Activation compte

### EPIC-F02 : Dashboards (19 SP)
34. Recharts — Graphiques
35. Skeleton loaders
36. Widget architecture
37. Polling vs WebSocket
38. Cache invalidation

### EPIC-F03 : Catalogue (24 SP)
39. Pagination serveur
40. Recherche full-text
41. Calcul TTC apercu vs serveur
42. Badge alerte stock
43. Upload photo
44. Cache categories
45. CRUD completion

### EPIC-F04 : Tiers (15 SP)
46. Adresse structuree (Quartier/Ville/Region)
47. Pattern liste reutilisable
48. Fiche detail avec historique

### EPIC-F05 : Achats (17 SP)
49. Formulaire multi-lignes dynamique
50. Recapitulatif temps reel
51. Modal confirmation irreversible
52. Etats commande conditionnes

### EPIC-F06 : Ventes (30 SP)
53. Ecran caisse tactile
54. Recherche article rapide
55. Erreur stock insuffisant
56. Annulation jour meme
57. Impression ticket
58. Partage WhatsApp
59. Fonctionnement offline degrade

### EPIC-F07 : Stock (23 SP)
60. Table filtrable
61. Champ motif obligatoire
62. Stock temps reel
63. Export

### EPIC-F08 : Groupe (16 SP)
64. Selecteur role
65. Garde route ADMIN_GROUPE
66. Creation filiale

### EPIC-F09 : Reporting (13 SP)
67. Recharts line/bar/pie
68. Filtres periodes
69. Top classement

### EPIC-F10 : Back-office (15 SP)
70. Theme distinct
71. Tenant management
72. Isolation donnees

### Concepts transverses backend lies
73. JWT — Structure, signature, claims
74. RBAC — Roles et permissions
75. Soft delete — Suppression logique
76. Mutation de stock — Entree/Sortie/Correction
77. Machine a etats — Cycle de vie des commandes
78. Immuabilite du journal — ANNULATION_VENTE
79. Isolation multi-tenant — entreprise_id
80. Snapshot prix et TVA

---

## CHAPITRE 1 : FONDATIONS (EPIC-F00 - 41 SP)

### 1.1 Architecture du projet frontend (US-F001)

**Concept : Feature-based architecture**

La structure du dossier `frontend/src/` suit un principe simple : **ce qui change ensemble vit ensemble**.

```
src/
├── app/           # Configuration de l'app (routing, layouts, providers)
├── features/      # 1 dossier = 1 module metier (auth, catalogue, achats...)
├── shared/        # Code partage ENTRE les features
└── mocks/         # Simulations API (MSW)
```

**Pourquoi pas une architecture par type technique ?**
- `components/Button.tsx`, `pages/Login.tsx`, `hooks/useAuth.ts` → 3 dossiers pour 1 fonctionnalite
- Avec feature-based : `features/auth/` contient TOUT ce qui concerne l'auth
- Avantage : facile a trouver, modifier, et supprimer

**Regle de dependance :**
```
app/  →  features/  →  shared/
(ne depend de rien)  (depend de shared)  (ne depend de personne)
```

---

### 1.2 Vite — Le build tool (US-F001)

**Definition :** Vite est un bundler qui compile votre code JS/TS en fichiers optimises pour le navigateur. Il remplace Webpack.

**Syntaxe :** `vite.config.ts` a la racine du projet frontend.

**Cas d'usage StockMaster :**
```typescript
// frontend/vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), // ← Raccourci d'import
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // ← Backend Spring Boot
        changeOrigin: true,
      },
    },
  },
});
```

**Explication ligne par ligne :**
- `plugins: [react()]` : Active le hot reload React (quand tu modifies un composant, le navigateur se met a jour sans perdre l'etat)
- `alias: { '@': './src' }` : Au lieu d'ecrire `import { Button } from '../../shared/ui/Button'`, tu ecris `import { Button } from '@/shared/ui/Button'`
- `proxy: { '/api': ... }` : Le frontend tourne sur le port 5173, le backend sur le port 8080. Le proxy redirige les appels `/api/*` vers le backend. **Pourquoi ?** Pour eviter les erreurs CORS (le navigateur bloque les requetes d'un port a un autre)

**Commandes :**
- `npm run dev` : Lance le serveur de dev avec hot reload
- `npm run build` : Compile pour la production
- `npm run preview` : Apercu du build de production

---

### 1.3 TypeScript strict (US-F001)

**Definition :** TypeScript ajoute des types a JavaScript. Le mode `strict` active toutes les verifications.

**Syntaxe :** Configure dans `tsconfig.json`

**Cas d'usage StockMaster :**
```json
{
  "compilerOptions": {
    "strict": true,              // ← Active toutes les verifs
    "noUnusedLocals": true,      // ← Bloque si variable inutilisee
    "noUnusedParameters": true,  // ← Bloque si parametre inutilise
    "noFallthroughCasesInSwitch": true, // ← Bloque si switch sans break
    "noEmit": true,              // ← Ne genere pas de JS (c'est Vite qui le fait)
    "jsx": "react-jsx"          // ← Active le JSX automatique
  }
}
```

**Exemple : Pourquoi `strict` est critique dans StockMaster ?**

```typescript
// SANS strict : ce code compile sans erreur
function getStock(articleId: number) {
  // ERREUR : la fonction ne retourne rien !
}
const stock = getStock(10);
console.log(stock.quantite); // → PLANTAGE : cannot read property 'quantite' of undefined

// AVEC strict : TypeScript nous force a gerer le cas
function getStock(articleId: number): StockResponse | null {
  const data = api.get(`/articles/${articleId}`);
  return data ?? null;
}
const stock = getStock(10);
if (stock === null) {
  return <Badge variant="destructive">Stock indisponible</Badge>;
}
console.log(stock.quantite); // → TypeScript sait que stock n'est pas null
```

**Lecon :** `strict: true` est le premier filet de securite de l'app. Il transforme des bugs silencieux en erreurs de compilation. Obligatoire pour un logiciel de caisse.

---

### 1.4 Path Aliases (US-F001)

**Definition :** Raccourcis pour les imports, evite les chemins relatifs `../../../`.

**Syntaxe :**
```typescript
// tsconfig.json
"paths": {
  "@/*": ["./src/*"]
}
// + vite.config.ts (alias correspondant)
alias: { '@': '/src' }
```

**Avant (sans alias) :**
```typescript
import { Button } from '../../../../shared/ui/Button';
```

**Apres (avec alias) :**
```typescript
import { Button } from '@/shared/ui/Button';
```

**Lecon :** Les alias rendent le code plus lisible et les refactorings plus faciles (deplacer un fichier ne casse pas les imports).

---

### 1.5 ESLint + Prettier (US-F001)

**Definition :** ESLint verifie la qualite du code (regles, bonnes pratiques). Prettier formate automatiquement (espaces, guillemets, virgules).

**Pourquoi les deux ?**
- ESLint : detecte les erreurs (variable inutilisee, `==` au lieu de `===`)
- Prettier : formate (guillemets simples, point-virgules, indentation)

**Exemple StockMaster :**
```javascript
// .eslintrc.cjs
module.exports = {
  rules: {
    'react-refresh/only-export-components': 'warn',
    '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
  },
};
```

**Lecon :** Un commit qui passe ESLint + Prettier = code propre et consistent. Le CI les verifie automatiquement (`npm run lint`).

---

### 1.6 Package.json — Les dependances analysees (US-F001)

**Definition :** Le `package.json` declare toutes les bibliotheques utilisees.

**Analyse detaillee de CHAQUE dependance StockMaster :**

```json
"dependencies": {
  "react": "^18.3.1",
```
**React 18** : Framework UI. Choix standard.
- **Pourquoi pas Vue/Angular ?** Voir ADR a ecrire (GS-FRONTEND-BACKLOG §2). React a ete choisi pour l'ecosysteme (TanStack Query, Zustand, shadcn/ui) et la facilite de recrutement.
- **Concept : Virtual DOM** — React maintient un arbre virtuel des composants et ne met a jour que ce qui a change dans le vrai DOM. C'est ce qui rend les mises a jour rapides.

```json
  "@tanstack/react-query": "^5.51.0",
```
**TanStack Query** : Gestion du cache serveur.
- **Concept :** Quand tu fais un appel API, TanStack Query stocke le resultat dans un cache. Si tu reviens sur la page, il utilise le cache au lieu de re-appeler l'API.
- **Pourquoi pas `useEffect` + `fetch` ?** TanStack Query gere automatiquement : loading, error, retry, cache invalidation, pagination, refetch au focus.
- **US liees :** US-F030 (liste articles), US-F032 (detail article), US-F040 (liste clients), etc.
- **StaleTime utilise :** 5 minutes par defaut pour les listes, 30 secondes pour le stock.

```json
  "zustand": "^4.5.4",
```
**Zustand** : Gestion d'etat client.
- **Concept :** Store = objet central qui contient l'etat ET les fonctions pour le modifier.
- **Pourquoi pas Redux ?** Redux necessite : actions, reducers, dispatch, connect. Zustand : un `create()` et c'est tout.
- **US liees :** US-F006 (auth state), US-F007 (layout state)

```json
  "axios": "^1.7.3",
  "zod": "^3.23.8",
  "react-hook-form": "^7.52.0",
```
**Axios** : Client HTTP avec intercepteurs.
**Zod** : Validation de formulaires cote client.
**React Hook Form** : Gestion des formulaires performante.

```json
  "recharts": "^2.12.7",
```
**Recharts** : Graphiques React.
- **US liees :** US-F023 (courbe CA), US-F090 (top articles)

```json
  "clsx": "^2.1.1",
  "tailwind-merge": "^2.4.0",
  "class-variance-authority": "^0.7.0"
```
**clsx + tailwind-merge** : Fusion de classes CSS.
**CVA** : Creation de variantes de composants (Button primary/secondary/danger).

---

### 1.7 Le Proxy API et le CORS (US-F001)

**Concept :** Le navigateur bloque les requetes d'un port a un autre (CORS).

**Sans proxy :**
```
Frontend http://localhost:5173  →  Backend http://localhost:8080
                ↓
        ERREUR CORS : bloquee par le navigateur
```

**Avec proxy :**
```
Frontend http://localhost:5173/api/articles
                ↓
Vite proxy intercepte → http://localhost:8080/api/articles
                ↓
        Pas d'erreur CORS (meme origine)
```

**Pourquoi pas un `@CrossOrigin` dans le backend ?** On l'a configure aussi (CorsProperties), mais le proxy est plus propre en dev :
- Il evite d'exposer l'API a toutes les origines
- Il permet aux cookies httpOnly de fonctionner
- Il simule le reverse proxy de production (Nginx)

---

### 1.8 TanStack Query — Le cache serveur (US-F004, F030, F032, F060)

**Definition :** TanStack Query est une librairie qui gere le **cycle de vie des donnees API** : chargement, succes, erreur, rechargement, cache.

**Sans TanStack Query (l'ancienne methode) :**
```tsx
function ArticleList() {
  const [articles, setArticles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch('/api/articles')
      .then(res => {
        if (!res.ok) throw new Error('Erreur reseau');
        return res.json();
      })
      .then(data => {
        setArticles(data.data.content);
        setLoading(false);
      })
      .catch(err => {
        setError(err);
        setLoading(false);
      });
  }, []);

  if (loading) return <Spinner />;
  if (error) return <Erreur message={error.message} />;
  return <Tableau data={articles} />;
}
```
**Problemes :** 30 lignes pour UN appel API. Pas de cache. Pas de retry. Pas de refetch.

**Avec TanStack Query :**
```tsx
function ArticleList() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['articles'],                  // ← Cle unique dans le cache
    queryFn: () => api.get('/articles'),     // ← Fonction de chargement
    staleTime: 5 * 60 * 1000,               // ← 5 min avant rechargement
  });

  if (isLoading) return <SkeletonTable rows={5} />;
  if (error) return <ErrorBanner />;
  return <Tableau data={data} />;
}
```
**Avantages :** 10 lignes, cache automatique, retry, refetch au focus, pagination integree.

**Mutation (pour les ecritures) :**
```tsx
function useCreerArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (article: ArticleFormData) => api.post('/articles', article),
    onSuccess: () => {
      // ← Invalide le cache pour forcer le rechargement de la liste
      queryClient.invalidateQueries({ queryKey: ['articles'] });
      toast.success('Article cree avec succes');
    },
    onError: (err) => {
      toast.error('Erreur', getErrorMessage(err));
    },
  });
}
```

**Concept cle : cache invalidation**
- Quand tu crees un article, la liste des articles n'est plus a jour
- `invalidateQueries(['articles'])` force TanStack Query a recharger la liste
- L'utilisateur voit automatiquement les nouvelles donnees, sans recharger la page

**US liees :**
| US | Query | Invalidation |
|---|---|---|
| US-F030 | `['articles', { search, page }]` | Apres US-F031/034 |
| US-F032 | `['articles', id]` | — |
| US-F040 | `['clients', { search, page }]` | Apres US-F042 |
| US-F050 | `['commandes-fournisseur', { etat, page }]` | Apres US-F048 validation |
| US-F060 | `['commandes-client', { etat, page }]` | Apres US-F060 validation |
| US-F070 | `['stock', articleId]` | Apres US-F071 correction |

---

### 1.9 Zustand — Le store d'etat (US-F006)

**Definition :** Zustand est une librairie de **gestion d'etat**. Un store = un objet qui contient des donnees ET des fonctions pour les modifier, accessible depuis TOUS les composants.

**Concept :** Sans store, si le composant A modifie une donnee que le composant B affiche, tu dois :
1. Remonter l'etat au parent commun
2. Le transmettre via les props
3. Creer des callbacks pour les modifications

→ A 3 niveaux de profondeur, c'est ingerable (prop drilling).

**Avec Zustand :**
```typescript
// features/auth/store.ts
import { create } from 'zustand';

interface AuthStore {
  user: User | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  isAuthenticated: false,

  login: async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    set({
      user: response.data.data.utilisateur,
      isAuthenticated: true,
    });
  },

  logout: async () => {
    await apiClient.post('/auth/logout');
    set({ user: null, isAuthenticated: false });
  },
}));
```

**Utilisation dans un composant :**
```tsx
function Header() {
  // ← On prend UNIQUEMENT ce dont on a besoin
  const { user, isAuthenticated, logout } = useAuthStore();

  return (
    <header>
      {isAuthenticated ? (
        <>
          <span>{user?.prenom} {user?.nom}</span>
          <button onClick={logout}>Deconnexion</button>
        </>
      ) : (
        <Link to="/login">Connexion</Link>
      )}
    </header>
  );
}
```

**Concept cle : re-rendu selectif**
- Si tu ne prends que `user` du store, ton composant ne se re-affiche que si `user` change
- Si `isAuthenticated` change mais pas `user`, le composant n'est pas re-rendu
- Optimisation automatique

**US liees :** US-F006 (auth state), US-F007 (layout/sidebar state)

---

### 1.10 Tailwind CSS — Les tokens du design system (US-F002)

**Definition :** Tailwind est un framework CSS "utility-first". Au lieu d'ecrire du CSS dans des fichiers `.css`, tu utilises des classes predefinies directement dans le JSX.

**Sans Tailwind :**
```css
.btn-primary {
  background-color: #2563eb;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
}
.btn-primary:hover { background-color: #1d4ed8; }
```

**Avec Tailwind :**
```tsx
<button className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
  Valider
</button>
```

**Les tokens du design system StockMaster (tailwind.config.ts) :**
```typescript
colors: {
  stock: {
    warning: '#f59e0b',   // Stock bas (US-D001)
    danger: '#ef4444',    // Rupture (US-D001)
    success: '#22c55e',   // Stock suffisant (US-D001)
  },
  // Couleurs semantiques (US-D001)
  primary: { DEFAULT: 'hsl(var(--primary))', ... },
  destructive: { DEFAULT: 'hsl(var(--destructive))', ... },
}
```

**Utilisation dans les composants :**
```tsx
<Badge className="bg-stock-warning text-white">Stock bas : 3 unites</Badge>
<Badge className="bg-stock-danger text-white">Rupture de stock</Badge>
<Badge className="bg-stock-success text-white">Stock suffisant</Badge>
```

**Le responsive (US-D005) :**
```tsx
<div className="
  w-full           /* Mobile 360px : 100% */
  md:w-1/2         /* Tablette 768px : 50% */
  lg:w-1/3         /* Desktop 1280px+ : 33% */
">
```

---

### 1.11 Les composants UI partages (US-F003)

**Definition :** Bibliotheque de composants atomiques reutilisables dans TOUS les ecrans. Mixture de shadcn/ui et de composants custom.

**Concept : Atomic Design**
- **Atomes** : Button, Input, Badge (composants les plus petits)
- **Molecules** : Card, TableRow, FormField (assemblage d'atomes)
- **Organismes** : DataTable, ArticleForm, Sidebar (assemblage de molecules)
- **Templates** : DashboardLayout, AuthLayout (mise en page)
- **Pages** : ListeArticles, Connexion (les ecrans reels)

**Pourquoi des composants partages ?**
- **Consistance visuelle** : tous les boutons se ressemblent
- **Maintenabilite** : modifier le Button = mis a jour partout
- **Hand-off design** : Siko cree les composants dans Figma, on les reproduit 1:1 en code
- **Reutilisabilite** : la Table est utilisee dans 6 EPICs differents

**Liste des composants de US-F003 :**

| Composant | Props | Variantes | Etats | Utilise dans |
|---|---|---|---|---|
| Button | label, variant, size, loading, disabled, onClick | primary, secondary, destructive, outline, ghost, link | normal, hover, focus, disabled, loading | TOUS les ecrans |
| Input | label, type, value, onChange, error, placeholder | text, email, password, number, tel | normal, focus, error, disabled | Tous les formulaires |
| Table | columns, data, loading, onSort, onPageChange | — | loading, empty, error | US-F030, F040, F050, F060, F070 |
| Badge | label, variant | en_preparation, validee, livree, annulee, default | — | US-F051, F053, F061 |
| Modal | open, onClose, title, children, footer | sm, md, lg | ouverte, fermee | US-F052, F061, F065 |
| Toast | type, title, message, duration | success, error, warning, info | apparition, disparition auto | US-F004 (erreurs API) |
| Card | title, children, className | — | — | Dashboards |
| Select | options, value, onChange, label | — | normal, error, disabled | US-F081 |
| Pagination | currentPage, totalPages, onPageChange | — | — | TOUTES les listes |
| Spinner | size | sm, md, lg | — | Chargement |
| Skeleton | width, height, rows | text, card, table | — | Chargement |
| EmptyState | icon, title, message, action | — | — | Liste vide |
| ErrorState | message, onRetry | — | — | Erreur API |

**Exemple : Composant Button avec CVA**
```tsx
// shared/ui/Button.tsx
import { cva, type VariantProps } from 'class-variance-authority';

const buttonVariants = cva(
  'inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground hover:bg-primary/90',
        destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
        outline: 'border border-input bg-background hover:bg-accent',
        secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
        ghost: 'hover:bg-accent hover:text-accent-foreground',
        link: 'text-primary underline-offset-4 hover:underline',
      },
      size: {
        default: 'h-10 px-4 py-2',
        sm: 'h-9 rounded-md px-3',
        lg: 'h-11 rounded-md px-8',
        icon: 'h-10 w-10',
      },
    },
    defaultVariants: { variant: 'default', size: 'default' },
  },
);

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, loading, children, ...props }, ref) => (
    <button
      className={cn(buttonVariants({ variant, size, className }))}
      ref={ref}
      disabled={props.disabled || loading}
      {...props}
    >
      {loading && <Spinner className="h-4 w-4 animate-spin" />}
      {children}
    </button>
  ),
);
```

---

### 1.12 Axios intercepteur JWT (US-F004)

**Definition :** Un intercepteur est une fonction qui s'execute automatiquement avant (request) ou apres (response) chaque appel Axios.

**Concept :** A chaque appel API, on veut :
1. AVANT : ajouter le token JWT dans le header `Authorization`
2. APRES : si la reponse est 401 (token expire), tenter un refresh automatique

```typescript
// shared/lib/api-client.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
});

// INTERCEPTEUR REQUEST : ajoute le token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// INTERCEPTEUR RESPONSE : refresh automatique
apiClient.interceptors.response.use(
  (response) => response, // Succes : on laisse passer
  async (error) => {
    if (error.response?.status !== 401) {
      return Promise.reject(error); // Pas une erreur 401 → on laisse tomber
    }

    // Tentative de refresh
    try {
      const refreshToken = localStorage.getItem('refresh_token');
      const response = await axios.post('/api/v1/auth/refresh', { refreshToken });
      const newToken = response.data.data.accessToken;
      
      localStorage.setItem('access_token', newToken);
      error.config.headers.Authorization = `Bearer ${newToken}`;
      
      return apiClient(error.config); // Rejoue la requete originale
    } catch (refreshError) {
      // Refresh echoue → deconnexion
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      window.location.href = '/login';
      return Promise.reject(refreshError);
    }
  },
);
```

**Que se passe-t-il pour l'utilisateur ?**
1. Il clique sur "Valider une commande"
2. Le token a expire (15 min)
3. Axios recoit 401 → intercepte → refresh automatique
4. La validation de commande est rejouee avec le nouveau token
5. L'utilisateur ne voit RIEN — c'est transparent

**Si le refresh echoue :**
1. L'utilisateur est redirige vers `/login`
2. Il doit se reconnecter
3. C'est normal — le refresh token a expire (7 jours)

---

### 1.13 React Router et les guards RBAC (US-F005)

**Definition :** React Router est la librairie qui permet de naviguer entre les pages sans recharger le navigateur.

**Les composants cles :**
- `<Routes>` : Conteneur de routes
- `<Route path="/" element={...} />` : Associe un chemin a un composant
- `<Link to="/articles">` : Lien de navigation
- `<Outlet />` : Emplacement du contenu enfant (dans un layout)
- `<Navigate to="/login" />` : Redirection

**Le guard `RequireAuth` (US-F005) :**
```tsx
function RequireAuth({ roles }: { roles?: Role[] }) {
  const { isAuthenticated, user, isLoading } = useAuthStore();

  if (isLoading) return <FullPageSpinner />;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user?.role)) {
    return <Navigate to="/dashboard" replace />;
  }
  return <Outlet />;
}
```

**Arborescence des routes (miroir de GS-IA-2026-01) :**
```tsx
<Routes>
  {/* PUBLIC : non authentifie */}
  <Route element={<RedirectIfAuth />}>
    <Route element={<AuthLayout />}>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/inscription" element={<ChoixInscription />} />
      <Route path="/inscription/entreprise-unique" element={<FormEntreprise />} />
      <Route path="/inscription/groupe" element={<FormGroupe />} />
      <Route path="/mot-de-passe-oublie" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
    </Route>
  </Route>

  {/* PROTEGE : authentifie */}
  <Route element={<RequireAuth />}>
    <Route element={<DashboardLayout />}>
      <Route path="/dashboard" element={<Dashboard />} />

      {/* ADMIN GROUPE uniquement */}
      <Route element={<RequireAuth roles={['ADMIN_GROUPE']} />}>
        <Route path="/groupe/filiales" element={<FilialeList />} />
        <Route path="/groupe/parametres" element={<GroupeSettings />} />
        <Route path="/transferts" element={<TransfertList />} />
        <Route path="/utilisateurs" element={<UserList />} />
      </Route>

      {/* CATALOGUE (acces large) */}
      <Route path="/catalogue/articles" element={<ArticleList />} />
      <Route path="/catalogue/articles/:id" element={<ArticleDetail />} />
      <Route path="/catalogue/categories" element={<CategorieList />} />

      {/* TIERS */}
      <Route path="/tiers/clients" element={<ClientList />} />
      <Route path="/tiers/fournisseurs" element={<FournisseurList />} />

      {/* ACHATS */}
      <Route path="/achats/commandes" element={<CommandeFournisseurList />} />
      <Route path="/achats/commandes/nouveau" element={<CommandeFournisseurForm />} />

      {/* VENTES */}
      <Route path="/ventes/commandes-client" element={<CommandeClientList />} />
      <Route path="/ventes/directes" element={<VenteDirecteList />} />
      <Route path="/ventes/caisse" element={<Caisse />} />

      {/* STOCK */}
      <Route path="/stock/mouvements" element={<MouvementStockList />} />
      <Route path="/stock/corrections" element={<CorrectionStockForm />} />

      {/* PROFIL */}
      <Route path="/profil" element={<Profil />} />
    </Route>
  </Route>
</Routes>
```

**Concept cle : Routing = reflet du RBAC**
- Si un role n'a pas acces a une route → il est redirige automatiquement
- Mais c'est le BACKEND qui est la source de verite
- Le frontend cache les routes non autorisees pour l'UX, mais le backend les bloque aussi

---

### 1.14 Le Layout Dashboard (US-F007)

**Concept :** Le layout est le cadre commun de toutes les pages apres connexion. Il contient :
- La sidebar (navigation)
- Le header (profil, deconnexion)
- Le contenu principal (Outlet)

**La sidebar dynamique selon le role :**
```tsx
// Pour chaque role, on definit les entrees de menu visible
const menuItems: MenuItem[] = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, roles: ['*'] },
  { path: '/groupe/filiales', label: 'Filiales', icon: Building2, roles: ['ADMIN_GROUPE'] },
  { path: '/catalogue/articles', label: 'Articles', icon: Package, roles: ['ADMIN_FILIALE', 'GESTIONNAIRE_STOCK'] },
  { path: '/ventes/caisse', label: 'Caisse', icon: CashRegister, roles: ['CAISSIER', 'COMMERCIAL', 'ADMIN_FILIALE'] },
  // ...
];

function DashboardLayout() {
  const { user, logout } = useAuthStore();

  // Filtrer les menus selon le role de l'utilisateur
  const allowedMenu = menuItems.filter(
    item => item.roles.includes('*') || item.roles.includes(user?.role ?? '')
  );

  return (
    <div className="flex h-screen">
      <Sidebar menuItems={allowedMenu} user={user} onLogout={logout} />
      <main className="flex-1 overflow-auto p-6">
        <Outlet />
      </main>
    </div>
  );
}
```

---

### 1.15 MSW — Mock Service Worker (US-F004)

**Definition :** MSW intercepte les appels HTTP au niveau du navigateur. Contrairement aux mocks qui remplacent la fonction `fetch`, MSW intercepte le VRAI `fetch`/`axios`.

**Pourquoi MSW et pas des mocks traditionnels ?**
- Les mocks Axios remplacent `apiClient.get` → ils ne testent pas le vrai code
- MSW intercepte l'appel au niveau du navigateur → le code reste identique a la prod
- MSW fonctionne aussi en tests (integration)

**Utilisation dans StockMaster :**
```typescript
// mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/v1/auth/login', async ({ request }) => {
    const body = await request.json();
    // 3 utilisateurs mokes pour tester les roles
    if (body.email === 'admin@groupe.test') {
      return HttpResponse.json({
        data: {
          accessToken: 'mock-token-xxx',
          utilisateur: { id: 1, role: 'ADMIN_GROUPE', email: 'admin@groupe.test' },
        },
      });
    }
    return HttpResponse.json({ detail: 'Identifiants invalides' }, { status: 401 });
  }),

  http.get('/api/v1/articles', () => {
    return HttpResponse.json({
      data: {
        content: [
          { id: 1, designation: 'Huile 1L', prixVenteTtc: 2500, stockActuel: 45 },
          { id: 2, designation: 'Riz 5kg', prixVenteTtc: 4500, stockActuel: 0 }, // Rupture !
        ],
      },
    });
  }),
];
```

**Activer le mode mock :**
```bash
VITE_ENABLE_MOCKS=true npm run dev
```

**Stephan peut donc coder TOUT le frontend AVANT que le backend soit pret :**
1. Il definit les handlers MSW avec les reponses attendues
2. Il code les ecrans avec `useQuery` et `useMutation`
3. Quand le backend est pret, il desactive les mocks → les appels vont vers le vrai serveur
4. **Aucune modification de code** entre le mode mock et le mode reel

---

## CHAPITRE 2 : FORMULAIRES ET VALIDATION (EPIC-F01 - 24 SP)

### 2.1 React Hook Form + Zod — La validation miroir (US-F012)

**Definition :** React Hook Form gere l'etat du formulaire. Zod valide les donnees. Ensemble, ils reproduisent cote client les memes regles de validation que le backend.

**Pourquoi "validation miroir" ?**
Le backend valide les donnees avec Jakarta Validation :
```java
@NotBlank(message = "Le nom est requis")
@Email(message = "Format email invalide")
@Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+]).{8,50}$",
         message = "8 caracteres min, 1 majuscule, 1 chiffre, 1 special")
private String motDePasse;
```

Le frontend MIROITE ces memes regles avec Zod :
```typescript
const inscriptionSchema = z.object({
  nomEntreprise: z.string().min(1, 'Le nom est requis'),
  email: z.string().email('Format email invalide'),
  motDePasse: z
    .string()
    .min(8, '8 caracteres minimum')
    .regex(/[A-Z]/, 'Doit contenir une majuscule')
    .regex(/[0-9]/, 'Doit contenir un chiffre')
    .regex(/[!@#$%^&*()_+]/, 'Doit contenir un caractere special'),
});
```

**Exemple complet : Formulaire d'inscription (US-F012)**
```tsx
function InscriptionForm() {
  const { register, handleSubmit, formState: { errors }, setError } = useForm({
    resolver: zodResolver(inscriptionSchema),
  });

  const mutation = useMutation({
    mutationFn: (data: InscriptionData) => api.post('/auth/inscription/entreprise-unique', data),
    onError: (err) => {
      if (err.status === 409) {
        // Erreur 409 = email deja utilise → on affiche l'erreur sur le champ email
        setError('email', { message: 'Cet email est deja utilise' });
      }
    },
  });

  return (
    <form onSubmit={handleSubmit((data) => mutation.mutate(data))}>
      <Input label="Email" error={errors.email?.message} {...register('email')} />
      <Input label="Mot de passe" type="password" error={errors.motDePasse?.message} {...register('motDePasse')} />
      <Button type="submit" loading={mutation.isPending}>Creer mon espace</Button>
    </form>
  );
}
```

**Gestion de l'erreur 409 (US-F012, CA) :**
Quand le backend retourne `409 EMAIL_ALREADY_EXISTS`, le formulaire affiche l'erreur sur le champ email. L'utilisateur peut corriger et re-soumettre.

**Gestion de l'erreur 401 (US-F014, CA) :**
Quand la connexion echoue, on affiche le message generique "Email ou mot de passe incorrect" — **sans reveler si c'est l'email ou le mot de passe qui est invalide** (securite).

---

### 2.2 Formulaire multi-etapes (US-F013)

**Concept :** Les formulaires longs sont decoupes en etapes pour ne pas submerger l'utilisateur.

**Exemple : Inscription Groupe (US-F013, US-D013)**
```
Etape 1 : Informations du groupe (nom, NIF, siege)
Etape 2 : Informations de l'admin (nom, email, mot de passe)
Etape 3 : Recapitulatif et validation
```

**Implementation avec React Hook Form :**
```tsx
function InscriptionGroupeForm() {
  const [etape, setEtape] = useState(0);
  const form = useForm({ resolver: zodResolver(groupeSchema) });

  const etapes = [
    { title: 'Groupe', fields: ['nomGroupe', 'nif', 'villeSiege'] },
    { title: 'Administrateur', fields: ['prenom', 'nom', 'email', 'motDePasse'] },
    { title: 'Recapitulatif', fields: [] },
  ];

  const suivant = async () => {
    const valid = await form.trigger(etapes[etape].fields);
    if (valid) setEtape(etape + 1);
  };

  return (
    <>
      <Stepper currentStep={etape} steps={etapes.map(e => e.title)} />
      {etape === 0 && <GroupeStep />}
      {etape === 1 && <AdminStep />}
      {etape === 2 && <RecapStep />}
      {etape < etapes.length - 1
        ? <Button onClick={suivant}>Suivant</Button>
        : <Button type="submit">Creer mon groupe</Button>
      }
    </>
  );
}
```

**Conception du Stepper (US-D013) :**
- Les 2 blocs (Groupe / Administrateur) sont visuellement separes
- La progression est claire : "Etape 1 sur 3"
- Le recapitulatif permet de verifier avant de soumettre

---

## CHAPITRE 3 : TABLEAUX DE BORD (EPIC-F02 - 19 SP)

### 3.1 Recharts — Les graphiques (US-F023)

**Definition :** Recharts est une librairie de graphiques React. Elle supporte les courbes, les barres, les camemberts.

**Exemple : Courbe d'evolution du CA (US-F023)**
```tsx
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

function CAEvolution() {
  const { data } = useQuery({
    queryKey: ['ca', '2026'],
    queryFn: () => api.get('/reporting/ca?granularite=MOIS&dateDebut=2026-01-01'),
  });

  return (
    <ResponsiveContainer width="100%" height={300}>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="mois" />
        <YAxis tickFormatter={(v) => `${v.toLocaleString()} FCFA`} />
        <Tooltip formatter={(v) => `${v.toLocaleString()} FCFA`} />
        <Line type="monotone" dataKey="ca" stroke="#2563eb" strokeWidth={2} />
      </LineChart>
    </ResponsiveContainer>
  );
}
```

**Concepts Recharts importants :**
- `ResponsiveContainer` : le graphique s'adapte a la taille de son conteneur
- `Line` : la courbe
- `XAxis`/`YAxis` : les axes
- `Tooltip` : infobulle au survol
- `CartesianGrid` : la grille de fond

**US liees :** US-F023 (courbe CA), US-F090 (top articles barres), US-F092 (comparaison barres)

---

### 3.2 Skeleton loaders — Chargement progressif (US-F020)

**Definition :** Les skeleton loaders sont des "fantomes" qui montrent la structure de la page pendant le chargement.

**Pourquoi pas un simple spinner ?**
- Un spinner dit juste "ca charge"
- Un skeleton montre ce qui VA apparaitre (structure, colonnes, lignes)
- L'utilisateur a l'impression que c'est plus rapide (psychologie)
- US-F020 : "Chargement progressif des widgets, pas de blocage total"

```tsx
function DashboardSkeleton() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {[1, 2, 3, 4].map(i => (
        <Card key={i}>
          <Skeleton className="h-4 w-24 mb-2" />
          <Skeleton className="h-8 w-32" />
        </Card>
      ))}
    </div>
  );
}
```

**Concept cle : chargement progressif**
Chaque widget du dashboard se charge independemment. Si un widget echoue, les autres continuent de s'afficher.

---

## CHAPITRE 4 : GESTION DU CATALOGUE (EPIC-F03 - 24 SP)

### 4.1 Pagination serveur (US-F030)

**Definition :** La pagination est geree par le BACKEND. Le frontend envoie `page` et `size` dans la requete.

**Pourquoi pas une pagination cote client (charger tout d'un coup) ?**
- Une entreprise peut avoir des milliers d'articles
- Tout charger = lenteur, memoire, temps de chargement
- Contrainte backend : "max 100 par page"

```tsx
function ArticleList() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['articles', { page, search }],
    queryFn: () => api.get(`/articles?search=${search}&page=${page}&size=20`),
  });

  return (
    <div>
      <Input
        placeholder="Rechercher un article..."
        value={search}
        onChange={e => { setSearch(e.target.value); setPage(0); }}
      />
      <Table
        columns={ArticleColumns}
        data={data?.content ?? []}
        loading={isLoading}
      />
      <Pagination
        currentPage={page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />
    </div>
  );
}
```

### 4.2 Calcul du TTC — Apercu vs Serveur (US-F031, regle §8.4)

**Regle critique StockMaster :** Le TTC est calcule par le BACKEND, JAMAIS par le frontend.

**Pourquoi ?**
- Le backend est la **source de verite** pour les calculs de TVA
- Le taux de TVA peut etre different selon la categorie, le pays, la date
- Si le frontend calcule le TTC et que le backend a un taux different, il y a un ecart

**Ce que fait le frontend :**
```tsx
function ArticleForm() {
  const [prixHT, setPrixHT] = useState(0);
  const [tva, setTVA] = useState(19.25);

  // Apercu LOCAL uniquement (pour l'UX)
  const apercuTTC = prixHT * (1 + tva / 100);

  return (
    <>
      <Input label="Prix HT" type="number" value={prixHT} onChange={...} />
      <Input label="TVA (%)" value={tva} disabled />
      <p className="text-sm text-muted-foreground">
        Apercu TTC : {formatCurrency(apercuTTC)}
        <br />
        <em>Le prix final est calcule par le serveur</em>
      </p>
      <Button type="submit">Enregistrer</Button>
    </>
  );
}
```

**Lecon :** Le frontend peut AFFICHER un apercu pour l'UX, mais la VALEUR PERSISTEE vient du backend. Ne JAMAIS envoyer le TTC calcule cote client dans la requete.

---

## CHAPITRE 5 : FORMULAIRES AVANCES (EPIC-F05 - 17 SP)

### 5.1 Formulaire multi-lignes dynamique (US-F050)

**Definition :** Un formulaire ou l'utilisateur peut ajouter/supprimer des lignes dynamiquement (ex: saisir les articles d'une commande).

```tsx
function CommandeForm() {
  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'lignes',
  });

  const total = fields.reduce((sum, _, index) => {
    const qte = form.watch(`lignes.${index}.quantite`) ?? 0;
    const pu = form.watch(`lignes.${index}.prixUnitaire`) ?? 0;
    return sum + qte * pu;
  }, 0);

  return (
    <form>
      {fields.map((field, index) => (
        <div key={field.id} className="flex gap-2">
          <Select name={`lignes.${index}.articleId`} control={form.control} />
          <Input type="number" {...form.register(`lignes.${index}.quantite`)} />
          <Input type="number" {...form.register(`lignes.${index}.prixUnitaire`)} />
          <Button type="button" onClick={() => remove(index)}>Supprimer</Button>
        </div>
      ))}
      <Button type="button" onClick={() => append({})}>Ajouter une ligne</Button>

      <div className="border-t pt-4">
        <p>Total HT : {formatCurrency(total)}</p>
      </div>
    </form>
  );
}
```

**Concepts :**
- `useFieldArray` : hook de React Hook Form pour gerer des listes dynamiques
- `append` : ajoute une ligne
- `remove` : supprime une ligne
- `form.watch` : surveille les valeurs en temps reel pour recalculer le total

---

## CHAPITRE 6 : ECRAN CAISSE (EPIC-F06 - 30 SP)

### 6.1 Interface tactile caisse (US-F063)

**Definition :** L'ecran de caisse est optimise pour un usage tactile rapide.

**Exigences (US-D063) :**
- Recherche d'article en 1-2 taps
- Fonctionnement en mode degrade si latence reseau elevee
- Saisie sans client (vente anonyme)
- Optionnellement, rattachement a un client existant (US-064b)

```tsx
function CaisseScreen() {
  const [lignes, setLignes] = useState<LigneVente[]>([]);
  const [search, setSearch] = useState('');
  const [clientId, setClientId] = useState<number | null>(null);

  const { data: articles } = useQuery({
    queryKey: ['articles-search', search],
    queryFn: () => api.get(`/articles?search=${search}&size=20`),
    enabled: search.length >= 2, // ← Ne cherche qu'a partir de 2 caracteres
  });

  const mutation = useMutation({
    mutationFn: (vente: VenteRequest) => api.post('/ventes', vente),
    onSuccess: () => {
      toast.success('Vente enregistree');
      setLignes([]); // ← Reset du panier
    },
  });

  const ajouterArticle = (article: Article) => {
    setLignes([...lignes, { articleId: article.id, designation: article.designation, quantite: 1, prixUnitaire: article.prixVenteTtc }]);
    setSearch('');
  };

  const total = lignes.reduce((sum, l) => sum + l.quantite * l.prixUnitaire, 0);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
      {/* Panneau gauche : saisie */}
      <div>
        <Input
          placeholder="Scanner ou rechercher un article..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          autoFocus
          className="text-lg h-12" // ← Grand champ pour usage tactile
        />
        <div className="grid grid-cols-3 gap-2 mt-2">
          {articles?.slice(0, 12).map(article => (
            <button
              key={article.id}
              className="border rounded-lg p-3 text-left hover:bg-accent"
              onClick={() => ajouterArticle(article)}
            >
              <p className="font-medium">{article.designation}</p>
              <p className="text-sm text-muted-foreground">{formatCurrency(article.prixVenteTtc)}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Panneau droit : panier */}
      <div>
        <h2 className="text-xl font-bold mb-4">Panier ({lignes.length} articles)</h2>
        {lignes.map((ligne, i) => (
          <div key={i} className="flex items-center justify-between py-2 border-b">
            <span>{ligne.designation}</span>
            <div className="flex items-center gap-2">
              <button onClick={() => modifQuantite(i, -1)} className="h-8 w-8 rounded-full border">-</button>
              <span>{ligne.quantite}</span>
              <button onClick={() => modifQuantite(i, 1)} className="h-8 w-8 rounded-full border">+</button>
              <span className="w-24 text-right">{formatCurrency(ligne.quantite * ligne.prixUnitaire)}</span>
              <button onClick={() => supprimerLigne(i)} className="text-destructive">×</button>
            </div>
          </div>
        ))}
        <div className="border-t-2 pt-4 mt-4">
          <p className="text-2xl font-bold">Total : {formatCurrency(total)}</p>
        </div>
        <Button
          size="lg"
          className="w-full mt-4 h-14 text-lg"
          onClick={() => mutation.mutate({ clientId, lignes })}
          loading={mutation.isPending}
        >
          Payer {formatCurrency(total)}
        </Button>
      </div>
    </div>
  );
}
```

---

## CHAPITRE 7 : CONCEPTS TRANSVERSES BACKEND A CONNAITRE

### 7.1 JWT — Les tokens (US-008, US-009, US-014)

**Definition :** JWT (JSON Web Token) est un format de token securise qui contient des donnees (claims) signees.

**Structure :**
```
header.payload.signature
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9.XdJ7...
```

**Les claims utilises dans StockMaster :**
```json
{
  "userId": 1,
  "entrepriseId": 2,
  "groupId": 1,
  "role": "ADMIN_GROUPE",
  "scope": "GROUPE",
  "jti": "uuid-unique",
  "iat": 1700000000,
  "exp": 1700000900
}
```

**Pourquoi le frontend doit comprendre JWT ?**
- Pour stocker le token dans `localStorage`
- Pour l'envoyer dans chaque requete
- Pour savoir quand il expire
- L'intercepteur Axios gere tout ca automatiquement

### 7.2 RBAC — Role-Based Access Control

**Les 7 roles StockMaster :**
| Role | Perimetre | Actions principales |
|---|---|---|
| `SUPER_ADMIN` | Plateforme entiere | Gerer les tenants, alertes techniques |
| `ADMIN_GROUPE` | Tout son groupe | Filiales, transferts, dashboard consolide |
| `ADMIN_FILIALE` | Sa filiale | Employes, catalogue, stock, rapports |
| `GESTIONNAIRE_STOCK` | Stock de sa filiale | Mouvements, corrections, inventaire |
| `RESP_ACHATS` | Achats de sa filiale | Commandes fournisseur |
| `COMMERCIAL` | Ventes de sa filiale | Commandes client, factures |
| `CAISSIER` | Caisse de sa filiale | Ventes directes |

**Dans le frontend :** chaque ecran verifie le role avant d'afficher les donnees.

### 7.3 Isolation multi-tenant

**Regle :** Chaque entreprise ne voit que SES donnees. Le `entreprise_id` est extrait du JWT, JAMAIS du corps de la requete.

**Pourquoi le frontend doit connaitre ce concept ?**
- Le store auth contient `user.entrepriseId`
- Tous les appels API sont filtres par cet id (cote backend)
- Le frontend ne peut PAS usurper l'entreprise d'un autre

### 7.4 Machine a etats des commandes

**Definition :** Les commandes traversent des etats. Chaque etat determine les actions possibles.

```
EN_PREPARATION → VALIDEE → LIVREE (final)
                → ANNULEE (final)
```

**Dans le frontend :** les boutons d'action sont actifs ou desactives selon l'etat :
```tsx
{etat === 'EN_PREPARATION' && (
  <>
    <Button onClick={modifier}>Modifier</Button>
    <Button onClick={valider}>Valider</Button>
    <Button variant="destructive" onClick={supprimer}>Supprimer</Button>
  </>
)}
{etat === 'VALIDEE' && (
  <>
    <Button onClick={livrer}>Marquer livree</Button>
    <Button onClick={facture}>Generer facture</Button>
  </>
)}
{etat === 'LIVREE' && (
  <Badge variant="livree">Livree</Badge>
)}
{etat === 'ANNULEE' && (
  <Badge variant="annulee">Annulee</Badge>
)}
```

---

## GLOSSAIRE COMPLET (80+ termes)

| Terme | Definition | Contexte StockMaster |
|---|---|---|
| **Atomique** | Operation "tout ou rien" | Une validation de commande cree TOUS les mouvements ou AUCUN |
| **Badge** | Petit indicateur visuel | Etat commande (EN_PREPARATION, VALIDEE, LIVREE) |
| **Barrel export** | Fichier `index.ts` qui re-exporte tout | `shared/ui/index.ts` exporte Button, Input, Table... |
| **Cache** | Stockage temporaire de donnees | TanStack Query garde les articles en cache 5 min |
| **CVA** | Class Variance Authority | Variantes de composants (Button primary/secondary) |
| **Claims** | Donnees contenues dans un JWT | `userId`, `role`, `entrepriseId` |
| **CORS** | Securite navigateur inter-origine | Le proxy Vite evite les erreurs CORS |
| **CRUD** | Create Read Update Delete | Les 4 operations de base sur les donnees |
| **Dev Mode** | Mode Figma pour developpeurs | Siko publie les maquettes en Dev Mode |
| **DTO** | Data Transfer Object | Objet qui transporte les donnees (LoginRequest) |
| **Feature-based** | Organisation par fonctionnalite | `features/auth/` contient tout l'auth |
| **FK** | Foreign Key | `client_id` dans la table `vente` |
| **Full-text** | Recherche dans tout le texte | Recherche d'articles par designation |
| **Guard** | Protection de route | `RequireAuth` empeche l'acces non autorise |
| **Hand-off** | Transmission designer → dev | Siko → Stephan via Figma Dev Mode |
| **HMR** | Hot Module Replacement | Modification du code → mise a jour sans rechargement |
| **Hook** | Fonction React speciale | `useState`, `useEffect`, `useQuery` |
| **Husky** | Pre-commit hook | Execute `npm run lint` avant chaque commit |
| **Immuable** | Qui ne peut pas etre modifie | Un mouvement SORTIE ne peut pas etre supprime |
| **Interceptor** | Middleware Axios | Ajoute le token JWT automatiquement |
| **Invalidation** | Marquage du cache comme obsolete | Apres creation d'article, le cache articles est invalide |
| **Isolation tenant** | Separation des donnees par entreprise | Chaque entreprise ne voit que ses donnees |
| **JWT** | JSON Web Token | Token d'authentification |
| **LCP** | Largest Contentful Paint | Metrique de performance : contenu principal < 2.5s |
| **LS** | localStorage | Stockage navigateur pour le token JWT |
| **Machine a etats** | Cycle de vie defini | EN_PREPARATION → VALIDEE → LIVREE |
| **Miroir (validation)** | Meme regles client et serveur | Zod cote front = Jakarta Validation cote back |
| **Mock** | Simulation | MSW simule les reponses API |
| **MSW** | Mock Service Worker | Intercepte les appels HTTP au niveau navigateur |
| **Mutation** | TanStack Query : ecriture | `useMutation` pour POST/PUT/DELETE |
| **Nullable** | Qui peut etre null | `clientId` nullable dans Vente (vente anonyme) |
| **Outlet** | Zone de contenu enfant | Dans `DashboardLayout`, le contenu change |
| **Pagination** | Decoupage par pages | 20 articles par page, backend calcule le total |
| **Payload** | Contenu du JWT | Les claims (userId, role...) |
| **Polling** | Rechargement periodique | Stock mis a jour toutes les 30s |
| **Prop drilling** | Transmission de props en cascade | Evite par Zustand |
| **Proxy** | Intermediaire reseau | Vite redirige /api/* vers le backend |
| **Query** | TanStack Query : lecture | `useQuery` pour GET |
| **Rate limiting** | Limitation du nombre de tentatives | 5 tentatives de connexion en 15 min |
| **RBAC** | Role-Based Access Control | Chaque role a des permissions differentes |
| **Recharts** | Librairie de graphiques | Courbe CA, barres top articles |
| **Refetch** | Rechargement des donnees | Au focus de la fenetre |
| **Refresh token** | Token de rafraichissement | Permet de renouveler l'access token sans se reconnecter |
| **Responsive** | Adaptation a la taille d'ecran | 360px mobile, 768px tablette, 1280px desktop |
| **Rollback** | Annulation d'une transaction | Si une etape echoue, tout est annule |
| **Skeleton** | Fantome de chargement | Montre la structure avant le chargement |
| **Snapshot** | Valeur figee a un instant T | Prix d'un article au moment de la commande |
| **Soft delete** | Suppression logique | `supprime = true` au lieu de DELETE SQL |
| **Spinner** | Roue de chargement | Animation de chargement |
| **StaleTime** | Temps avant revalidation | 5 min pour les listes, 30s pour le stock |
| **Strict (TS)** | Mode strict TypeScript | Active toutes les verifications |
| **TTC** | Toutes Taxes Comprises | Prix final = HT * (1 + TVA/100) |
| **TanStack Query** | Librairie de cache serveur | Gere automatiquement le chargement des donnees API |
| **Tenant** | Client (groupe) du SaaS | Chaque groupe est un tenant isole |
| **Toast** | Notification temporaire | Succes, erreur, warning |
| **Token** | Jeton d'authentification | JWT envoye dans chaque requete |
| **Validation miroir** | Memes regles cote client et serveur | Zod = Jakarta Validation |
| **Vite** | Build tool rapide | Compile le TS, sert le dev server |
| **WCAG** | Normes d'accessibilite web | Contraste minimum 4.5:1 |
| **WebSocket** | Communication bidirectionnelle | Alternative au polling pour le stock temps reel |
| **Wireframe** | Schema sans style | Valider la structure avant le design |
| **Zod** | Librairie de validation | Schemas de validation typescript |
| **Zustand** | Librairie de gestion d'etat | Store d'authentification |
