import { Component, OnInit } from "@angular/core"
import { FormControl } from "@angular/forms"
import { Subject } from "rxjs"
import { PaginatedMetadata } from "../../PaginatedMetadata"
import { ApiJobCode, IJobCode } from "../../job-codes/Jobcode"
import { ApiNamedReference, INamedReference } from "../../named-references/NamedReference"
import { TableActionDefinition } from "../../../table/skills-library-table/has-action-definitions"
import { ButtonAction } from "../../../auth/auth-roles"
import { AuthService } from "../../../auth/auth-service"
import { MetadataType } from "../../rsd-metadata.enum"
import { JobCodeService } from "../../job-codes/service/job-code.service"
import { ToastService } from "../../../toast/toast.service"
import { AbstractListComponent } from "../../../table/abstract-list.component"

@Component({
  selector: "app-metadata-list",
  templateUrl: "./metadata-list.component.html"
})
export class MetadataListComponent extends AbstractListComponent<IJobCode | INamedReference> implements OnInit {

  selectedMetadataType = MetadataType.Category

  typeControl: FormControl = new FormControl(this.selectedMetadataType)

  showSearchEmptyMessage = false
  canDeleteMetadata = this.authService.isEnabledByRoles(ButtonAction.MetadataAdmin)

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
    protected jobCodeService: JobCodeService,
    protected toastService: ToastService
  ) {
    super()
  }

  ngOnInit(): void {
    this.typeControl.valueChanges.subscribe(
      value => {
        this.selectedMetadataType = value
        this.loadNextPage()
      })
    this.loadNextPage()
    }

  clearSearch(): void {
    this.searchForm.get("search")?.patchValue("")
    this.matchingQuery = ""
    this.handleDefaultSubmit()
  }

  handleDefaultSubmit(): void {
    this.matchingQuery = this.searchForm.get("search")?.value ?? ""
    this.from = 0
    this.loadNextPage()
  }

  loadNextPage(): void {
    this.selectedData = []
    if (this.isJobCodeDataSelected) {
      this.resultsLoaded = this.jobCodeService.paginatedJobCodes(this.size, this.from, this.columnSort, this.matchingQuery)
      this.resultsLoaded.subscribe(jobCodes => this.results = jobCodes)
    } else {
      this.results = this.sampleNamedReferenceResult
    }
  }

  get metadataCountLabel(): string {
    return `${this.totalCount} ${this.selectedMetadataType}`
  }

  get isJobCodeDataSelected(): boolean {
    return this.selectedMetadataType === MetadataType.JobCode
  }

  getJobCodes(): IJobCode[] {
    return (this.results?.data) as IJobCode[]
  }

  getNamedReferences(): INamedReference[] {
    return (this.results?.data) as INamedReference[]
  }

  rowActions(): TableActionDefinition[] {
    const tableActions = []
    if (this.canDeleteMetadata && !this.selectAllChecked) {
      tableActions.push(new TableActionDefinition({
        label: `Delete`,
        callback: (action: TableActionDefinition, metadata?: IJobCode | INamedReference) => this.handleClickDeleteItem(metadata),
        visible: () => !this.selectAllChecked
      }))
    }
    return tableActions
  }

  tableActions(): TableActionDefinition[] {
    const tableActions: TableActionDefinition[] = []
    tableActions.push(
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: () => this.handleClickBackToTop(),
        visible: () => true
      }),
      new TableActionDefinition({
        label: "Delete Selected",
        icon: "remove",
        callback: () => this.handleDeleteMultipleMetadata(),
        visible: () => (this.selectedData?.length ?? 0) > 0
      })
    )
    return tableActions
  }

  get selectedJobCodesOrderedByLevel(): IJobCode[] {
    return (this.selectedData as IJobCode[]).sort((a, b) => {
      if ((a.jobCodeLevelAsNumber ?? 0) > (b.jobCodeLevelAsNumber ?? 0)) {
        return -1
      } else if ((a.jobCodeLevelAsNumber) ?? 0 < (b.jobCodeLevelAsNumber ?? 0)) {
        return 1
      }
      return 0
    })
  }

  private handleDeleteMultipleMetadata(): void {
    if (this.isJobCodeDataSelected) {
      if (confirm("Confirm that you want to delete multiple job codes")) {
        this.handleDeleteMultipleJobCodes(this.selectedJobCodesOrderedByLevel, 0)
      }
    }
  }

  private handleDeleteMultipleJobCodes(jobCodes: IJobCode[], index: number, notDeleted = 0): void {
    if (index < jobCodes.length) {
      this.jobCodeService.deleteJobCodeWithResult(jobCodes[index].id ?? 0).subscribe(data => {
        if (data && data.success) {
          this.handleDeleteMultipleJobCodes(jobCodes, index + 1, notDeleted)
        } else if (data && !data.success) {
          this.handleDeleteMultipleJobCodes(jobCodes, index + 1, notDeleted + 1)
        }
      })
    } else {
      if (notDeleted > 0) {
        this.toastService.showToast("Warning", "Some occupations cannot be deleted")
      } else {
        this.toastService.showToast("Success", "All selected occupations have been deleted")
      }
      this.loadNextPage()
    }
  }

  private handleClickDeleteItem(metadata: IJobCode | INamedReference | undefined): void {
    if (this.isJobCodeDataSelected) {
      this.handleDeleteJobCode(metadata as IJobCode)
    }
  }

  private handleDeleteJobCode(jobCode: IJobCode): void {
    if (confirm("Confirm that you want to delete the job code with name " + (jobCode as ApiJobCode)?.targetNodeName)) {
      this.jobCodeService.deleteJobCodeWithResult((jobCode as ApiJobCode)?.id ?? 0).subscribe(data => {
        if (data && data.success) {
          this.toastService.showToast("Success", "You deleted a job code " + (jobCode as ApiJobCode)?.targetNodeName)
          this.loadNextPage()
        } else if (data && !data.success) {
          this.toastService.showToast("Warning", data.message ?? "You cannot delete this job code")
        }
      })
    }
  }

}
