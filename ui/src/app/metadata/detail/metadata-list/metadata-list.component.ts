import {Component, OnInit, ViewChild} from "@angular/core"
import {MetadataType} from "../../rsd-metadata.enum"
import {Observable, Subject} from "rxjs"
import {PaginatedMetadata} from "../../IMetadata"
import {ApiSortOrder, INamedReference} from "../../../richskill/ApiSkill"
import {ICollectionSummary} from "../../../richskill/ApiSkillSummary"
import {IJobCode} from "../../job-codes/Jobcode"
import {TableActionBarComponent} from "../../../table/skills-library-table/table-action-bar.component"
import {Whitelabelled} from "../../../../whitelabel"

@Component({
  selector: "app-metadata-list",
  templateUrl: "./metadata-list.component.html"
})
export class MetadataListComponent extends Whitelabelled {

  @ViewChild(TableActionBarComponent) actionBar!: TableActionBarComponent

  title = "Metadata"
  selectedMetadataType = MetadataType.Category
  matchingQuery?: string[]

  from = 0
  size = 50
  selectedMetadata?: IJobCode[]|INamedReference[]

  resultsLoaded: Observable<PaginatedMetadata> | undefined

  clearSelectedItemsFromTable = new Subject<void>()
  constructor() {
    super()
  }

  results: PaginatedMetadata | undefined
  columnSort: ApiSortOrder = ApiSortOrder.NameAsc

  loadNextPage(): void {}
  handleSelectAll(selectAllChecked: boolean): void {}

  handleNewSelection(selected: IJobCode[]|INamedReference[]): void {
    this.selectedMetadata = selected
  }

  handleHeaderColumnSort(sort: ApiSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get metadataCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.totalCount} ${this.selectedMetadataType}${this.totalCount > 1 ? "s" : ""}`
    }
    return `0 ${this.selectedMetadataType}s`
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

  get curPageCount(): number {
    return this.results?.metadata.length ?? 0
  }

  getMobileSortOptions(): {[s: string]: string} {
    return {
      "metadata.asc": "Metadata Name (ascending)",
      "metadata.desc": "Metadata Name (descending)",
    }
  }

  get emptyResults(): boolean {
    return this.curPageCount < 1
  }
  get isJobCodeDataSelected(): boolean {
    return  this.selectedMetadataType === MetadataType.JobCode && !this.emptyResults
  }

  getSelectAllCount(): number {
    return this.curPageCount
  }

  getSelectAllEnabled(): boolean {
    return true
  }

  focusActionBar(): void {
    this.actionBar?.focus()
  }

  ngOnInit(): void {
  }

}
