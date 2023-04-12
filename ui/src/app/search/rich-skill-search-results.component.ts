import { Component, Inject, LOCALE_ID, OnInit } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router";

import { AuthService } from "../auth/auth-service";
import { ExtrasSelectedSkillsState } from "../collection/add-skills-collection.component";
import { CollectionService } from "../collection/service/collection.service"
import { ExportRsdComponent } from "../export/export-rsd.component";
import { determineFilters } from "../PublishStatus";
import { ApiSkillSummary } from "../richskill/ApiSkillSummary";
import { SkillsListComponent } from "../richskill/list/skills-list.component";
import { RichSkillService } from "../richskill/service/rich-skill.service";
import { ApiSearch, PaginatedSkills } from "../richskill/service/rich-skill-search.service";
import { SearchService } from "./search.service";
import { TableActionDefinition } from "../table/skills-library-table/has-action-definitions";
import { ToastService } from "../toast/toast.service";


@Component({
  selector: "app-rich-skill-search-results",
  templateUrl: "../richskill/list/skills-list.component.html"
})
export class RichSkillSearchResultsComponent extends SkillsListComponent implements OnInit {

  apiSearch: ApiSearch | undefined
  showAdvancedFilteredSearch = true

  title = "Search Results"

  selectAllChecked = false
  showSearchEmptyMessage = true
  showExportSelected = true
  exporter = new ExportRsdComponent(
    this.richSkillService,
    this.toastService,
    this.locale
  )
  private multiplePagesSelected: boolean = false

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected collectionService: CollectionService,
    protected toastService: ToastService,
    protected searchService: SearchService,
    protected route: ActivatedRoute,
    protected titleService: Title,
    protected authService: AuthService,
    @Inject(LOCALE_ID) protected locale: string
) {
    super(router, richSkillService, collectionService, toastService, authService)
    this.searchService.searchQuery$.subscribe(apiSearch => this.handleNewSearch(apiSearch) )
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Search Results | ${this.whitelabel.toolName}`)
    if (history.state?.advanced) {
      this.searchService.latestSearch = new ApiSearch(history.state);
    }

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
      this.setResults(new PaginatedSkills([], 0))
      return
    }

    if (this.apiSearch !== undefined) {
      this.apiSearch.filtered = this.selectedKeywords
      this.resultsLoaded = this.richSkillService.getSkillsFiltered(
        this.size,
        this.from,
        this.apiSearch,
        determineFilters(this.selectedFilters),
        this.columnSort
      )
      this.resultsLoaded.subscribe(results => this.setResults(results))
    }
  }

  getApiSearch(skill?: ApiSkillSummary): ApiSearch | undefined {
    return this.selectAllChecked ? this.apiSearch : super.getApiSearch(skill)
  }

  handleSelectAll(selectAllChecked: boolean): void {
    this.multiplePagesSelected = this.totalPageCount > 1
    this.selectAllChecked = selectAllChecked
  }

  getSelectAllCount(): number {
    return this.totalCount
  }

  handleClickAddCollection(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    const selectedSkills = this.getSelectedSkills(skill)

    this.router.navigate(["/collections/add-skills"], {
      // If there are selected skills, use them.  Otherwise, use the search results.
      state: {
        selectedSkills,
        totalCount: this.selectAllChecked || !selectedSkills?.length ? this.totalCount : selectedSkills?.length,
        search: this.selectAllChecked || !selectedSkills?.length ? this.apiSearch : undefined
      } as ExtrasSelectedSkillsState
    })
    return false
  }


  protected onlyDraftsSelected(skill?: ApiSkillSummary): boolean {
    return !this.multiplePagesSelected || super.onlyDraftsSelected(skill)
  }

  protected getRsdCsv(): void {
    this.exporter.exportSearchCsv(
      this.getApiSearch() ?? new ApiSearch({}),
      this.matchingQuery ?? [""],
      this.selectedFilters
    )
  }

  protected getRsdXlsx(): void {
    this.exporter.exportSearchXlsx(
      this.getApiSearch() ?? new ApiSearch({}),
      this.matchingQuery ?? [""]
    )
  }

  protected exportSearchVisible(): boolean {
    return ((this.selectedSkills?.length ?? 0) > 0)
  }

}
