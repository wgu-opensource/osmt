import { ENABLE_ROLES, ButtonAction, ActionByRoles } from './auth-roles';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { DEFAULT_INTERRUPTSOURCES, Idle } from '@ng-idle/core';
import { Keepalive } from '@ng-idle/keepalive';
import { Whitelabelled } from '../../whitelabel';
import { IAuthService } from './iauth-service';
import { AppConfig } from '../app.config';

export const STORAGE_KEY_TOKEN = 'OSMT.AuthService.accessToken';
export const STORAGE_KEY_RETURN = 'OSMT.AuthService.return';
export const STORAGE_KEY_ROLE = 'OSMT.AuthService.role';
export const STORAGE_KEY_NOAUTH = 'OSMT.AuthService.noauth';

@Injectable()
export class AuthService extends Whitelabelled implements IAuthService {
  serverIsDown = false;

  constructor(
    private router: Router,
    private http: HttpClient,
    private idle: Idle,
    private keepalive: Keepalive
  ) {
    super();
  }

  init(): void {
    // N/A
  }

  setup(): void {
    this.watchForIdle();
  }

  start(returnPath: string): void {
    const safeReturn = returnPath?.startsWith('/logout')
      ? undefined
      : returnPath;
    this.router.navigate(['/login'], {
      queryParams: safeReturn ? { return: safeReturn } : {},
    });
  }

  storeToken(accessToken: string): void {
    localStorage.setItem(STORAGE_KEY_TOKEN, accessToken);
    try {
      const decoded = JSON.parse(atob(accessToken.split('.')[1]));
      const rawRoles = decoded?.roles;
      if (rawRoles !== undefined) {
        const roleValue = Array.isArray(rawRoles)
          ? rawRoles.join(',')
          : String(rawRoles);
        if (roleValue) {
          localStorage.setItem(STORAGE_KEY_ROLE, roleValue);
        }
      }
    } catch (e) {
      // Token may not be a JWT, ignore
    }
  }

  /**
   * Authenticates admin user with username/password.
   *
   * Calls the login API endpoint and stores the returned JWT token.
   * On successful login, stores the token and admin role in localStorage.
   *
   * @param username - Admin username
   * @param password - Admin password
   * @returns Promise that resolves on successful login, rejects on failure
   */
  async login(username: string, password: string): Promise<void> {
    try {
      const response = await this.http
        .post<{
          token: string;
          expiresIn: number;
          tokenType: string;
        }>('/api/auth/login', { username, password })
        .toPromise();

      if (response?.token) {
        // Store the JWT token
        this.storeToken(response.token);
        // Store admin role for UI authorization checks
        localStorage.setItem(STORAGE_KEY_ROLE, 'ROLE_Osmt_Admin');
        localStorage.setItem(STORAGE_KEY_NOAUTH, 'true'); // Keep for backward compatibility
      } else {
        throw new Error('No token received from server');
      }
    } catch (error) {
      if (error instanceof HttpErrorResponse) {
        throw error;
      }
      throw new Error('Login failed');
    }
  }

  storeReturn(returnRoute: string): void {
    if (returnRoute?.startsWith('/logout')) {
      return;
    }
    localStorage.setItem(STORAGE_KEY_RETURN, returnRoute);
  }

  restoreReturnAsync(): void {
    // Implementation not needed for current auth flow
  }

  popReturn(): string | null {
    const ret = localStorage.getItem(STORAGE_KEY_RETURN);
    localStorage.removeItem(STORAGE_KEY_RETURN);
    return ret?.startsWith('/logout') ? null : ret;
  }

  logout(): void {
    const baseUrl = AppConfig.settings?.baseApiUrl ?? '';
    const logoutUrl = baseUrl
      ? `${baseUrl.replace(/\/$/, '')}/api/auth/logout`
      : '/api/auth/logout';
    this.http
      .post(logoutUrl, {}, { withCredentials: true })
      .toPromise()
      .catch(() => {});
    localStorage.removeItem(STORAGE_KEY_TOKEN);
    localStorage.removeItem(STORAGE_KEY_NOAUTH);
    localStorage.removeItem(STORAGE_KEY_ROLE);
    localStorage.removeItem(STORAGE_KEY_RETURN);
  }

  currentAuthToken(): string | null {
    return localStorage.getItem(STORAGE_KEY_TOKEN);
  }

  isAuthenticated(): boolean {
    // In single-auth mode, check if admin auth is set
    if (localStorage.getItem(STORAGE_KEY_NOAUTH) === 'true') {
      return !this.serverIsDown && this.getRole() !== null;
    }
    return !this.serverIsDown && this.currentAuthToken() !== null;
  }

  setServerIsDown(isDown: boolean): void {
    this.serverIsDown = isDown;
  }

  getRole(): string {
    return localStorage.getItem(STORAGE_KEY_ROLE) as string;
  }

  hasRole(requiredRoles: string[], userRoles: string[]): boolean {
    for (const role of userRoles) {
      if (requiredRoles?.indexOf(role) !== -1) {
        return true;
      }
    }
    return false;
  }

  isEnabledByRoles(buttonAction: ButtonAction): boolean {
    if (ENABLE_ROLES) {
      const allowedRoles = ActionByRoles.get(buttonAction) ?? [];
      const userRoles = this.getRole()?.split(',');
      return this.hasRole(allowedRoles, userRoles ?? []);
    }
    return true;
  }

  private watchForIdle(): void {
    this.idle.setIdle(this.whitelabel.idleTimeoutInSeconds);
    this.idle.setTimeout(1);
    this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);

    this.idle.onTimeout.subscribe(() => {
      console.log('Idle time out!');
      this.router.navigate(['/logout'], { queryParams: { timeout: true } });
    });
    this.keepalive.interval(15);
    this.idle.watch();
  }
}
