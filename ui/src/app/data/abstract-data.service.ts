import { Location } from "@angular/common"
import { HttpClient, HttpHeaders, HttpResponse } from "@angular/common/http"
import { Inject, Injectable } from "@angular/core"
import { Router } from "@angular/router"

import { Observable } from "rxjs"
import { map, share } from "rxjs/operators"

import { AbstractService, ApiGetParams, IRelatedSkillsService } from "../abstract.service"
import { AuthService } from "../auth/auth-service"
import { PublishStatus } from "../PublishStatus"
import { ApiSortOrder } from "../richskill/ApiSkill"
import { ApiSearch, PaginatedSkills } from "../richskill/service/rich-skill-search.service"
import { ApiSkillSummary } from "../richskill/ApiSkillSummary"
import { ApiJobCode, IJobCode, IJobCodeUpdate } from "../metadata/job-code/Jobcode"
import { ApiNamedReference, ApiNamedReferenceUpdate } from "../metadata/named-reference/NamedReference"
import { ApiTaskResult } from "../task/ApiTaskResult"
import { ApiBatchResult } from "../richskill/ApiBatchResult"

@Injectable({ providedIn: "root" })
export abstract class AbstractDataService extends AbstractService implements IRelatedSkillsService<number> {

  protected abstract serviceUrl: string;

  protected constructor(
    httpClient: HttpClient,
    authService: AuthService,
    router: Router,
    location: Location,
    @Inject("BASE_API") baseApi: string
  ) {
    super(httpClient, authService, router, location, baseApi);
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
      observe: "response"}).pipe(share());
    observable
      .subscribe(() => {}, (err) => { this.redirectToLogin(err) });
    return observable;
  }

  abstract getById(id: number): Observable<ApiJobCode | ApiNamedReference>

  abstract create(newObject: ApiNamedReferenceUpdate | IJobCode): Observable<ApiJobCode | ApiNamedReference>

  abstract update(id: number, updateObject: ApiNamedReferenceUpdate | IJobCodeUpdate): Observable<ApiJobCode | ApiNamedReference>

  abstract delete(id: number): Observable<ApiTaskResult>

  abstract deleteWithResult(id: number): Observable<ApiBatchResult>

  getRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedSkills> {
    const errorMsg = `Could not find skills for metadata [${entityId}]`;

    return this.post<ApiSkillSummary[]>({
      path: `${this.serviceUrl}/${entityId}/skills`,
      headers: new HttpHeaders({
        Accept: "application/json"
      }),
      params: this.buildTableParams(size, from, statusFilters, sort),
    })
      .pipe(share())
      .pipe(map(({body, headers}) =>
        new PaginatedSkills(this.safeUnwrapBody(body, errorMsg), Number(headers.get("X-Total-Count")))
      ));
  }

  searchRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder,
    apiSearch?: ApiSearch
  ): Observable<PaginatedSkills> {
    const errorMsg = `Could not find skills for metadata [${entityId}]`;

    return this.post<ApiSkillSummary[]>({
      path: `${this.serviceUrl}/${entityId}/skills`,
      headers: new HttpHeaders({
        Accept: "application/json"
      }),
      params: this.buildTableParams(size, from, statusFilters, sort),
      body: apiSearch ?? new ApiSearch({})
    })
      .pipe(share())
      .pipe(map(({body, headers}) =>
        new PaginatedSkills(this.safeUnwrapBody(body, errorMsg), Number(headers.get("X-Total-Count")))
      ));
  }

}
