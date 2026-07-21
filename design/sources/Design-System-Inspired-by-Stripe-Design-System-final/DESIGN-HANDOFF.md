# stockmaster-design-system implementation handoff

This archive is the source of truth for turning the design into production code. Start from `system/index.html`, then preserve the visual system, responsive behavior, and interactions found in the exported files.

## Implementation target
- Build production UI from the exported design, not a loose reinterpretation.
- Preserve typography scale, spacing rhythm, color tokens, border radii, shadows, motion timing, and component states.
- Replace static placeholders only when the target app has real data or functional equivalents.
- Keep generated product UI free of Open Design chrome, preview labels, or design-process annotations.
- Treat this handoff as a visual contract: if implementation choices conflict, match the exported pixels and behavior first, then refactor internals.

## Source map
- Primary entry: `system/index.html`
- HTML screens detected: 20
- Stylesheets detected: 5
- Script/component files detected: 10
- Supporting assets detected: 192

## Responsive contract
Validate the implementation across this 2025–2026 viewport matrix:
- Mobile compact: 360×800
- Mobile standard: 390×844
- Mobile large: 430×932
- Foldable / small tablet: 600×960
- Tablet portrait: 820×1180
- Tablet landscape: 1024×768
- Laptop: 1366×768
- Desktop: 1440×900
- Wide desktop: 1920×1080

For responsive web exports, treat these as a modern breakpoint system for one adaptive web experience, not three fixed screenshots. Do not split responsive web into unrelated native app screens unless the project explicitly includes native targets. Use semantic layout thresholds, fluid `clamp()` type/spacing, and container queries where component width matters more than viewport width. Preserve any CSS media queries, container queries, fluid `clamp()` scales, and layout changes already present in the exported files.

## Design fidelity contract
- Extract reusable tokens before writing components: background, surface, foreground, muted text, border, accent, radius, shadow, spacing, type scale, and motion duration/easing.
- Map product screens, in-app modules/components, optional landing page, and optional OS widget surfaces before coding. Keep these surfaces separate in the target architecture.
- Match layout geometry: max-widths, gutters, grid columns, card proportions, sticky/fixed elements, and viewport-specific navigation.
- Preserve real copy, labels, and data shown in the export. Do not replace specific text with generic marketing filler.
- Preserve interactive affordances: hover, focus, pressed, disabled, loading, validation, copy/share, tab/accordion, modal/sheet, and keyboard states where present.
- Preserve accessibility semantics when converting: headings stay hierarchical, controls remain buttons/links/inputs, focus states stay visible.
- Do not keep prototype-only annotations, frame labels, or Open Design chrome in the production UI.

## CJX-ready UX contract
- Use `DESIGN-MANIFEST.json` as the machine-readable map for screens, app modules, OS widgets, landing pages, tokens, interactions, and viewport checks.
- Screen-file-first: when multiple user-facing surfaces exist, implement each HTML screen as its own route/file. Treat `index.html` as a launcher/overview when the manifest marks it that way, not as a combined final UI.
- If `landing.html`, app screens, platform screens, or OS widget files exist, preserve those boundaries in the target app instead of merging them into one page.
- A single self-contained `system/index.html` is acceptable only when the export truly contains one user-facing screen and its CSS/JS are structured enough to extract tokens, components, states, and behavior.
- If separate `css/` or `js/` files exist, treat them as source of truth for token/component/interactions before porting to React, Vue, SwiftUI, Compose, or another target stack.
- In-app modules/components are product UI blocks inside the app. OS widgets are home-screen/lock-screen/quick-access surfaces outside the app. Do not merge those concepts.

## Color and brand contract
- Use the exported design tokens and product/domain context as the color source of truth.
- Do not introduce warm beige / cream / peach / pink / orange-brown background washes unless they are already explicit brand/reference colors in the export.
- A stylesheet or design/token file was detected; inspect it for canonical color variables before choosing framework theme tokens.

