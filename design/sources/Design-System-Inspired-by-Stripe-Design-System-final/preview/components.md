# StockMaster CM - Atomic Components

> **Catégorie:** Composants | **Type:** Atomiques | **Dernière mise à jour:** 15 Juillet 2026

Bibliothèque complète des composants atomiques de StockMaster CM, conçus pour être réutilisables, accessibles et cohérents avec le design system inspiré de StockMaster.

---

## 📝 Typography Components

Composants de typographie de base pour structurer le contenu textuel.

### Composants Disponibles

| Composant | HTML Tag | Usage | Taille | Poids |
|-----------|----------|-------|-------|-------|
| `H1` | `<h1>` | Titre principal de page | 28-40px | 700 |
| `H2` | `<h2>` | Titre de section | 24-32px | 600 |
| `H3` | `<h3>` | Sous-titre | 20px | 600 |
| `Text` | `<p>` | Texte corps | 16px | 400 |
| `Code` | `<code>` | Code, valeurs techniques | 12px | 400 |

### Famille de polices
- **Display:** `"SF Pro Display", system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif`
- **Body:** `system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif`
- **Mono:** `ui-monospace, SFMono-Regular, Menlo, monospace`

### Exemple d'utilisation

```jsx
import { H1, H2, H3, Text, Code } from './components/Atomic';

<H1>Titre Principal</H1>
<H2>Titre de Section</H2>
<H3>Sous-titre</H3>
<Text>Paragraphe de texte standard avec typographie body.</Text>
<Code>code_article: "ART-001"</Code>
```

---

## 🎨 Layout Components

Composants utilitaires pour la mise en page et l'espacement.

### Composants Disponibles

| Composant | Description | Props |
|-----------|-------------|-------|
| `Divider` | Ligne horizontale de séparation | `className` |
| `Spacer` | Espaceur vertical | `size` (xs, sm, md, lg, xl) |
| `Container` | Container responsive | `fluid`, `className` |

### Tailles Spacer

| Size | Valeur CSS | Utilisation |
|------|------------|-------------|
| `xs` | 12px | Espacement très petit |
| `sm` | 16px | Espacement petit |
| `md` | 24px | Espacement standard |
| `lg` | 32px | Espacement grand |
| `xl` | 48px | Espacement très grand |

### Exemple d'utilisation

```jsx
import { Divider, Spacer, Container } from './components/Atomic';

<Container>
  <H1>Titre</H1>
  <Spacer size="md" />
  <Text>Contenu...</Text>
  <Spacer size="lg" />
  <Divider />
  <Spacer size="md" />
  <Text>Autre section</Text>
</Container>
```

---

## 🏷️ Badge Components

Badges pour afficher les états des commandes et mouvements de stock.

### Order Status Badges

| Badge | Couleur | Hex | État |
|-------|---------|-----|------|
| `OrderStatusBadge` | Brand Dark | `#1c1e54` | EN_PREPARATION |
| `OrderStatusBadge` | Success | `#10b981` | VALIDEE |
| `OrderStatusBadge` | Success Dark | `#059669` | LIVREE |
| `OrderStatusBadge` | Ruby/Error | `#ea2261` | ANNULEE |

### Stock Movement Badges

| Badge | Couleur | Hex | Type |
|-------|---------|-----|------|
| `StockMovementBadge` | Success | `#10b981` | ENTREE |
| `StockMovementBadge` | Ruby/Error | `#ea2261` | SORTIE |
| `StockMovementBadge` | Magenta/Info | `#f96bee` | CORRECTION_POS |
| `StockMovementBadge` | Lemon/Warning | `#9b6829` | CORRECTION_NEG |
| `StockMovementBadge` | Primary Soft | `#665efd` | TRANSFERT_ENTREE |
| `StockMovementBadge` | Primary Deep | `#4434d4` | TRANSFERT_SORTIE |
| `StockMovementBadge` | Muted | `#64748d` | ANNULATION_VENTE |

### Exemple d'utilisation

```jsx
import { OrderStatusBadge, StockMovementBadge } from './components/Badges';

// États de commande
<OrderStatusBadge status="EN_PREPARATION" />
<OrderStatusBadge status="VALIDEE" />

// Mouvements de stock
<StockMovementBadge type="ENTREE" />
<StockMovementBadge type="SORTIE" />
```

