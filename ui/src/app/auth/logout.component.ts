import {Component, OnInit} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import {AuthService} from "./auth-service"

@Component({
  selector: "app-logout",
  templateUrl: "./logout.component.html"
})
export class LogoutComponent implements OnInit {
  private isTimeout: boolean = false

  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.logout()
    this.route.queryParams.subscribe(params => {
      this.isTimeout = params.timeout || false
    })
  }

  get title(): string {
    return "You're logged out of OSMT."
  }

  get body(): string {
    return this.isTimeout ?
      "You have been logged out due to inactivity." :
      "Important: You still might be logged into your identity provider. If you're using a public computer, be sure to log out there and close your browser window."
  }
}
