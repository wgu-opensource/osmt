import { Injectable } from "@angular/core"
import {HttpClient, HttpHeaders, HttpParams, HttpResponse} from "@angular/common/http"
import {Observable} from "rxjs"
import {share} from "rxjs/operators"
import {AbstractService} from "../abstract.service"
import {IAuthService} from "../auth/iauth-service"
import {Router} from "@angular/router"
import {Location} from "@angular/common"


interface ApiGetParams {
  path: string,
  headers?: HttpHeaders,
  params?: HttpParams | {
    [param: string]: string | string[];
  },
  body?: unknown
}
@Injectable({
providedIn: "root"
})
export abstract class AbstractDataService extends AbstractService{

  protected constructor(httpClient: HttpClient, authService: IAuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

}