---

## 🔘 Button Components

Boutons atomiques avec plusieurs variantes et tailles.

### Variantes

| Variante | Description | Couleur | Usage |
|----------|-------------|--------|-------|
| `primary` | Bouton principal | Primary (`#533afd`) | Actions principales |
| `secondary` | Bouton secondaire | Surface + Border | Actions secondaires |
| `ghost` | Bouton fantôme | Transparent | Actions discrètes |
| `danger` | Bouton dangereux | Ruby/Error (`#ea2261`) | Actions destructives |
| `icon` | Bouton icône | Transparent | Actions avec icône seule |

### Tailles

| Taille | Padding | Min-height | Utilisation |
|--------|---------|------------|-------------|
| `sm` | 4px 12px | 32px | Boutons compacts |
| `md` | 8px 16px | 40px | Taille par défaut |
| `lg` | 12px 24px | 48px | Boutons grands |

### Props

| Prop | Type | Default | Description |
|------|------|--------|-------------|
| `children` | node | - | Contenu du bouton |
| `onClick` | function | - | Handler de clic |
| `disabled` | boolean | false | État désactivé |
| `type` | string | 'button' | Type du bouton |
| `variant` | string | 'primary' | Variante |
| `size` | string | 'md' | Taille |
| `className` | string | '' | Classes additionnelles |

### Exemple d'utilisation

```jsx
import { Button, IconButton, ButtonGroup } from './components/Buttons';
import { PlusIcon, TrashIcon } from './components/Icons';

// Boutons standards
<Button variant="primary" size="md">Enregistrer</Button>
<Button variant="secondary" disabled>Annuler</Button>
<Button variant="danger" size="sm">Supprimer</Button>

// Boutons avec icônes
<IconButton icon={PlusIcon} label="Ajouter" variant="primary" />
<IconButton icon={TrashIcon} label="Supprimer" variant="danger" />

// Groupe de boutons
<ButtonGroup>
  <Button variant="secondary">Précédent</Button>
  <Button variant="primary">Suivant</Button>
</ButtonGroup>
```

---

## 📥 Input Components

Composants de saisie pour les formulaires.

### TextInput

Champ de texte standard.

**Props:**
| Prop | Type | Default | Description |
|------|------|--------|-------------|
| `type` | string | 'text' | Type de l'input |
| `value` | string/number | '' | Valeur |
| `onChange` | function | - | Handler de changement |
| `placeholder` | string | '' | Placeholder |
| `disabled` | boolean | false | Désactivé |
| `error` | boolean | false | État d'erreur |
| `label` | string | '' | Libellé |

### AmountInput

Champ de saisie pour les montants XAF (entiers uniquement).

**Props:** Même que TextInput + `formatter` optionnel

### SelectInput

Menu déroulant.

**Props:**
| Prop | Type | Default | Description |
|------|------|--------|-------------|
| `options` | array | [] | Liste d'options |
| `value` | string | '' | Valeur sélectionnée |
| `onChange` | function | - | Handler de changement |

### Textarea

Zone de texte multi-lignes.

**Props:** Même que TextInput + `rows`

### Exemple d'utilisation

```jsx
import { TextInput, AmountInput, SelectInput, Textarea } from './components/Inputs';

<TextInput
  label="Désignation"
  placeholder="Nom de l'article"
  value={designation}
  onChange={(e) => setDesignation(e.target.value)}
/>

<AmountInput
  label="Prix HT"
  placeholder="0"
  value={prixHT}
  onChange={(e) => setPrixHT(parseInt(e.target.value) || 0)}
/>

<SelectInput
  label="Catégorie"
  options={[
    { value: 'alimentaire', label: 'Alimentaire' },
    { value: 'pharmacie', label: 'Pharmacie' }
  ]}
  value={categorie}
  onChange={(e) => setCategorie(e.target.value)}
/>

<Textarea
  label="Description"
  placeholder="Description détaillée..."
  rows={4}
/>
```

---

## 🃏 Card Components

Cartes pour regrouper et présenter du contenu.

### Variantes

