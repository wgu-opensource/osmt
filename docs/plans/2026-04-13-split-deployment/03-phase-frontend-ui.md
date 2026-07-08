# Phase 3: Frontend UI Adaptations for Read-Only Mode

## Scope of Phase

Update the Angular frontend to adapt UI based on the instance type:

1. Update `IAppConfig` model with split deployment fields
2. Update `login.component.ts` and `.html` for read-only/author display
3. Update `header.component.ts` and `.html` to hide login in read-only mode

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### 1. Update `IAppConfig` Model

**File**: `ui/src/app/models/app-config.model.ts`

Add split deployment fields:

```typescript
export interface AuthProvider {
  id: string;
  name: string;
  authorizationUrl: string;
}

export interface IAppConfig {
  baseApiUrl: string;
  loginUrl: string;
  authMode?: string;
  authProviders?: AuthProvider[];
  singleAuthEnabled?: boolean;
  editableAuthor: boolean;
  defaultAuthorValue: string;
  toolName: string;
  toolNameLong: string;
  publicSkillTitle: string;
  publicCollectionTitle: string;
  licensePrimary: string;
  licenseSecondary: string;
  poweredBy: string;
  poweredByUrl: string;
  poweredByLabel: string;
  idleTimeoutInSeconds: number;
  colorBrandAccent1?: string;
  logoUrl?: string;
  // NEW: Split deployment fields
  instanceType?: 'read-only' | 'writable';
  writableInstanceUrl?: string;
  writableInstanceName?: string;
  readOnlyMessage?: string;
}

// Default configuration
export class DefaultAppConfig implements IAppConfig {
  baseApiUrl = '';
  loginUrl = '';
  editableAuthor = true;
  defaultAuthorValue = '';
  toolName = 'OSMT';
  toolNameLong = 'Open Skills Management Tool';
  publicSkillTitle = 'Rich Skill Descriptor';
  publicCollectionTitle = 'Rich Skill Descriptor Collection';
  licensePrimary = 'Copyright © OSMT Contributors';
  licenseSecondary = 'All rights reserved.';
  poweredBy = '';
  poweredByUrl = '';
  poweredByLabel = '';
  idleTimeoutInSeconds = 24 * 60 * 60;
  colorBrandAccent1 = undefined;
  logoUrl = '/assets/images/logo-light.svg';
  dynamicWhitelabel = false;
  authProviders: AuthProvider[] = [];
  singleAuthEnabled = false;
  // NEW: Default to writable (single-instance deployments)
  instanceType = 'writable';
  writableInstanceUrl = '';
  writableInstanceName = 'Author Portal';
  readOnlyMessage = 'This is the public skill browser. To edit content, visit the Author Portal.';
}
```

### 2. Update Login Component

**File**: `ui/src/app/auth/login.component.ts`

Add helper properties:

```typescript
export class LoginComponent implements OnInit {
  oauthProviders: AuthProvider[] = [];
  singleAuthEnabled = false;
  baseApiUrl = '';
  username = '';
  password = '';
  loginError = '';
  isLoading = false;

  // NEW: Split deployment properties
  get isReadOnly(): boolean {
    return AppConfig.settings.instanceType === 'read-only';
  }

  get writableInstanceUrl(): string {
    return AppConfig.settings.writableInstanceUrl || '';
  }

  get writableInstanceName(): string {
    return AppConfig.settings.writableInstanceName || 'Author Portal';
  }

  get readOnlyMessage(): string {
    return AppConfig.settings.readOnlyMessage || 
      'This is the public skill browser. To edit content, visit the Author Portal.';
  }

  // ... rest of component
}
```

**File**: `ui/src/app/auth/login.component.html`

Update template to handle read-only mode:

```html
<div class="l-stickyBar">
  <div class="l-stickySidebar l-container">
    <div class="l-stickySidebar-x-content t-padding-medium t-padding-top">
      <div class="m-skillBackground l-skillBackground">
        <div class="t-margin-medium t-margin-bottom">
          <h3 class="m-iconTitle">
            <div class="m-iconTitle-x-label">
              <ng-container *ngIf="isReadOnly; else loginTitle">
                Public Skill Browser
              </ng-container>
              <ng-template #loginTitle>Sign In</ng-template>
            </div>
          </h3>
        </div>

        <!-- Read-Only Mode: Show message with link to writable instance -->
        <ng-container *ngIf="isReadOnly">
          <div class="t-margin-medium t-margin-bottom">
            <p class="t-type-body">{{ readOnlyMessage }}</p>
            <div class="t-margin-medium t-margin-top" *ngIf="writableInstanceUrl">
              <a
                class="m-button"
                [href]="writableInstanceUrl + '/login'"
                role="button"
              >
                <span class="m-button-x-text">Go to {{ writableInstanceName }}</span>
              </a>
            </div>
          </div>
        </ng-container>

        <!-- Writable Mode: Show normal login UI -->
        <ng-container *ngIf="!isReadOnly">
          <ng-container *ngIf="showLoginPage">
            <div
              *ngIf="oauthProviders?.length"
              class="t-margin-medium t-margin-bottom"
            >
              <p class="t-type-body">Sign in with your organization account:</p>
              <div class="oauth-providers">
                <a
                  *ngFor="let provider of oauthProviders"
                  class="m-button oauth-provider-button"
                  [attr.href]="provider.authorizationUrl"
                  role="button"
                  aria-label="Sign in with {{ provider.name }}"
                >
                  <span
                    class="oauth-provider-button-x-icon"
                    *ngIf="getIcon(provider.id) as icon"
                  >
                    <svg class="t-icon" viewBox="0 0 24 24" aria-hidden="true">
                      <path [attr.d]="icon.path" [attr.fill]="'#' + icon.hex" />
                    </svg>
                  </span>
                  <span class="m-button-x-text">{{ provider.name }}</span>
                </a>
              </div>
            </div>

            <div *ngIf="singleAuthEnabled" class="t-margin-medium">
              <p
                *ngIf="oauthProviders?.length"
                class="t-type-body t-margin-small"
              >
                Or sign in with an OSMT username:
              </p>
              <form (ngSubmit)="onLogin($event)" class="login-form">
                <!-- ... existing form fields ... -->
              </form>
            </div>
          </ng-container>

          <div *ngIf="!showLoginPage" class="t-margin-medium">
            <p class="t-type-body">
              No login options available. Ensure the backend is running at
              {{ baseApiUrl }} and OAuth or single-auth is configured.
            </p>
          </div>
        </ng-container>
      </div>
    </div>
  </div>
</div>
```

