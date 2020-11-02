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
    this.setLatestSearch(ApiSearch.factory({query}))
    this.router.navigate(["/search/skills"], {queryParams: {q: query}})
  }
  advancedSkillSearch(advanced: ApiAdvancedSearch): void {
    this.setLatestSearch(ApiSearch.factory({advanced}))
    this.router.navigate(["/search/skills"])
  }

  protected setLatestSearch(apiSearch?: ApiSearch): void {
    this.latestSearch = apiSearch
    this.searchQuerySource.next(this.latestSearch)
  }
}
