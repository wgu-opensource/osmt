import {Component, OnInit} from "@angular/core";
import {SearchService} from "./search.service";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {Observable} from "rxjs";
import {ApiSkill} from "../richskill/ApiSkill";
import {ApiSearch, PaginatedSkills} from "../richskill/service/rich-skill-search.service";


@Component({
  selector: "app-rich-skill-search-results",
  templateUrl: "./rich-skill-search-results.component.html"
})
export class RichSkillSearchResultsComponent implements OnInit {
  resultsLoaded: Observable<PaginatedSkills> | undefined

  private results: PaginatedSkills | undefined
  private apiSearch: ApiSearch | undefined

  private from: number = 0
  private size: number = 2

  constructor(private searchService: SearchService, private richSkillService: RichSkillService) {
    searchService.searchQuery$.subscribe(apiSearch => this.handleNewSearch(apiSearch) )
    searchService.pageNavigation$.subscribe(newPageNo => this.navigateToPage(newPageNo) )
  }

  ngOnInit(): void {
  }

  private handleNewSearch(apiSearch: ApiSearch): void {
    this.apiSearch = apiSearch
    this.loadNextPage()
  }

  private navigateToPage(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  private loadNextPage(): void {
    if (this.apiSearch !== undefined) {
      this.resultsLoaded = this.richSkillService.searchSkills(this.apiSearch, this.size, this.from)
      this.resultsLoaded.subscribe(results => this.setResults(results))
    }
  }

  private setResults(results: PaginatedSkills): void {
    this.results = results
  }

  private get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  private get curPageCount(): number {
    return this.results?.skills.length ?? 0
  }

  private get emptyResults(): boolean {
    return this.curPageCount < 1
  }

  private get firstRecordNo(): number {
    return this.from + 1
  }
  private get lastRecordNo(): number {
    return this.from + this.curPageCount
  }

  private get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }
  private get currentPageNo(): number {
    return Math.floor(this.from / this.size)+1
  }
}
