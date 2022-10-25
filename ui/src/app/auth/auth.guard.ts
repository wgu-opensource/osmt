import {Injectable} from "@angular/core"
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router"
import {AuthRoles} from "./auth-roles"
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
        if (!AuthRoles.instance.isEnabled || this.authService.hasRole(requiredRoles, userRoles)) {
          return true
        }
        this.toastService.showToast("Whoops!", "You need permission to perform this action. If this seems to be an error, please contact your OSMT administrator.")
        return false
      }
      return true
    }

    this.authService.start(state.url)
    return false
  }

}
