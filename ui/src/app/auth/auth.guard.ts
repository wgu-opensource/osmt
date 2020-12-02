import {Injectable} from "@angular/core"
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router"
import {AuthService} from "./auth-service"

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private authService: AuthService) {

  }

  // tslint:disable-next-line:max-line-length
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {

    if (this.authService.isAuthenticated()) {
      return true
    }

    this.router.navigate(["/login"], {queryParams: {return: state.url}})
    return false
  }

}
