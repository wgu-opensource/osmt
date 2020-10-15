import {HttpClient, HttpHeaders, HttpParams, HttpResponse} from "@angular/common/http"
import {AppConfig} from "./app.config"
import {Observable} from "rxjs"
import {AuthService} from "./auth/auth-service";

interface ApiGetParams {
  path: string,
  headers?: HttpHeaders,
  params?: HttpParams | {
    [param: string]: string | string[];
  },
  body?: unknown
}

export abstract class AbstractService {

  constructor(protected httpClient: HttpClient, protected authService: AuthService) {
  }

  /**
   * Perform a get request with a json response.  The resulting promise will return the whole response object, whose
   * properties can be destructured.
   *
   *   const {body, headers, status, type, url} = response
   *
   * @param path The relative path to the endpoint
   * @param headers Json blob defining headers
   * @param params Json blob defining path params
   */
  get<T>({path, headers, params}: ApiGetParams): Observable<HttpResponse<T>> {
    return this.httpClient.get<T>(this.buildUrl(path), {
      headers: this.wrapHeaders(headers),
      params,
      observe: "response"})
  }
  post<T>({path, headers, params, body}: ApiGetParams): Observable<HttpResponse<T>> {
    return this.httpClient.post<T>(this.buildUrl(path), body, {
      headers: this.wrapHeaders(headers),
      params,
      observe: "response"})
  }

  protected safeUnwrapBody<T>(body: T | null, failureMessage: string): T {
    if (!body) {
      throw new Error(failureMessage)
    }
    return body
  }

  protected buildUrl(path: string): string {
    const baseUrl = AppConfig.settings.baseApiUrl

    // if user defined, make sure it delineates between the host and path
    if (baseUrl && !baseUrl.endsWith("/")) {
      return baseUrl + "/" + path
    } else {
      return baseUrl + path
    }
  }

  wrapHeaders(headers?: HttpHeaders): HttpHeaders | undefined {
    const token = this.authService.currentAuthToken()
    if (token !== undefined) {
      if (headers === undefined) {
        headers = new HttpHeaders()
      }
      headers = headers.set("Authorization", `Bearer ${token}`)
    }
    return headers
  }
}
