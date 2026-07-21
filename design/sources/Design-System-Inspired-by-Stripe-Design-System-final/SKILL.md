---
id: stockmaster-cm-design-system
name: StockMaster CM Design System
version: 2.0.0
category: Design System / Brand
surface: web
tags:
  - design-system
  - react
  - stockmaster
  - mobile-first
  - inventory-management
  - cameroon
  - stock-management
  - saas
  - responsive
  - accessibility
  - wcag-aa
  - offline-first
---

# 🎨 StockMaster CM Design System

> **Version :** 2.0.0 | **Catégorie :** Design System / Brand | **Stack :** React + TypeScript + Vite + Tailwind CSS

**StockMaster CM Design System** est un système de design complet pour les applications SaaS de gestion de stock, spécialement conçu pour les PME camerounaises. Ce design system intègre des contraintes spécifiques au marché africain : usage mobile dominant, lisibilité en plein soleil, coupures réseau fréquentes, et gestion de la devise XAF.

## Contexte

StockMaster CM est une solution de gestion de stock multi-filiales destinée aux PME camerounaises (épiceries, pharmacies, distributeurs multi-sites).

### Contraintes spécifiques
- Usage mobile dominant (contexte africain)
- Coupures réseau fréquentes → architecture offline-first
- Gestion de la devise XAF (franc CFA, pas de décimales)
- Écrans mobiles bas de gamme (360px+)
- Éviter les couleurs jaune/orange (confusion avec Orange Money / MTN Mobile Money)

## Architecture

### Stack technique
- **Framework :** React 18 + TypeScript + Vite
- **Styling :** Tailwind CSS avec tokens CSS personnalisés
- **Composants :** 15 composants atomiques réutilisables (Button, Input, Badge, Card, Modal, Table, Toast, etc.)
- **Icônes :** Lucide React (~50 icônes documentées par domaine fonctionnel)
- **Animations :** CSS transitions avec easing personnalisés

### Structure des fichiers
```
frontend/src/
├── shared/ui/          # Composants atomiques
│   ├── Button.tsx
│   ├── Input.tsx
│   ├── Badge.tsx
│   ├── Card.tsx
│   ├── Modal.tsx
│   ├── Table.tsx
│   ├── Toast.tsx
│   ├── Tabs.tsx
│   ├── Select.tsx
│   ├── Avatar.tsx
│   ├── Skeleton.tsx
│   ├── EmptyState.tsx
│   ├── SearchInput.tsx
│   ├── DataTable.tsx
│   └── OfflineBanner.tsx
├── index.css           # Tokens CSS (palette, typo, espacement, motion)
└── tailwind.config.ts  # Configuration Tailwind
```

### Dépendances
- `class-variance-authority` — gestion des variants de composants (CVA)
- `clsx` + `tailwind-merge` — fusion de classes (`cn()`)
- `lucide-react` — bibliothèque d'icônes
- `zustand` — store pour les toasts

## Principes du design system

1. **Mobile-first** — tous les composants sont conçus pour 360px minimum
2. **Accessible** — contrastes WCAG AA, attributs ARIA, focus rings visibles
3. **Résilient** — états de chargement (Skeleton), erreur (EmptyState), hors ligne (OfflineBanner)
4. ** Cohérent** — palette unique, espacement 8px, typographie système
5. **Spécifique au contexte** — adaptation aux contraintes camerounaises (XAF, mobile, réseau)

## Composants clés

### Icônes (US-D006)
47 icônes Lucide organisées par domaine fonctionnel :
- **Navigation :** LayoutDashboard, Building2, Warehouse, ArrowLeftRight, Menu, X
- **Actions :** Plus, Pencil, Trash2, Save, Download, Upload, Search, Filter
- **Statuts :** CheckCircle, AlertCircle, AlertTriangle, Info, Ban, Clock, Package
- **Stock :** Package, PackageOpen, Box, Barcode, QrCode
- **Commandes :** ShoppingCart, Receipt, Truck, ClipboardList, FileText
- **Utilisateurs :** User, Users, UserCircle, UserPlus, UserCheck, Shield
- **Finance :** DollarSign, CreditCard, Wallet, ReceiptText, Coins, HandCoins
- **Communication :** Mail, Phone, MessageSquare, Bell, BellRing
- **Outils :** Settings, LogOut, ChevronDown, ChevronUp, ChevronLeft, ChevronRight, ChevronsUpDown, MoreHorizontal, MoreVertical, Download, Upload, ExternalLink, Copy, Printer

### Boutons
Variants : default, destructive, outline, secondary, ghost, link, **brand**, **brand-outline**, **pill**, **pill-outline**
Tailles : sm, default, lg, xl, icon, icon-sm

### Badges d'état
Variants statut commande : en_preparation, validee, livree, annulee, en_attente, partiel, rembourse
Variants génériques : default, secondary, destructive, outline, soft, success, warning

### Toast (notifications)
Types : success, error, warning, info
Fonctionnalités : barre de progression, auto-dismiss, position configurable

### OfflineBanner
États : online, offline, reconnecting
Détection automatique (navigator.onLine + fetch périodique)
Reconnexion avec limite de tentatives (maxRetries)

## Utilisation

### Installation
```bash
cd frontend && npm install
```

### Développement
```bash
npm run dev
```

### Build
```bash
npm run build
```

### Exemple d'utilisation
```tsx
import { Button, Card, CardContent, EmptyState, DataTable } from '@/shared/ui';

function MaPage() {
  return (
    <Card variant="elevated">
      <CardContent>
        <DataTable
          columns={[
            { key: 'code', label: 'Code', sortable: true },
            { key: 'designation', label: 'Désignation' },
            { key: 'stock', label: 'Stock', sortable: true },
          ]}
          data={articles}
          keyExtractor={(item) => item.id}
          isLoading={loading}
          emptyTitle="Aucun article"
        />
      </CardContent>
    </Card>
  );
}
```

## Résolution de problèmes

### Le build échoue avec ERR_MODULE_NOT_FOUND
```bash
cd frontend && npm install
```

### Les composants n'ont pas les bons styles
Vérifier que `frontend/src/index.css` est bien importé dans `main.tsx` :
```tsx
import './index.css';
```

### Les animations ne fonctionnent pas
Les animations Tailwind doivent être définies dans `tailwind.config.ts` (extend → keyframes → animation). Vérifier que les classes `animate-fade-in`, `animate-scale-in`, etc. y sont présentes.

## Métadonnées

- **Version :** 2.0.0
- **Dernière mise à jour :** Juillet 2026
- **Licence :** Propriétaire — StockMaster CM
