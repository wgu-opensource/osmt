import {Component, OnInit, ViewChild} from "@angular/core"
import { FormControl, FormGroup } from "@angular/forms"
import { Observable, Subject } from "rxjs"
import { PaginatedMetadata } from "../../PaginatedMetadata"
import { ApiSortOrder } from "../../../richskill/ApiSkill"
import { IJobCode } from "../../job-codes/Jobcode"
import { TableActionBarComponent } from "../../../table/skills-library-table/table-action-bar.component"
import { Whitelabelled } from "../../../../whitelabel"
import { ApiNamedReference, INamedReference } from "../../named-references/NamedReference"
import { TableActionDefinition } from "../../../table/skills-library-table/has-action-definitions"
import { ButtonAction } from "../../../auth/auth-roles"
import { AuthService } from "../../../auth/auth-service"
import { MetadataType } from "../../rsd-metadata.enum"
import { JobCodeService } from "../../job-codes/service/job-code.service"

@Component({
  selector: "app-metadata-list",
  templateUrl: "./metadata-list.component.html"
})
export class MetadataListComponent extends Whitelabelled implements OnInit {

  @ViewChild(TableActionBarComponent) actionBar!: TableActionBarComponent

  title = "Metadata"
  handleSelectedMetadata?: IJobCode[]|INamedReference[]
  selectedMetadataType = "category"
  matchingQuery?: string = ""

  typeControl: FormControl = new FormControl(this.selectedMetadataType)
  columnSort: ApiSortOrder = ApiSortOrder.NameAsc

  from = 0
  size = 50
  showSearchEmptyMessage = false
  resultsLoaded: Observable<PaginatedMetadata> | undefined
  canDeleteMetadata = this.authService.isEnabledByRoles(ButtonAction.MetadataAdmin)

  searchForm = new FormGroup({
    search: new FormControl("")
  })

  sampleNamedReferenceResult = new PaginatedMetadata([
    new ApiNamedReference({id: "id1", framework: "framework1", name: "name1", type: MetadataType.Category, value: "value1"}),
    new ApiNamedReference({id: "id2", framework: "framework2", name: "name2", type: MetadataType.Category, value: "value2"}),
    new ApiNamedReference({id: "id3", framework: "framework3", name: "name3", type: MetadataType.Category, value: "value3"}),
    new ApiNamedReference({id: "id4", framework: "framework4", name: "name4", type: MetadataType.Category, value: "value4"}),
    new ApiNamedReference({id: "id5", framework: "framework5", name: "name5", type: MetadataType.Category, value: "value5"}),
    new ApiNamedReference({id: "id6", framework: "framework6", name: "name6", type: MetadataType.Category, value: "value6"}),
    new ApiNamedReference({id: "id7", framework: "framework7", name: "name7", type: MetadataType.Category, value: "value7"}),
    new ApiNamedReference({id: "id8", framework: "framework8", name: "name8", type: MetadataType.Category, value: "value8"}),
  ], 8)

  results: PaginatedMetadata = this.sampleNamedReferenceResult

  clearSelectedItemsFromTable = new Subject<void>()
  constructor(
    protected authService: AuthService,
    protected jobCodeService: JobCodeService
  ) {
    super()
  }

  ngOnInit(): void {
    this.typeControl.valueChanges.subscribe(
      value => {
        this.selectedMetadataType = value
        this.loadNextPage()
      })
    this.searchForm.get("search")?.valueChanges.subscribe( value => this.matchingQuery = value ?? "")
    this.loadNextPage()
    }

  clearSearch(): void {
    this.searchForm.get("search")?.patchValue("")
    this.handleDefaultSubmit()
  }

  handleDefaultSubmit(): void {
    this.from = 0
    this.loadNextPage()
  }

  loadNextPage(): void {
    if (this.selectedMetadataType === MetadataType.JobCode) {
      this.jobCodeService.paginatedJobCodes(this.size, this.from, this.columnSort, this.matchingQuery).subscribe(
        jobCodes => this.results = jobCodes
      )
    } else {
      this.results = this.sampleNamedReferenceResult
    }
  }

  handleSelectAll(selectAllChecked: boolean): void {}

  handleNewSelection(selected: IJobCode[]|INamedReference[]): void {
    this.handleSelectedMetadata = selected
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
      if (this.selectedMetadataType !== MetadataType.Category) {
        return `${this.totalCount} ${this.selectedMetadataType}${this.totalCount > 1 ? "s" : ""}`
      }
      else if (this.totalCount > 1) {
        return `${this.totalCount} categories`
      }
      else {
        return `${this.totalCount} category`
      }
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

  get emptyResults(): boolean {
    return this.curPageCount < 1
  }
  get isJobCodeDataSelected(): boolean {
    return this.selectedMetadataType === MetadataType.JobCode
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

  rowActions(): TableActionDefinition[] {
    const tableActions = []
    if (this.canDeleteMetadata) {
      tableActions.push(new TableActionDefinition({
        label: `Delete`,
        callback: (action: TableActionDefinition, skill?: IJobCode|INamedReference) => this.handleClickDeleteItem(action, skill),
      }))
    }
    return tableActions
  }

  // tslint:disable-next-line:typedef
  private handleClickDeleteItem(action: TableActionDefinition, skill: IJobCode|INamedReference | undefined) {
  }
}
