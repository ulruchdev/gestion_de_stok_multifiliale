# StockMaster CM - Design System

> **SaaS de gestion de stock multi-tenant pour PME camerounaises**
> Design System Inspired by StockMaster - Version 2.0

---

## 📌 Aperçu

StockMaster CM est un système de design complet pour une solution SaaS de gestion de stock destinée aux PME camerounaises (commerces, pharmacies, quincailleries, grossistes). Ce design system fournit toutes les fondations nécessaires pour construire des interfaces professionnelles, accessibles et adaptées aux contraintes locales.

### Caractéristiques clés

- **Mobile-first** : Optimisé pour les écrans bas de gamme (360px+)
- **Haute lisibilité** : Contraste WCAG AA minimum pour usage en plein soleil
- **Mode hors ligne** : Composants et patterns pour les coupures réseau
- **Devise XAF** : Gestion des montants entiers (pas de décimales)
- **Multi-tenant** : Architecture isolée par entreprise

---

## 🏗️ Structure du projet

```
brand-design-system-inspired-by-stockmaster-9db9b9/
├── DESIGN.md                          # Documentation complète du design system
├── README.md                          # Ce fichier
├── SKILL.md                           # Définition des compétences et usage
├── colors_and_type.css                # Tokens CSS pour couleurs et typographie
├── design-tokens.css                  # Tokens CSS complets (alternatif)
├── brand.json                         # Données structurées du design system
│
├── components/                        # Composants React
│   ├── index.jsx                      # Export centralisé
│   ├── Atomic.jsx                     # Composants atomiques de base
│   ├── Badges.jsx                     # Badges d'état et de mouvement
│   ├── Buttons.jsx                    # Système de boutons
│   ├── Cards.jsx                      # Système de cartes
│   ├── Inputs.jsx                     # Champs de formulaire
│   └── Alerts.jsx                     # Alertes et états
│
├── preview/                           # Cartes de prévisualisation
│   ├── colors.md                      # Palette de couleurs
│   ├── typography.md                 # Échelle typographique
│   ├── components.md                 # Composants visuels
│   └── layout.md                      # Grilles et breakpoints
│
├── system/                           # Système généré
│   ├── variables.css                  # Variables CSS principales
│   ├── variables.dark.css            # Variables CSS theme sombre
│   ├── tokens.default.json           # Tokens JSON (light)
│   ├── tokens.dark.json              # Tokens JSON (dark)
│   ├── tokens.compact.json          # Tokens JSON (dense)
│   ├── theme.json                    # Theme complet
│   ├── seed.json                     # Seed de génération
│   └── artifacts/                    # Templates
│       ├── landing.html
│       ├── deck.html
│       ├── poster.html
│       └── ...
│
├── *.html                            # Pages de démonstration
└── GS-DESIGN-BACKLOG-2026-01.md      # Backlog design
```

---

## 🎨 Design System

### Palette de couleurs

| Rôle | Couleur | Hex | Usage |
|------|---------|-----|-------|
| Background | White Canvas | `#ffffff` | Canvas principal |
| **Text Primary** | **Neutre** | **`#262626`** | Texte corps et titres |
| Accent | Brand Dark | `#1c1e54` | Actions principales |
| Surface | Surface | `#f5faf7` | Cartes et panneaux |
| Muted | Muted | `#93cba8` | Texte secondaire |
| Border | Border | `#e5edf5` | Bordures |
| Success | Success Green | `#15be53` | Actions secondaires |
| Error | Error | `#ff4d4f` | Erreurs |
| Warning | Warning | `#faad14` | Avertissements |
| Info | Info | `#1677ff` | Informations |

### Typographie

| Rôle | Famille | Poids | Usage |
|------|---------|-------|-------|
| **Display** | SFMono, system-ui, -apple-system, ... | 400, 700 | Titres, gros texte |
| **Body** | system-ui, -apple-system, Segoe UI, ... | 400, 700 | **Texte standard** |
| **Mono** | ui-monospace, SFMono-Regular, Menlo | 400, 700 | Codes techniques uniquement |

### Layout

- **Border Radius** : 8px (standard)
- **Border Weight** : 1px
- **Spacing** : Grille de base 8px
- **Breakpoints** : 360px (Mobile S), 768px (Mobile), 1024px (Tablet), 1280px (Desktop), 1440px (Wide)

---

## 📦 Installation et utilisation

### Utilisation dans un projet HTML

```html
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Mon Projet StockMaster CM</title>
  
  <!-- Charger les tokens CSS -->
  <link rel="stylesheet" href="colors_and_type.css">
  
  <!-- Ou utiliser les variables générées -->
  <link rel="stylesheet" href="system/variables.css">
</head>
<body>
  <!-- Votre contenu -->
</body>
</html>
```

### Utilisation dans un projet React

```jsx
// Importer les tokens CSS dans votre index.html ou App.jsx
import './colors_and_type.css';

// Utiliser les variables CSS
function MyComponent() {
  return (
    <div style={{
      backgroundColor: 'var(--bg)',
      color: 'var(--fg)',
      fontFamily: 'var(--font-body)'
    }}>
      Contenu avec le design system
    </div>
  );
}
```

### Utilisation des composants (sans bundler)

