import {Component, OnInit} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import { AppConfig } from "../app.config"
import {AuthService} from "./auth-service"
import {Title} from "@angular/platform-browser";
import {Whitelabelled} from "../../whitelabel";

@Component({
  selector: "app-logout",
  templateUrl: "./logout.component.html"
})
export class LogoutComponent extends Whitelabelled implements OnInit {
  private isTimeout = false

  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService,
              protected titleService: Title
  ) {
    super()
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Logged Out | ${this.whitelabel.toolName}`)

    this.authService.logout()
    this.route.queryParams.subscribe(params => {
      this.isTimeout = params.timeout || false

      if (AppConfig.settings.logoutUrl) {
        // This will circumvent the need for the message in the body() method.
        window.location.href = AppConfig.settings.logoutUrl
      }
    })
  }

  get title(): string {
    return `You're logged out of ${this.whitelabel.toolName}.`
  }

  get body(): string {
    return this.isTimeout ?
      "You have been logged out due to inactivity." :
      "Important: You still might be logged into your identity provider. If you're using a public computer, be sure to log out there and close your browser window."
  }
}
