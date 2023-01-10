import {Injectable} from "@angular/core"
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http"
import {Observable, of, throwError} from "rxjs"
import {ApiAuditLog, ApiSkill, ApiSortOrder, IAuditLog, ISkill} from "../ApiSkill"
import {delay, map, retryWhen, share, switchMap} from "rxjs/operators"
import {AbstractService} from "../../abstract.service"
import {ApiSkillUpdate} from "../ApiSkillUpdate"
import {AuthService} from "../../auth/auth-service"
import {ApiSearch, PaginatedSkills} from "./rich-skill-search.service"
import {PublishStatus} from "../../PublishStatus"
import {ApiBatchResult} from "../ApiBatchResult"
import {ApiTaskResult, ITaskResult} from "../../task/ApiTaskResult"
import {ApiSkillSummary, ISkillSummary} from "../ApiSkillSummary"
import {Router} from "@angular/router"
import {Location} from "@angular/common"


@Injectable({
  providedIn: "root"
})
export class RichSkillService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

  private serviceUrl = "api/skills"

  getSkills(
    size: number = 50,
    from: number = 0,
    filterByStatuses: Set<PublishStatus> | undefined,
    sort: ApiSortOrder | undefined,
  ): Observable<PaginatedSkills> {

    const params = this.buildTableParams(size, from, filterByStatuses, sort)
    return this.get<ApiSkillSummary[]>({
      path: `${this.serviceUrl}`,
      params,
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
      path: "api/search/skills",
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

  libraryExport(): Observable<ApiTaskResult> {
    return this.httpClient
      .get<ApiTaskResult>(this.buildUrl("api/export/library"), {
        headers: this.wrapHeaders(new HttpHeaders({
            Accept: "application/json"
          }
        )),
        observe: "response"
      })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  exportSearch(uuids: string[]): Observable<ApiTaskResult> {
    return this.httpClient.post<ApiTaskResult>(this.buildUrl("api/export/skills"), uuids, {
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
  getResultExportedLibrary(url: string, pollIntervalMs: number = 1000): Observable<any> {
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

  publishSkillsWithResult(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    collectionUuid?: string,
    pollIntervalMs: number = 1000,
  ): Observable<ApiBatchResult> {
    return this.pollForTaskResult(
      this.bulkStatusChange("api/skills/publish", apiSearch, newStatus, filterByStatuses, collectionUuid),
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
      path: "api/search/skills/similarity",
      body: {statement}
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return body?.map(it => new ApiSkillSummary(it)) || []
      }))
  }

  similaritiesCheck(statements: string[]): Observable<boolean[]> {
    return this.post<boolean[]>({
      path: "api/search/skills/similarities",
      body: statements.map(statement => ({statement}))
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return body || []
      }))
  }

}
