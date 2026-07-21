# StockMaster CM - Layout System

> **Catégorie:** Design Tokens | **Type:** Mise en page | **Dernière mise à jour:** 15 Juillet 2026

Système de layout complet de StockMaster CM, conçus pour une expérience responsive mobile-first, adaptée au contexte camerounais.

---

## 📐 Grid System

Système de grille basé sur 12 colonnes avec approche mobile-first.

### Breakpoints Responsives

| Breakpoint | Width Min | Max Container | Usage |
|------------|-----------|---------------|-------|
| `xs` | 0px | 100% | Mobile très petit (360px) |
| `sm` | 360px | 100% | Mobile standard |
| `md` | 480px | 100% | Mobile large |
| `lg` | 768px | 100% | Tablette portrait |
| `xl` | 1024px | 1200px | Tablette landscape / Desktop |
| `2xl` | 1280px | 1200px | Desktop large |

### Container Widths

```css
.container {
  width: 100%;
  padding-left: 16px;
  padding-right: 16px;
  margin-left: auto;
  margin-right: auto;
  max-width: 1200px; /* @xl et @2xl */
}
```

### Usage du Container

```jsx
import { Container } from './components/Atomic';

function Page() {
  return (
    <Container>
      {/* Votre contenu ici */}
    </Container>
  );
}
```

---

## 📏 Spacing System

Système d'espacement basé sur une grille de **8px**.

### Espacements Disponibles

| Token | Valeur | Usage |
|-------|--------|-------|
| `--space-xxs` | 2px | Micro-espacement |
| `--space-xs` | 4px | Espacement très petit |
| `--space-sm` | 8px | Espacement petit |
| `--space-md` | 12px | Espacement standard |
| `--space-lg` | 16px | Espacement moyen |
| `--space-xl` | 24px | Espacement grand |
| `--space-2xl` | 32px | Espacement très grand |
| `--space-3xl` | 48px | Espacement maximum |

### Échelle Complète

```css
:root {
  --space-xxs: 2px;
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 24px;
  --space-2xl: 32px;
  --space-3xl: 48px;
}
```

### Utilisation

```jsx
import { Spacer } from './components/Atomic';

// Avec le composant Spacer
<div>
  <H1>Titre</H1>
  <Spacer size="md" />  {/* 12px */}
  <Text>Contenu</Text>
  <Spacer size="xl" />  {/* 24px */}
  <Button>Action</Button>
</div>

// Avec des styles CSS
<div style={{ marginTop: 'var(--space-lg, 16px)' }}>
  {/* Contenu */}
</div>
```

---

## 🔳 Border System

Système de bordures cohérent.

### Tokens de Bordure

| Token | Valeur | Usage |
|-------|--------|-------|
| `--border-weight` | 1px | Épaisseur standard |
| `--border-style` | solid | Style standard |
| `--border` | `#e3e8ee` | Couleur par défaut |
| `--radius-sm` | 4px | Rayon petit (boutons) |
| `--radius-md` | 8px | Rayon standard (cartes) |
| `--radius-lg` | 12px | Rayon grand (modales) |

### Border Radius Scale

```css
:root {
  --radius-sm: 4px;    /* Boutons, inputs */
  --radius-md: 8px;    /* Cartes, alertes */
  --radius-lg: 12px;   /* Modales, dialogs */
}
```

### Utilisation

```css
/* Boutons */
.button {
  border-radius: var(--radius-sm, 4px);
}

/* Cartes */
.card {
  border-radius: var(--radius-md, 8px);
}

/* Inputs */
.input {
  border-radius: var(--radius-sm, 4px);
}
```

---

## 📱 Responsive Design

Stratégie mobile-first avec media queries.

### Breakpoints CSS

```css
/* Mobile-first: commence par mobile, puis override pour desktop */

/* >= 360px */
@media (min-width: 360px) {
  /* Styles pour mobile standard */
}

/* >= 480px */
@media (min-width: 480px) {
  /* Styles pour mobile large */
}

/* >= 768px */
@media (min-width: 768px) {
  /* Styles pour tablette */
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* >= 1024px */
@media (min-width: 1024px) {
  /* Styles pour desktop */
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* >= 1280px */
@media (min-width: 1280px) {
  /* Styles pour grand écran */
}
```

### Exemple de composant responsive

```jsx
function ResponsiveCard() {
  const breakpoint = useBreakpoint();
  
  return (
    <Card
      padding={breakpoint === 'xs' ? 'sm' : 'md'}
      variant={breakpoint === 'xs' ? 'minimal' : 'standard'}
    >
      {breakpoint >= 'lg' && (
        <CardHeader>En-tête visible sur desktop</CardHeader>
      )}
      <CardBody>
        <Text>Contenu principal</Text>
      </CardBody>
    </Card>
  );
}
```

---

## 🖥️ Table Layout (Desktop)

Règles spécifiques pour les tableaux sur desktop.

### Principes

1. **Jamais de tableau sous 768px** - Toujours utiliser MobileTable ou une alternative
2. **Scroll horizontal** - Les tableaux doivent être dans un container avec overflow-x: auto
3. **Densité contrôlée** - Maximum 8 colonnes visibles sans scroll
4. **Headers fixes** - Les en-têtes restent visibles lors du scroll vertical

### Structure de base

```jsx
<div className="table-container" style={{ overflowX: 'auto' }}>
  <Table
    columns={columns}
    data={data}
    keyField="id"
  />
</div>
```

