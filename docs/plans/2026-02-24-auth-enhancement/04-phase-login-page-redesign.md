# Phase 4: Login Page Redesign

## Scope of phase

Redesign login page to match skill form layout. Add OAuth provider picker and single-auth form. Use app shell (header, content), m-skillBackground, m-iconTitle, form components.

## Code Organization Reminders

- Match skill form structure: l-stickySidebar, m-skillBackground, t-margin-*, m-iconTitle
- Use app-formfield-text for username/password when single-auth
- Use m-button for OAuth provider buttons and submit
- Keep component logic focused; minimal nesting

## Implementation Details

### 1. Update login.component.ts

- Remove `isSingleAuthMode` based only on authMode; use `singleAuthEnabled` from AppConfig
- Add `oauthProviders: AuthProvider[]` from AppConfig.settings.authProviders
- Logic:
  - If authenticated → navigate to ''
  - If single provider + no single-auth → redirect to that provider's authorizationUrl (current behavior)
  - If multiple providers OR single-auth enabled → show login page (no auto-redirect)
- Add method to navigate to provider: `navigateToProvider(url: string)`

### 2. Update login.component.html

Structure (skill-form style):

```html
<div class="l-stickyBar">
  <div class="l-stickySidebar l-container">
    <div class="l-stickySidebar-x-content t-padding-medium t-padding-top">
      <div class="m-skillBackground l-skillBackground">
        <div class="t-margin-medium t-margin-bottom">
          <h3 class="m-iconTitle">
            <div class="m-iconTitle-x-icon">
              <svg class="t-icon" aria-hidden="true">
                <use xlink:href="/assets/images/svg-defs.svg#icon-doc"></use>
              </svg>
            </div>
            <div class="m-iconTitle-x-label">Sign In</div>
          </h3>
        </div>

        <!-- OAuth providers -->
        <div *ngIf="oauthProviders?.length" class="t-margin-medium t-margin-bottom">
          <p class="t-type-body">Sign in with your organization account:</p>
          <div class="oauth-providers">
            <a *ngFor="let provider of oauthProviders"
               class="m-button"
               [attr.href]="provider.authorizationUrl"
               role="button">
              <span class="m-button-x-text">Sign in with {{ provider.name }}</span>
            </a>
          </div>
        </div>

        <!-- Single-auth form -->
        <div *ngIf="singleAuthEnabled" class="t-margin-medium t-margin-top">
          <p *ngIf="oauthProviders?.length" class="t-type-body t-margin-small">Or sign in with admin credentials:</p>
          <form (ngSubmit)="onLogin($event)" class="login-form">
            <div class="t-margin-medium t-margin-bottom">
              <app-formfield-text ...username... />
            </div>
            <div class="t-margin-medium t-margin-bottom">
              <app-formfield-text type="password" ...password... />
            </div>
            <div *ngIf="loginError" class="error-message">...</div>
            <app-formfield-submit ... />
          </form>
        </div>

        <!-- OAuth loader: only when single provider, no single-auth, auto-redirect -->
        <div *ngIf="showOAuthLoader" class="l-loader-x-loader">...</div>
      </div>
    </div>
  </div>
</div>
```

Use app-formfield-text if it supports password type; otherwise use input with m-text. Match skill form field structure. For single-auth without ReactiveForms, keep existing template-driven form with ngModel.

### 3. Update login.component.scss

- Remove full-page gradient; use standard content background
- Add .oauth-providers with flex/gap for provider buttons
- Use design tokens: var(--color-*), t-margin-*, t-type-body
- Ensure buttons match m-button pattern from skill form

### 4. Handle showOAuthLoader

When one OAuth provider and no single-auth, redirect immediately (current behavior). Use Router or window.location. Show loader only briefly during redirect.

### 5. Auth guard / routing

Login route remains public. No change to auth guard.

## Tests

- login.component.spec.ts: mock AppConfig with authProviders, singleAuthEnabled
- Verify correct UI shown for: multiple providers, single provider + single-auth, single provider only
- Verify redirect when single provider and no single-auth

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt/ui
npm run lint
npm run ci-test
npm run build-prod
```
