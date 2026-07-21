# 🎨 DESIGN TOKENS REFERENCE — StockMaster CM

> Guide de référence des tokens design du système StockMaster pour PME camerounaises.
> **Fichiers :** `frontend/src/index.css`, `frontend/tailwind.config.ts`, `frontend/src/shared/ui/*.tsx`

---

## 1. Palette de Couleurs

### Tokens shadcn/ui (palette StockMaster)

| Token (CSS) | Valeur HSL | Couleur | Usage |
|---|---|---|---|
| `--background` | `0 0% 100%` | `#ffffff` | Fond de page |
| `--foreground` | `210 65% 15%` | `#0d253d` | Texte principal |
| `--card` | `210 50% 98%` | `#f6f9fc` | Cartes et panneaux |
| `--card-foreground` | `210 65% 15%` | `#0d253d` | Texte sur carte |
| `--primary` | `249 98% 61%` | `#533afd` | Actions / liens principaux |
| `--primary-foreground` | `0 0% 100%` | `#ffffff` | Texte sur primaire |
| `--secondary` | `210 10% 95%` | `#f0f2f3` | Fond secondaire |
| `--secondary-foreground` | `208 35% 25%` | `#2a3f54` | Texte sur secondaire |
| `--muted` | `210 8% 96%` | `#f5f6f7` | Fond muet / tertiaire |
| `--muted-foreground` | `215 16% 47%` | `#64748b` | Texte secondaire |
| `--accent` | `252 100% 97%` | `#f4f0ff` | Fond accent (survol, focus) |
| `--accent-foreground` | `249 98% 61%` | `#533afd` | Texte accent |
| `--destructive` | `359 100% 65%` | `#ff4d4f` | Actions destructives / erreur |
| `--destructive-foreground` | `0 0% 100%` | `#ffffff` | Texte sur destructif |
| `--border` | `218 20% 91%` | `#e3e8ee` | Bordures légères |
| `--input` | `215 15% 87%` | `#dbdee2` | Bordures des champs |
| `--ring` | `249 98% 61%` | `#533afd` | Cercle de focus |
| `--radius` | `0.375rem` | 6px | Bordure standard des composants |

### Tokens Marque StockMaster

```css
--brand-primary: #533afd;
--brand-primary-hover: #7d63ff;
--brand-primary-active: #3727d6;
--brand-primary-bg: #f4f0ff;
--brand-primary-border: #c6b5ff;
--brand-ink: #0d253d;
--brand-ink-muted: #64748b;
--brand-canvas: #ffffff;
--brand-canvas-soft: #f6f9fc;
--brand-hairline: #e3e8ee;
--brand-dark: #1c1e54;
```

### Tokens États de Stock (contexte camerounais)

```css
--stock-warning: #faad14;      /* Seuil d'alerte stock bas */
--stock-warning-bg: #fffbe6;   /* Fond alerte stock */
--stock-danger: #ff4d4f;       /* Stock négatif / rupture */
--stock-danger-bg: #fff2f0;    /* Fond rupture stock */
--stock-success: #52c41a;      /* Stock suffisant */
--stock-success-bg: #f6ffed;   /* Fond stock OK */
--stock-info: #533afd;         /* Information générale stock */
```

### Classes Tailwind disponibles

```tsx
/* Marque StockMaster */
brand-accent, brand-ink, brand-canvas, brand-canvas-soft,
brand-hairline, brand-dark, brand-ink-muted

/* États stock */
stock-warning, stock-danger, stock-success,
stock-warning-bg, stock-danger-bg, stock-success-bg
```

---

## 2. Typographie

### Font stacks

```css
--brand-font-display: 'SF Pro Display', system-ui, -apple-system, sans-serif;
--brand-font-body: system-ui, -apple-system, sans-serif;
--brand-font-mono: ui-monospace, 'SF Mono', 'Cascadia Code', monospace;
```

### Classes Tailwind

```tsx
font-display   /* Titres et affichage */
font-body      /* Corps de texte */
font-mono      /* Données financières, codes */
```

### Règles typographiques

- `body` : 15px, line-height 1.5, chiffres tabulaires activés (`font-feature-settings: 'tnum' on`) pour lisibilité des montants en XAF
- `h1-h6` : font-display, weight 600, letter-spacing -0.025em
- `code, pre, .tabular-nums` : font-mono (monospace)
- Rendu optimisé : `-webkit-font-smoothing: antialiased` (lisibilité sur écrans mobiles)
- Sélection : fond primary à 20% d'opacité

---

## 3. Espacement (Grille 8px)

```css
--brand-space-xxs: 2px;    /* Espacements minimaux */
--brand-space-xs: 4px;     /* Paddings minimum */
--brand-space-sm: 8px;     /* Unité de base */
--brand-space-md: 16px;    /* Espacement standard */
--brand-space-lg: 24px;    /* Espacement large */
--brand-space-xl: 32px;    /* Sections */
--brand-space-xxl: 48px;   /* Grandes sections */
--brand-space-huge: 64px;  /* Pages d'accueil */
```

### Guidelines
- Paddings de cartes : 16px (md) ou 24px (lg)
- Gaps entre sections dashboard : 32px (xl)
- Paddings de page : 24px (lg) desktop, 16px (md) mobile

---

## 4. Rayons de Bordure

```css
--brand-radius-sm: 3px;     /* Petits badges, tags */
--brand-radius-md: 6px;     /* Standard (inputs, cartes) */
--brand-radius-lg: 12px;    /* Grands conteneurs */
--brand-radius-pill: 9999px; /* Boutons arrondis, badges */
```

