import {Injectable} from "@angular/core";
import {ApiSearch} from "../richskill/service/rich-skill-search.service";
import {Router} from "@angular/router";
import {Subject} from "rxjs";

@Injectable({
  providedIn: "root"
})
export class SearchService {
  latestSearch?: ApiSearch
  currentPage = 1

  private searchQuerySource: Subject<ApiSearch> = new Subject()
  searchQuery$ = this.searchQuerySource.asObservable()

  private pageNavigationSource: Subject<number> = new Subject()
  pageNavigation$ = this.pageNavigationSource.asObservable()

  constructor(private router: Router) {
  }

  simpleSkillSearch(query: string): void {
    this.setLatestSearch(ApiSearch.factory({query}))
    this.router.navigate(["/search/skills"])
  }

  setCurrentPage(newPageNo: number): void {
    this.currentPage = newPageNo
    this.pageNavigationSource.next(newPageNo)
  }

  protected setLatestSearch(apiSearch?: ApiSearch): void {
    this.latestSearch = apiSearch
    this.searchQuerySource.next(this.latestSearch)
  }
}
