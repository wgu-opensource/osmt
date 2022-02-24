import {Injectable} from "@angular/core"
import {ApiLibrarySummary, ILibrarySummary, PaginatedLibraries} from "./api/ApiLibrary"
import {ApiSearch} from "../../richskill/service/rich-skill-search.service"
import {ApiExternalAdvancedSearch} from "./api/ApiExternalAdvancedSearch"
import {Router} from "@angular/router"
import {Observable, Subject} from "rxjs"
import {AbstractService} from "../../abstract.service"
import {HttpClient} from "@angular/common/http"
import {AuthService} from "../../auth/auth-service"
import {Location} from "@angular/common"
import {map, share} from "rxjs/operators"

@Injectable({
  providedIn: "root"
})
export class SearchHubService extends AbstractService {
  latestSearch?: ApiSearch

  private searchQuerySource: Subject<ApiSearch> = new Subject()
  searchQuery$ = this.searchQuerySource.asObservable()

  constructor(httpClient: HttpClient, authService: AuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

  private serviceBasePath = "api/external"

  get isEnabled(): boolean {
      return true
  }

  getLibraries(): Observable<PaginatedLibraries> {
    const errorMsg = `Could not retrieve libraries`
    return this.get<ILibrarySummary[]>({
      path: `${this.serviceBasePath}/libraries`,
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedLibraries(
          this.safeUnwrapBody(body, errorMsg)?.map(library => new ApiLibrarySummary(library)) || [],
          Number(headers.get("X-Total-Count"))
        )
      }))
  }

  advancedCollectionSearch(advanced: ApiExternalAdvancedSearch): void {
    this.setLatestSearch(new ApiSearch({advanced}))
    this.router.navigate(["/collections/search/external"])
  }

  advancedSkillSearch(advanced: ApiExternalAdvancedSearch): void {
    this.setLatestSearch(new ApiSearch({advanced}))
    this.router.navigate(["/skills/search/external"])
  }

  protected setLatestSearch(apiSearch?: ApiSearch): void {
    this.latestSearch = apiSearch
    this.searchQuerySource.next(this.latestSearch)
  }

  public clearSearch(): void {
    this.latestSearch = undefined
    this.searchQuerySource.next(this.latestSearch)
  }
}
