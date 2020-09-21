import {HttpClient, HttpHeaders, HttpParams, HttpResponse} from "@angular/common/http"
import {AppConfig} from "./app.config"
import {Observable} from "rxjs"

interface ApiGetParams {
  path: string,
  headers?: HttpHeaders | {
    [header: string]: string | string[];
  },
  params?: HttpParams | {
    [param: string]: string | string[];
  }
}

export abstract class AbstractService {

  constructor(protected httpClient: HttpClient) {
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
    return this.httpClient.get<T>(this.buildUrl(path), { headers, params, observe: "response"})
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
}
