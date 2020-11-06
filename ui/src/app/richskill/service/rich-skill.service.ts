import {Injectable} from "@angular/core"
import {HttpClient, HttpHeaders} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiSkill, ApiSkillSortOrder, ISkill} from "../ApiSkill"
import {map, share} from "rxjs/operators"
import {AbstractService} from "../../abstract.service"
import {ApiSkillUpdate} from "../ApiSkillUpdate"
import {AuthService} from "../../auth/auth-service"
import {ApiSearch, PaginatedSkills} from "./rich-skill-search.service";
import {PublishStatus} from "../../PublishStatus";
import {ApiBatchResult} from "../ApiBatchResult";
import {ApiTaskResult, ITaskResult} from "../../task/ApiTaskResult";
import {ApiSkillSummary, IApiSkillSummary} from "../ApiSkillSummary"


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
    sort: ApiSkillSortOrder | undefined,
  ): Observable<PaginatedSkills> {

    const params = this.buildTableParams(size, from, filterByStatuses, sort)
    return this.get<IApiSkillSummary[]>({
      path: `${this.serviceUrl}`,
      params,
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedSkills(
          body?.map(skill => new ApiSkillSummary(skill)) || [],
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
      throw new Error("No uuid provided for single skill csv export")
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
    const errorMsg = `Error creating skill`
    return this.post<ISkill[]>({
      path: this.serviceUrl,
      body: [updateObject]
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg).map(s => new ApiSkill(s))[0]))
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
    size: number | undefined,
    from: number | undefined,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSkillSortOrder,
  ): Observable<PaginatedSkills> {
    const errorMsg = `Failed to unwrap response for skill search`

    const params = this.buildTableParams(size, from, filterByStatuses, sort)

    return this.post<IApiSkillSummary[]>({
      path: "api/search/skills",
      params,
      body: apiSearch,
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        const totalCount = Number(headers.get("X-Total-Count"))
        const skills = body?.map(skill => new ApiSkillSummary(skill)) || []
        return new PaginatedSkills(skills, !isNaN(totalCount) ? totalCount : skills.length)
      }))
  }

  publishSkills(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
  ): Observable<ApiTaskResult> {
    const params: any = {
      newStatus: newStatus.toString(),
    }
    if (filterByStatuses !== undefined) {
      params.filterByStatus = Array.from(filterByStatuses).map(s => s.toString())
    }
    return this.post<ITaskResult>({
      path: "api/skills/publish",
      params,
      body: apiSearch
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  publishSkillsWithResult(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    pollIntervalMs: number = 1000,
  ): Observable<ApiBatchResult> {
    return this.pollForTaskResult(this.publishSkills(apiSearch, newStatus, filterByStatuses), pollIntervalMs)
  }
}
