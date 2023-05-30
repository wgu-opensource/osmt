import { Location } from "@angular/common"
import { HttpClient, HttpHeaders } from "@angular/common/http"
import { Inject, Injectable } from "@angular/core"
import { Router } from "@angular/router"

import { Observable, of, throwError } from "rxjs"
import { delay, map, retryWhen, share, switchMap } from "rxjs/operators"

import { ApiSearch, PaginatedSkills } from "./rich-skill-search.service"
import { ApiBatchResult } from "../ApiBatchResult"
import { ApiAuditLog, ApiSkill, ApiSortOrder, IAuditLog, ISkill } from "../ApiSkill"
import { ApiSkillSummary, ISkillSummary } from "../ApiSkillSummary"
import { ApiSkillUpdate } from "../ApiSkillUpdate"
import { AbstractService } from "../../abstract.service"
import { AuthService } from "../../auth/auth-service"
import { PublishStatus } from "../../PublishStatus"
import { ApiTaskResult, ITaskResult } from "../../task/ApiTaskResult"


@Injectable({
  providedIn: "root"
})
export class RichSkillService extends AbstractService {

  constructor(
    httpClient: HttpClient,
    authService: AuthService,
    router: Router,
    location: Location,
    @Inject("BASE_API") baseApi: string
  ) {
    super(httpClient, authService, router, location, baseApi)
  }

  private serviceUrl = "skills"

