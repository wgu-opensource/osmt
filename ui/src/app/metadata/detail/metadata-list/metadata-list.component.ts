import { Component, OnInit } from "@angular/core"
import { FormControl } from "@angular/forms"
import { Subject } from "rxjs"
import { ApiJobCode, IJobCode } from "../../job-codes/Jobcode"
import { ApiNamedReference, INamedReference } from "../../named-references/NamedReference"
import { TableActionDefinition } from "../../../table/skills-library-table/has-action-definitions"
import { ButtonAction } from "../../../auth/auth-roles"
import { AuthService } from "../../../auth/auth-service"
import { MetadataType } from "../../rsd-metadata.enum"
import { JobCodeService } from "../../job-codes/service/job-code.service"
import { ToastService } from "../../../toast/toast.service"
import { AbstractListComponent } from "../../../table/abstract-list.component"
import {NamedReferenceService} from "../../named-references/service/named-reference.service";

@Component({
  selector: "app-metadata-list",
  templateUrl: "./metadata-list.component.html"
})
export class MetadataListComponent extends AbstractListComponent<IJobCode | INamedReference> implements OnInit {

  selectedMetadataType = MetadataType.Category

  typeControl: FormControl = new FormControl(this.selectedMetadataType)

  showSearchEmptyMessage = false
  canDeleteMetadata = this.authService.isEnabledByRoles(ButtonAction.MetadataAdmin)


  clearSelectedItemsFromTable = new Subject<void>()
  constructor(
    protected authService: AuthService,
    protected jobCodeService: JobCodeService,
    protected toastService: ToastService,
    protected namedReferenceService: NamedReferenceService
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
      // this.results = this.sampleNamedReferenceResult
      this.resultsLoaded = this.namedReferenceService.paginatedNamedReferences(this.size, this.from, this.columnSort, this.selectedMetadataType, this.matchingQuery)
      this.resultsLoaded.subscribe(namedReferences => this.results = namedReferences)
    }
  }

  get metadataCountLabel(): string {
      if (this.selectedMetadataType !== MetadataType.Category) {
        return `${this.totalCount} ${this.selectedMetadataType}${this.totalCount != 1 ? "s" : ""}`
      }
      else if (this.totalCount != 1) {
        return `${this.totalCount} categories`
      }
      else {
        return `${this.totalCount} category`
      }
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
    else {
      if (confirm("Confirm that you want to delete multiple Named References..")) {
        this.handleDeleteMultipleNamedReferences(this.selectedData as INamedReference[], 0)
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

  private handleDeleteMultipleNamedReferences(namedReferences: INamedReference[], index: number, notDeleted = 0): void {
    if (index < namedReferences.length) {
      this.namedReferenceService.deleteNamedReference(namedReferences[index].id ?? 0).subscribe(data => {
        if (data) {
          this.handleDeleteMultipleNamedReferences(namedReferences, index + 1, notDeleted)
        } else {
          this.handleDeleteMultipleNamedReferences(namedReferences, index + 1, notDeleted + 1)
        }
      })
    } else {
      if (notDeleted > 0) {
        this.toastService.showToast("Warning", "Some Named References could not be deleted")
      } else {
        this.toastService.showToast("Success", "All selected Named References have been deleted")
      }
      this.loadNextPage()
    }
  }

  private handleClickDeleteItem(metadata: IJobCode | INamedReference | undefined): void {
    if (this.isJobCodeDataSelected) {
      this.handleDeleteJobCode(metadata as IJobCode)
    } else {
      this.handleDeleteNamedReference(metadata as INamedReference)

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

  private handleDeleteNamedReference(namedReference: INamedReference): void {
    if (confirm("Confirm that you want to delete the Named Reference with name " + (namedReference as ApiNamedReference)?.name)) {
      this.namedReferenceService.deleteNamedReferenceWithResult((namedReference as ApiNamedReference)?.id ?? 0).subscribe(data => {
        if (data && data.success) {
          this.toastService.showToast("Successfully Deleted", "" + (namedReference as ApiNamedReference)?.name)
          this.loadNextPage()
        } else if (data && !data.success) {
          this.toastService.showToast("Warning", data.message ?? "You cannot delete this Named Reference")
        }
      })
    }
  }

}
