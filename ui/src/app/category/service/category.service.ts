import {Location} from "@angular/common"
import {HttpClient, HttpHeaders} from "@angular/common/http"
import { Inject, Injectable } from "@angular/core"
import {Router} from "@angular/router"
import {Observable} from "rxjs"
import {map, share} from "rxjs/operators"
import {AbstractService, IRelatedSkillsService} from "../../abstract.service"
import {AuthService} from "../../auth/auth-service"
import {PublishStatus} from "../../PublishStatus"
import {ApiSearch, PaginatedSkills} from "../../richskill/service/rich-skill-search.service"
import {ApiSortOrder} from "../../richskill/ApiSkill"
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {ApiCategory, IKeyword, KeywordSortOrder, PaginatedCategories} from "../ApiCategory"

@Injectable({
  providedIn: "root"
})
export class CategoryService extends AbstractService implements IRelatedSkillsService<number> {

  constructor(
    httpClient: HttpClient,
    authService: AuthService,
    router: Router,
    location: Location,
    @Inject("BASE_API") baseApi: string
  ) {
    super(httpClient, authService, router, location, baseApi)
  }

  private serviceUrl = "categories"

  getAllPaginated(
    size: number = 50,
    from: number = 0,
    sort: KeywordSortOrder | undefined,
  ): Observable<PaginatedCategories> {
    const params = this.buildTableParams(size, from, undefined, sort)

    return this.get<IKeyword[]>({
      path: `${this.serviceUrl}`,
      params
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedCategories(
          body?.map(kw => kw) || [],
          Number(headers.get("X-Total-Count"))
        )
      }))
  }

  getById(identifier: string): Observable<ApiCategory> {
    const errorMsg = `Could not find category by id [${identifier}]`
    return this.get<IKeyword>({
      path: `${this.serviceUrl}/${identifier}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiCategory(this.safeUnwrapBody(body, errorMsg))))
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
      path: `${this.serviceUrl}/${entityId}/skills`,
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
      ))
  }
}
