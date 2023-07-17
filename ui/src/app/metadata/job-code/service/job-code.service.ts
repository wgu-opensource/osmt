import { Location } from "@angular/common"
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http"
import { Inject, Injectable } from "@angular/core"
import { Router } from "@angular/router"
import { Observable } from "rxjs"
import { map, share } from "rxjs/operators"
import { ApiJobCode, IJobCode, IJobCodeUpdate } from "../Jobcode"
import { AuthService } from "../../../auth/auth-service"
import { ApiSortOrder } from "../../../richskill/ApiSkill"
import { ApiBatchResult } from "../../../richskill/ApiBatchResult"
import { ApiTaskResult, ITaskResult } from "../../../task/ApiTaskResult"
import { PaginatedMetadata } from "../../PaginatedMetadata"
import { AbstractDataService } from "../../../data/abstract-data.service"

@Injectable({
  providedIn: "root"
})
export class JobCodeService extends AbstractDataService {

  protected serviceUrl = "metadata/jobcodes"

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
    query: string | undefined
  ): Observable<PaginatedMetadata> {
    const params = new HttpParams({
      fromObject: {size, from, sort: sort ?? "", query: query ?? ""}
    })
    return this.get<ApiJobCode[]>({
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

  getById(id: number): Observable<ApiJobCode> {
    const errorMsg = `Could not find JobCode with id [${id}]`
    return this.get<ApiJobCode>({
      path: `${this.serviceUrl}/${id}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiJobCode(this.safeUnwrapBody(body, errorMsg))))
  }

  create(newObject: IJobCode): Observable<ApiJobCode> {
    const errorMsg = `Error creating JobCode`
    return this.post<ApiJobCode[]>({
      path: this.serviceUrl,
      body: [newObject]
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg).map(s => new ApiJobCode(s))[0]))
  }

  update(id: number, updateObject: IJobCodeUpdate): Observable<ApiJobCode> {
    const errorMsg = `Could not find JobCode with id: [${id}]`
    return this.post<IJobCode>({
      path: `${this.serviceUrl}/${id}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiJobCode(this.safeUnwrapBody(body, errorMsg))))
  }

  deleteWithResult(id: number): Observable<ApiBatchResult> {
    return this.pollForTaskResult<ApiBatchResult>(this.delete(id))
  }

  delete(id: number): Observable<ApiTaskResult> {
    return this.httpClient.delete<ITaskResult>(this.buildUrl("api/metadata/jobcodes/" + id + "/remove"), {
      headers: this.wrapHeaders(new HttpHeaders({
          Accept: "application/json"
        }
      ))
    })
      .pipe(share())
      .pipe(map((body) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }
}
