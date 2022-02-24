import {Component, ElementRef, OnInit, ViewChild} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import {ToastService} from "../../../toast/toast.service"
import {ApiCollectionSummary, ApiSkillSummary} from "../../../richskill/ApiSkillSummary"
import {Title} from "@angular/platform-browser"
import {ExternalSearch, ExternalSearchService} from "../external-search.service"
import {Observable} from "rxjs"
import {PaginatedCollections, PaginatedSkills} from "../../../richskill/service/rich-skill-search.service"
import {QuickLinksHelper} from "../../../core/quick-links-helper"
import {first} from "rxjs/operators"


export enum SearchResultType {
  RSD = "RSD",
  COLLECTION = "COLLECTION"
}

@Component({
  selector: "app-external-search-results",
  templateUrl: "./external-search-results.component.html"
})
export class ExternalSearchResultsComponent extends QuickLinksHelper implements OnInit {

  @ViewChild("titleHeading") titleElement!: ElementRef

  /*
   * In default configuration, ElasticSearch has an upper limit of returning 10000 elements.  For a short-term
   * usability fix, we're simply going to add a "+" character when displaying 10000 (or more) total hits.
   */
  readonly upperLimit = 10000

  searchResultType: SearchResultType | undefined

  from = 0
  size = 50

  title = "External Search Results"

  externalSearch: ExternalSearch | undefined

  resultsLoaded: Observable<PaginatedSkills> | Observable<PaginatedCollections> | undefined

  private apiResults: PaginatedSkills | PaginatedCollections | undefined

  get isCollectionSearchResults(): boolean {
    return (this.searchResultType === SearchResultType.COLLECTION)
  }

  get isRsdSearchResults(): boolean {
    return (this.searchResultType === SearchResultType.RSD)
  }

  get matchingQuery(): string[] {
    if (this.externalSearch?.search?.query !== undefined) {
      return [this.externalSearch.search.query]
    }
    else if (this.externalSearch?.search?.advanced !== undefined) {
      return this.externalSearch?.search?.advancedMatchingQuery()
    }
    else {
      return []
    }
  }

  get resultTypeLabel(): string {
    switch (this.searchResultType) {
      case SearchResultType.RSD:
        return "RSD"
      case SearchResultType.COLLECTION:
        return "Collection"
      default:
        return ""
    }
  }

  get results(): ApiSkillSummary[] | ApiCollectionSummary[] {
    if (this.apiResults) {
      return (this.apiResults instanceof PaginatedCollections) ? this.apiResults.collections : this.apiResults.skills
    }

    return []
  }

  get resultsCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.totalCount}${this.totalCount >= this.upperLimit ? "+" : ""} ${this.resultTypeLabel}${this.totalCount > 1 ? "s" : ""}`
    }
    return `0 ${this.resultTypeLabel}s`
  }

  get totalCount(): number {
    return this.apiResults?.totalCount ?? 0
  }

  get curPageCount(): number {
    return this.results.length ?? 0
  }

  get emptyResults(): boolean {
    return this.curPageCount < 1
  }

  get firstRecordNo(): number {
    return this.from + 1
  }

  get lastRecordNo(): number {
    return Math.min(this.from + this.curPageCount, this.totalCount)
  }

  get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }

  get currentPageNo(): number {
    return Math.floor(this.from / this.size) + 1
  }

  constructor(
    protected router: Router,
    protected toastService: ToastService,
    protected searchService: ExternalSearchService,
    protected route: ActivatedRoute,
    protected titleService: Title
  ) {
    super()

    const routeSub = this.route.data.pipe(first()).subscribe(d => {
      if (d.searchResultType) {
        this.searchResultType = d.searchResultType
      }
    })

    this.searchService.searchQuery$.subscribe(apiSearch => this.handleNewSearch(apiSearch) )
  }

  ngOnInit(): void {
    this.titleService.setTitle(`External Search Results | ${this.whitelabel.toolName}`)

    if (this.searchService.latestSearch !== undefined) {
      this.handleNewSearch(this.searchService.latestSearch)
    }
  }

  loadNextPage(): void {
    if (this.externalSearch !== undefined && this.searchResultType !== undefined) {
      switch (this.searchResultType) {
        case SearchResultType.COLLECTION:
          this.resultsLoaded = this.searchService.searchCollections(this.externalSearch, this.size, this.from)
          this.resultsLoaded.subscribe(results => this.setResults(results))
          break
        case SearchResultType.RSD:
          this.resultsLoaded = this.searchService.searchSkills(this.externalSearch, this.size, this.from)
          this.resultsLoaded.subscribe(results => this.setResults(results))
          break
      }
    }
  }

  navigateToPage(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

  protected setResults(results: PaginatedCollections | PaginatedSkills): void {
    this.apiResults = results
  }

  private handleNewSearch(search?: ExternalSearch): void {
    this.externalSearch = search

    this.from = 0
    this.loadNextPage()
  }
}