---

## 5. Motion & Animations

### Durées

```css
--brand-duration-fast: 100ms;  /* Hover, transitions rapides */
--brand-duration-mid: 200ms;   /* Apparition, focus */
--brand-duration-slow: 400ms;  /* Navigation, slide */
```

### Fonctions d'accélération

```css
--brand-ease-out: cubic-bezier(0.22, 1, 0.36, 1);     /* Sortie naturelle */
--brand-ease-in-out: cubic-bezier(0.65, 0, 0.35, 1);  /* Entrée-sortie */
```

### Classes d'animation Tailwind

```tsx
animate-fade-in        /* Apparition en fondu */
animate-fade-in-up     /* Apparition par le bas */
animate-scale-in       /* Apparition avec zoom */
animate-slide-in-right /* Glissement depuis la droite */
animate-slide-in-left  /* Glissement depuis la gauche */
animate-slide-in-down  /* Glissement depuis le haut (OfflineBanner) */
```

---

## 6. Composants UI

### Liste complète (15 composants)

| Composant | Fichier | Variants principaux |
|---|---|---|
| **Button** | `Button.tsx` | default, destructive, outline, secondary, ghost, link, brand, brand-outline, pill, pill-outline |
| **Input** | `Input.tsx` | default, lg, error, warning, success, avec/sans icône |
| **Badge** | `Badge.tsx` | default, secondary, destructive, outline, soft, success, warning, + 7 statuts commande |
| **Modal** | `Modal.tsx` | sm, md, lg, xl, avec/sans blur |
| **Table** | `Table.tsx` | default, compact, striped |
| **Toast** | `Toast.tsx` | success, error, warning, info (positions configurables) |
| **Card** | `Card.tsx` | default, elevated, bordered, interactive (hover) |
| **Avatar** | `Avatar.tsx` | sm, md, lg, xl ; circle, rounded, square ; statut online/offline/away |
| **Skeleton** | `Skeleton.tsx` | text, circle, rectangle, card, table-row (+ compositions) |
| **EmptyState** | `EmptyState.tsx` | default, error, no-results, no-data, no-permissions |
| **Tabs** | `Tabs.tsx` | underline, pills, segmented (avec React Context) |
| **Select** | `Select.tsx` | default, lg, error |
| **SearchInput** | `SearchInput.tsx` | default, pill (avec debounce) |
| **DataTable** | `DataTable.tsx` | tri colonnes, pagination, recherche, états loading/error/empty |
| **OfflineBanner** | `OfflineBanner.tsx` | online/offline/reconnecting (contexte coupures réseau) |

---

## 7. Layouts

### AuthLayout
- Fond : `bg-[var(--brand-canvas-soft)]` (`#f6f9fc`)
- Header : 56px (h-14), bordure hairline
- Contenu : centré, max-w-sm, animation `animate-fade-in-up`
- Footer : texte 12px, couleur ink-muted

### DashboardLayout
- Sidebar : fond `bg-[var(--brand-dark)]` (`#1c1e54`)
- Navigation inactive : `text-white/60`
- Navigation active : `text-white`, fond `bg-white/10`
- Icônes : 18px
- Contenu : fond canvas-soft, padding 24px
- Breakpoint responsive sidebar : lg (1024px)

---

## 8. Mode Sombre

Activé par la classe `.dark` sur un élément parent.

### Principaux changements
- Background : `#1d1d1d` — gris foncé (pas noir pur, moins fatiguant)
- Actions : `#6d57dc` — violet clair pour lisibilité
- Cartes : `#141414`
- Bordures : `#3e3e3e`
- Couleurs stock adaptées en version dark

---

## 9. Spécification Responsive

### Breakpoints (mobile-first)

| Breakpoint | Largeur min | Cible | Usage |
|---|---|---|---|
| `xs` | 360px | Mobile bas de gamme | Contexte camerounais |
| `sm` | 640px | Grand mobile | iPhone Plus / Android large |
| `md` | 768px | Tablette | iPad / Galaxy Tab |
| `lg` | 1024px | Desktop | Sidebar fixe |
| `xl` | 1280px | Desktop large | Écrans 13-14\" |
| `2xl` | 1536px | Grand écran | Écrans 15\"+ |

### Adaptation par composant

| Composant | Mobile (< 768px) | Desktop (≥ 1024px) |
|---|---|---|
| **Sidebar** | Cachée → overlay avec burger menu | Fixe à gauche, toujours visible |
| **AuthLayout** | Padding 16px, contenu max-w-sm centré | Idem |
| **Table** | Scroll horizontal si trop de colonnes | Affichage normal |
| **Modal** | Pleine largeur avec marge 16px | Centré avec overlay |
| **Cards** | 1 colonne | 2-3 colonnes (grille) |
| **EmptyState** | Centré, padding 32px | Idem |
| **Toast** | 90% largeur, bottom-right | max-w-sm, bottom-right |

### Points de test recommandés

| # | Taille | Appareil type |
|---|---|---|
| 1 | 360×800 | Mobile compact (Android entrée gamme) |
| 2 | 390×844 | iPhone 14 |
| 3 | 414×896 | Grand Android |
| 4 | 744×1133 | iPad Mini portrait |
| 5 | 834×1194 | iPad Air paysage |
| 6 | 1024×768 | Desktop petit écran |
| 7 | 1280×720 | Desktop HD |
| 8 | 1440×900 | Desktop standard |
| 9 | 1920×1080 | Grand écran |

---

*Document créé le : Juillet 2026*
*Dernière mise à jour : Juillet 2026*
