# StockMaster CM Design System — Guide

Design system propriétaire de StockMaster CM, conçu pour la gestion de stock multi-sites des PME camerounaises.

## Palette de couleurs

- **Canvas** `#ffffff` — fond de page
- **Ink** `#0d253d` — texte principal
- **Primary** `#533afd` — actions
- **Canvas Soft** `#f6f9fc` — fond cartes
- **Ink Mute** `#64748b` — texte secondaire
- **Hairline** `#e3e8ee` — bordures
- **Brand Dark 900** `#1c1e54` — sidebar / navigation

### Rôles des couleurs

| Rôle | Token | Réservé à |
|------|-------|-----------|
| Fond principal | `--background` | Toute surface de page |
| Texte principal | `--foreground` | Tout texte de corps, titres |
| Action / CTA | `--primary` | Boutons, liens, éléments interactifs |
| Fond secondaire | `--card` / `--secondary` | Cartes, panneaux, sections |
| Texte atténué | `--muted-foreground` | Métadonnées, libellés, textes d'aide |
| Bordures | `--border` / `--hairline` | Séparateurs, cadres |
| Alerte stock | `--stock-warning` | Seuils d'alerte, stocks bas |
| Rupture stock | `--stock-danger` | Stocks négatifs, ruptures |
| Stock OK | `--stock-success` | Stocks suffisants |

## Typographie

- **Affichage :** SF Pro Display (titres) — graisses 500, 600, 700
- **Corps :** system-ui (15px) — graisse 400
- **Monospace :** ui-monospace (codes, données financières)

## Espacement

Grille de base 8px. Paddings : 16px standard, 24px large, 32px sections.

## Épaisseurs et rayons

- Bordures : 1px
- Coins : 6px (standard), 12px (modales), 9999px (boutons arrondis)
