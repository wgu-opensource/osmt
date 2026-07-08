# Tailwind CSS Integration

Tailwind CSS has been added to the OSMT UI with design tokens ported from the
vendored WGU pattern library CSS (`ui/src/pattern-library/`; see
`docs/features/2026-03-19-pattern-library-css-in-repo.md`). This enables
incremental migration from that legacy stylesheet while preserving consistent
styling.

## Setup

### Installed Packages

- `tailwindcss@^3`
- `postcss`
- `autoprefixer`

### Configuration Files

- **`ui/tailwind.config.js`** – Theme extended with WGU design tokens
- **`ui/postcss.config.js`** – PostCSS pipeline (tailwindcss, autoprefixer)
- **`ui/src/styles.scss`** – Imports Tailwind components and utilities (base/preflight skipped to avoid conflict with pattern lib during migration)

### Build Order

Styles are loaded in this order:

1. Pattern library CSS — `ui/src/pattern-library/css/screen.css`
2. `styles.scss` (Tailwind + app overrides)

## Design Tokens (from WGU Pattern Library)

### Colors

| Token                               | Usage                      |
| ----------------------------------- | -------------------------- |
| `brand1`                            | Primary brand (whitelabel) |
| `background-100` … `background-500` | Surfaces                   |
| `text-1` … `text-4`                 | Semantic text colors       |
| `interactive-1`, `interactive-2`    | Links, buttons             |
| `attention`                         | Error, alert               |
| `focus`                             | Focus ring                 |
| `positive`                          | Success                    |
| `warning`                           | Warning                    |

Semantic colors use CSS variables (`var(--color-*)`) so whitelabel and dark mode continue to work.

**Tailwind usage:** `bg-background-300`, `text-text-1`, `border-attention`, etc.

### Spacing

| Token        | Value (mobile) | Value (tablet 768px+) |
| ------------ | -------------- | --------------------- |
| `extraSmall` | 8px            | 8px                   |
| `small`      | 16px           | 16px                  |
| `medium`     | 32px           | 40px                  |
| `large`      | 48px           | 64px                  |

**Tailwind usage:** `p-small`, `mt-medium`, `mb-large`. Use responsive variants for tablet values: `mt-medium tablet:mt-mediumTablet`, or `p-mediumTablet` for fixed 40px.

### Typography

| Token        | Mobile    | Tablet 768px+ |
| ------------ | --------- | ------------- |
| `heading1`   | 32px/40px | 48px/56px     |
| `heading2`   | 24px/32px | 32px/40px     |
| `heading3`   | 14px/24px | —             |
| `body`       | 16px/24px | —             |
| `bodyLarge`  | 16px/24px | 24px/32px     |
| `small`      | 12px/16px | —             |
| `extraSmall` | 8px/10px  | —             |
| `button`     | 14px/16px | —             |

**Tailwind usage:** `text-heading1`, `text-body`, `text-small`. Responsive: `text-bodyLarge tablet:text-bodyLargeTablet`.

### Breakpoints

| Name        | Value   | Tailwind Variant |
| ----------- | ------- | ---------------- |
| tablet      | 768px   | `tablet:`        |
| desktop     | 1024px  | `desktop:`       |
| hd          | 1440px  | `hd:`            |
| max-mobile  | ≤479px  | `max-mobile:`    |
| max-tablet  | ≤767px  | `max-tablet:`    |
| max-desktop | ≤1023px | `max-desktop:`   |
| max-hd      | ≤1439px | `max-hd:`        |

### Transitions

- Duration: `0.12s`
- Easing: `cubic-bezier(0.42, 0, 0.58, 1)`

## Migration Strategy

1. **Incremental** – New components and refactors use Tailwind.
2. **Pattern library CSS remains** – Vendored in-repo; existing `m-*` components
   still rely on it; `t-*` and `l-*` classes can be replaced gradually.
3. **`@tailwind base`** – Intentionally omitted to avoid preflight conflicts.
   Re-enable when pattern-library CSS is fully removed from the bundle.

## Example: Pattern Lib → Tailwind

| Pattern library classes           | Tailwind equivalent                 |
| --------------------------------- | ----------------------------------- |
| `t-type-body`                     | `text-body`                         |
| `t-type-heading1`                 | `text-heading1 font-semibold`       |
| `t-margin-medium t-margin-bottom` | `mb-medium`                         |
| `t-padding-large t-padding-top`   | `pt-large`                          |
| `l-container`                     | `mx-auto max-w-container` + margins |
| `t-hidden-desktopMin`             | `max-desktop:block desktop:hidden`  |
| `t-type-text1`                    | `text-text-1`                       |

## Whitelabel

Brand colors (`--color-brand1`, `--color-a11yOnBrand`) are still set in `app.component.ts` via `document.documentElement.style.setProperty()`. Tailwind semantic colors reference these CSS variables, so whitelabel behavior is unchanged.
