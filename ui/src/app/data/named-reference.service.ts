import { Inject, Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Router } from "@angular/router"
import { Location } from "@angular/common"
import { AuthService } from "../auth/auth-service"
import { AbstractDataService } from "./abstract-data.service"

@Injectable({
  providedIn: "root"
})
export class NamedReferenceService extends AbstractDataService{

  private baseServiceUrl = "named-references"

  constructor(
    protected httpClient: HttpClient,
    protected authService: AuthService,
    protected router: Router,
    protected location: Location,
    @Inject("BASE_API") baseApi: string
  ) {
    super(httpClient, authService, router, location, baseApi)
  }
}
