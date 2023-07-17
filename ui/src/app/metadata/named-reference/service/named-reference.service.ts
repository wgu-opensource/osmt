import { Inject, Injectable } from "@angular/core"
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http"
import { Router } from "@angular/router"
import { Location } from "@angular/common"
import { AuthService } from "../../../auth/auth-service"
import { AbstractDataService } from "../../../data/abstract-data.service"
import { ApiSortOrder } from "../../../richskill/ApiSkill";
import { Observable } from "rxjs";
import { PaginatedMetadata } from "../../PaginatedMetadata";
import { map, share } from "rxjs/operators";
import { ApiNamedReference, ApiNamedReferenceUpdate, NamedReferenceInterface } from "../NamedReference";
import { ApiTaskResult, ITaskResult } from "../../../task/ApiTaskResult";
import { ApiBatchResult } from "../../../richskill/ApiBatchResult";

@Injectable({
  providedIn: "root"
})
export class NamedReferenceService extends AbstractDataService {

  protected serviceUrl = "metadata/keywords"

  constructor(
    protected httpClient: HttpClient,
    protected authService: AuthService,
    protected router: Router,
    protected location: Location,
    @Inject("BASE_API") baseApi: string
  ) {
    super(
      httpClient,
      authService,
      router,
      location,
      baseApi
    );
  }

  paginated(
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
      path: `${this.serviceUrl}`,
      params,
    }).pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedMetadata(
          body || [],
          Number(headers.get("X-Total-Count"))
        )
      }))
  }

  getById(id: number): Observable<ApiNamedReference> {
    const errorMsg = `Could not find NamedReference with id [${id}]`
    return this.get<ApiNamedReference>({
      path: `${this.serviceUrl}/${id}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiNamedReference(this.safeUnwrapBody(body, errorMsg))))
  }

  create(newObject: ApiNamedReferenceUpdate): Observable<ApiNamedReference> {
    const errorMsg = `Error creating NamedReference`
    return this.post<NamedReferenceInterface>({
      path: this.serviceUrl,
      body: newObject
    })
      .pipe(share())
      .pipe(map(({body}) =>
        new ApiNamedReference(this.safeUnwrapBody<ApiNamedReference>(body, errorMsg)))
      )
  }

  update(id: number, updateObject: ApiNamedReferenceUpdate): Observable<ApiNamedReference> {
    const errorMsg = `Could not find NamedReference with id: [${id}]`
    return this.post<NamedReferenceInterface>({
      path: `${this.serviceUrl}/${id}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiNamedReference(this.safeUnwrapBody(body, errorMsg))))
  }

  deleteNamedReferenceWithResult(id: number): Observable<ApiBatchResult> {
    return this.pollForTaskResult<ApiBatchResult>(this.delete(id))
  }

  delete(id: number): Observable<ApiTaskResult> {
    return this.httpClient.delete<ITaskResult>(this.buildUrl("api/metadata/keywords/" + id + "/remove"), {
      headers: this.wrapHeaders(new HttpHeaders({
          Accept: "application/json"
        }
      ))
    })
      .pipe(share())
      .pipe(map((body) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }
}