## Implementation sequence for AI coding tools
1. Open `system/index.html` and `DESIGN-MANIFEST.json`; identify every screen file, launcher/overview file, app module, and interaction before coding.
2. If multiple HTML screens exist, map them to separate routes/surfaces first; do not merge `landing.html`, product app screens, platform screens, or OS widgets into one route.
3. Extract a token table from CSS/root styles and inline styles before building framework components.
4. Build product screens and domain-specific in-app modules from largest layout regions down to controls; avoid starting with isolated atoms that lose spatial intent.
5. Port responsive behavior across the modern viewport matrix and test each semantic breakpoint before cleanup.
6. Port interactions and states, then replace static placeholders only with real app data or functional equivalents.
7. Keep optional landing page and OS widget surfaces as separate surfaces if present.
8. Compare final screenshots against the export at 360×800, 390×844, 430×932, 820×1180, 1024×768, 1366×768, 1440×900, and 1920×1080 before declaring done.

## Entry points
- `badge-demo.html`
- `brand.html`
- `browser/snapshots/google.com-2026-07-15T12-00-59-009Z/page.html`
- `components-demo.html`
- `design-system-demo.html`
- `preview/badges.html`
- `preview/colors.html`
- `preview/spacing.html`
- `preview/typography.html`
- `response.html`
- `system/artifacts/deck.html`
- `system/artifacts/email.html`
- `system/artifacts/form.html`
- `system/artifacts/landing.html`
- `system/artifacts/newsletter.html`
- `system/artifacts/poster.html`
- `system/index.html`
- `system/kit.dark.html`
- `system/kit.html`
- `text-color-options-demo.html`

## Styles
- `browser/snapshots/google.com-2026-07-15T12-00-59-009Z/styles.css`
- `colors_and_type.css`
- `design-tokens.css`
- `system/variables.css`
- `system/variables.dark.css`

## Scripts/components
- `components/Alerts.jsx`
- `components/Atomic.jsx`
- `components/Badges.jsx`
- `components/Buttons.jsx`
- `components/Cards.jsx`
- `components/Icons.jsx`
- `components/index.jsx`
- `components/Inputs.jsx`
- `components/Tables.jsx`
- `system/scripts/apply-design-tokens.mjs`

