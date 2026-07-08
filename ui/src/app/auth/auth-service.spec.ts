import {
  AuthService,
  STORAGE_KEY_NOAUTH,
  STORAGE_KEY_RETURN,
  STORAGE_KEY_ROLE,
  STORAGE_KEY_TOKEN,
} from './auth-service';
import { AppConfig } from '../app.config';

function makeJwt(payload: Record<string, unknown>): string {
  const enc = btoa(JSON.stringify(payload));
  return `header.${enc}.sig`;
}

const mockHttp = {
  post: () => ({ toPromise: () => Promise.resolve() }),
};

describe('AuthService', () => {
  // @ts-ignore
  const authService = new AuthService(null, mockHttp, null, null);

  beforeEach(() => {
    localStorage.removeItem(STORAGE_KEY_RETURN);
    localStorage.removeItem(STORAGE_KEY_ROLE);
    localStorage.removeItem(STORAGE_KEY_TOKEN);
    localStorage.removeItem(STORAGE_KEY_NOAUTH);
    AppConfig.settings = { baseApiUrl: '' } as never;
  });

  it('should be created', () => {
    expect(authService).toBeTruthy();
  });

  it('storeReturn should not store /logout path', () => {
    authService.storeReturn('/logout');
    expect(localStorage.getItem(STORAGE_KEY_RETURN)).toBeNull();
  });

  it('storeReturn should not store /logout path with query params', () => {
    authService.storeReturn('/logout?timeout=true');
    expect(localStorage.getItem(STORAGE_KEY_RETURN)).toBeNull();
  });

  it('storeReturn should store valid return paths', () => {
    authService.storeReturn('/skills');
    expect(localStorage.getItem(STORAGE_KEY_RETURN)).toBe('/skills');
  });

  it('popReturn should return null for stored /logout path', () => {
    localStorage.setItem(STORAGE_KEY_RETURN, '/logout');
    expect(authService.popReturn()).toBeNull();
  });

  it('popReturn should return path for valid stored paths', () => {
    localStorage.setItem(STORAGE_KEY_RETURN, '/skills');
    expect(authService.popReturn()).toBe('/skills');
  });

  it('should return true with correct roles', () => {
    const requiredRoles: string[] = ['admin', 'curator'];
    const userRoles: string[] = ['curator', 'viewer'];
    expect(authService.hasRole(requiredRoles, userRoles)).toEqual(true);
  });

  it('should return false with incorrect roles', () => {
    const requiredRoles: string[] = ['admin', 'curator'];
    const userRoles: string[] = ['guest', 'viewer'];
    expect(authService.hasRole(requiredRoles, userRoles)).toEqual(false);
  });

  it('storeToken should extract role from roles claim', () => {
    const jwt = makeJwt({ roles: 'ROLE_Osmt_Admin' });
    authService.storeToken(jwt);
    expect(localStorage.getItem(STORAGE_KEY_ROLE)).toBe('ROLE_Osmt_Admin');
  });

  it('storeToken should extract role from roles array', () => {
    const jwt = makeJwt({ roles: ['ROLE_Osmt_View', 'ROLE_Osmt_Admin'] });
    authService.storeToken(jwt);
    expect(localStorage.getItem(STORAGE_KEY_ROLE)).toBe(
      'ROLE_Osmt_View,ROLE_Osmt_Admin'
    );
  });

  it('logout should clear all auth storage including STORAGE_KEY_RETURN', () => {
    localStorage.setItem(STORAGE_KEY_TOKEN, 'token');
    localStorage.setItem(STORAGE_KEY_ROLE, 'ROLE_Admin');
    localStorage.setItem(STORAGE_KEY_RETURN, '/skills');
    authService.logout();
    expect(localStorage.getItem(STORAGE_KEY_TOKEN)).toBeNull();
    expect(localStorage.getItem(STORAGE_KEY_ROLE)).toBeNull();
    expect(localStorage.getItem(STORAGE_KEY_RETURN)).toBeNull();
  });
});
