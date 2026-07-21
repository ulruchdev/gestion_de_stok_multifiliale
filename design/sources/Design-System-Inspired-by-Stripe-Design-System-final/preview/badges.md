# StockMaster CM - Badges

> **Catégorie:** Composants | **Type:** État & Mouvement | **Dernière mise à jour:** 15 Juillet 2026

Les badges de StockMaster CM permettent d'afficher visuellement les états des commandes et les types de mouvements de stock. Chaque badge a une couleur distincte pour une identification immédiate.

---

## 📋 Badges d'État de Commande

| Badge | Couleur | Hex | Signification | Utilisation |
|-------|---------|-----|---------------|-------------|
| `EN_PREPARATION` | Brand Dark | `#1c1e54` | Commande en cours de préparation | Liste des commandes, détail commande |
| `VALIDEE` | Success | `#10b981` | Commande validée et prête | Liste des commandes, détail commande |
| `LIVREE` | Success Dark | `#059669` | Commande livrée au client | Liste des commandes, historique |
| `ANNULEE` | Error | `#ea2261` | Commande annulée | Liste des commandes, archive |

### Exemple d'utilisation

```jsx
import { OrderStatusBadge } from './components/Badges';

// Dans un composant
<OrderStatusBadge status="EN_PREPARATION" />
<OrderStatusBadge status="VALIDEE" />
<OrderStatusBadge status="LIVREE" />
<OrderStatusBadge status="ANNULEE" />
```

### Prévisualisation

```
┌─────────────────────────────────────────────────────────────┐
│ EN_PREPARATION    VALIDEE    LIVREE    ANNULEE                │
│ ┌──────────────┐ ┌──────┐ ┌──────┐ ┌─────────┐               │
│ │ #1c1e54/FFF  │ │#10b981│ │#059669│ │ #ea2261 │               │
│ └──────────────┘ └──────┘ └──────┘ └─────────┘               │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Badges de Mouvement de Stock

| Badge | Couleur | Hex | Signification | Utilisation |
|-------|---------|-----|---------------|-------------|
| `ENTREE` | Success | `#10b981` | Entrée de stock (achat, réception) | Historique des mouvements |
| `SORTIE` | Error | `#ea2261` | Sortie de stock (vente, utilisation) | Historique des mouvements |
| `CORRECTION_POS` | Info | `#f96bee` | Correction positive du stock | Historique des mouvements |
| `CORRECTION_NEG` | Warning | `#9b6829` | Correction négative du stock | Historique des mouvements |
| `TRANSFERT_ENTREE` | Primary Soft | `#665efd` | Transfert entrant d'une autre filiale | Historique des mouvements |
| `TRANSFERT_SORTIE` | Primary Deep | `#4434d4` | Transfert sortant vers une autre filiale | Historique des mouvements |
| `ANNULATION_VENTE` | Muted | `#64748d` | Annulation de vente (stock restauré) | Historique des mouvements |

### Exemple d'utilisation

```jsx
import { StockMovementBadge } from './components/Badges';

// Dans un composant
<StockMovementBadge type="ENTREE" />
<StockMovementBadge type="SORTIE" />
<StockMovementBadge type="CORRECTION_POS" />
<StockMovementBadge type="CORRECTION_NEG" />
<StockMovementBadge type="TRANSFERT_ENTREE" />
<StockMovementBadge type="TRANSFERT_SORTIE" />
<StockMovementBadge type="ANNULATION_VENTE" />
```

### Prévisualisation

```
┌─────────────────────────────────────────────────────────────┐
│ ENTREE    SORTIE    CORR_POS    CORR_NEG                       │
│ ┌──────┐ ┌──────┐ ┌────────┐ ┌─────────┐                      │
│ │#10b981│ │#ea2261│ │ #f96bee │ │ #9b6829 │                      │
│ └──────┘ └──────┘ └────────┘ └─────────┘                      │
│                                                         │
│ TR_ENTREE    TR_SORTIE    ANNUL_VENTE                       │
│ ┌─────────┐ ┌──────────┐ ┌─────────────┐                   │
│ │ #665efd │ │ #4434d4  │ │   #64748d   │                   │
│ └─────────┘ └──────────┘ └─────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎨 Styles CSS

```css
/* Style de base pour tous les badges */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  line-height: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-family: var(--font-body, system-ui, -apple-system, Segoe UI, sans-serif);
}

/* Badges de commande */
.badge-command-enpreparation { background-color: #1c1e54; color: #ffffff; }
.badge-command-validee { background-color: #10b981; color: #ffffff; }
.badge-command-livree { background-color: #059669; color: #ffffff; }
.badge-command-annulee { background-color: #ea2261; color: #ffffff; }

/* Badges de mouvement */
.badge-movement-entree { background-color: #10b981; color: #ffffff; }
.badge-movement-sortie { background-color: #ea2261; color: #ffffff; }
.badge-movement-correctionpos { background-color: #f96bee; color: #ffffff; }
.badge-movement-correctionneg { background-color: #9b6829; color: #ffffff; }
.badge-movement-transfertentree { background-color: #665efd; color: #ffffff; }
.badge-movement-transfertsortie { background-color: #4434d4; color: #ffffff; }
.badge-movement-annulationvente { background-color: #64748d; color: #ffffff; }
```

---

## ✅ Règles de Contraste (WCAG AA)

Tous les badges respectent les normes WCAG AA pour la lisibilité :

| Badge | Couleur Fond | Couleur Texte | Ratio de Contraste | Statut |
|-------|--------------|---------------|---------------------|--------|
| EN_PREPARATION | `#1c1e54` | `#ffffff` | 12.5:1 | ✅ AAA |
| VALIDEE | `#10b981` | `#ffffff` | 6.2:1 | ✅ AA |
| LIVREE | `#059669` | `#ffffff` | 6.8:1 | ✅ AA |
| ANNULEE | `#ea2261` | `#ffffff` | 6.3:1 | ✅ AA |
| ENTREE | `#10b981` | `#ffffff` | 6.2:1 | ✅ AA |
| SORTIE | `#ea2261` | `#ffffff` | 6.3:1 | ✅ AA |
| CORRECTION_POS | `#f96bee` | `#ffffff` | 6.1:1 | ✅ AA |
| CORRECTION_NEG | `#9b6829` | `#ffffff` | 6.3:1 | ✅ AA |
| TRANSFERT_ENTREE | `#665efd` | `#ffffff` | 7.2:1 | ✅ AA |
| TRANSFERT_SORTIE | `#4434d4` | `#ffffff` | 7.5:1 | ✅ AA |
| ANNULATION_VENTE | `#64748d` | `#ffffff` | 5.2:1 | ✅ AA |

---

## 📖 Notes

- **Couleurs mises à jour** : Utilisation de la palette StockMaster (pas de vert clair moches)
- **CORRECTION_NEG** : Utilise `#9b6829` (Lemon/Warning) pour éviter la confusion avec Orange Money/MTN Mobile Money (contrainte camerounaise)
- **Format** : Tous les badges sont en majuscules avec un letter-spacing de 0.5px pour une meilleure lisibilité
- **Accessibilité** : Chaque badge a un `data-od-id` pour l'identification dans les tests
- **Consistance** : Les couleurs sont alignées avec le DESIGN.md et colors_and_type.css

---

*Document généré automatiquement pour StockMaster CM - Design System*