## Assets and supporting files
- `awesome-design-md-1.zip`
- `awesome-design-md.zip`
- `brand.json`
- `browser/latest-page-snapshot.json`
- `browser/snapshots/google.com-2026-07-15T12-00-59-009Z/manifest.json`
- `context/input-DESIGN.md`
- `DESIGN.backup.md`
- `DESIGN.md`
- `EPIC-D00-COMPLET.md`
- `EPIC-D00-VALIDATION.md`
- `extracted-design-md/awesome-design-md/CONTRIBUTING.md`
- `extracted-design-md/awesome-design-md/design-md/airbnb/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/airbnb/README.md`
- `extracted-design-md/awesome-design-md/design-md/airtable/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/airtable/README.md`
- `extracted-design-md/awesome-design-md/design-md/apple/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/apple/README.md`
- `extracted-design-md/awesome-design-md/design-md/binance/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/binance/README.md`
- `extracted-design-md/awesome-design-md/design-md/bmw-m/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/bmw-m/README.md`
- `extracted-design-md/awesome-design-md/design-md/bmw/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/bmw/README.md`
- `extracted-design-md/awesome-design-md/design-md/bugatti/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/bugatti/README.md`
- `extracted-design-md/awesome-design-md/design-md/cal/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/cal/README.md`
- `extracted-design-md/awesome-design-md/design-md/claude/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/claude/README.md`
- `extracted-design-md/awesome-design-md/design-md/clay/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/clay/README.md`
- `extracted-design-md/awesome-design-md/design-md/clickhouse/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/clickhouse/README.md`
- `extracted-design-md/awesome-design-md/design-md/cohere/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/cohere/README.md`
- `extracted-design-md/awesome-design-md/design-md/coinbase/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/coinbase/README.md`
- `extracted-design-md/awesome-design-md/design-md/composio/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/composio/README.md`
- `extracted-design-md/awesome-design-md/design-md/cursor/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/cursor/README.md`
- `extracted-design-md/awesome-design-md/design-md/dell-1996/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/dell-1996/README.md`
- `extracted-design-md/awesome-design-md/design-md/elevenlabs/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/elevenlabs/README.md`
- `extracted-design-md/awesome-design-md/design-md/expo/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/expo/README.md`
- `extracted-design-md/awesome-design-md/design-md/ferrari/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/ferrari/README.md`
- `extracted-design-md/awesome-design-md/design-md/figma/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/figma/README.md`
- `extracted-design-md/awesome-design-md/design-md/framer/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/framer/README.md`
- `extracted-design-md/awesome-design-md/design-md/hashicorp/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/hashicorp/README.md`
- `extracted-design-md/awesome-design-md/design-md/hp/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/hp/README.md`
- `extracted-design-md/awesome-design-md/design-md/ibm/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/ibm/README.md`
- `extracted-design-md/awesome-design-md/design-md/intercom/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/intercom/README.md`
- `extracted-design-md/awesome-design-md/design-md/kraken/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/kraken/README.md`
- `extracted-design-md/awesome-design-md/design-md/lamborghini/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/lamborghini/README.md`
- `extracted-design-md/awesome-design-md/design-md/linear.app/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/linear.app/README.md`
- `extracted-design-md/awesome-design-md/design-md/lovable/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/lovable/README.md`
- `extracted-design-md/awesome-design-md/design-md/mastercard/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/mastercard/README.md`
- `extracted-design-md/awesome-design-md/design-md/meta/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/meta/README.md`
- `extracted-design-md/awesome-design-md/design-md/minimax/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/minimax/README.md`
- `extracted-design-md/awesome-design-md/design-md/mintlify/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/mintlify/README.md`
- `extracted-design-md/awesome-design-md/design-md/miro/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/miro/README.md`
- `extracted-design-md/awesome-design-md/design-md/mistral.ai/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/mistral.ai/README.md`
- `extracted-design-md/awesome-design-md/design-md/mongodb/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/mongodb/README.md`
- `extracted-design-md/awesome-design-md/design-md/nike/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/nike/README.md`
- `extracted-design-md/awesome-design-md/design-md/nintendo-2001/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/nintendo-2001/README.md`
- `extracted-design-md/awesome-design-md/design-md/notion/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/notion/README.md`
- `extracted-design-md/awesome-design-md/design-md/nvidia/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/nvidia/README.md`
- `extracted-design-md/awesome-design-md/design-md/ollama/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/ollama/README.md`
- `extracted-design-md/awesome-design-md/design-md/opencode.ai/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/opencode.ai/README.md`
- `extracted-design-md/awesome-design-md/design-md/pinterest/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/pinterest/README.md`
- `extracted-design-md/awesome-design-md/design-md/playstation/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/playstation/README.md`
- `extracted-design-md/awesome-design-md/design-md/posthog/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/posthog/README.md`
- `extracted-design-md/awesome-design-md/design-md/raycast/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/raycast/README.md`
- `extracted-design-md/awesome-design-md/design-md/renault/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/renault/README.md`
- `extracted-design-md/awesome-design-md/design-md/replicate/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/replicate/README.md`
- `extracted-design-md/awesome-design-md/design-md/resend/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/resend/README.md`
- `extracted-design-md/awesome-design-md/design-md/revolut/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/revolut/README.md`
- `extracted-design-md/awesome-design-md/design-md/runwayml/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/runwayml/README.md`
- `extracted-design-md/awesome-design-md/design-md/sanity/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/sanity/README.md`
- `extracted-design-md/awesome-design-md/design-md/sentry/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/sentry/README.md`
- `extracted-design-md/awesome-design-md/design-md/shopify/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/shopify/README.md`
- `extracted-design-md/awesome-design-md/design-md/slack/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/spacex/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/spacex/README.md`
- `extracted-design-md/awesome-design-md/design-md/spotify/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/spotify/README.md`
- `extracted-design-md/awesome-design-md/design-md/starbucks/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/starbucks/README.md`
- `extracted-design-md/awesome-design-md/design-md/stockmaster/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/stockmaster/README.md`
- `extracted-design-md/awesome-design-md/design-md/supabase/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/supabase/README.md`
- `extracted-design-md/awesome-design-md/design-md/superhuman/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/superhuman/README.md`
- `extracted-design-md/awesome-design-md/design-md/tesla/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/tesla/README.md`
- `extracted-design-md/awesome-design-md/design-md/theverge/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/theverge/README.md`
- `extracted-design-md/awesome-design-md/design-md/together.ai/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/together.ai/README.md`
- `extracted-design-md/awesome-design-md/design-md/uber/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/uber/README.md`
- `extracted-design-md/awesome-design-md/design-md/vercel/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/vercel/README.md`
- `extracted-design-md/awesome-design-md/design-md/vodafone/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/vodafone/README.md`
- `extracted-design-md/awesome-design-md/design-md/voltagent/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/voltagent/README.md`
- `extracted-design-md/awesome-design-md/design-md/warp/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/warp/README.md`
- `extracted-design-md/awesome-design-md/design-md/webflow/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/webflow/README.md`
- `extracted-design-md/awesome-design-md/design-md/wired/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/wired/README.md`
- `extracted-design-md/awesome-design-md/design-md/wise/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/wise/README.md`
- `extracted-design-md/awesome-design-md/design-md/x.ai/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/x.ai/README.md`
- `extracted-design-md/awesome-design-md/design-md/zapier/DESIGN.md`
- `extracted-design-md/awesome-design-md/design-md/zapier/README.md`
- `extracted-design-md/awesome-design-md/LICENSE`
- `extracted-design-md/awesome-design-md/README.md`
- `GS-DESIGN-BACKLOG-2026-01-1.md`
- `GS-DESIGN-BACKLOG-2026-01.md`
- `guide.md`
- `plugin-source/stockmaster-cm---design-system-mrly74ao/open-design.json`
- `plugin-source/stockmaster-cm---design-system-mrly74ao/references/provenance.json`
- `plugin-source/stockmaster-cm---design-system-mrly74ao/references/source-1-README.md`
- `plugin-source/stockmaster-cm---design-system-mrly74ao/references/source-2-SKILL.md`
- `plugin-source/stockmaster-cm---design-system-mrly74ao/SKILL.md`
- `plugin-source/stockmaster-cm---design-system-mrly7haz/open-design.json`
- `plugin-source/stockmaster-cm---design-system-mrly7haz/references/provenance.json`
- `plugin-source/stockmaster-cm---design-system-mrly7haz/references/source-1-README.md`
- `plugin-source/stockmaster-cm---design-system-mrly7haz/references/source-2-SKILL.md`
- `plugin-source/stockmaster-cm---design-system-mrly7haz/SKILL.md`
- `plugin-source/stockmaster-cm---design-system-mrly91lg/open-design.json`
- `plugin-source/stockmaster-cm---design-system-mrly91lg/references/provenance.json`
- `plugin-source/stockmaster-cm---design-system-mrly91lg/references/source-1-README.md`
- `plugin-source/stockmaster-cm---design-system-mrly91lg/references/source-2-SKILL.md`
- `plugin-source/stockmaster-cm---design-system-mrly91lg/SKILL.md`
- `preview/badges.md`
- `preview/colors.md`
- `preview/components.md`
- `preview/layout.md`
- `preview/spacing.md`
- `preview/typography.md`
- `README.md`
- `SKILL.md`
- `system/BRAND-SYSTEM.md`
- `system/seed.json`
- `system/theme.json`
- `system/tokens.compact.json`
- `system/tokens.dark.json`
- `system/tokens.default.json`

## Coding checklist for AI tools
1. Inspect `system/index.html` and `DESIGN-MANIFEST.json` first and identify reusable components before coding.
2. Implement each user-facing screen file as its own route/surface; keep launcher, landing, app, platform, and OS widget files separate.
3. Extract design tokens into the target stack: colors, type scale, spacing, radius, shadows, and motion.
4. Implement layout with real 2025–2026 responsive breakpoints, fluid type/spacing, and container-query-aware component behavior; test with no horizontal overflow.
5. Preserve interactive controls, hover/focus/pressed states, form behavior, validation, and copy actions where present.
6. Implement domain-specific in-app modules with real states; do not flatten them into generic cards.
7. Keep landing page, product screens, and OS widget/quick-access surfaces separate when present.
8. Confirm the production result visually matches the exported design before refactoring internals.
9. Reject implementation shortcuts that flatten the design into generic cards, generic gradients, placeholder stats, or framework-default typography.
10. If a detail is ambiguous, keep the exported HTML/CSS/JS behavior rather than inventing a new pattern.
