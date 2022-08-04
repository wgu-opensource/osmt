import {Injectable} from "@angular/core"
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router"
import {ENABLE_ROLES} from "./auth-roles"
import {AuthService} from "./auth-service"
import {ToastService} from "../toast/toast.service"

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private router: Router, protected toastService: ToastService, private authService: AuthService) {

  }

  // tslint:disable-next-line:max-line-length
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {

    if (this.authService.isAuthenticated()) {

      const requiredRoles = route.data?.roles
      if (requiredRoles) {
        const userRoles = this.authService.getRole()?.split(",")
        if (!ENABLE_ROLES || this.hasRole(requiredRoles, userRoles)) {
          return true
        }
        this.toastService.showToast("Whoops!", "You need permission to perform this action. If this seems to be an error, please contact your OSMT administrator.")
        return false
      }
      return true
    }

    this.router.navigate(["/login"], {queryParams: {return: state.url}})
    return false
  }

  private hasRole(requiredRoles: string[], userRoles: string[]): boolean {
    for (const role of userRoles) {
      if (requiredRoles?.indexOf(role) !== -1) {
        return true
      }
    }
    return false
  }

}
