import { Component, OnInit } from "@angular/core"
import { Location } from "@angular/common"
import {Whitelabelled} from "../../whitelabel"
import {AuthService} from "../auth/auth-service"
import {ActivatedRoute, Router} from "@angular/router"
import {AppConfig} from "../app.config"
import {ButtonAction} from "../auth/auth-roles"

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
})
export class HeaderComponent extends Whitelabelled implements OnInit {
  menuExpanded: boolean = false
  canHaveWorkspace = this.authService.isEnabledByRoles(ButtonAction.MyWorkspace)

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute, private location: Location) {
    super()
  }

  ngOnInit(): void {
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated()
  }

  showPublicNavbar(): boolean {
    const url = this.location.path()
    const pattern = /(api\/)?(skills|collections)\/[-0-9a-f]{36}$/   // exclude public canonical URL paths
    return !this.isAuthenticated() || url.match(pattern) !== null
  }

  handleClickMenu(): boolean {
    this.menuExpanded = !this.menuExpanded
    return false
  }

  get collectionsActive(): boolean {
    return window.location.pathname.startsWith("/collections")
  }

  get myWorkspaceActive(): boolean {
    return this.router.url.startsWith("/my-workspace")
  }

  get skillsActive(): boolean {
    return this.router.url.startsWith("/skills")
  }

  handleQuicklink(elementId: string): boolean {
    const el = document.getElementById(elementId) as HTMLElement
    if (el) {
      el.focus()
      el.scrollIntoView()
    }

    return false
  }
}
