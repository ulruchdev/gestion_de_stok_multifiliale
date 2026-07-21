# StockMaster CM Design System - Typography

> **Category:** Design Tokens | **Type:** Typography | **Source:** StockMaster

StockMaster's typography system is built around **SF Pro Display** at thin (300) weights with negative letter-spacing — the brand's editorial-density signature. Display sizes use negative tracking from -1.4px to -0.2px depending on size.

---

## 📝 Font Families

| Role | Family | Fallbacks | Weights | Usage |
|------|--------|-----------|---------|-------|
| **Display** | **"SF Pro Display"** | system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif | 300, 400, 500, 600, 700 | Titles (H1-H6), large text |
| **Body** | **system-ui** | -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif | 300, 400 | Standard text, paragraphs |
| **Mono** | **ui-monospace** | SFMono-Regular, Menlo, monospace | 400, 700 | Code, technical data, command codes |

### ⚠️ CRITICAL RULE

> **Display font uses SF Pro Display, NOT SFMono.** SFMono is reserved exclusively for code and technical data. The Body uses system-ui for native platform typography.

### Font Substitutes

SF Pro Display is proprietary. For maximum brand fidelity:
- Use **Inter** (open-source via Google Fonts) at weight 300 with `letter-spacing: -1.4px` and `font-feature-settings: "ss01"` for display tiers
- Avoid Helvetica or system-ui defaults — they're heavier than the brand needs

---

## 📏 Typographic Scale

### Display (Large Headlines)

| Class | Size (Mobile → Desktop) | Weight | Line Height | Letter Spacing | Usage |
|-------|---------------------------|--------|-------------|----------------|-------|
| Display XXL | `clamp(48px, 8vw, 80px)` | 300 | 1.03 | -1.4px | Hero headline |
| Display XL | `clamp(48px, 8vw, 60px)` | 300 | 1.15 | -0.96px | Section opener |
| Display LG | `clamp(32px, 6vw, 48px)` | 300 | 1.1 | -0.64px | Card title / sub-section |
| Display MD | `clamp(26px, 5vw, 36px)` | 300 | 1.12 | -0.26px | Compact card title |
| Display SM | `clamp(24px, 4vw, 32px)` | 300 | 1.12 | -0.26px | Small display |
| Display XS | `clamp(20px, 3.5vw, 28px)` | 300 | 1.15 | 0 | Micro display |

### Heading (Hierarchy)

| Class | Size (Mobile → Desktop) | Weight | Line Height | Letter Spacing | Usage |
|-------|---------------------------|--------|-------------|----------------|-------|
| **H1** | **`clamp(28px, 6vw, 40px)`** | **300** | **1.03** | **-1.4px** | Page title |
| **H2** | **`clamp(24px, 5vw, 32px)`** | **300** | **1.15** | **-0.96px** | Section title |
| **H3** | **`clamp(20px, 4vw, 28px)`** | **300** | **1.1** | **-0.64px** | Sub-section title |
| H4 | `clamp(18px, 3.5vw, 24px)` | 300 | 1.1 | -0.22px | Group title |
| H5 | `clamp(16px, 3vw, 20px)` | 300 | 1.4 | -0.2px | Card title |
| H6 | `clamp(14px, 2.5vw, 18px)` | 300 | 1.4 | 0 | Small heading |

### Body (Standard Text)

| Class | Size | Weight | Line Height | Letter Spacing | Usage |
|-------|------|--------|-------------|----------------|-------|
| **Base** | **16px** | **300** | **1.5** | **0** | Default body text |
| Large | 18px | 300 | 1.4 | 0 | Lead text, descriptions |
| Small | 14px | 300 | 1.4 | 0 | Secondary text, metadata |
| Extra Small | 12px | 300 | 1.4 | 0 | Labels, badges, captions |

### Special (Tabular Figures)

| Class | Size | Weight | Line Height | Letter Spacing | Usage |
|-------|------|--------|-------------|----------------|-------|
| Body Tabular | 14px | 300 | 1.4 | -0.42px | Money / numeric tables (uses `tnum`) |

### Button

| Class | Size | Weight | Line Height | Letter Spacing | Usage |
|-------|------|--------|-------------|----------------|-------|
| Button MD | 16px | 400 | 1.0 | 0 | Pill button label |
| Button SM | 14px | 400 | 1.0 | 0 | Compact pill label |

### Utility

| Class | Size | Weight | Line Height | Letter Spacing | Usage |
|-------|------|--------|-------------|----------------|-------|
| Caption | 13px | 400 | 1.4 | -0.39px | Helper, table labels |
| Micro | 11px | 300 | 1.4 | 0 | Fine print |
| Micro Cap | 10px | 400 | 1.15 | 0.1px | All-caps eyebrow |

---

## 🎯 CSS Tokens

### Font Families

```css
:root {
  /* Display - For headlines and large text */
  --font-display: "SF Pro Display", system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif;
  
  /* Body - For standard text */
  --font-body: system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif;
  
  /* Mono - For code and technical data */
  --font-mono: ui-monospace, SFMono-Regular, Menlo, monospace;
}
```

### Font Weights

```css
:root {
  --font-weight-thin: 300;
  --font-weight-normal: 400;
  --font-weight-medium: 500;
  --font-weight-semibold: 600;
  --font-weight-bold: 700;
}
```

### Font Sizes

