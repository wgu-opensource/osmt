import { Component, OnInit } from "@angular/core"
import {Whitelabelled} from "../../whitelabel"
import {AuthService} from "../auth/auth-service"
import {ActivatedRoute, Router} from "@angular/router"
import {AppConfig} from "../app.config"

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
})
export class HeaderComponent extends Whitelabelled implements OnInit {
  menuExpanded: boolean = false

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) {
    super()
  }

  ngOnInit(): void {
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated()
  }

  handleClickMenu(): boolean {
    this.menuExpanded = !this.menuExpanded
    return false
  }

  get collectionsActive(): boolean {
    return window.location.pathname.startsWith("/collections")
  }

  get skillsActive(): boolean {
    return window.location.pathname.startsWith("/skills")
  }
}
