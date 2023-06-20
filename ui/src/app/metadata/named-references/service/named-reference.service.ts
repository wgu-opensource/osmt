import { Injectable } from "@angular/core"
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http"
import { Router } from "@angular/router"
import { Location } from "@angular/common"
import { AuthService } from "../../../auth/auth-service"
import { AbstractDataService } from "../../../data/abstract-data.service"
import {ApiSortOrder} from "../../../richskill/ApiSkill";
import {Observable} from "rxjs";
import {PaginatedMetadata} from "../../PaginatedMetadata";
import {map, share} from "rxjs/operators";
import {ApiNamedReference, INamedReference} from "../NamedReference";
import {PublishStatus} from "../../../PublishStatus";
import {ApiSearch, PaginatedSkills} from "../../../richskill/service/rich-skill-search.service";
import {ApiSkillSummary} from "../../../richskill/ApiSkillSummary";
import {ApiTaskResult, ITaskResult} from "../../../task/ApiTaskResult";
import {ApiBatchResult} from "../../../richskill/ApiBatchResult";

@Injectable({
  providedIn: "root"
})
export class NamedReferenceService extends AbstractDataService{

  private baseServiceUrl = "api/metadata/keywords"

  constructor(protected httpClient: HttpClient, protected authService: AuthService,
              protected router: Router, protected location: Location) {
    super(httpClient, authService, router, location)
  }

  paginatedNamedReferences(
    size = 50,
    from = 0,
    sort: ApiSortOrder | undefined,
    type: string,
    query: string | undefined
  ): Observable<PaginatedMetadata> {
    const params = new HttpParams({
      fromObject: {size, from, sort: sort ?? "", query: query ?? "", type: type}
    })
    return this.get<ApiNamedReference[]>({
      path: `${this.baseServiceUrl}`,
      params,
    }).pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedMetadata(
          body || [],
          Number(headers.get("X-Total-Count"))
        )
      }))
  }

  getNamedReferenceById(id: string): Observable<ApiNamedReference> {
    const errorMsg = `Could not find NamedReference with id [${id}]`
    return this.get<ApiNamedReference>({
      path: `${this.baseServiceUrl}/${id}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiNamedReference(this.safeUnwrapBody(body, errorMsg))))
  }

  createNamedReference(newObject: INamedReference): Observable<INamedReference> {
    const errorMsg = `Error creating NamedReference`
    return this.post<INamedReference[]>({
      path: this.baseServiceUrl,
      body: [newObject]
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg).map(s => new ApiNamedReference(s))[0]))
  }

  updateNamedReference(id: string, updateObject: INamedReference): Observable<ApiNamedReference> {
    const errorMsg = `Could not find NamedReference with id: [${id}]`
    return this.post<INamedReference>({
      path: `${this.baseServiceUrl}/${id}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiNamedReference(this.safeUnwrapBody(body, errorMsg))))
  }

  deleteNamedReferenceWithResult(id: number): Observable<ApiBatchResult> {
    return this.pollForTaskResult<ApiBatchResult>(this.deleteNamedReference(id))
  }

  deleteNamedReference(id: number): Observable<ApiTaskResult> {
    return this.httpClient.delete<ITaskResult>(this.buildUrl("api/metadata/keywords/" + id + "/remove"), {
      headers: this.wrapHeaders(new HttpHeaders({
          Accept: "application/json"
        }
      ))
    })
      .pipe(share())
      .pipe(map((body) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  getRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedSkills> {
    const errorMsg = `Could not find skills in category [${entityId}]`

    return this.get<ApiSkillSummary[]>({
      path: `${this.baseServiceUrl}/${entityId}/skills`,
      headers: new HttpHeaders({
        Accept: "application/json"
      }),
      params: this.buildTableParams(size, from, statusFilters, sort),
    })
      .pipe(share())
      .pipe(map(({body, headers}) =>
        new PaginatedSkills(this.safeUnwrapBody(body, errorMsg), Number(headers.get("X-Total-Count")))
      ))
  }

  searchRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder,
    apiSearch?: ApiSearch
  ): Observable<PaginatedSkills> {
    const errorMsg = `Could not find skills in category [${entityId}]`

    return this.post<ApiSkillSummary[]>({
      path: `${this.baseServiceUrl}/${entityId}/skills`,
      headers: new HttpHeaders({
        Accept: "application/json"
      }),
      params: this.buildTableParams(size, from, statusFilters, sort),
      body: apiSearch ?? new ApiSearch({})
    })
      .pipe(share())
      .pipe(map(({body, headers}) =>
        new PaginatedSkills(this.safeUnwrapBody(body, errorMsg), Number(headers.get("X-Total-Count")))
      ))
  }
}
