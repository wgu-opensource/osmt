import { Component, OnInit } from "@angular/core"
import {Whitelabelled} from "../../whitelabel"
import {AuthService} from "../auth/auth-service"
import {Router} from "@angular/router"
import {AppConfig} from "../app.config"

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
})
export class HeaderComponent extends Whitelabelled implements OnInit {

  constructor(private authService: AuthService, private router: Router) {
    super()
  }

  ngOnInit(): void {
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated()
  }

  clickLogin(): boolean {
    const loginUrl = AppConfig.settings.loginUrl
    window.location.href = loginUrl
    return false
  }

  clickLogout(): boolean {
    this.router.navigate(["logout"])
    return false
  }
}
