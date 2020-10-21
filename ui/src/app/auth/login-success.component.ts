import {Component, OnInit} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import {AuthService} from "./auth-service"

@Component({
  selector: "app-login-success",
  template: "",
})
export class LoginSuccessComponent implements OnInit {
  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params.token
      this.authService.storeToken(token)
    })
    const returnRoute = this.authService.popReturn()
    this.router.navigate([returnRoute ?? ""])
  }
}
