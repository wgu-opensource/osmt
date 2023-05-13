import {Component, ViewChild} from "@angular/core"
import {MetadataType} from "../../rsd-metadata.enum"
import {Observable, Subject} from "rxjs"
import {PaginatedMetadata} from "../../IMetadata"
import {ApiSortOrder} from "../../../richskill/ApiSkill"
import {ApiJobCode, IJobCode} from "../../job-codes/Jobcode"
import {TableActionBarComponent} from "../../../table/skills-library-table/table-action-bar.component"
import {Whitelabelled} from "../../../../whitelabel"
import {ApiNamedReference, INamedReference} from "../../named-references/NamedReference"
import {FormControl, FormGroup} from "@angular/forms"

@Component({
  selector: "app-metadata-list",
  templateUrl: "./metadata-list.component.html"
})
export class MetadataListComponent extends Whitelabelled {

  @ViewChild(TableActionBarComponent) actionBar!: TableActionBarComponent

  title = "Metadata"
  selectedMetadataType = MetadataType.Category
  matchingQuery?: string[]

  sizeControl?: FormControl

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  sampleJobCodeResult = new PaginatedMetadata([
    new ApiJobCode({code: "code1", targetNodeName: "targetNodeName1", frameworkName: "frameworkName1", url: "url1", broad: "broad1"}),
    new ApiJobCode({code: "code2", targetNodeName: "targetNodeName2", frameworkName: "frameworkName2", url: "url2", broad: "broad2"}),
    new ApiJobCode({code: "code3", targetNodeName: "targetNodeName3", frameworkName: "frameworkName3", url: "url3", broad: "broad3"}),
    new ApiJobCode({code: "code4", targetNodeName: "targetNodeName4", frameworkName: "frameworkName4", url: "url4", broad: "broad4"}),
    new ApiJobCode({code: "code5", targetNodeName: "targetNodeName5", frameworkName: "frameworkName5", url: "url5", broad: "broad5"}),
    new ApiJobCode({code: "code6", targetNodeName: "targetNodeName6", frameworkName: "frameworkName6", url: "url6", broad: "broad6"}),
    new ApiJobCode({code: "code7", targetNodeName: "targetNodeName7", frameworkName: "frameworkName7", url: "url7", broad: "broad7"}),
    new ApiJobCode({code: "code8", targetNodeName: "targetNodeName8", frameworkName: "frameworkName8", url: "url8", broad: "broad8"}),
  ], 8)

  sampleNamedReference = new PaginatedMetadata([
    new ApiNamedReference({id: "id1", framework: "framework1", name: "name1", type: MetadataType.Category, value: "value1"}),
    new ApiNamedReference({id: "id2", framework: "framework2", name: "name2", type: MetadataType.Category, value: "value2"}),
    new ApiNamedReference({id: "id3", framework: "framework3", name: "name3", type: MetadataType.Category, value: "value3"}),
    new ApiNamedReference({id: "id4", framework: "framework4", name: "name4", type: MetadataType.Category, value: "value4"}),
    new ApiNamedReference({id: "id5", framework: "framework5", name: "name5", type: MetadataType.Category, value: "value5"}),
    new ApiNamedReference({id: "id6", framework: "framework6", name: "name6", type: MetadataType.Category, value: "value6"}),
    new ApiNamedReference({id: "id7", framework: "framework7", name: "name7", type: MetadataType.Category, value: "value7"}),
    new ApiNamedReference({id: "id8", framework: "framework8", name: "name8", type: MetadataType.Category, value: "value8"}),
  ], 8)

  results: PaginatedMetadata = this.sampleNamedReference
  columnSort: ApiSortOrder = ApiSortOrder.NameAsc

  from = 0
  size = 50
  selectedMetadata?: IJobCode[]|INamedReference[]
  showSearchEmptyMessage = false
  resultsLoaded: Observable<PaginatedMetadata> | undefined

  clearSelectedItemsFromTable = new Subject<void>()
  constructor() {
    super()
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    return false
  }

  handleDefaultSubmit(): boolean {
    this.loadNextPage()
    this.from = 0

    return false
  }
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
    return  this.selectedMetadataType === MetadataType.JobCode
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
  getJobCodes(): IJobCode[] {
    return (this.results?.metadata) as IJobCode[]
  }

  getNamedReferences(): INamedReference[] {
    return (this.results?.metadata) as INamedReference[]
  }
  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }

  navigateToPage(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

  // tslint:disable-next-line:use-lifecycle-interface
  ngOnInit(): void {
  }

}
