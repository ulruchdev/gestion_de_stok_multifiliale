# StockMaster CM - Spacing System

> **Catégorie:** Design Tokens | **Type:** Espacement | **Dernière mise à jour:** 15 Juillet 2026

Système d'espacement de StockMaster CM basé sur une **grille de base de 8px**, inspiré de StockMaster et adapté pour une expérience mobile-first.

---

## 📏 Baseline Grid (8px)

Tous les espacements sont des multiples de **8px** pour assurer la cohérence et l'alignement parfait entre les éléments.

### Échelle Complète des Espacements

| Token | Valeur | Usage | Multiplicateur |
|-------|--------|-------|----------------|
| `--space-0` | 0px | Reset | 0×8 |
| `--space-2` | 2px | Micro-espacement | 0.25×8 |
| **`--space-4`** | **4px** | Espacement très petit | 0.5×8 |
| **`--space-8`** | **8px** | **Baseline - Espacement standard** | 1×8 |
| `--space-12` | 12px | Espacement moyen | 1.5×8 |
| **`--space-16`** | **16px** | Espacement grand | 2×8 |
| `--space-20` | 20px | Espacement étendu | 2.5×8 |
| `--space-24` | 24px | Espacement très grand | 3×8 |
| `--space-28` | 28px | Espacement large | 3.5×8 |
| `--space-32` | 32px | Espacement extra-large | 4×8 |
| `--space-36` | 36px | Espacement XL | 4.5×8 |
| `--space-40` | 40px | Espacement XXL | 5×8 |
| `--space-44` | 44px | Touch target minimum | 5.5×8 |
| `--space-48` | 48px | Espacement maximum | 6×8 |

### Espacements Négatifs

| Token | Valeur | Usage |
|-------|--------|-------|
| `--space-neg-2` | -2px | Correction micro |
| `--space-neg-4` | -4px | Correction petite |
| `--space-neg-8` | -8px | Correction standard |
| `--space-neg-12` | -12px | Correction moyenne |
| `--space-neg-16` | -16px | Correction grande |

### Alias pour Compatibilité

```css
--spacing-xs: var(--space-4);     /* 4px */
--spacing-sm: var(--space-8);     /* 8px */
--spacing-md: var(--space-12);    /* 12px */
--spacing-lg: var(--space-16);    /* 16px */
--spacing-xl: var(--space-24);    /* 24px */
--spacing-2xl: var(--space-32);   /* 32px */
--spacing-3xl: var(--space-48);   /* 48px */
```

---

## 🎨 Prévisualisation des Espacements

```
┌─────────────────────────────────────────────────────────────┐
│ ESPACEMENTS STOCKMASTER CM                                       │
├─────────────────────────────────────────────────────────────┤
│ ┌───┐    ┌─────┐    ┌───────┐    ┌─────────┐    ┌───────────┐ │
│ │2px│    │ 4px │    │  8px  │    │  12px   │    │   16px   │ │
│ └───┘    └─────┘    └───────┘    └─────────┘    └───────────┘ │
│                                                                 │
│ ┌───────────┐    ┌─────────────┐    ┌───────────────┐              │
│ │   20px    │    │    24px     │    │     32px      │              │
│ └───────────┘    └─────────────┘    └───────────────┘              │
│                                                                 │
│ ┌─────────────────┐    ┌───────────────────┐                │
│ │       40px        │    │        48px        │                │
│ └─────────────────┘    └───────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

---

## 📐 Usage par Composant

### Cartes (Cards)

| Élément | Espacement | Token |
|---------|------------|-------|
| Padding interne | 16px | `--space-16` |
| Gap entre éléments | 12px | `--space-12` |
| Marge externe | 16px | `--space-16` |

### Boutons (Buttons)

| Élément | Espacement | Token |
|---------|------------|-------|
| Padding vertical (sm) | 4px | `--space-4` |
| Padding vertical (md) | 8px | `--space-8` |
| Padding vertical (lg) | 12px | `--space-12` |
| Padding horizontal | 12px-24px | `--space-12` à `--space-24` |
| Gap dans ButtonGroup | 8px | `--space-8` |

### Formulaires (Forms)

| Élément | Espacement | Token |
|---------|------------|-------|
| Gap entre champs | 12px | `--space-12` |
| Gap label → input | 4px | `--space-4` |
| Marge section | 24px | `--space-24` |

### Listes (Lists)

| Élément | Espacement | Token |
|---------|------------|-------|
| Gap entre items | 8px | `--space-8` |
| Padding item | 12px | `--space-12` |

### Layouts

| Élément | Espacement | Token |
|---------|------------|-------|
| Container padding | 16px | `--space-16` |
| Section gap | 24px | `--space-24` |
| Page padding | 16px | `--space-16` |

---

## 📱 Touch Targets (Mobile)

**Règle critique :** Tous les éléments interactifs doivent avoir une **taille minimale de 44×44px** pour une bonne expérience tactile.

| Élément | Taille Minimum | Espacement Recommandé |
|---------|----------------|------------------------|
| Boutons | 44×44px | 8px entre boutons |
| Inputs | 44px hauteur | 12px margin verticale |
| Cases à cocher | 44×44px | 8px entre cases |
| Liens | 44px hauteur | 16px padding |

```css
/* Exemple de bouton mobile-friendly */
.button {
  min-height: var(--space-44, 44px);
  min-width: var(--space-44, 44px);
  padding: var(--space-8, 8px) var(--space-16, 16px);
}
```

---

## 🎯 Règles d'Espacement

### 1. **Grille de base 8px**
- TOUS les espacements doivent être des multiples de 8px
- Exception: les micro-espacements (2px, 4px) pour les ajustements fins

### 2. **Hiérarchie visuelle**
- **12px** : Séparation entre éléments dans un groupe
- **16px** : Séparation entre groupes d'éléments
- **24px** : Séparation entre sections distinctes
- **32px+** : Séparation entre composants majeurs

### 3. **Consistance**
- Utiliser les **mêmes tokens** pour les mêmes types d'espacement
- Éviter les valeurs arbitraires (ex: 13px, 17px)
- Préférer les **alias** (`--spacing-sm`, `--spacing-md`) pour la lisibilité

### 4. **Mobile-first**
- Commencer par les valeurs mobiles
- Augmenter progressivement pour desktop
- Toujours tester sur **360px**

---

## 📊 Résumé

| Type | Valeurs | Usage |
|------|---------|-------|
| **Base** | 8px | Grille de référence |
| **Standard** | 8px, 12px, 16px | 90% des cas |
| **Large** | 24px, 32px, 48px | Séparations majeures |
| **Micro** | 2px, 4px | Ajustements fins |
| **Touch** | 44px minimum | Éléments interactifs |

**Total: 15 valeurs d'espacement + 5 négatifs + 6 alias**

---

## ✅ Vérification WCAG

- ✅ Tous les touch targets ≥ 44px (WCAG 2.5.5)
- ✅ Espacement suffisant entre éléments interactifs
- ✅ Contraste maintenu avec les couleurs de fond

---

*Documentation générée pour StockMaster CM - Design System - EPIC-D00*
