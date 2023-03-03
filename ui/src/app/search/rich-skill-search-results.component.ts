import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core";
import {SearchService} from "./search.service";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {ApiSearch, PaginatedSkills} from "../richskill/service/rich-skill-search.service";
import {ActivatedRoute, NavigationStart, Router} from "@angular/router";
import {ToastService} from "../toast/toast.service";
import {SkillsListComponent} from "../richskill/list/skills-list.component";
import {ApiSkillSummary} from "../richskill/ApiSkillSummary";
import {determineFilters} from "../PublishStatus";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions";
import {ExtrasSelectedSkillsState} from "../collection/add-skills-collection.component";
import {Title} from "@angular/platform-browser";
import {AuthService} from "../auth/auth-service";
import {formatDate} from "@angular/common"
import * as FileSaver from "file-saver"
import {CollectionService} from "../collection/service/collection.service"


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
  private multiplePagesSelected: boolean = false

  constructor(protected router: Router,
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
    return (this.multiplePagesSelected) ? this.apiSearch : super.getApiSearch(skill)
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

  protected handleClickExportSearch(): void {
    this.toastService.loaderSubject.next(true)
    this.richSkillService.exportSearch(this.selectedUuids() as string[])
      .subscribe((apiTask) => {
        this.richSkillService.getResultExportedLibrary(apiTask.id.slice(1)).subscribe(
          response => {
            this.downloadAsCsvFile(response.body)
            this.toastService.loaderSubject.next(false)
          }
        )
      })
  }

  private downloadAsCsvFile(csv: string): void {
    const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
    const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
    FileSaver.saveAs(blob, `RSD Skills - ${this.matchingQuery} - ${date}.csv`)
  }

  protected exportSearchVisible(): boolean {
    return ((this.selectedSkills?.length ?? 0) > 0)
  }

}
