import { ENABLE_ROLES, ButtonAction, ActionByRoles} from "./auth-roles"
import { Injectable } from "@angular/core"
import { Router } from "@angular/router"
import { DEFAULT_INTERRUPTSOURCES, Idle } from "@ng-idle/core"
import { Keepalive } from "@ng-idle/keepalive"
import { Whitelabelled } from "../../whitelabel"
import { IAuthService } from "./iauth-service"

export const STORAGE_KEY_TOKEN = "OSMT.AuthService.accessToken"
export const STORAGE_KEY_RETURN = "OSMT.AuthService.return"
export const STORAGE_KEY_ROLE = "OSMT.AuthService.role"

@Injectable()
export class AuthService extends Whitelabelled implements IAuthService {
  serverIsDown = false

  constructor(
    private router: Router,
    private idle: Idle,
    private keepalive: Keepalive,
  ) {
    super()
  }

  init(): void {
    // N/A
  }

  setup(): void {
    this.watchForIdle()
  }

  start(returnPath: string): void {
    this.router.navigate(["/login"], { queryParams: { return: returnPath } })
  }

  storeToken(accessToken: string): void {
    localStorage.setItem(STORAGE_KEY_TOKEN, accessToken)
    localStorage.setItem(STORAGE_KEY_ROLE, JSON.parse(atob(accessToken.split(".")[1]))?.roles)
  }

  storeReturn(returnRoute: string): void {
    localStorage.setItem(STORAGE_KEY_RETURN, returnRoute)
  }

  restoreReturnAsync(): void {
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

  hasRole(requiredRoles: string[], userRoles: string[]): boolean {
    for (const role of userRoles) {
      if (requiredRoles?.indexOf(role) !== -1) {
        return true
      }
    }
    return false
  }

  isEnabledByRoles(buttonAction : ButtonAction): boolean {
    if (ENABLE_ROLES) {
      const allowedRoles = ActionByRoles.get(buttonAction) ?? [];
      const userRoles = this.getRole()?.split(",");
      return this.hasRole(allowedRoles, userRoles ?? []);
    }
    return true;
  }

  private watchForIdle(): void {
    this.idle.setIdle(this.whitelabel.idleTimeoutInSeconds)
    this.idle.setTimeout(1)
    this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES)

    this.idle.onTimeout.subscribe(() => {
      console.log("Idle time out!")
      this.router.navigate(["/logout"], {queryParams: {timeout: true}})
    })
    this.keepalive.interval(15)
    this.idle.watch()
  }
}