### MobileTable Alternative

Pour les petits écrans, utiliser MobileTable qui affiche les données sous forme de cartes.

```jsx
<MobileTable
  columns={columns}
  data={data}
  keyField="id"
/>
```

---

## 📐 Grid Layout

Utilisation de CSS Grid pour les mises en page complexes.

### Grille standard

```css
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-md, 12px);
}

/* Sur mobile: 1 colonne */
@media (max-width: 767px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

/* Sur tablette: 2 colonnes */
@media (min-width: 768px) and (max-width: 1023px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Sur desktop: 3-4 colonnes */
@media (min-width: 1024px) {
  .grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
}
```

### Exemple avec les composants

```jsx
import { Container, Spacer } from './components/Atomic';
import { Card } from './components/Cards';

function Dashboard() {
  return (
    <Container>
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: 'var(--space-md, 12px)'
      }}>
        <Card variant="standard">
          <Text>Carte 1</Text>
        </Card>
        <Card variant="standard">
          <Text>Carte 2</Text>
        </Card>
        <Card variant="standard">
          <Text>Carte 3</Text>
        </Card>
      </div>
    </Container>
  );
}
```

---

## 🎨 Layout Patterns

Patterns de layout réutilisables.

### Pattern: Page Standard

```jsx
function StandardPage() {
  return (
    <Container>
      {/* Header */}
      <header style={{ marginBottom: 'var(--space-xl, 24px)' }}>
        <H1>Titre de la page</H1>
        <Text>Description ou sous-titre</Text>
      </header>
      
      {/* Actions */}
      <div style={{ 
        display: 'flex', 
        gap: 'var(--space-sm, 8px)',
        marginBottom: 'var(--space-lg, 16px)'
      }}>
        <Button variant="primary">Action principale</Button>
        <Button variant="secondary">Action secondaire</Button>
      </div>
      
      {/* Contenu */}
      <div style={{ marginBottom: 'var(--space-xl, 24px)' }}>
        {/* ... */}
      </div>
      
      {/* Footer */}
      <footer style={{ 
        paddingTop: 'var(--space-lg, 16px)',
        borderTop: '1px solid var(--border, #e5edf5)'
      }}>
        <Text>Informations complémentaires</Text>
      </footer>
    </Container>
  );
}
```

### Pattern: Formulaire

```jsx
function FormPage() {
  return (
    <Container>
      <H1>Créer un article</H1>
      <Spacer size="lg" />
      
      <Card variant="standard" padding="lg">
        <form>
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: 'var(--space-md, 12px)'
          }}>
            <TextInput label="Désignation" required />
            <AmountInput label="Prix HT" required />
            <SelectInput label="Catégorie" options={categories} />
            <Textarea label="Description" rows={4} />
            
            <Spacer size="lg" />
            
            <div style={{
              display: 'flex',
              gap: 'var(--space-sm, 8px)',
              justifyContent: 'flex-end'
            }}>
              <Button variant="secondary" type="button">Annuler</Button>
              <Button variant="primary" type="submit">Enregistrer</Button>
            </div>
          </div>
        </form>
      </Card>
    </Container>
  );
}
```

### Pattern: Liste avec filtres

```jsx
function ListPage() {
  return (
    <Container>
      <H1>Articles</H1>
      <Spacer size="lg" />
      
      {/* Filtres */}
      <Card variant="standard" padding="md">
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: 'var(--space-md, 12px)'
        }}>
          <TextInput label="Recherche" />
          <SelectInput label="Catégorie" options={categories} />
          <SelectInput label="Statut" options={statusOptions} />
        </div>
      </Card>
      
      <Spacer size="lg" />
      
      {/* Résultats */}
      <ArticlesTable articles={filteredArticles} />
    </Container>
  );
}
```

---

## 📱 Mobile-Specific Considerations

### Touch Targets

- **Minimum:** 44x44px pour tous les éléments cliquables
- **Recommandé:** 48x48px pour une meilleure expérience

```css
.button {
  min-height: 44px;
  min-width: 44px;
  padding: 12px 24px;
}
```

### Scroll Behavior

- **Always visible scrollbar:** Non (masquée sur mobile)
- **Pull to refresh:** Pas implémenté nativement
- **Infinite scroll:** À éviter (préférer la pagination)

### Viewport

```html
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
```

---

## 🎯 Layout Checklist

- [ ] Mobile-first approach (360px minimum)
- [ ] Responsive breakpoints définis
- [ ] Touch targets ≥ 44px
- [ ] No horizontal scroll sur mobile
- [ ] Tableau → MobileTable sur < 768px
- [ ] Text lisible sur écran bas de gamme
- [ ] Contraste WCAG AA minimum
- [ ] Espacement cohérent (grille 8px)
- [ ] Border radius cohérent (4px/8px)
- [ ] Container max-width: 1200px

---

## 📊 Summary

| Catégorie | Éléments | Valeurs |
|----------|----------|---------|
| **Grid** | Breakpoints | xs, sm, md, lg, xl, 2xl |
| **Spacing** | Échelle | 2px, 4px, 8px, 12px, 16px, 24px, 32px, 48px |
| **Border** | Radius | 4px (sm), 8px (md), 12px (lg) |
| **Container** | Max-width | 1200px |
| **Mobile** | Min-width | 360px |

**Total: 1 système de layout cohérent et complet**

---

*Documentation générée automatiquement à partir du Design System StockMaster CM - EPIC-D00*