  getSkillsFiltered(
    size: number = 50,
    from: number = 0,
    apiSearch: ApiSearch,
    filterByStatuses: Set<PublishStatus> | undefined,
    sort: ApiSortOrder | undefined,
  ): Observable<PaginatedSkills> {

    const params = this.buildTableParams(size, from, filterByStatuses, sort)
    return this.post<ApiSkillSummary[]>({
      path: `${this.serviceUrl}/filter`,
      params,
      body: apiSearch
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedSkills(
          body?.map(skill => skill) || [],
          Number(headers.get("X-Total-Count"))
        )
      }))
  }

  getSkillByUUID(uuid: string): Observable<ApiSkill> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.get<ISkill>({
      path: `${this.serviceUrl}/${uuid}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiSkill(this.safeUnwrapBody(body, errorMsg))))
  }

  // These two nearly identical getSkill functions can probably be joined.  the angular httpclient is weird though
  // and overloads it's functions many times which makes any kind of abstraction seeking to broaden flexibility incompatible
  getSkillCsvByUuid(uuid: string): Observable<string> {
    if (!uuid) {
      throw new Error("No uuid provided for single skill csv export")
    }
    const errorMsg = `Could not find skill by uuid [${uuid}]`

    return this.httpClient
      .get(this.buildUrl(`${this.serviceUrl}/${uuid}`), {
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "text/csv"
          }
        )),
        responseType: "text",
        observe: "response"
      })
      .pipe(share())
      .pipe(map((response) => this.safeUnwrapBody(response.body, errorMsg)))
  }

  // These two nearly identical getSkill functions can probably be joined.  the angular httpclient is weird though
  // and overloads it's functions many times which makes any kind of abstraction seeking to broaden flexibility incompatible
  getSkillXlsxByUuid(uuid: string): Observable<string> {
    if (!uuid) {
      throw new Error("No uuid provided for single skill xlsx export")
    }
    const errorMsg = `Could not find skill by uuid [${uuid}]`

    return this.httpClient
      .get(this.buildUrl(`${this.serviceUrl}/${uuid}`), {
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "application/vnd.ms-excel"
          }
        )),
        responseType: "blob" as "json",
        observe: "response"
      })
      .pipe(share())
      .pipe(map((response) => this.safeUnwrapBody(response.body as string, errorMsg)))
  }

  getSkillJsonByUuid(uuid: string): Observable<string> {
    if (!uuid) {
      throw new Error("No uuid provided for single skill json export")
    }
    const errorMsg = `Could not find skill by uuid [${uuid}]`

    return this.httpClient
      .get(this.buildUrl(`${this.serviceUrl}/${uuid}`), {
        headers: this.wrapHeaders(new HttpHeaders(
          {
            Accept: "application/json"
          }
        )),
        responseType: "text",
        observe: "response"
      })
      .pipe(share())
      .pipe(map((response) => this.safeUnwrapBody(response.body, errorMsg)))
  }

  createSkill(updateObject: ApiSkillUpdate, pollIntervalMs: number = 1000): Observable<ApiSkill> {
    return this.pollForTaskResult<ApiSkill[]>(
      this.createSkills([updateObject]),
      pollIntervalMs
    ).pipe(map(it => it !== undefined ? it[0] : it))
  }

  createSkills(updateObjects: ApiSkillUpdate[]): Observable<ApiTaskResult> {
    const errorMsg = `Error creating skill`
    return this.post<ITaskResult>({
      path: this.serviceUrl,
      body: updateObjects
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, errorMsg))))
  }

  updateSkill(uuid: string, updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.post<ISkill>({
      path: `${this.serviceUrl}/${uuid}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiSkill(this.safeUnwrapBody(body, errorMsg))))
  }


  searchSkills(
    apiSearch: ApiSearch,
    size?: number,
    from?: number,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedSkills> {
    const errorMsg = `Failed to unwrap response for skill search`

    const params = this.buildTableParams(size, from, filterByStatuses, sort)

    return this.post<ApiSkillSummary[]>({
      path: "search/skills",
      params,
      body: apiSearch,
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        const totalCount = Number(headers.get("X-Total-Count"))
        const skills = body?.map(skill => skill) || []
        return new PaginatedSkills(skills, !isNaN(totalCount) ? totalCount : skills.length)
      }))
  }

  libraryExportCsv(): Observable<ApiTaskResult> {
    return this.httpClient
      .get<ApiTaskResult>(
        this.buildUrl("export/library/csv"), {
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "application/json"
          }
        )),
        observe: "response"
      })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  libraryExportXlsx(): Observable<ApiTaskResult> {
    return this.httpClient
      .get<ApiTaskResult>(
        this.buildUrl("export/library/xlsx"), {
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "application/json"
          }
        )),
        observe: "response"
      })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  exportSearchCsv(
    apiSearch: ApiSearch,
    filterByStatuses?: Set<PublishStatus>,
  ): Observable<ApiTaskResult> {
    const params = this.buildTableParams(undefined, undefined, filterByStatuses, undefined)
    return this.httpClient.post<ApiTaskResult>(this.buildUrl("export/skills/csv"), apiSearch, {
      params,
      headers: this.wrapHeaders(new HttpHeaders({
          Accept: "application/json"
        }
      )),
      observe: "response"
    })
    .pipe(share())
    .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  exportSearchXlsx(
    apiSearch: ApiSearch,
    filterByStatuses?: Set<PublishStatus>,
  ): Observable<ApiTaskResult> {
    const params = this.buildTableParams(undefined, undefined, filterByStatuses, undefined)
    return this.httpClient.post<ApiTaskResult>(
      this.buildUrl("export/skills/xlsx"), apiSearch, {
        params,
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "application/json"
          }
        )),
        observe: "response"
      })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  /**
   * Check if result exported with libraryExport() is ready if not check again every 1000 milliseconds.
   * @param url Url to get RSD library exported as csv
   * @param pollIntervalMs Milliseconds to retry request
   */
  getResultExportedCsvLibrary(url: string, pollIntervalMs: number = 1000): Observable<any> {
    return this.httpClient
      .get(this.buildUrl(url), {
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "text/csv"
          }
        )),
        responseType: "text",
        observe: "response"
      })
      .pipe(
        retryWhen(errors => errors.pipe(
          switchMap((error) => {
            if (error.status === 404) {
              return of(error.status)
            }
            return throwError(error)
          }),
          delay(pollIntervalMs),
      )))
  }

  /**
   * Check if result exported with libraryExport() is ready if not check again every 1000 milliseconds.
   * @param url Url to get RSD library exported as xlsx
   * @param pollIntervalMs Milliseconds to retry request
   */
  getResultExportedXlsxLibrary(url: string, pollIntervalMs: number = 1000): Observable<any> {
    console.log(`endpoint: ${url}`)
    return this.httpClient
      .get(this.buildUrl(url), {
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "application/vnd.ms-excel;charset=utf-8;"
          }
        )),
        responseType: "blob" as "json",
        observe: "response"
      })
      .pipe(
        retryWhen(errors => errors.pipe(
          switchMap((error) => {
            if (error.status === 404) {
              return of(error.status)
            }
            return throwError(error)
          }),
          delay(pollIntervalMs),
      )))
  }

  publishSkillsWithResult(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    collectionUuid?: string,
    pollIntervalMs: number = 1000,
  ): Observable<ApiBatchResult> {
    return this.pollForTaskResult(
      this.bulkStatusChange("skills/publish", apiSearch, newStatus, filterByStatuses, collectionUuid),
      pollIntervalMs
    )
  }

  auditLog(
    skillUuid?: string
  ): Observable<ApiAuditLog[]> {
    return this.get<IAuditLog[]>({
      path: `${this.serviceUrl}/${skillUuid}/log`,
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return body?.map(it => new ApiAuditLog(it)) || []
      }))
  }

  similarityCheck(statement: string): Observable<ApiSkillSummary[]> {
    return this.post<ISkillSummary[]>({
      path: "search/skills/similarity",
      body: {statement}
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return body?.map(it => new ApiSkillSummary(it)) || []
      }))
  }

  similaritiesCheck(statements: string[]): Observable<boolean[]> {
    return this.post<boolean[]>({
      path: "search/skills/similarities",
      body: statements.map(statement => ({statement}))
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return body || []
      }))
  }

}