| Variante | Description | Style |
|----------|-------------|-------|
| `standard` | Carte standard | Background surface, border 1px |
| `featured` | Carte mise en avant | Background accent, texte blanc |
| `minimal` | Carte minimale | Pas de bordure |

### Padding

| Padding | Valeur | Utilisation |
|---------|--------|-------------|
| `none` | 0px | Sans padding |
| `xs` | 8px | Padding très petit |
| `sm` | 12px | Padding petit |
| `md` | 16px | Padding standard |
| `lg` | 24px | Padding grand |
| `xxl` | 32px | Padding très grand |

### Composants

| Composant | Description |
|-----------|-------------|
| `Card` | Container principal de la carte |
| `CardHeader` | En-tête de la carte (titre + actions) |
| `CardBody` | Corps de la carte (contenu principal) |
| `CardFooter` | Pied de page de la carte (actions) |

### Exemple d'utilisation

```jsx
import { Card, CardHeader, CardBody, CardFooter } from './components/Cards';

<Card variant="standard" padding="md">
  <CardHeader>
    <span>Article: ART-001</span>
    <OrderStatusBadge status="VALIDEE" />
  </CardHeader>
  <CardBody>
    <Text>Riz Basmati 1kg</Text>
    <Text>Stock: 50 unités</Text>
  </CardBody>
  <CardFooter>
    <Button variant="primary" size="sm">Éditer</Button>
  </CardFooter>
</Card>

<Card variant="featured" padding="lg">
  <CardHeader>
    <span style={{color: '#fff'}}>⚠️ Stock Faible</span>
  </CardHeader>
  <CardBody>
    <Text style={{color: '#fff'}}>Seulement 5 unités restantes</Text>
  </CardBody>
</Card>
```

---

## ⚠️ Alert Components

Composants de feedback pour les notifications et états.

### Composants

| Composant | Type | Description |
|-----------|------|-------------|
| `Alert` | Permanent | Notification permanente |
| `Toast` | Temporaire | Notification qui disparaît |
| `EmptyState` | État vide | Affiche quand aucune donnée |
| `Skeleton` | Chargement | Placeholder de chargement |
| `OfflineState` | Hors ligne | Mode hors ligne |
| `LoadingState` | Chargement | État de chargement global |

### Alert Types

| Type | Couleur | Usage |
|------|---------|-------|
| `success` | Success (`#10b981`) | Opérations réussies |
| `warning` | Lemon (`#9b6829`) | Avertissements |
| `error` | Ruby (`#ea2261`) | Erreurs |
| `info` | Magenta (`#f96bee`) | Informations |

### Exemple d'utilisation

```jsx
import { Alert, Toast, EmptyState, Skeleton, OfflineState, LoadingState } from './components/Alerts';

// Alertes
<Alert type="success" title="Succès" message="Article ajouté avec succès" />
<Alert type="error" title="Erreur" message="Impossible de supprimer" />

// Toast (auto-disparition)
<Toast type="success" message="Enregistré" autoClose={3000} />

// État vide
<EmptyState
  icon={<PackageIcon size={48} />}
  title="Aucun article"
  message="Commencez par ajouter un article"
  action={<Button variant="primary">Ajouter</Button>}
/>

// Skeleton
<Skeleton variant="text" width="100%" height="20px" />
<Skeleton variant="card" width="200px" height="150px" />

// Offline
<OfflineState message="Hors ligne - Synchronisation en attente" />

// Loading
<LoadingState message="Chargement..." />
```

---

## 📊 Table Components

Composants de tableau responsive.

### Composants

| Composant | Description |
|-----------|-------------|
| `Table` | Table desktop standard |
| `MobileTable` | Table responsive (cartes sur mobile) |
| `ArticlesTable` | Table spécialisée pour les articles |
| `CommandesTable` | Table spécialisée pour les commandes |
| `MouvementsStockTable` | Table spécialisée pour les mouvements |

### Fonctions utilitaires

| Fonction | Description |
|----------|-------------|
| `formatXAF(amount)` | Formate un montant en XAF |
| `formatDate(date)` | Formate une date |
| `formatDateTime(date)` | Formate une date avec heure |

### Exemple d'utilisation

