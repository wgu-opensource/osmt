# Phase 2: Add logoUrl Support to Frontend

## Scope

Add a `logoUrl` field to `IAppConfig` so the header logo is driven by the
whitelabel config rather than hardcoded. Update the footer to gracefully hide
empty `poweredBy` fields.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files.
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later.

## Implementation Details

### 1. Add `logoUrl` to `IAppConfig`

In `ui/src/app/models/app-config.model.ts`:

Add `logoUrl` to the `IAppConfig` interface:

```typescript
export interface IAppConfig {
  // ... existing fields ...
  logoUrl?: string;
}
```

Add the default in `DefaultAppConfig`:

```typescript
logoUrl = '/assets/images/logo-dark.svg';
```

### 2. Update header template

In `ui/src/app/navigation/header.component.html`, change the hardcoded logo:

```html
<img
  src="/assets/images/logo-dark.svg"
  alt="Site logo"
  aria-hidden="true"
  width="110"
  height="24"
/>
```

to a bound value:

```html
<img
  [src]="whitelabel.logoUrl || '/assets/images/logo-dark.svg'"
  [alt]="whitelabel.toolName + ' logo'"
  width="110"
  height="24"
/>
```

Note: Remove `aria-hidden="true"` since the logo now has a meaningful alt text.

### 3. Update footer to hide empty poweredBy

In `ui/src/app/navigation/footer.component.html`, the powered-by section
currently always renders. Wrap it in `*ngIf` so it hides when empty:

```html
<div class="m-footer-x-tagline" *ngIf="whitelabel.poweredBy">
  <p>
    {{ whitelabel.poweredBy }}
    <a [href]="whitelabel.poweredByUrl">{{
      whitelabel.poweredByLabel
    }}</a
    >.
  </p>
</div>
```

### 4. Add `logoUrl` to whitelabel JSON files

Add `"logoUrl": "/assets/images/logo-dark.svg"` to both:

- `ui/src/whitelabel/whitelabel.json`
- `api/docker/whitelabel/whitelabel.json`

## Tests

### header.component.spec.ts

If it exists, update to verify:
- Default logo URL renders when no `logoUrl` is set
- Custom `logoUrl` renders when set in config

If no spec exists, create `header.component.spec.ts`:

```typescript
it('should use default logo when logoUrl is not set', () => {
  const img = fixture.nativeElement.querySelector(
    '.m-navBar-x-brand img'
  );
  expect(img.src).toContain('/assets/images/logo-dark.svg');
});

it('should use custom logo when logoUrl is set', () => {
  AppConfig.settings.logoUrl = 'https://example.com/logo.svg';
  fixture.detectChanges();
  const img = fixture.nativeElement.querySelector(
    '.m-navBar-x-brand img'
  );
  expect(img.src).toBe('https://example.com/logo.svg');
});
```

### footer.component.spec.ts

Verify the powered-by section is hidden when `poweredBy` is empty.

### app.component.spec.ts

Existing tests should still pass — no changes to `app.component.ts` in this
phase.

## Validate

```bash
cd ui && npm run ci-test && npm run format:check
```
