import { Injectable } from "@angular/core"
import {HttpClient, HttpResponse} from "@angular/common/http"
import { Router } from "@angular/router"
import { Location } from "@angular/common"
import { AuthService } from "../auth/auth-service"
import {AbstractService, ApiGetParams} from "../abstract.service"
import {Observable} from "rxjs"
import {share} from "rxjs/operators"

@Injectable({ providedIn: "root" })
export abstract class AbstractDataService extends AbstractService {

  protected constructor(httpClient: HttpClient, authService: AuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

  /**
   * Perform a delete request with a json response.  The resulting promise will return the whole
   * deleted response object
   *
   *   const {body, headers, status, type, url} = response
   *
   * @param path The relative path to the endpoint
   * @param headers Json blob defining headers
   * @param params Json blob defining path params
   */
  delete<T>({path, headers, params}: ApiGetParams): Observable<HttpResponse<T>> {
    const observable =  this.httpClient.delete<T>(this.buildUrl(path),  {
      headers: this.wrapHeaders(headers),
      params,
      observe: "response"}).pipe(share())
    observable
      .subscribe(() => {}, (err) => { this.redirectToLogin(err) })
    return observable
  }

  /**
   * Perform a patch request with a json response.  The resulting promise will return the whole
   * modified response object
   *
   *   const {body, headers, status, type, url} = response
   *
   * @param path The relative path to the endpoint
   * @param headers Json blob defining headers
   * @param params Json blob defining path params
   * @param body Json blob defining the changes to be applied to the object
   */
  patch<T>({path, headers, params, body}: ApiGetParams): Observable<HttpResponse<T>> {
    const observable =  this.httpClient.patch<T>(this.buildUrl(path), body, {
      headers: this.wrapHeaders(headers),
      params,
      observe: "response"}).pipe(share())
    observable
      .subscribe(() => {}, (err) => { this.redirectToLogin(err) })
    return observable
  }

}
