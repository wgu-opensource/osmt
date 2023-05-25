import { Location } from "@angular/common"
import { HttpClient, HttpResponse } from "@angular/common/http"
import { Inject, Injectable } from "@angular/core"
import { Router } from "@angular/router"
import { Observable } from "rxjs"
import { share } from "rxjs/operators"
import { AbstractService, ApiGetParams } from "../abstract.service"
import { AuthService } from "../auth/auth-service"

@Injectable({ providedIn: "root" })
export abstract class AbstractDataService extends AbstractService {

  protected constructor(
    httpClient: HttpClient,
    authService: AuthService,
    router: Router,
    location: Location,
    @Inject("BASE_API") base: string
  ) {
    super(httpClient, authService, router, location, base)
  }

  /**
   * Perform a patch request.
   *
   *   const {body, headers, status, type, url} = response
   *
   * @param path The relative path to the endpoint
   * @param headers Json blob defining headers
   * @param params Json blob defining path params
   * @param body Json blob defining the changes to be applied to the object
   */
  patch<T>({path, headers, params, body}: ApiGetParams): Observable<HttpResponse<T>> {
    const observable =  this.httpClient.patch<T>(this.buildUrl(path + "/update"), body, {
      headers: this.wrapHeaders(headers),
      params,
      observe: "response"}).pipe(share())
    observable
      .subscribe(() => {}, (err) => { this.redirectToLogin(err) })
    return observable
  }

}
