import {Injectable} from "@angular/core"
import {ApiLibrarySummary, ILibrarySummary, PaginatedLibraries} from "./api/ApiLibrary"
import {Router} from "@angular/router"
import {Observable, Subject} from "rxjs"
import {AbstractService} from "../../abstract.service"
import {HttpClient} from "@angular/common/http"
import {AuthService} from "../../auth/auth-service"
import {Location} from "@angular/common"
import {map, share} from "rxjs/operators"
import {
  ApiAdvancedSearch,
  ApiSearch
} from "../../richskill/service/rich-skill-search.service"
import {ApiSkillSearchResult, PaginatedSkillSearchResults} from "./api/ApiSkillSearchResult"
import {ApiCollectionSearchResult, PaginatedCollectionSearchResults} from "./api/ApiCollectionSearchResult"

@Injectable({
  providedIn: "root"
})
export class ExternalSearchService extends AbstractService {
  latestSearch?: ExternalSearch

  private searchQuerySource: Subject<ExternalSearch> = new Subject()
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

  searchCollections(
    search: ExternalSearch,
    size?: number,
    from?: number,
  ): Observable<PaginatedCollectionSearchResults> {
    const errorMsg = `Failed to unwrap response for external collections search`

    const params = this.buildTableParams(size, from, undefined, undefined)

    return this.post<ApiCollectionSearchResult[]>({
      path: "api/external/search/collections",
      params,
      body: {
        libraries: (search.libraries) ? Array.from(search.libraries).map(l => l.uuid) : undefined,
        search: search.search
      }
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        const totalCount = Number(headers.get("X-Total-Count"))
        const collections = this.safeUnwrapBody(body, errorMsg)?.map(collection => collection) || []
        return new PaginatedCollectionSearchResults(collections, !isNaN(totalCount) ? totalCount : collections.length)
      }))
  }

  searchSkills(
    search: ExternalSearch,
    size?: number,
    from?: number,
  ): Observable<PaginatedSkillSearchResults> {
    const errorMsg = `Failed to unwrap response for external skill search`

    const params = this.buildTableParams(size, from, undefined, undefined)

    return this.post<ApiSkillSearchResult[]>({
      path: "/api/external/search/skills",
      params,
      body: {
        libraries: (search.libraries) ? Array.from(search.libraries).map(l => l.uuid) : undefined,
        search: search.search
      }
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        const totalCount = Number(headers.get("X-Total-Count"))
        const skills = this.safeUnwrapBody(body, errorMsg)?.map(skill => skill) || []
        return new PaginatedSkillSearchResults(skills, !isNaN(totalCount) ? totalCount : skills.length)
      }))
  }

  advancedCollectionSearch(search: ApiAdvancedSearch, libraries: ILibrarySummary[]): void {
    this.setLatestSearch(search, libraries)
    this.router.navigate(["/collections/search/external"])
  }

  advancedSkillSearch(search: ApiAdvancedSearch, libraries?: ILibrarySummary[]): void {
    this.setLatestSearch(search, libraries)
    this.router.navigate(["/skills/search/external"])
  }

  protected setLatestSearch(search?: ApiAdvancedSearch, libraries?: ILibrarySummary[]): void {
    this.latestSearch = (search) ?  new ExternalSearch(new ApiSearch({advanced: search}), libraries) : undefined
    this.searchQuerySource.next(this.latestSearch)
  }

  public clearSearch(): void {
    this.latestSearch = undefined
    this.searchQuerySource.next(this.latestSearch)
  }
}

export class ExternalSearch {
  readonly libraries: ILibrarySummary[]
  readonly search: ApiSearch

  constructor(search: ApiSearch, libraries: ILibrarySummary[] = []) {
    this.libraries = libraries
    this.search = search
  }
}
