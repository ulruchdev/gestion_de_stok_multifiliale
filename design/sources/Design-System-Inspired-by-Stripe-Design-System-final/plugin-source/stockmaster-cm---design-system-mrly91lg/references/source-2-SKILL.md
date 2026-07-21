# StockMaster CM - Design System Skill

> **ID :** `stockmaster-cm-design-system`  
> **Version :** `2.0.0`  
> **Catégorie :** `Design System / Brand`  
> **Surface :** `Web (React)`  

---

## 🎯 Description

**StockMaster CM Design System** est un système de design complet pour les applications SaaS de gestion de stock, spécialement conçu pour les PME camerounaises. Inspiré par les meilleures pratiques de StockMaster, ce design system intègre des contraintes spécifiques au marché africain : usage mobile dominant, lisibilité en plein soleil, coupures réseau fréquentes, et gestion de la devise XAF.

---

## 📦 Capacités

### ✅ Fonctionnalités Principales

1. **Système de Tokens Complet**
   - Palette de 10 couleurs (7 base + 3 sémantiques)
   - Échelle typographique complète
   - Spacing system (grille 8px)
   - Border radius et weights

2. **Bibliothèque de Composants** (40+ composants)
   - **Badges** : États de commande + mouvements de stock
   - **Boutons** : Primary, secondary, outline, ghost, danger
   - **Inputs** : Text, Amount (XAF), Select, Textarea
   - **Cartes** : Card, CardHeader, CardBody, CardFooter
   - **Alertes** : Alert, Toast, EmptyState, Skeleton, OfflineState, LoadingState
   - **Tables** : Table, MobileTable, tables spécialisées
   - **Icônes** : 47 icônes personnalisées (Lucide-inspirées)

3. **Système Responsive**
   - Mobile-first (360px minimum)
   - 5 breakpoints : Mobile S (360px), Mobile (480px), Tablet (768px), Desktop (1024px), Wide (1280px)
   - MobileTable : Affichage en cartes sur mobile

4. **Gestion des Contraintes Spécifiques**
   - Contraste WCAG AA pour lisibilité plein soleil
   - Mode hors ligne intégré
   - Montants XAF entiers uniquement
   - Éviter orange/jaune (conflit Orange Money/MTN)

---

## 🎨 Design Tokens

### Couleurs

```css
/* Palette principale */
--bg: #ffffff;           /* background */
--fg: #262626;           /* foreground */
--accent: #1c1e54;        /* Brand Dark */
--surface: #f5faf7;      /* Surface */
--muted: #93cba8;        /* Muted */
--border: #e5edf5;       /* Border Default */
--accent-secondary: #15be53; /* Success Green */

/* Sémantiques */
--brand-color-success: #15be53;
--brand-color-error: #ff4d4f;
--brand-color-warning: #faad14;
--brand-color-info: #1677ff;
```

### Typography

```css
/* Display */
--font-display: SFMono, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif;

/* Body */
--font-body: system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif;

/* Mono */
--font-mono: ui-monospace, SFMono-Regular, Menlo, monospace;
```

### Spacing

```css
--space-2: 2px;
--space-4: 4px;
--space-8: 8px;   /* Baseline grid */
--space-12: 12px;
--space-16: 16px;
--space-24: 24px;
--space-32: 32px;
--space-48: 48px;
```

---

## 🧩 Composants Clés

### Badges (US-D004)

**États de Commande :**
- `EN_PREPARATION` - `#1c1e54` (Brand Dark)
- `VALIDEE` - `#15be53` (Success Green)
- `LIVREE` - `#52c41a` (Green-6)
- `ANNULEE` - `#ff4d4f` (Error)

**Mouvements de Stock :**
- `ENTREE` - `#52c41a`
- `SORTIE` - `#ff4d4f`
- `CORRECTION_POS` - `#1677ff`
- `CORRECTION_NEG` - `#d48806`
- `TRANSFERT_ENTREE` - `#13c2c2`
- `TRANSFERT_SORTIE` - `#eb2f96`
- `ANNULATION_VENTE` - `#8c8c8c`

### Icônes (US-D006)

**47 icônes disponibles** dans 5 catégories :
- Stock (5) : Stock, Package, PackageOpen, Boxes, Archive
- Alertes (4) : AlertTriangle, AlertCircle, Bell, BellOff
- Transferts (4) : Truck, ArrowLeftRight, Move, Exchange
- Factures (4) : FileInvoice, FileText, Receipt, Printer
- Utilisateurs (5) : User, Users, UserPlus, UserCog, Shield
- Commandes (4) : ShoppingCart, ShoppingBasket, ClipboardList, List
- Caisse (4) : Calculator, CreditCard, Banknote, Coins
- Navigation (6) : Home, Settings, Menu, X, ChevronRight, ChevronLeft, ChevronUp, ChevronDown
- Actions (11) : Search, Filter, SortAsc, SortDesc, Plus, Minus, Edit, Trash, Eye, EyeOff, Check, XCircle, Loader
- États (3) : CheckCircle, AlertOctagon, Info

