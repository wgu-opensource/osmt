import {Component, OnInit} from "@angular/core";
import {Location} from "@angular/common";
import {ActivatedRoute, Router} from "@angular/router";
import {ApiCollectionSummary, ApiSkillSummary} from "../richskill/ApiSkillSummary";
import {Title} from "@angular/platform-browser";
import {PublishStatus} from "../PublishStatus";
import {FormControl, FormGroup} from "@angular/forms";
import {Observable} from "rxjs";
import {
  ApiSearch,
  ApiSkillListUpdate,
  PaginatedCollections,
  PaginatedSkills
} from "../richskill/service/rich-skill-search.service";
import {ApiSortOrder} from "../richskill/ApiSkill";
import {CollectionService} from "./service/collection.service";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions";
import {ToastService} from "../toast/toast.service";

export interface ExtrasSelectedSkillsState {
  selectedSkills: ApiSkillSummary[]
  totalCount: number
  search?: ApiSearch
}

@Component({
  selector: "app-add-skills-collection",
  templateUrl: "./add-skills-collection.component.html"
})
export class AddSkillsCollectionComponent implements OnInit {
  uuidParam?: string

  from = 0
  size = 50

  resultsLoaded: Observable<PaginatedCollections> | undefined
  results: PaginatedCollections | undefined

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }

  state?: ExtrasSelectedSkillsState

  columnSort: ApiSortOrder = ApiSortOrder.SkillAsc
  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published])

  constructor(protected router: Router,
              protected titleService: Title,
              protected route: ActivatedRoute,
              protected location: Location,
              protected collectionService: CollectionService,
              protected toastService: ToastService
  ) {
    this.state = this.router.getCurrentNavigation()?.extras.state as ExtrasSelectedSkillsState
    this.titleService.setTitle("Add RSDs to a Collection")
    this.uuidParam = this.route.snapshot.paramMap.get("uuid") || undefined

  }

  get addingCount(): number {
    return this.state?.totalCount ?? 0
  }

  ngOnInit(): void {
    if ((this.state?.selectedSkills?.length ?? 0) < 1) {
      this.router.navigate(["/"])
    }
  }
  handleDefaultSubmit(): boolean {
    this.loadNextPage()
    this.from = 0

    return false
  }

  loadNextPage(): void {
    const query = this.searchQuery.trim()

    if (this.selectedFilters.size < 1 || query.length < 1) {
      this.setResults(new PaginatedCollections([], 0))
      return
    }

    const apiSearch = new ApiSearch({query})
    this.resultsLoaded = this.collectionService.searchCollections(apiSearch, this.size, this.from, this.selectedFilters, this.columnSort)
    this.resultsLoaded.subscribe((results) => {
      this.setResults(results)
    })
  }

  get isPlural(): boolean {
    return this.addingCount > 1
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    return false
  }

  handleFiltersChanged(newFilters: Set<PublishStatus>): void {
    this.selectedFilters = newFilters
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  handleHeaderColumnSort(sort: ApiSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }

  protected setResults(results: PaginatedCollections): void {
    this.results = results
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get curPageCount(): number {
    return this.results?.collections.length ?? 0
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

  rowActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Select Collection",
        callback: (action: TableActionDefinition, collection?: ApiCollectionSummary) => this.handleSelectCollection(action, collection),
      })
    ]
  }

  private handleSelectCollection(action: TableActionDefinition, collection?: ApiCollectionSummary): boolean {
    if (collection?.uuid === undefined) { return false }

    const update = new ApiSkillListUpdate({
      add: (this.state?.search !== undefined) ? this.state?.search : new ApiSearch({uuids: this.state?.selectedSkills?.map(it => it.uuid) })
    })

    this.toastService.showBlockingLoader()
    this.collectionService.updateSkillsWithResult(collection.uuid, update).subscribe(result => {
      if (result) {
        const message = `Added ${result.modifiedCount} RSDs to collection`
        this.toastService.showToast("Success!", message)
        this.toastService.hideBlockingLoader()
        this.return()
      }
    })
    return false
  }

  private return(): void {
    this.location.back()
  }
}