```css
:root {
  /* Display */
  --font-size-display-xxl: clamp(48px, 8vw, 80px);
  --font-size-display-xl: clamp(48px, 8vw, 60px);
  --font-size-display-lg: clamp(32px, 6vw, 48px);
  --font-size-display-md: clamp(26px, 5vw, 36px);
  --font-size-display-sm: clamp(24px, 4vw, 32px);
  --font-size-display-xs: clamp(20px, 3.5vw, 28px);
  
  /* Heading */
  --font-size-h1: clamp(28px, 6vw, 40px);
  --font-size-h2: clamp(24px, 5vw, 32px);
  --font-size-h3: clamp(20px, 4vw, 28px);
  --font-size-h4: clamp(18px, 3.5vw, 24px);
  --font-size-h5: clamp(16px, 3vw, 20px);
  --font-size-h6: clamp(14px, 2.5vw, 18px);
  
  /* Body */
  --font-size-base: 16px;
  --font-size-lg: 18px;
  --font-size-sm: 14px;
  --font-size-xs: 12px;
  
  /* Special */
  --font-size-body-tabular: 14px;
  --font-size-button-md: 16px;
  --font-size-button-sm: 14px;
  --font-size-caption: 13px;
  --font-size-micro: 11px;
  --font-size-micro-cap: 10px;
}
```

### Line Heights

```css
:root {
  --line-height-display: 1.03;
  --line-height-heading: 1.15;
  --line-height-body: 1.5;
  --line-height-dense: 1.4;
  --line-height-relaxed: 1.6;
}
```

### Letter Spacing (StockMaster Signature)

```css
:root {
  --letter-spacing-display-xxl: -1.4px;
  --letter-spacing-display-xl: -0.96px;
  --letter-spacing-display-lg: -0.64px;
  --letter-spacing-display-md: -0.26px;
  --letter-spacing-display-sm: -0.26px;
  --letter-spacing-heading-lg: -0.22px;
  --letter-spacing-heading-md: -0.2px;
  --letter-spacing-body-tabular: -0.42px;
  --letter-spacing-caption: -0.39px;
  --letter-spacing-normal: normal;
  --letter-spacing-wide: 0.5px;
  --letter-spacing-wider: 1px;
}
```

---

## 📱 Responsive Behavior

### Mobile (360px)
- Display XXL: 48px
- Display XL: 48px
- Display LG: 32px
- H1: 28px
- H2: 24px
- H3: 20px
- Base: 16px
- Uses minimum sizes for optimal readability

### Desktop (1440px+)
- Display XXL: 80px
- Display XL: 60px
- Display LG: 48px
- H1: 40px
- H2: 32px
- H3: 28px
- Base: 16px
- Uses maximum sizes for strong hierarchy

### Clamp() Usage
All display and heading sizes use `clamp(min, preferred, max)` for fluid scaling:
- **min**: Minimum size on mobile
- **preferred**: Fluid size based on viewport
- **max**: Maximum size on desktop

---

## ✨ Usage Examples

### HTML
```html
<h1 class="font-display" style="font-size: var(--font-size-h1); font-weight: var(--font-weight-thin); letter-spacing: var(--letter-spacing-display-xxl);">
  Hero Headline
</h1>
<h2 class="font-display" style="font-size: var(--font-size-h2); font-weight: var(--font-weight-thin); letter-spacing: var(--letter-spacing-display-xl);">
  Section Title
</h2>
<p class="font-body" style="font-size: var(--font-size-base); font-weight: var(--font-weight-normal);">
  Body text with standard typography
</p>
<code class="font-mono">CODE-ARTICLE-001</code>
```

### React/JSX
```jsx
import { H1, H2, Text, Code } from './components/Atomic';

const MyComponent = () => (
  <>
    <H1 style={{ letterSpacing: 'var(--letter-spacing-display-xxl)' }}>
      Hero Headline
    </H1>
    <H2 style={{ letterSpacing: 'var(--letter-spacing-display-xl)' }}>
      Section Title
    </H2>
    <Text size="body">Body text with standard typography</Text>
    <Code>CODE-ARTICLE-001</Code>
  </>
);
```

---

## 🎨 Preview

```
┌─────────────────────────────────────────────────────────────┐
│ H1: StockMaster CM Design System (28-40px, SF Pro Display, 300, -1.4px) │
├─────────────────────────────────────────────────────────────┤
│ H2: Typography System (24-32px, SF Pro Display, 300, -0.96px)            │
├─────────────────────────────────────────────────────────────┤
│ H3: Fluid Scaling (20-28px, SF Pro Display, 300, -0.64px)                │
├─────────────────────────────────────────────────────────────┤
│ Base: The quick brown fox jumps over the lazy dog. (16px, system-ui, 300) │
├─────────────────────────────────────────────────────────────┤
│ Small: Secondary text or metadata. (14px, system-ui, 300)                  │
├─────────────────────────────────────────────────────────────┤
│ Code: ART-001 (12px, ui-monospace, 400)                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Typography Rules

1. **Thin weight is the brand**: Display tiers always render at weight 300
2. **Negative tracking on display**: -1.4px at 48px, scaling proportionally down to -0.2px at 20px
3. **Tabular figures for money**: Any cell rendering currency uses `font-feature-settings: "tnum"`
4. **`ss01` globally**: Apply `font-feature-settings: "ss01"` to enable stylistic set substitution
5. **Hierarchy**: Each heading level must be visually distinct
6. **Mobile readability**: Minimum 16px for body text
7. **Line height**: Minimum 1.5 for body text

---

## 📖 Notes

- **SF Pro Display** gives the professional, modern look inspired by StockMaster
- **system-ui** for body ensures native platform experience
- **ui-monospace** for mono guarantees correct technical data display across all systems
- Negative letter-spacing on display sizes is StockMaster's typographic signature
- All sizes use `clamp()` for fluid, responsive typography

---

*Generated from StockMaster DESIGN.md - StockMaster CM Design System*
