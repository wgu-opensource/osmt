import {Injectable} from "@angular/core"
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router"
import {AuthService} from "./auth-service"
import {ToastService} from "../toast/toast.service"

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private router: Router, protected toastService: ToastService, private authService: AuthService) {

  }

  // tslint:disable-next-line:max-line-length
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {

    if (this.authService.isAuthenticated()) {

      if (route.data.roles){
        const userRoles = this.authService.getRole()?.split(",")
        for (const roles of userRoles){
          if (route.data.roles.indexOf(roles) !== -1) {
            return true
          }
        }
        this.toastService.showToast("Whoops!", "You need permission to perform this action. If this seems to be an error, please contact your OSMT administrator.")
        return false
      }
      return true
    }

    this.router.navigate(["/login"], {queryParams: {return: state.url}})
    return false
  }

}
