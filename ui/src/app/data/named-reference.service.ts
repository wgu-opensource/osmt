import { Injectable } from "@angular/core"
import {AbstractDataService} from "./abstract-data.service"
import {HttpClient} from "@angular/common/http"
import {IAuthService} from "../auth/iauth-service"
import {Router} from "@angular/router"
import {Location} from "@angular/common"

@Injectable({
  providedIn: "root"
})
export class NamedReferenceService extends AbstractDataService{

  private baseServiceUrl = "api/named-references"

  constructor(protected httpClient: HttpClient, protected authService: IAuthService,
              protected router: Router, protected location: Location) {
    super(httpClient, authService, router, location)
  }
}
