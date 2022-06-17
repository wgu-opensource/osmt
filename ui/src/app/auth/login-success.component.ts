import {Component, OnInit} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import { Observable } from "rxjs"
import {AuthService} from "./auth-service"
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http"
import {AuthToken} from "./auth-token"
import {AppConfig} from "../app.config"

@Component({
  selector: "app-login-success",
  template: "",
})
export class LoginSuccessComponent implements OnInit {
  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService,
              private http: HttpClient) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params =>
      this.processAuthCode(params.code).subscribe(
        result => {
          this.authService.storeToken(result.access_token)
          const returnRoute = this.authService.popReturn()
          if (returnRoute === "autoclose") {
            window.close()
          } else {
            this.router.navigate([returnRoute ?? ""])
          }
        })
    )
  }

  private processAuthCode(authCode: string): Observable<AuthToken> {
    const params = new HttpParams()
      .set("grant_type", "authorization_code")
      .set("code", authCode)
      .set("redirect_uri", AppConfig.settings.redirectUrl)
    const clientIdHash = AppConfig.settings.clientIdHash
    const headers = new HttpHeaders()
      .set("Authorization", `Basic ${clientIdHash}`)
      .set("Content-Type", "application/x-www-form-urlencoded")
    return this.http.post<AuthToken>(AppConfig.settings.authUrl + "/as/token.oauth2", params, {
      headers
    })
  }
}
