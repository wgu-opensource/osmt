import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  siApple,
  siAuth0,
  siFusionauth,
  siGithub,
  siGoogle,
  siKeycloak,
  siOkta,
  siOpenid,
} from 'simple-icons';
import { AuthService } from './auth-service';
import { AppConfig } from '../app.config';
import { AuthProvider } from '../models/app-config.model';

// Curated allowlist of bundled simple-icons marks. An iconSlug resolves only
// against this map — we do not import the whole package (bundle size). Brands
// absent here (e.g. PingFederate, Microsoft Entra) are supplied via iconUrl.
const PROVIDER_ICONS: Record<string, { path: string; hex: string }> = {
  apple: { path: siApple.path, hex: siApple.hex },
  auth0: { path: siAuth0.path, hex: siAuth0.hex },
  fusionauth: { path: siFusionauth.path, hex: siFusionauth.hex },
  github: { path: siGithub.path, hex: siGithub.hex },
  google: { path: siGoogle.path, hex: siGoogle.hex },
  keycloak: { path: siKeycloak.path, hex: siKeycloak.hex },
  okta: { path: siOkta.path, hex: siOkta.hex },
  openid: { path: siOpenid.path, hex: siOpenid.hex },
};

const ID_ALIASES: Record<string, string> = {};

// Registration id of the generic OIDC provider. It never falls back to a
// built-in mark: no icon appears unless the deployment configures one.
const GENERIC_OIDC_ID = 'oidc';

export type ResolvedIcon =
  | { kind: 'url'; url: string }
  | { kind: 'svg'; path: string; hex: string }
  | null;

const DEFAULT_READ_ONLY_MESSAGE =
  "This is the public skill browser. Use your organization's authoring URL to edit.";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  oauthProviders: AuthProvider[] = [];
  singleAuthEnabled = false;
  baseApiUrl = '';
  username = '';
  password = '';
  loginError = '';
  isLoading = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['']);
      return;
    }

    this.oauthProviders = AppConfig.settings.authProviders ?? [];
    this.singleAuthEnabled = AppConfig.settings.singleAuthEnabled ?? false;
    this.baseApiUrl = AppConfig.settings.baseApiUrl ?? '';

    this.route.queryParams.subscribe(params => {
      const returnRoute = params['return'];
      if (returnRoute) {
        this.authService.storeReturn(returnRoute);
      }
    });
  }

  get isReadOnly(): boolean {
    return AppConfig.settings.readOnlyMode === true;
  }

  get readOnlyMessageText(): string {
    return DEFAULT_READ_ONLY_MESSAGE;
  }

  get authoringWelcomeText(): string {
    return AppConfig.settings.authoringWelcomeMessage?.trim() ?? '';
  }

  get showLoginPage(): boolean {
    if (this.isReadOnly) {
      return false;
    }
    return this.oauthProviders.length >= 1 || this.singleAuthEnabled;
  }

  getIcon(provider: AuthProvider): ResolvedIcon {
    // 1. Explicit image URL wins (institution-hosted brand asset).
    if (provider.iconUrl) {
      return { kind: 'url', url: provider.iconUrl };
    }
    // 2. Explicit bundled icon slug, resolved against the curated allowlist only.
    const slug = provider.iconSlug ?? ID_ALIASES[provider.id];
    if (slug && PROVIDER_ICONS[slug]) {
      const icon = PROVIDER_ICONS[slug];
      return { kind: 'svg', path: icon.path, hex: icon.hex };
    }
    // 3. Built-in default keyed on registration id — never for the generic
    //    oidc provider, which shows no icon unless one is configured.
    const builtin = PROVIDER_ICONS[provider.id];
    if (builtin && provider.id !== GENERIC_OIDC_ID) {
      return { kind: 'svg', path: builtin.path, hex: builtin.hex };
    }
    return null;
  }

  async onLogin(event?: Event): Promise<void> {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    if (!this.username || !this.password) {
      this.loginError = 'Please enter both username and password';
      return;
    }

    this.isLoading = true;
    this.loginError = '';

    try {
      await this.authService.login(this.username, this.password);
      const returnRoute = this.authService.popReturn() || '';
      this.router.navigate([returnRoute]);
    } catch (error: unknown) {
      const err = error as { error?: { error?: string } };
      this.loginError =
        err?.error?.error || 'Login failed. Please check your credentials.';
    } finally {
      this.isLoading = false;
    }
  }
}