```html
<!-- Dans votre HTML -->
<script src="https://unpkg.com/react@18/umd/react.development.js"></script>
<script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
<script src="https://unpkg.com/@babel/standalone@7/babel.min.js"></script>

<!-- Charger les composants -->
<script type="text/babel" src="components/index.jsx"></script>

<!-- Utiliser -->
<script type="text/babel">
  ReactDOM.createRoot(document.getElementById('root')).render(
    <StockMaster.Button variant="primary">Valider</StockMaster.Button>
  );
</script>
```

---

## 📋 Backlog et Statut

### EPIC-D00 : Fondations Design System (23 SP)

| US | Titre | SP | Statut | Fichiers concernés |
|----|-------|----|--------|-------------------|
| US-D001 | Palette de couleurs | 3 | ✅ **Complet** | DESIGN.md, colors_and_type.css |
| US-D002 | Typographie | 2 | ✅ **Complet** | DESIGN.md, colors_and_type.css |
| US-D003 | Bibliothèque composants atomiques | 8 | ✅ **Complet** | components/*.jsx |
| US-D004 | Badges d'état commande + mouvement stock | 2 | ✅ **Complet** | components/Badges.jsx |
| US-D005 | Grilles responsive | 3 | ✅ **Complet** | DESIGN.md, colors_and_type.css |
| US-D006 | Set d'icônes (lucide-react) | 2 | ⚠️ **À faire** | components/Icons.jsx |
| US-D007 | Templates état vide/chargement/erreur | 3 | ✅ **Complet** | components/Alerts.jsx |

**Statut EPIC-D00: 21/23 SP (91%)**

---

## 🎯 Contraintes Métier à Respecter

### ❌ Anti-patterns (À éviter absolument)

#### Formulaires
1. **Jamais** de champ TTC saisissable - toujours calculé automatiquement
2. Montants toujours en **XAF entiers** (pas de décimales)
3. Motif **obligatoire** pour les corrections de stock
4. Validation en temps réel avec feedback visuel

#### États
1. **Ne pas confondre** alerte et erreur
2. **Pas de spinner infini** - toujours timeout ou message d'erreur
3. Confirmation avant toute action irréversible
4. Historique complet de toutes les modifications

#### Layout
1. **Jamais de tableau sous 768px** - utiliser des cartes sur mobile
2. **Pas de scroll horizontal** - tout doit tenir dans 360px
3. Informations critiques **toujours visibles** (stock, prix)
4. Touch targets minimum **44px** sur mobile

#### Couleurs
1. **Éviter orange/jaune purs** (confusion avec Orange Money/MTN)
2. **Max 2 accents par écran**
3. Contraste **WCAG AA minimum**

#### UX
1. **Caisse fonctionne hors ligne** - synchronisation différée
2. **Catalogue accessible hors ligne** - données en cache
3. **Stock en temps réel** sur tous les terminaux
4. Pagination adaptée au mobile

#### Technique
1. **Pas de dépendance JS pour contenu critique**
2. Gestion des erreurs réseau avec reconnexion automatique
3. Performances < 2s sur 3G
4. Compatible avec navigateurs bas de gamme

---

## 🚀 Commandes utiles

```bash
# Naviguer vers le projet
cd 'C:\Users\bobot\AppData\Roaming\Open Design\namespaces\release-stable-win\data\projects\brand-design-system-inspired-by-stockmaster-9db9b9'

# Ouvrir la documentation dans le navigateur
start DESIGN.md

# Ouvrir les démonstrations
start components-demo.html
start badge-demo.html
start design-system-demo.html

# Vérifier la cohérence des couleurs
grep -r "#262626" --include="*.css" --include="*.jsx" --include="*.html" --include="*.md" .
```

---

## 📞 Support et Contribution

### Structure de contribution

1. **Fork** le projet
2. Créez une branche (`git checkout -b feature/nouvelle-fonctionnalité`)
3. **Commitez** vos modifications (`git commit -m 'Ajout de la fonctionnalité X'`)
4. **Push** (`git push origin feature/nouvelle-fonctionnalité`)
5. Ouvrez une Pull Request

### Règles de commit

- Utilisez des messages de commit clairs et descriptifs
- Suivez la convention [Conventional Commits](https://www.conventionalcommits.org/)
- **Ne pas inclure** de co-auteur automatique dans les commits
- Exemples :
  - `feat: ajouter composant Button`
  - `fix: corriger couleur texte dans Cards.jsx`
  - `docs: mettre à jour DESIGN.md avec nouvelles règles`

### Signing off

Ce projet ne nécessite pas de signature spécifique. Les contributions sont acceptées sous la licence du projet.

---

## 📜 Licence

Ce Design System est fourni **sans licence explicite** pour usage interne. Pour toute utilisation externe, veuillez contacter l'équipe projet.

---

## 🔗 Liens utiles

- [Open Design](http://opendesign.tools)
- [StockMaster CM - Backlog Technique](GS-DESIGN-BACKLOG-2026-01.md)
- [Modèle de données](context/input-DESIGN.md)

---

*Documentation générée automatiquement - Dernière mise à jour: 2026-07-14*

## Provenance

Formalized by Open Design from candidate cc328d65-8651-4f95-a23e-30d2be879808.
