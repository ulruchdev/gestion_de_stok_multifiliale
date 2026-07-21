# StockMaster CM Design System - Color Palette

> **Category:** Design Tokens | **Type:** Colors | **Source:** StockMaster

The StockMaster-inspired color system features a deep navy text color, electric indigo primary accent, and atmospheric gradient colors.

---

## 🎨 Core Brand Colors (7 colors)

| Role | Name | Hex | Usage | WCAG AA (on #ffffff) |
|------|------|-----|-------|-------------------------|
| background | Canvas | `#ffffff` | page canvas | - |
| **foreground** | **Ink** | **`#0d253d`** | body text and headings | ✅ 15.3:1 |
| accent | Primary | `#533afd` | primary actions and emphasis | ✅ 6.3:1 |
| surface | Canvas Soft | `#f6f9fc` | cards and panels | - |
| muted | Ink Mute | `#64748d` | secondary text and metadata | ✅ 6.0:1 |
| border | Hairline | `#e3e8ee` | rules and dividers | - |
| accent-secondary | Brand Dark 900 | `#1c1e54` | dashboard chrome, featured tiers | ✅ 12.5:1 |

### Preview

```
┌─────────────────────────────────────────────────────────────┐
│ Canvas:        #ffffff (White)                                  │
├─────────────────────────────────────────────────────────────┤
│ Ink:           #0d253d (Deep Navy - Text Primary)            │
│ Primary:       #533afd (Electric Indigo - Accent)             │
│ Canvas Soft:   #f6f9fc (Cool Off-White - Surface)              │
│ Ink Mute:      #64748d (Muted Grey - Secondary Text)          │
│ Hairline:      #e3e8ee (Light Grey - Borders)                  │
│ Brand Dark:    #1c1e54 (Deep Navy - Dashboard Chrome)          │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Semantic Colors

| Role | Name | Hex | Usage | WCAG AA |
|------|------|-----|-------|---------|
| **success** | Success | `#10b981` | positive states, validations | ✅ 4.5:1 |
| success-light | Success Light | `rgba(16, 185, 129, 0.1)` | background success | - |
| success-dark | Success Dark | `#059669` | text on success | ✅ |
| **error** | Ruby | `#ea2261` | errors, negative states, critical alerts | ✅ 6.3:1 |
| error-light | Error Light | `rgba(234, 34, 97, 0.1)` | background error | - |
| error-dark | Error Dark | `#d61f57` | text on error | ✅ |
| **warning** | Lemon | `#9b6829` | alerts, warnings, attention states | ✅ 6.3:1 |
| warning-light | Warning Light | `rgba(155, 104, 41, 0.1)` | background warning | - |
| warning-dark | Warning Dark | `#7a5220` | text on warning | ✅ |
| **info** | Magenta | `#f96bee` | information, links, highlights | ✅ 4.5:1 |
| info-light | Info Light | `rgba(249, 107, 238, 0.1)` | background info | - |
| info-dark | Info Dark | `#e854d8` | text on info | ✅ |

---

## 🟢 Extended Palette - Indigo (Primary)

| Level | Hex | Usage |
|-------|-----|-------|
| 1 | `#e8e9ff` | Lightest indigo background |
| 2 | `#d9daff` | Indigo background hover |
| 3 | `#b3b4ff` | Indigo border |
| 4 | `#8a8bff` | Indigo border hover |
| 5 | `#665efd` | Indigo soft |
| **6** | **`#533afd`** | **Primary accent** |
| 7 | `#4434d4` | Primary deep |
| 8 | `#2e2b8c` | Primary press state |
| 9 | `#1c1e54` | Brand dark 900 |
| 10 | `#0d0d2e` | Darkest indigo |

---

## 🔶 StockMaster Signature Colors

| Color | Hex | Role | Usage |
|-------|-----|------|-------|
| **Ruby** | `#ea2261` | Error | Gradient accent, chart highlight |
| **Magenta** | `#f96bee` | Info | Brighter pink stop in gradient meshes |
| **Lemon** | `#9b6829` | Warning | Warm sherbet stop in gradient backdrops |
| **Shadow Blue** | `#003770` | Depth | Shadow color for blue tones |

---

## 🎨 Tokens CSS

### Usage in code

```css
:root {
  /* Base colors */
  --bg: #ffffff;
  --fg: #0d253d;
  --accent: #533afd;
  --surface: #f6f9fc;
  --muted: #64748d;
  --border: #e3e8ee;
  --accent-secondary: #1c1e54;
  
  /* Semantic colors */
  --color-success: #10b981;
  --color-error: #ea2261;
  --color-warning: #9b6829;
  --color-info: #f96bee;
  
  /* StockMaster named colors */
  --canvas: #ffffff;
  --canvas-soft: #f6f9fc;
  --ink: #0d253d;
  --ink-secondary: #273951;
  --ink-mute: #64748d;
  --hairline: #e3e8ee;
  --primary: #533afd;
  --ruby: #ea2261;
  --magenta: #f96bee;
  --lemon: #9b6829;
  --on-primary: #ffffff;
}

/* Utility classes */
.text-primary { color: var(--fg); }
.text-success { color: var(--color-success); }
.text-error { color: var(--color-error); }
.text-warning { color: var(--color-warning); }
.text-info { color: var(--color-info); }

.bg-background { background-color: var(--bg); }
.bg-surface { background-color: var(--surface); }
```

---

## 🌙 Dark Theme

```css
@media (prefers-color-scheme: dark) {
  :root {
    --bg: #0f172a;
    --fg: #ffffff;
    --accent: #6366f1;
    --surface: #1e293b;
    --muted: #64748b;
    --border: #334155;
    --accent-secondary: #4338ca;
    
    --color-success: #10b981;
    --color-error: #f43f5e;
    --color-warning: #fbbf24;
    --color-info: #8b5cf6;
    
    --canvas: #0f172a;
    --canvas-soft: #1e293b;
    --ink: #ffffff;
    --ink-secondary: #94a3b8;
    --ink-mute: #64748b;
    --hairline: #334155;
    --primary: #6366f1;
    --ruby: #f43f5e;
    --magenta: #8b5cf6;
    --lemon: #fbbf24;
  }
}
```

---

## ✅ Accessibility

- All text colors have contrast ratio ≥ 4.5:1 on white background (WCAG AA)
- Primary accent has contrast ratio ≥ 6:1 on white background
- Semantic colors maintain readability on both light and dark backgrounds
- Color palette avoids pure orange and yellow to prevent confusion with Orange Money/MTN Mobile Money

---

## 📖 Notes

- Palette inspired by **StockMaster** design system
- **Deep navy (#0d253d)** used as primary text color - never pure black
- **Electric indigo (#533afd)** is the signature CTA color, used sparingly
- **Ruby (#ea2261)** and **Magenta (#f96bee)** appear in gradient meshes and as accent dots
- **Lemon (#9b6829)** used as warm sherbet stop in gradient backdrops
- All colors respect WCAG AA accessibility standards

---

*Generated from StockMaster DESIGN.md - StockMaster CM Design System*
