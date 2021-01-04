import {HttpClient, HttpHeaders, HttpParams, HttpResponse} from "@angular/common/http"
import {AppConfig} from "./app.config"
import {Observable, Subscription} from "rxjs"
import {AuthService} from "./auth/auth-service";
import {ApiTaskResult, ITaskResult} from "./task/ApiTaskResult";
import {PublishStatus} from "./PublishStatus";
import {ApiSortOrder} from "./richskill/ApiSkill";
import {ApiBatchResult} from "./richskill/ApiBatchResult";
import {ApiSearch} from "./richskill/service/rich-skill-search.service";
import {map, share} from "rxjs/operators";
import {DefaultUrlSerializer, Router, UrlSerializer} from "@angular/router";
import {Location} from "@angular/common";

interface ApiGetParams {
  path: string,
  headers?: HttpHeaders,
  params?: HttpParams | {
    [param: string]: string | string[];
  },
  body?: unknown
}

export abstract class AbstractService {

  constructor(protected httpClient: HttpClient,
              protected authService: AuthService,
              protected router: Router,
              protected location: Location
  )
  {
  }

  redirectToLogin(error: any): void {
    const status: number = error?.status ?? 500
    if (status === 401) {
      this.authService.logout()
      window.open("/login?return=autoclose", "_blank")
      return
    }
    else if (status === 0) {
      this.authService.setServerIsDown(true)
    }
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
    const observable = this.httpClient.get<T>(this.buildUrl(path), {
      headers: this.wrapHeaders(headers),
      params,
      observe: "response"})
    observable
      .pipe(share())
      .subscribe(() => {}, (err) => { this.redirectToLogin(err) })
    return observable
  }
  post<T>({path, headers, params, body}: ApiGetParams): Observable<HttpResponse<T>> {
    const observable =  this.httpClient.post<T>(this.buildUrl(path), body, {
      headers: this.wrapHeaders(headers),
      params,
      observe: "response"})
    observable
      .pipe(share())
      .subscribe(() => {}, (err) => { this.redirectToLogin(err) })
    return observable
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
    if (token) {
      if (headers === undefined) {
        headers = new HttpHeaders()
      }
      headers = headers.set("Authorization", `Bearer ${token}`)
    }
    return headers
  }

  pollForTaskResult(obs: Observable<ApiTaskResult>, pollIntervalMs: number = 1000): Observable<ApiBatchResult> {
    return new Observable((observer) => {
      obs.subscribe(task => {
        this.observableForTaskResult<ApiBatchResult>(task, pollIntervalMs).subscribe(result => {
          observer.next(result)
          if (result) {
            observer.complete()
          }
        })
      })
    })
  }

  observableForTaskResult<T>(task: ApiTaskResult, pollIntervalMs: number = 1000): Observable<T> {
    return new Observable((observer) => {

      const tick = () => {
        this.httpClient.get<any>(task.id, {
          headers: this.wrapHeaders(),
          observe: "response"
        }).subscribe(({body, status}) => {
          if (status === 200) {
            observer.next(body as T)
            observer.complete()
          } else {
            observer.next(undefined)
            setTimeout(() => tick(), pollIntervalMs)
          }
        })
      }

      tick()

    })
  }

  buildTableParams(
    size: number | undefined,
    from: number | undefined,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder): any {

    const params: any = {
      sort
    }
    if (filterByStatuses !== undefined) {
      params.status = Array.from(filterByStatuses).map(s => s.toString())
    }
    if (size !== undefined) { params.size = size }
    if (from !== undefined) { params.from = from }
    if (sort !== undefined) { params.sort = sort.toString()}

    return params
  }


  bulkStatusChange(
    path: string,
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    collectionUuid?: string
  ): Observable<ApiTaskResult> {
    const params: any = {
      newStatus: newStatus.toString(),
    }
    if (filterByStatuses !== undefined) {
      params.filterByStatus = Array.from(filterByStatuses).map(s => s.toString())
    }
    if (collectionUuid !== undefined) {
      params.collectionUuid = collectionUuid
    }
    return this.post<ITaskResult>({
      path,
      params,
      body: apiSearch
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }
}
