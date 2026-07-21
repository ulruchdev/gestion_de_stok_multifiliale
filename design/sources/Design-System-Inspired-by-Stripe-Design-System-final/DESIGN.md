---
name: "StockMaster CM Design System"
category: Brands
surface: web
colors:
  canvas: "#ffffff"
  ink: "#0d253d"
  primary: "#533afd"
  canvas-soft: "#f6f9fc"
  ink-mute: "#64748d"
  hairline: "#e3e8ee"
  brand-dark-900: "#1c1e54"
---

# StockMaster CM Design System

> Category: Brands

> Surface: web

*Design épuré, typographie lisible, composants pensés pour le contexte africain.*

**Domaine :** Logiciel métier > Gestion de stock & inventaire multi-sites. Palette indigo professionnelle avec tons neutres dominants, typographie système pour une lisibilité optimale sur mobile, composants adaptés aux contraintes réseau du contexte camerounais (hors ligne, reconnexion automatique).

## Palette de couleurs

| Rôle | Nom | Hex | Usage |
| --- | --- | --- | --- |
| fond | Canvas | `#ffffff` | fond de page |
| texte | Ink | `#0d253d` | texte principal et titres |
| action | Primary | `#533afd` | actions principales et CTA |
| surface | Canvas Soft | `#f6f9fc` | cartes et panneaux |
| atténué | Ink Mute | `#64748d` | texte secondaire et métadonnées |
| bordure | Hairline | `#e3e8ee` | règles et séparateurs |
| sidebar | Brand Dark 900 | `#1c1e54` | barre latérale, navigation |

## Typographie

- **Affichage :** SF Pro Display — graisses 500, 600, 700 — fallbacks: system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif
- **Corps :** system-ui — graisses 400, 500 — fallbacks: -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif
- **Monospace :** ui-monospace — graisses 400, 700 — fallbacks: SFMono-Regular, Menlo, Consolas, monospace
- **Chiffres :** tabulaires activés (`tnum`) pour montants XAF et quantités stock

## Voix & Ton

- **Adjectifs :** Professionnel, fiable, moderne, clair, efficace, accessible, rassurant
- **Ton :** Design épuré et professionnel avec palette indigo sobre. Hiérarchie visuelle claire adaptée à une utilisation mobile intensive. Composants optimisés pour la fiabilité et la lisibilité en contexte africain (plein soleil, connexion intermittente).

### Piliers de communication

- Palette indigo professionnelle sobre (tons neutres dominants)
- Typographie système lisible, optimisée pour les écrans mobiles
- Boutons aux coins arrondis avec hiérarchie CTA claire (indigo → blanc → outline)
- Chiffres tabulaires pour montants XAF et données de stock
- Composants résilients : état hors ligne, reconnexion, chargement optimiste

### Vocabulaire

- **À utiliser :** Stock, inventaire, produit, article, filiale, entrepôt, transfert, commande, fournisseur, client, vente, caisse, facture, alerte, seuil, rupture, mouvement, catalogue, catégorie, gestion, pilotage, dashboard
- **À éviter :** Jargon technique excessif, termes abstraits, formulations négatives sur les difficultés du métier

## Imagerie

- **Style :** Captures d'écran d'interface, tableaux de bord, fiches produits, graphiques d'évolution
- **Sujets :** Interfaces de gestion de stock, catalogue produits, tableaux de bord, alertes de stock, suivi de commandes
- **Traitement :** Fonds sobres, ombres subtiles, mise en avant des données (chiffres, graphiques, tableaux)
- **À éviter :** Éléments décoratifs superflus, motifs complexes, imagerie non liée au produit

## Layout

- **Rayons :** 4px (badges), 6px (inputs), 8px (cartes), 12px (modales), 9999px (boutons arrondis)
- **Épaisseur bordure :** 1px
- **Espacement :** Grille 8px (2, 4, 8, 12, 16, 24, 32, 48, 64px)

### Règles de posture

- Pages dashboard centrées dans conteneur ~1400px avec sidebar fixe à gauche
- Tableaux de données responsifs : colonnes réduites sur mobile (<768px), scroll horizontal si nécessaire
- Cartes du dashboard : grille 3 colonnes (desktop) → 2 colonnes (tablette) → 1 colonne (mobile)
- Padding sections : 32-48px dashboard, 32px pour les fiches
- Padding interne cartes : 24px pour les fiches produit, 16px pour les listes denses