```jsx
import { Table, ArticlesTable, formatXAF, formatDate } from './components/Tables';

// Table générique
<Table
  columns={[
    { key: 'code', header: 'Code' },
    { key: 'designation', header: 'Désignation' },
    { key: 'stock', header: 'Stock' }
  ]}
  data={articles}
  keyField="id"
/>

// Table articles
<ArticlesTable
  articles={articles}
  onEdit={handleEdit}
  onDelete={handleDelete}
/>

// Utilisation des formatters
<Text>{formatXAF(2500)}</Text>  // Affiche: 2 500 XAF
<Text>{formatDate(new Date())}</Text>  // Affiche: 15/07/2026
```

---

## 🎯 Hooks Utilitaires

Hooks personnalisés pour des fonctionnalités courantes.

### useBreakpoint

Détecte le breakpoint actuel.

**Retour:** `'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl'`

```jsx
import { useBreakpoint } from './components/Atomic';

function MyComponent() {
  const breakpoint = useBreakpoint();
  
  return (
    <div>
      {breakpoint === 'sm' && <MobileNav />}
      {breakpoint >= 'md' && <DesktopNav />}
    </div>
  );
}
```

### useLoading

Gère les états de chargement.

**Retour:** `{ isLoading: boolean, startLoading: function, stopLoading: function }`

```jsx
import { useLoading } from './components/Atomic';

function SaveButton() {
  const { isLoading, startLoading, stopLoading } = useLoading();
  
  const handleSave = async () => {
    startLoading();
    try {
      await saveData();
    } finally {
      stopLoading();
    }
  };
  
  return (
    <Button onClick={handleSave} disabled={isLoading}>
      {isLoading ? 'Enregistrement...' : 'Enregistrer'}
    </Button>
  );
}
```

### injectKeyframes

Injecte les keyframes CSS nécessaires pour les animations.

```jsx
import { injectKeyframes } from './components/Atomic';

// Appeler une fois au chargement de l'application
injectKeyframes();
```

---

## 📋 Résumé des Composants

| Catégorie | Composants | Nombre | Statut |
|----------|------------|--------|--------|
| Typography | H1, H2, H3, Text, Code | 5 | ✅ |
| Layout | Divider, Spacer, Container | 3 | ✅ |
| Badges | OrderStatusBadge, StockMovementBadge | 2 | ✅ |
| Buttons | Button, IconButton, ButtonGroup | 3 | ✅ |
| Inputs | TextInput, AmountInput, SelectInput, Textarea | 4 | ✅ |
| Cards | Card, CardHeader, CardBody, CardFooter | 4 | ✅ |
| Alerts | Alert, Toast, EmptyState, Skeleton, OfflineState, LoadingState | 6 | ✅ |
| Tables | Table, MobileTable, ArticlesTable, CommandesTable, MouvementsStockTable | 5 | ✅ |
| Hooks | useBreakpoint, useLoading, injectKeyframes | 3 | ✅ |

**Total: 35 composants atomiques**

---

## 🔗 Intégration

### Avec Babel Standalone (pour démos)

```html
<!-- Dans le <head> -->
<script src="https://unpkg.com/react@18/umd/react.development.js"></script>
<script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
<script src="https://unpkg.com/@babel/standalone@7/babel.min.js"></script>
<script src="https://unpkg.com/lucide@latest/dist/umd/lucide.js"></script>

<!-- Charger les composants -->
<script type="text/babel" src="components/Atomic.jsx"></script>
<script type="text/babel" src="components/Badges.jsx"></script>
<script type="text/babel" src="components/Buttons.jsx"></script>
<!-- ... autres composants ... -->

<!-- Utiliser dans le body -->
<script type="text/babel">
  ReactDOM.createRoot(document.getElementById('root')).render(
    <H1>Bonjour StockMaster CM</H1>
  );
</script>
```

### Avec un Bundler (pour production)

```javascript
// Import des composants
import { Button, TextInput, Card, H1 } from './components';

// Utilisation dans un composant React
function MyComponent() {
  return (
    <Card>
      <H1>Titre</H1>
      <TextInput label="Nom" />
      <Button variant="primary">Enregistrer</Button>
    </Card>
  );
}
```

---

*Documentation générée automatiquement à partir du Design System StockMaster CM - EPIC-D00*