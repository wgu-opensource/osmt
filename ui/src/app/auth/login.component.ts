import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { siApple, siAuth0, siGithub, siGoogle, siOkta } from 'simple-icons';
import { AuthService } from './auth-service';
import { AppConfig } from '../app.config';
import { AuthProvider } from '../models/app-config.model';

const PROVIDER_ICONS: Record<string, { path: string; hex: string }> = {
  apple: { path: siApple.path, hex: siApple.hex },
  auth0: { path: siAuth0.path, hex: siAuth0.hex },
  github: { path: siGithub.path, hex: siGithub.hex },
  google: { path: siGoogle.path, hex: siGoogle.hex },
  okta: { path: siOkta.path, hex: siOkta.hex },
};

const ID_ALIASES: Record<string, string> = {};

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

  getIcon(providerId: string): { path: string; hex: string } | null {
    const slug = ID_ALIASES[providerId] ?? providerId;
    return PROVIDER_ICONS[slug] ?? null;
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
