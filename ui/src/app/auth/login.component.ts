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
    const loginUrl = AppConfig.settings.loginUrl
    window.location.href = loginUrl
  }
}
