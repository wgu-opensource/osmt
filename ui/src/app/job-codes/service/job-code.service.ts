import { Injectable } from "@angular/core"
import { HttpClient, HttpHeaders } from "@angular/common/http"
import { Router } from "@angular/router"
import { Location } from "@angular/common"
import { AbstractDataService } from "../../data/abstract-data.service"
import { ApiSortOrder } from "../../richskill/ApiSkill"
import { Observable } from "rxjs"
import { map, share } from "rxjs/operators"
import { ApiJobCode, IJobCode, IJobCodeUpdate } from "../Jobcode"
import { ApiTaskResult } from "../../task/ApiTaskResult"
import { ApiBatchResult } from "../../richskill/ApiBatchResult"
import { AuthService } from "../../auth/auth-service"

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

  getJobCodeByCode(code: string): Observable<ApiJobCode> {
    const errorMsg = `Could not find JobCode with target Node [${code}]`
    return this.get<ApiJobCode>({
      path: `${this.baseServiceUrl}/${code}`
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

  updateJobCode(targetNode: string, updateObject: IJobCodeUpdate): Observable<ApiJobCode> {
    const errorMsg = `Could not find JobCode with id: [${targetNode}]`
    return this.post<IJobCode>({
      path: `${this.baseServiceUrl}/${targetNode}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiJobCode(this.safeUnwrapBody(body, errorMsg))))
  }

  deleteJobCode(id: string): Observable<ApiTaskResult> {
    return this.delete<ApiTaskResult>( {
      path: `${this.baseServiceUrl}/${id}/remove`,
      headers: new HttpHeaders({
        Accept: "application/json"
      })
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
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
