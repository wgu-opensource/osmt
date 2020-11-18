import {Injectable} from "@angular/core";
import {ApiAdvancedSearch, ApiSearch, IAdvancedSearch} from "../richskill/service/rich-skill-search.service";
import {Router} from "@angular/router";
import {Subject} from "rxjs";

@Injectable({
  providedIn: "root"
})
export class SearchService {
  latestSearch?: ApiSearch

  private searchQuerySource: Subject<ApiSearch> = new Subject()
  searchQuery$ = this.searchQuerySource.asObservable()

  constructor(private router: Router) {
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
}
