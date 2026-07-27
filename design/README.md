# 🎨 Design System — StockMaster CM

> Point d'entrée central des livrables design.
> Ce dossier contient toute la documentation, les sources d'inspiration et les règles du design system propriétaire StockMaster CM.

---

## Structure

```
design/
├── README.md                          ← Vous êtes ici
├── sources/                           ← Design system source et inspirations
│   └── Design-System-Inspired-by-*    ← Fichiers source du design system (références)
├── references/                        ← Bibliothèques de référence
│   ├── awesome-design-md.zip          ← 50+ design systems de référence
│   └── awesome-design-md-1.zip        ← (copie)
└── rules/                             ← Règles design (à venir)
```

---

## Où trouver quoi ?

| Livrable | Emplacement |
|---|---|
| **16 composants UI** (code) | `frontend/src/shared/ui/` (dont Logo.tsx) |
| **Tokens CSS** (palette, typo, espacement) | `frontend/src/index.css` |
| **Configuration Tailwind** (classes utilitaires) | `frontend/tailwind.config.ts` |
| **Guide des tokens** (documentation) | `document/01-architecture/DESIGN_TOKENS_REFERENCE.md` |
| **Backlog design** (US-D001 à D007) | `document/02-backlogs/GS-DESIGN-BACKLOG-2026-01.md` |
| **Suivi des corrections** | `document/02-backlogs/DESIGN_CORRECTIONS.md` |
| **Design system source** (HTML/CSS de référence) | `design/sources/` |
| **Bibliothèque awesome-design-md** | `design/references/awesome-design-md.zip` |
| **Rapport d'analyse design** | Voir historique conversation Freebuff |

---

## Principe

> **Le code = le design.** Puisque nous ne passons pas par Figma, le design system vit directement dans le code React.
>
> - Les **tokens** sont dans `index.css` (variables CSS)
> - Les **composants** sont dans `shared/ui/` (React + Tailwind)
> - Les **règles** sont documentées ici et dans `document/`

### Design system propriétaire

Bien que le design system s'inspire des meilleures pratiques de l'industrie (notamment via awesome-design-md), **StockMaster CM a son propre design system propriétaire** :
- Palette indigo professionnelle adaptée au contexte africain
- Composants optimisés pour les coupures réseau (OfflineBanner, Skeleton)
- Typographie lisible sur écrans mobiles bas de gamme
- Gestion de la devise XAF (chiffres tabulaires, pas de décimales)
- Évitement des couleurs jaune/orange (conflit avec Orange Money/MTN Mobile Money)

---

## Pour les développeurs

### Démarrer le frontend

```bash
cd frontend && npm install && npm run dev
```

### Utiliser les composants

```tsx
import { Button, Card, Input, DataTable, ToastContainer } from '@/shared/ui';
```

### Modifier les tokens

Éditer `frontend/src/index.css` — les variables CSS sont automatiquement chargées par Tailwind.

### Ajouter un composant

1. Créer le fichier dans `frontend/src/shared/ui/MonComposant.tsx`
2. L'exporter dans `frontend/src/shared/ui/index.ts`
3. Builder avec `npm run build` pour vérifier

---

## Versions

| Version | Date | Changements |
|---------|------|-------------|
| 1.1.0 | Juillet 2026 | Ajout Logo.tsx — 4 variantes, 3 tailles, intégration dans 7 pages/layouts |
| 1.0.0 | Juillet 2026 | Design system initial — 15 composants, tokens Stripe-inspired, EPIC-D00 terminé |

---

*Dernière mise à jour : Juillet 2026*
