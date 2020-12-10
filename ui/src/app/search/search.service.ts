import {Injectable} from "@angular/core"
import {ApiAdvancedSearch, ApiSearch} from "../richskill/service/rich-skill-search.service"
import {Router} from "@angular/router"
import {Observable, Subject} from "rxjs"
import {AbstractService} from "../abstract.service"
import {HttpClient, HttpParams} from "@angular/common/http"
import {AuthService} from "../auth/auth-service"
import {map, share} from "rxjs/operators"
import {INamedReference} from "../richskill/ApiSkill"

@Injectable({
  providedIn: "root"
})
export class SearchService extends AbstractService{
  latestSearch?: ApiSearch

  private searchQuerySource: Subject<ApiSearch> = new Subject()
  searchQuery$ = this.searchQuerySource.asObservable()

  constructor(private router: Router, httpClient: HttpClient, authService: AuthService) {
    super(httpClient, authService)
  }

  simpleSkillSearch(query: string): void {
    this.setLatestSearch(new ApiSearch({query}))
    this.router.navigate(["/skills/search"], {queryParams: {q: query}})
  }
  advancedSkillSearch(advanced: ApiAdvancedSearch): void {
    this.setLatestSearch(new ApiSearch({advanced}))
    this.router.navigate(["/skills/search"])
  }

  simpleCollectionSearch(query: string): void {
    this.setLatestSearch(new ApiSearch({query}))
    this.router.navigate(["/collections/search"], {queryParams: {q: query}})
  }
  advancedCollectionSearch(advanced: ApiAdvancedSearch): void {
    this.setLatestSearch(new ApiSearch({advanced}))
    this.router.navigate(["/collections/search"])
  }

  protected setLatestSearch(apiSearch?: ApiSearch): void {
    this.latestSearch = apiSearch
    this.searchQuerySource.next(this.latestSearch)
  }

  public clearSearch(): void {
    this.latestSearch = undefined
    this.searchQuerySource.next(this.latestSearch)
  }

  searchForKeyword(
    query: string,
    keyword: "category" | "standard" | "certification" | "alignment" | "employer" | "author"
  ): Observable<string[]> {
    return this.get<INamedReference[]>({
      path: `api/search/keywords`,
      params: new HttpParams()
        .append("query", query)
        .append("type", keyword)
    })
      .pipe(share())
      .pipe(map(({body}) => {
        console.log("the resulting body" + body)
        return body?.filter(k => !!k)?.map(k => k.name as string) ?? []
      }))
  }
}
