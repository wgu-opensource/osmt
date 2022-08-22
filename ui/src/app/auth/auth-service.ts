import {ENABLE_ROLES, ROLES_AUTHORITY} from "./auth-roles";

export const STORAGE_KEY_TOKEN = "OSMT.AuthService.accessToken"
export const STORAGE_KEY_RETURN = "OSMT.AuthService.return"
export const STORAGE_KEY_ROLE = "OSMT.AuthService.role"

export class AuthService {
  serverIsDown = false

  constructor() {
  }

  storeToken(accessToken: string): void {
    localStorage.setItem(STORAGE_KEY_TOKEN, accessToken)
    localStorage.setItem(STORAGE_KEY_ROLE, JSON.parse(atob(accessToken.split(".")[1])).roles)
  }

  storeReturn(returnRoute: string): void {
    localStorage.setItem(STORAGE_KEY_RETURN, returnRoute)
  }

  popReturn(): string | null {
    const ret = localStorage.getItem(STORAGE_KEY_RETURN)
    localStorage.removeItem(STORAGE_KEY_RETURN)
    return ret
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY_TOKEN)
  }

  currentAuthToken(): string | null {
    return localStorage.getItem(STORAGE_KEY_TOKEN)
  }

  isAuthenticated(): boolean {
    return !this.serverIsDown && this.currentAuthToken() !== null
  }

  setServerIsDown(isDown: boolean): void {
    this.serverIsDown = isDown
  }

  getRole(): string {
    return localStorage.getItem(STORAGE_KEY_ROLE) as string
  }

  isDisabledByRoles(path : string): boolean {
    if (!ENABLE_ROLES) {
      return false;
    }

    let disabled = true;
    const allowedRoles = ROLES_AUTHORITY[path];
    const userRoles = this.getRole()?.split(",");

    for (const roles of userRoles) {
      if (allowedRoles.indexOf(roles) !== -1) {
        disabled = false
      }
    }

    return disabled
  }
}