### 3. Update Header Component

**File**: `ui/src/app/navigation/header.component.ts`

Add helper properties:

```typescript
export class HeaderComponent extends Whitelabelled implements OnInit {
  menuExpanded = false;

  // NEW: Split deployment properties
  get isReadOnly(): boolean {
    return AppConfig.settings.instanceType === 'read-only';
  }

  get writableInstanceUrl(): string {
    return AppConfig.settings.writableInstanceUrl || '';
  }

  get writableInstanceName(): string {
    return AppConfig.settings.writableInstanceName || 'Author Portal';
  }

  // ... existing properties and methods
}
```

**File**: `ui/src/app/navigation/header.component.html`

Update to hide login button in read-only mode:

```html
<!-- ... existing nav items ... -->

<ul class="m-navBar-x-desktopNav">
  <li>
    <a
      class="m-navItem"
      [class.m-navItem-is-active]="skillsActive"
      [attr.aria-current]="skillsActive ? 'page' : null"
      routerLink="/skills"
      >RSD Library</a
    >
  </li>
  <li>
    <a
      class="m-navItem"
      [class.m-navItem-is-active]="collectionsActive"
      [attr.aria-current]="collectionsActive ? 'page' : null"
      routerLink="/collections"
      >Collections</a
    >
  </li>
  <li>
    <a
      class="m-navItem"
      [class.m-navItem-is-active]="categoriesActive"
      [attr.aria-current]="categoriesActive ? 'page' : null"
      routerLink="/categories"
      >Categories</a
    >
  </li>
  
  <!-- Login button: Hidden in read-only mode -->
  <li *ngIf="!isReadOnly && !isAuthenticated()">
    <a
      class="m-navItem"
      routerLink="/login"
      [queryParams]="{ return: getCurrentUrl() }"
      >Login</a
    >
  </li>
  
  <!-- Writable instance link: Optional, shown in read-only mode if configured -->
  <li *ngIf="isReadOnly && writableInstanceUrl">
    <a
      class="m-navItem"
      [href]="writableInstanceUrl"
      target="_blank"
      rel="noopener noreferrer"
      >{{ writableInstanceName }}</a
    >
  </li>
  
  <!-- Workspace and sync: Only when authenticated (writable mode) -->
  <li *ngIf="isAuthenticated() && canHaveWorkspace" id="li-my-workspace">
    <a
      class="m-navItem"
      [class.m-navItem-is-active]="myWorkspaceActive"
      routerLink="/my-workspace"
    >
      My Workspace
    </a>
  </li>
  <li *ngIf="isAuthenticated() && canSyncManage" id="li-sync">
    <a
      class="m-navItem"
      [class.m-navItem-is-active]="syncActive"
      routerLink="/admin/sync"
    >
      Sync
    </a>
  </li>
  <li *ngIf="isAuthenticated()">
    <a
      class="m-iconInteractive m-iconInteractive-onDark"
      routerLink="/logout"
    >
      <span class="t-visuallyHidden">Icon Interactive</span>
      <span class="m-iconInteractive-x-icon">
        <svg aria-hidden="true">
          <use xlink:href="/assets/images/svg-defs.svg#icon-logout"></use>
        </svg>
      </span>
    </a>
  </li>
</ul>

<!-- ... rest of header ... -->
```

### 4. Tests

**File**: `ui/src/app/auth/login.component.spec.ts`

Add tests for read-only mode:

```typescript
describe('LoginComponent', () => {
  // ... existing tests

  it('should show read-only message when instanceType is read-only', () => {
    // Mock AppConfig.settings with instanceType = 'read-only'
    // Verify read-only message is displayed
    // Verify login form is not displayed
  });

  it('should show login form when instanceType is writable', () => {
    // Mock AppConfig.settings with instanceType = 'writable'
    // Verify normal login UI is displayed
  });
});
```

**File**: `ui/src/app/navigation/header.component.spec.ts`

Add tests for header in read-only mode:

```typescript
describe('HeaderComponent', () => {
  // ... existing tests

  it('should hide login button in read-only mode', () => {
    // Mock AppConfig.settings with instanceType = 'read-only'
    // Verify login button is not present
  });

  it('should show author portal link in read-only mode when configured', () => {
    // Mock AppConfig.settings with instanceType = 'read-only' and writableInstanceUrl
    // Verify author portal link is displayed
  });
});
```

## Validate

Run the frontend tests:

```bash
cd /Users/yona/dev/skybridge/osmt/ui
npm test -- --include="login.component.spec.ts,header.component.spec.ts"
```

Check formatting:

```bash
npm run format:check
```

Fix any formatting issues:

```bash
npx prettier --write src/app/auth/login.component.ts src/app/auth/login.component.html
npx prettier --write src/app/navigation/header.component.ts src/app/navigation/header.component.html
npx prettier --write src/app/models/app-config.model.ts
```
