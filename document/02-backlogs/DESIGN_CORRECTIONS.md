# 🎨 DESIGN_CORRECTIONS — StockMaster CM

> Fichier maître listant toutes les corrections design appliquées au frontend React.
> **Base de référence :** awesome-design-md (50+ design systems) — design propriétaire StockMaster CM
> **Contexte :** SaaS multi-tenant de gestion de stock pour PME camerounaises
> **Dernière mise à jour :** Juillet 2026

---

## Tables des Matières

1. [Phase 1 — Tokens CSS Fondamentaux](#phase-1--tokens-css-fondamentaux)
2. [Phase 2 — Configuration Tailwind](#phase-2--configuration-tailwind)
3. [Phase 3 — Composants UI](#phase-3--composants-ui)
4. [Phase 4 — Layouts](#phase-4--layouts)
5. [Phase 5 — Nouveaux Composants](#phase-5--nouveaux-composants)
6. [Phase 6 — Validation & Documentation](#phase-6--validation--documentation)

---

## Phase 1 — Tokens CSS Fondamentaux

**Fichier cible :** `frontend/src/index.css`
**Objectif :** Palette de couleurs, typographie, espacements et motions pour StockMaster CM.

### C-001 : Tokens `:root` (thème clair)

Mapping des tokens shadcn/ui vers la palette StockMaster :

| Token | Valeur HSL | Usage | 
|---|---|---|
| `--background` | `0 0% 100%` | Fond de page |
| `--foreground` | `210 65% 15%` | Texte principal |
| `--primary` | `249 98% 61%` | Actions principales |
| `--secondary` | `210 10% 95%` | Fond secondaire |
| `--muted-foreground` | `215 16% 47%` | Texte secondaire |
| `--accent` | `252 100% 97%` | Fond survol/focus |
| `--destructive` | `359 100% 65%` | Actions destructives |
| `--border` | `218 20% 91%` | Bordures légères |
| `--ring` | `249 98% 61%` | Focus |

### C-002 : Tokens `.dark` (thème sombre)

Tokons adaptés pour le mode sombre avec contraste optimal.

### C-003 : Tokens spécifiques StockMaster

Ajout des tokens marque (`--brand-*`) et états stock (`--stock-*`) pour le contexte camerounais.

### C-004 : Body & typographie

`font-size: 15px`, `line-height: 1.5`, chiffres tabulaires (`tnum`) pour montants XAF.

### C-005 : Styles de base

Focus ring, selection, headings typography, monospace pour données financières.

---

## Phase 2 — Configuration Tailwind

**Fichier cible :** `frontend/tailwind.config.ts`

### C-006 : Couleurs marque et stock

Classes `brand-*` (accent, ink, canvas, hairline, dark) et `stock-*` (warning, danger, success).

### C-007 : Polices

`font-display`, `font-body`, `font-mono` — trois stacks typographiques.

### C-008 : Animations

5 animations : fade-in, fade-in-up, scale-in, slide-in-right, slide-in-left, slide-in-down.

---

## Phase 3 — Composants UI

**Fichiers cibles :** `frontend/src/shared/ui/*.tsx`

### C-009 : Button.tsx — Variants et tailles

- Nouveaux variants : `brand`, `brand-outline`, `pill`, `pill-outline`
- Nouvelle taille : `xl` (h-12), `icon-sm` (h-8 w-8)

### C-010 : Input.tsx — États et icône

- États : `error`, `warning`, `success`
- Support icône à gauche, `inputSize` (default/lg)
- Description sous le champ

### C-011 : Badge.tsx — Tokens et fonctions

- Variants soft : `soft`, `success`, `warning`
- Statuts commande : `en_preparation`, `validee`, `livree`, `annulee`, `en_attente`, `partiel`, `rembourse`
- Fonctions : `dot` (indicateur), `removable` (supprimable), tailles (sm/default/lg)

### C-012 : Modal.tsx — Animations et taille

- Nouvelle taille `xl` (max-w-4xl)
- Animation `animate-scale-in` à l'ouverture
- Prop `blur` pour désactiver le flou optionnellement

### C-013 : Table.tsx — Variants compact et striped

- Trois variants : `default`, `compact`, `striped`
- Utilisation de `group/data-variant` pour éviter le prop drilling
- En-tête en majuscules (uppercase)

### C-014 : Toast.tsx — Barre de progression et positions

- Barre de progression animée pour l'auto-dismiss
- Position configurable : bottom-right, top-right, top-center, bottom-center
- Couleurs basées sur les tokens StockMaster

---

## Phase 4 — Layouts

**Fichiers cibles :** `frontend/src/app/layouts/*.tsx`

### C-015 : AuthLayout — Design épuré

- Fond : `bg-[var(--brand-canvas-soft)]`
- Animation d'entrée : `animate-fade-in-up`
- Header réduit (h-14) avec bordure hairline

### C-016 : DashboardLayout — Sidebar brand-dark

- Sidebar : fond `bg-[var(--brand-dark)]` (#1c1e54)
- Navigation : texte blanc avec opacité (60% inactif, 100% actif)
- Fond actif : `bg-white/10`

---

## Phase 5 — Nouveaux Composants

**Fichiers cibles :** `frontend/src/shared/ui/`

### C-017 : Card.tsx

Variants : default, elevated, bordered, interactive. Sous-composants : Header, Title, Description, Content, Footer.

### C-018 : Avatar.tsx

Tailles : sm/md/lg/xl. Formes : circle/rounded/square. Statut : online/offline/away. AvatarGroup avec overflow +N.

### C-019 : Skeleton.tsx

Variants : text/circle/rectangle/card/table-row. Compositions : SkeletonText, SkeletonTable, SkeletonCard.

### C-020 : EmptyState.tsx

Variants : default/error/no-results/no-data/no-permissions. Icônes spécifiques, CTA action.

### C-021 : Tabs.tsx

Variants : underline/pills/segmented. Utilise React Context pour propager l'état actif.

### C-022 : Select.tsx

Champ select stylisé avec chevron custom, label, état error.

### C-023 : SearchInput.tsx

Recherche avec debounce (300ms), bouton clear, variant pill.

### C-024 : DataTable.tsx

Table enrichie : tri colonnes, pagination avec ellipsis, recherche, 4 états (loading/error/empty/data).

### C-025 : OfflineBanner.tsx

Bannière hors ligne avec 3 états (online/offline/reconnecting). Détection navigateur + fetch périodique. Reconnexion automatique limitée (maxRetries). Indicateur persistant en bas à gauche.

---

## Phase 6 — Validation & Documentation

### C-026 : Build

`vite build` — 1910 modules, 0 erreurs. CSS: ~28 kB, JS: ~286 kB.

### C-027 : Documentation

Fichiers mis à jour : HUMAN_CHANGELOG.md, progress-ledger.md, knowledge.md, DESIGN_TOKENS_REFERENCE.md.

### C-028 : Logo.tsx — Composant logotype réutilisable

**Fichier cible :** `frontend/src/shared/ui/Logo.tsx`

**4 variantes contextuelles :**

| Variante | Contexte | Comportement |
|---|---|---|
| `default` | Affichage statique (mobile header) | Non cliquable, juste l'icône |
| `landing` | En-tête de l'accueil publique | Redirige vers `/` |
| `sidebar` | Navigation latérale dashboard | Redirige vers `/dashboard` |
| `auth` | Pages d'authentification (login, inscription, forgot/reset) | Redirige vers `/` |

**3 tailles :** sm (icône 20px, boîte 28px), md (20px/32px), lg (24px/40px)

**Propriété `showText` :** `false` pour n'afficher que le logomark (icône Warehouse) sans le logotype "StockMaster."

**Intégration dans toutes les pages :**

| Page/Layout | Variante | Taille | showText |
|---|---|---|---|
| `AuthLayout.tsx` — Header | `auth` | md | true |
| `DashboardLayout.tsx` — Sidebar | `sidebar` | md | true |
| `DashboardLayout.tsx` — Mobile header | `default` | sm | false |
| `AccueilPage.tsx` — Header | `landing` | md | true |
| `InscriptionChoixPage.tsx` — Hero | `auth` | lg | true |
| `InscriptionEntrepriseUniquePage.tsx` — Titre | `auth` | md | true |
| `InscriptionGroupePage.tsx` — Titre | `auth` | md | true |

**Design tokens utilisés :** `--brand-primary` (fond du logomark), `--brand-ink` (texte), `--brand-ink` + opacity (sidebar)

---

## Progression

| Phase | Statut | Corrections |
|---|---|---|
| **Phase 1** — Tokens CSS | ✅ Terminé | C-001 → C-005 |
| **Phase 2** — Config Tailwind | ✅ Terminé | C-006 → C-008 |
| **Phase 3** — Composants UI | ✅ Terminé | C-009 → C-014 |
| **Phase 4** — Layouts | ✅ Terminé | C-015 → C-016 |
| **Phase 5** — Nouveaux composants | ✅ Terminé | C-017 → C-025 |
| **Phase 6** — Validation | ✅ Terminé | C-026 → C-028 |

> **Build vérifié :** ✅ `vite build` — 1910 modules, 0 erreurs
> **Toutes les phases sont terminées.** Le design system StockMaster est intégré dans le frontend.
