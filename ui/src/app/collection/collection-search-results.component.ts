import {Component, OnInit} from "@angular/core";
import {SkillsListComponent} from "../richskill/list/skills-list.component";
import {CollectionsListComponent} from "./collections-list.component";
import {ApiSearch, PaginatedCollections, PaginatedSkills} from "../richskill/service/rich-skill-search.service";
import {ActivatedRoute, NavigationStart, Router} from "@angular/router";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {ToastService} from "../toast/toast.service";
import {SearchService} from "../search/search.service";
import {CollectionService} from "./service/collection.service";
import {ApiSkillSummary, ICollectionSummary} from "../richskill/ApiSkillSummary";
import {determineFilters} from "../PublishStatus";
import {Title} from "@angular/platform-browser";
import {AuthService} from "../auth/auth-service";

@Component({
  selector: "app-collection-search-results",
  templateUrl: "./collections-list.component.html"
})
export class CollectionSearchResultsComponent extends CollectionsListComponent implements OnInit {

  apiSearch: ApiSearch | undefined

  title = "Search Results"

  showSearchEmptyMessage = true
  private multiplePagesSelected: boolean = false

  constructor(protected router: Router,
              protected toastService: ToastService,
              protected collectionService: CollectionService,
              protected searchService: SearchService,
              protected route: ActivatedRoute,
              protected titleService: Title,
              protected authService: AuthService
  ) {
    super(router, toastService, collectionService, authService)
    this.searchService.searchQuery$.subscribe(apiSearch => this.handleNewSearch(apiSearch) )
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Search Results | ${this.whitelabel.toolName}`)

    if (this.searchService.latestSearch !== undefined) {
      this.handleNewSearch(this.searchService.latestSearch)
    } else {
      this.route.queryParams.subscribe(params => {
        const query = params.q
        if (query && query.length > 0) {
          this.handleNewSearch(new ApiSearch({query}))
        }
      })
    }
  }

  private handleNewSearch(apiSearch?: ApiSearch): void {
    this.apiSearch = apiSearch
    if (this.apiSearch?.query !== undefined) {
      this.matchingQuery = [this.apiSearch.query]
    } else if (this.apiSearch?.advanced !== undefined) {
      this.matchingQuery = this.apiSearch?.advancedMatchingQuery()
    }
    this.from = 0
    this.loadNextPage()
  }

  loadNextPage(): void {
    if (this.selectedFilters.size < 1) {
      this.setResults(new PaginatedCollections([], 0))
      return
    }

    if (this.apiSearch !== undefined) {
      this.resultsLoaded = this.collectionService.searchCollections(
        this.apiSearch,
        this.size,
        this.from,
        determineFilters(this.selectedFilters),
        this.columnSort)
      this.resultsLoaded.subscribe(results => this.setResults(results))
    }
  }

  getApiSearch(item?: ICollectionSummary): ApiSearch | undefined {
    return (this.multiplePagesSelected) ? this.apiSearch : super.getApiSearch(item)
  }

  handleSelectAll(selectAllChecked: boolean): void {
    this.multiplePagesSelected = this.totalPageCount > 1
  }

  getSelectAllCount(): number {
    return this.totalCount
  }
}
