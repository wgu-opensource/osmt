import { Injectable } from "@angular/core"
import {AbstractDataService} from "../../data/abstract-data.service"
import {HttpClient, HttpHeaders} from "@angular/common/http"
import {IAuthService} from "../../auth/iauth-service"
import {Router} from "@angular/router"
import {Location} from "@angular/common"
import {ApiSortOrder} from "../../richskill/ApiSkill"
import {Observable} from "rxjs"
import {map, share} from "rxjs/operators"
import {ApiJobCode, IJobCode} from "../Jobcode"
import {ApiTaskResult, ITaskResult} from "../../task/ApiTaskResult"
import {ApiBatchResult} from "../../richskill/ApiBatchResult"
import {AuthService} from "../../auth/auth-service"

@Injectable({
  providedIn: "root"
})
export class JobCodeService extends AbstractDataService{

  private baseServiceUrl = "api/job-codes"

  constructor(protected httpClient: HttpClient, protected authService: AuthService,
              protected router: Router, protected location: Location) {
    super(httpClient, authService, router, location)
  }

  getJobCodes(
    size: number = 50,
    from: number = 0,
    sort: ApiSortOrder | undefined
  ): Observable<PaginatedJobCodes> {
    const params = this.buildTableParams(size, from, undefined, sort)
    return this.get<ApiJobCode[]>({
      path: `${this.baseServiceUrl}`,
      params,
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedJobCodes(
          body || [],
          Number(headers.get("X-Total-Count"))
        )
      }))
  }

  getJobCodeById(id: string): Observable<ApiJobCode> {
    const errorMsg = `Could not find JobCode with id [${id}]`
    return this.get<ApiJobCode>({
      path: `${this.baseServiceUrl}/${id}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiJobCode(this.safeUnwrapBody(body, errorMsg))))
  }

  createJobCode(updateObject: IJobCode): Observable<ApiJobCode> {
    const errorMsg = `Error creating JobCode`
    return this.post<ApiJobCode[]>({
      path: this.baseServiceUrl,
      body: [updateObject]
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg).map(s => new ApiJobCode(s))[0]))
  }

  updateJobCode(id: string, updateObject: IJobCode): Observable<ApiJobCode> {
    const errorMsg = `Could not find JobCode with id: [${id}]`
    return this.post<IJobCode>({
      path: `${this.baseServiceUrl}/${id}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiJobCode(this.safeUnwrapBody(body, errorMsg))))
  }

  deleteJobCode(id: string): Observable<ApiTaskResult> {
    return this.httpClient.delete<ITaskResult>(this.buildUrl("api/job-codes/" + id + "/remove"), {
      headers: this.wrapHeaders(new HttpHeaders({
          Accept: "application/json"
        }
      ))
    })
      .pipe(share())
      .pipe(map((body) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  deleteJobCodeWithResult(id: string): Observable<ApiBatchResult> {
    return this.pollForTaskResult<ApiBatchResult>(this.deleteJobCode(id))
  }
}

export class PaginatedJobCodes {
  totalCount = 0
  jobCodes: ApiJobCode[] = []
  constructor(jobCodes: ApiJobCode[], totalCount: number) {
    this.jobCodes = jobCodes
    this.totalCount = totalCount
  }
}
