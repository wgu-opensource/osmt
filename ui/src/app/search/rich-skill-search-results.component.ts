import {Component, OnInit} from "@angular/core";
import {SearchService} from "./search.service";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {Observable} from "rxjs";
import {ApiSearch, PaginatedSkills} from "../richskill/service/rich-skill-search.service";
import {ApiSkill} from "../richskill/ApiSkill";
import {PublishStatus} from "../PublishStatus";
import {ActivatedRoute} from "@angular/router";
import {ApiTaskResult} from "../task/ApiTaskResult";
import {ApiBatchResult} from "../richskill/ApiBatchResult";
import {TableActionDefinition} from "../table/has-action-definitions";
import {ToastService} from "../toast/toast.service";


@Component({
  selector: "app-rich-skill-search-results",
  templateUrl: "./rich-skill-search-results.component.html"
})
export class RichSkillSearchResultsComponent implements OnInit {
  resultsLoaded: Observable<PaginatedSkills> | undefined

  results: PaginatedSkills | undefined
  apiSearch: ApiSearch | undefined

  from: number = 0
  size: number = 50
  selectedSkills?: ApiSkill[]
  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unpublished, PublishStatus.Published])
  skillsSaved?: Observable<ApiBatchResult>

  get skillCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.curPageCount} of ${this.totalCount} skill${this.curPageCount > 1 ? "s" : ""}`
    }
    return `0 skills`
  }

  constructor(private searchService: SearchService,
              private richSkillService: RichSkillService,
              private route: ActivatedRoute,
              private toastService: ToastService
              ) {
    searchService.searchQuery$.subscribe(apiSearch => this.handleNewSearch(apiSearch) )
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const query = params.q
      if (query && query.length > 0) {
        this.handleNewSearch(ApiSearch.factory({query}))
      }
    })
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
      this.resultsLoaded = this.richSkillService.searchSkills(this.apiSearch, this.size, this.from, this.selectedFilters)
      this.resultsLoaded.subscribe(results => this.setResults(results))
    }
  }

  private setResults(results: PaginatedSkills): void {
    this.results = results
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get curPageCount(): number {
    return this.results?.skills.length ?? 0
  }

  get emptyResults(): boolean {
    return this.curPageCount < 1
  }

  get firstRecordNo(): number {
    return this.from + 1
  }
  get lastRecordNo(): number {
    return this.from + this.curPageCount
  }

  get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }
  get currentPageNo(): number {
    return Math.floor(this.from / this.size) + 1
  }

  handleNewSelection(selectedSkills: ApiSkill[]): void {
    this.selectedSkills = selectedSkills
    console.log("got new selection", selectedSkills)
  }


  actionsVisible(): boolean {
    return (this.selectedSkills?.length ?? 0) > 0
  }
  publishVisible(): boolean {
    const unpublishedSkill = this.selectedSkills?.find(s => s.status === PublishStatus.Unpublished)
    return unpublishedSkill !== undefined
  }
  archiveVisible(): boolean {
    const unarchivedSkills = this.selectedSkills?.find(s => s.status === PublishStatus.Published)
    return unarchivedSkills !== undefined
  }
  unarchiveVisible(): boolean {
    const archivedSkill = this.selectedSkills?.find(s => s.status === PublishStatus.Archived)
    return archivedSkill !== undefined
  }

  tableActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: () => this.handleClickBackToTop(),
      }),

      new TableActionDefinition({
        label: "Publish",
        icon: "publish",
        callback: () => this.handleClickPublish(),
        visible: () => this.publishVisible()
      }),

      new TableActionDefinition({
        label: "Archive",
        icon: "archive",
        callback: () => this.handleClickArchive(),
        visible: () => this.archiveVisible()
      }),

      new TableActionDefinition({
        label: "Unarchive",
        icon: "unarchive",
        callback: () => this.handleClickUnarchive(),
        visible: () => this.unarchiveVisible()
      }),

      new TableActionDefinition({
        label: "Add to Collection",
        icon: "collection",
        primary: true,
        callback: () => this.handleClickAddCollection()
      }),
    ]

  }

  private handleClickBackToTop(): boolean {
    return false
  }

  private handleClickAddCollection(): boolean {
    return false
  }

  private handleClickUnarchive(): boolean {
    return false
  }

  private handleClickArchive(): boolean {
    return false
  }

  handleClickPublish(): boolean  {
    const selectedUuids = this.selectedSkills?.map(s => s.uuid)
    if (selectedUuids === undefined) {
      return false
    }

    console.log("PROCESSING PUBLISH", selectedUuids)

    this.toastService.showBlockingLoader()

    const apiSearch = ApiSearch.factory({uuids: selectedUuids})
    this.skillsSaved = this.richSkillService.publishSkillsWithResult(apiSearch)
    this.skillsSaved.subscribe((result) => {
      if (result !== undefined) {
        this.toastService.hideBlockingLoader()
        this.loadNextPage()
      }
      else {
        console.log("tick")
      }
    })
    return false
  }

  handleFiltersChanged(newFilters: Set<PublishStatus>): void {
    this.selectedFilters = newFilters
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

}
