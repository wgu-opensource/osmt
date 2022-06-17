import {Component, OnInit} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import {AuthService} from "./auth-service"
import {AppConfig} from "../app.config"

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html"
})
export class LoginComponent implements OnInit {
  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    const loginUrl = AppConfig.settings.authUrl + "/as/authorization.oauth2?"
      + "client_id=" + AppConfig.settings.clientId
      + "&response_type=code"
      + "&redirect_uri=" + AppConfig.settings.redirectUrl

    if (this.authService.isAuthenticated()) {
      this.router.navigate([""])
      return
    }

    this.route.queryParams.subscribe(params => {
      const returnRoute = params.return
      if (returnRoute) {
        this.authService.storeReturn(returnRoute)
      }
      window.location.href = loginUrl
    })
  }
}
