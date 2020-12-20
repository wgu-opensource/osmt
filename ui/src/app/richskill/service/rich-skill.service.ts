import {Injectable} from "@angular/core"
import {HttpClient, HttpHeaders} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiAuditLog, ApiSkill, ApiSortOrder, IAuditLog, ISkill} from "../ApiSkill"
import {map, share} from "rxjs/operators"
import {AbstractService} from "../../abstract.service"
import {ApiSkillUpdate} from "../ApiSkillUpdate"
import {AuthService} from "../../auth/auth-service"
import {ApiSearch, PaginatedSkills} from "./rich-skill-search.service"
import {PublishStatus} from "../../PublishStatus"
import {ApiBatchResult} from "../ApiBatchResult"
import {ApiTaskResult, ITaskResult} from "../../task/ApiTaskResult"
import {ApiSkillSummary, ISkillSummary} from "../ApiSkillSummary"


@Injectable({
  providedIn: "root"
})
export class RichSkillService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService) {
    super(httpClient, authService)
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
      .get(`${this.serviceUrl}/${uuid}`, {
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

  createSkill(updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    return this.createSkills([updateObject]).pipe(map(it => it[0]))
  }

  createSkills(updateObjects: ApiSkillUpdate[]): Observable<ApiSkill[]> {
    const errorMsg = `Error creating skill`
    return this.post<ISkill[]>({
      path: this.serviceUrl,
      body: updateObjects
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg).map(s => new ApiSkill(s))))
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
}