---

## 📁 Structure des Fichiers

```
.
├── DESIGN.md                      # Documentation complète
├── README.md                      # Guide d'utilisation
├── SKILL.md                       # Ce fichier
├── colors_and_type.css            # Tokens CSS centralisés
├── design-tokens.css              # Tokens CSS alternatifs
│
├── components/
│   ├── index.jsx                  # Export centralisé (compatible Babel)
│   ├── Icons.jsx                  # 47 icônes personnalisées
│   ├── Badges.jsx                 # Badges d'état
│   ├── Buttons.jsx                # Système de boutons
│   ├── Inputs.jsx                 # Champs de formulaire
│   ├── Cards.jsx                  # Système de cartes
│   ├── Alerts.jsx                 # Alertes et états
│   ├── Tables.jsx                 # Tables et listes
│   └── Atomic.jsx                 # Composants atomiques
│
├── preview/
│   ├── badges.html                # Prévisualisation badges
│   ├── buttons.html               # Prévisualisation boutons
│   ├── colors.html                # Palette de couleurs
│   ├── typography.html            # Échelle typographique
│   └── components.html            # Tous les composants
│
└── system/
    ├── variables.css              # Variables CSS (light + dark)
    ├── variables.dark.css         # Theme sombre
    ├── tokens.default.json        # Tokens JSON light
    ├── tokens.dark.json           # Tokens JSON dark
    ├── tokens.compact.json        # Tokens JSON dense
    └── artifacts/                 # Templates générés
```

---

## 🚀 Utilisation

### Configuration Requise

```html
<!-- Dans le <head> de votre HTML -->
<script src="https://unpkg.com/react@18/umd/react.development.js"></script>
<script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
<script src="https://unpkg.com/@babel/standalone@7/babel.min.js"></script>
<link rel="stylesheet" href="system/variables.css">
```

### Chargement des Composants

```html
<!-- Charger tous les composants -->
<script type="text/babel" src="components/Icons.jsx"></script>
<script type="text/babel" src="components/Badges.jsx"></script>
<script type="text/babel" src="components/Buttons.jsx"></script>
<script type="text/babel" src="components/Inputs.jsx"></script>
<script type="text/babel" src="components/Cards.jsx"></script>
<script type="text/babel" src="components/Alerts.jsx"></script>
<script type="text/babel" src="components/Tables.jsx"></script>
```

### Utilisation dans le Code

```jsx
// Les composants sont disponibles dans window
const { Button, Card, OrderStatusBadge, StockMasterIcons } = window;

ReactDOM.createRoot(document.getElementById('root')).render(
  <Card>
    <Button variant="primary" startIcon={<StockMasterIcons.Package size={16} />}>
      Ajouter un article
    </Button>
    <OrderStatusBadge status="VALIDEE" />
  </Card>
);
```

---

## 📊 Métriques

| Métrique | Valeur |
|----------|--------|
| Composants | 40+ |
| Icônes | 47 |
| Couleurs | 10 |
| Breakpoints | 5 |
| Taille totale | ~250 KB |
| Couverture EPIC-D00 | 100% |

---

## 🎓 Bonnes Pratiques

### ✅ À Faire
- Utiliser les tokens CSS pour toutes les couleurs et tailles
- Suivre la grille de spacing 8px
- Utiliser `AmountInput` pour les montants XAF
- Toujours calculer le TTC, jamais le saisir
- Utiliser `MobileTable` sur mobile (< 768px)

### ❌ À Éviter
- Ne pas utiliser de couleurs en dehors de la palette
- Ne pas utiliser `Inter` ou `Roboto` comme police display
- Ne pas utiliser de gradients agressifs
- Ne pas utiliser orange/jaune pur comme accent principal
- Ne pas afficher de tableaux sous 768px

---

## 🔍 Résolution des Problèmes

### Problème : `SyntaxError: Cannot use import statement outside a module`

**Cause :** Les fichiers JSX utilisent `import/export` mais sont chargés directement dans HTML.

**Solution :** 
1. Utiliser `type="text/babel"` dans les balises script
2. Charger Babel Standalone
3. Les composants sont exportés dans `window` via `Object.assign(window, {...})`

### Problème : Icônes non affichées

**Solution :** 
- Vérifier que `components/Icons.jsx` est chargé
- Utiliser `window.StockMasterIcons.NomIcone` ou `window.NomIcone`

---

## 📞 Support

Pour toute question ou problème avec ce Design System, consulter :
- `DESIGN.md` - Documentation complète
- `GS-DESIGN-BACKLOG-2026-01.md` - Backlog des tâches
- Les fichiers de démonstration dans le dossier racine

---

## 🏷️ Tags

`design-system`, `react`, `stockmaster-inspired`, `mobile-first`, `fintech`, `cameroun`, `stock-management`, `saas`, `responsive`, `accessibility`, `wcag-aa`, `offline-first`

---

*Dernière mise à jour : Juillet 2026*
*Version : 2.0.0*
