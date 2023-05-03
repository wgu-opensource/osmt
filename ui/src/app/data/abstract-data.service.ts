import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Router } from "@angular/router"
import { Location } from "@angular/common"
import { AuthService } from "../auth/auth-service"
import { AbstractService } from "../abstract.service"

@Injectable({ providedIn: "root" })
export abstract class AbstractDataService extends AbstractService {

  protected constructor(httpClient: HttpClient, authService: AuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

}
