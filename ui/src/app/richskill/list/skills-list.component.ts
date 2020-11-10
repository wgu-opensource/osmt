import {ApiSearch, PaginatedSkills} from "../service/rich-skill-search.service";
import {ApiSkillSummary, IApiSkillSummary} from "../ApiSkillSummary";
import {PublishStatus} from "../../PublishStatus";
import {TableActionDefinition} from "../../table/has-action-definitions";
import {Component} from "@angular/core";
import {Observable} from "rxjs";
import {ApiBatchResult} from "../ApiBatchResult";
import {RichSkillService} from "../service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {ApiSkillSortOrder} from "../ApiSkill";
import {Router} from "@angular/router";


@Component({
  selector: "app-skills-list",
  templateUrl: "./skills-list.component.html"
})
export class SkillsListComponent {

  from = 0
  size = 50

  resultsLoaded: Observable<PaginatedSkills> | undefined
  results: PaginatedSkills | undefined

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unpublished, PublishStatus.Published])
  selectedSkills?: IApiSkillSummary[]
  skillsSaved?: Observable<ApiBatchResult>

  columnSort: ApiSkillSortOrder = ApiSkillSortOrder.CategoryAsc

  showSearchEmptyMessage = false
  showLibraryEmptyMessage = false

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService
  ) {
  }

  // "abstract" methods to be implemented by a "subclas"
  matchingQuery?: string[]
  title?: string
  loadNextPage(): void {}
  handleSelectAll(selectAllChecked: boolean): void {}


  // base component methods

  protected setResults(results: PaginatedSkills): void {
    this.results = results
    this.selectedSkills = undefined
  }

  get skillCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.totalCount} RSD${this.curPageCount > 1 ? "s" : ""}`
    }
    return `0 RSDs`
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
    return Math.min(this.from + this.curPageCount, this.totalCount)
  }

  get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }
  get currentPageNo(): number {
    return Math.floor(this.from / this.size) + 1
  }

  navigateToPage(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  actionsVisible(): boolean {
    return (this.selectedSkills?.length ?? 0) > 0
  }

  publishVisible(skill?: ApiSkillSummary): boolean {
    if (skill !== undefined) {
      return skill.status === PublishStatus.Unpublished
    } else {
      const unpublishedSkill = this.selectedSkills?.find(s => s.status === PublishStatus.Unpublished)
      return unpublishedSkill !== undefined
    }
  }
  archiveVisible(skill?: ApiSkillSummary): boolean {
    if (skill !== undefined) {
      return skill.status === PublishStatus.Published
    } else {
      const unarchivedSkills = this.selectedSkills?.find(s => s.status === PublishStatus.Published)
      return unarchivedSkills !== undefined
    }
  }
  unarchiveVisible(skill?: ApiSkillSummary): boolean {
    if (skill !== undefined) {
      return skill.status === PublishStatus.Archived
    } else {
      const archivedSkill = this.selectedSkills?.find(s => s.status === PublishStatus.Archived)
      return archivedSkill !== undefined
    }
  }


  handleFiltersChanged(newFilters: Set<PublishStatus>): void {
    this.selectedFilters = newFilters
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

  handleNewSelection(selectedSkills: IApiSkillSummary[]): void {
    this.selectedSkills = selectedSkills
  }

  handleHeaderColumnSort(sort: ApiSkillSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }


  rowActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Archive RSD",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickArchive(action, skill),
        visible: (skill?: ApiSkillSummary) => this.archiveVisible(skill)
      }),
      new TableActionDefinition({
        label: "Unarchive RSD",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickUnarchive(action, skill),
        visible: (skill?: ApiSkillSummary) => this.unarchiveVisible(skill)
      }),
      new TableActionDefinition({
        label: "Publish RSD",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickPublish(action, skill),
        visible: (skill?: ApiSkillSummary) => this.publishVisible(skill)
      }),
      new TableActionDefinition({
        label: "Add to Collection",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickAddCollection(action, skill),
      }),
    ]
  }

  tableActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickBackToTop(action, skill),
      }),

      new TableActionDefinition({
        label: "Publish",
        icon: "publish",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickPublish(action, skill),
        visible: (skill?: ApiSkillSummary) => this.publishVisible(skill)
      }),

      new TableActionDefinition({
        label: "Archive",
        icon: "archive",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickArchive(action, skill),
        visible: (skill?: ApiSkillSummary) => this.archiveVisible(skill)
      }),

      new TableActionDefinition({
        label: "Unarchive",
        icon: "unarchive",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickUnarchive(action, skill),
        visible: (skill?: ApiSkillSummary) => this.unarchiveVisible(skill)
      }),

      new TableActionDefinition({
        label: "Add to Collection",
        icon: "collection",
        primary: true,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickAddCollection(action, skill),
      }),
    ]

  }

  private handleClickBackToTop(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    return false
  }

  private handleClickAddCollection(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.router.navigate(["/collections/add-skills"], {
      state: this.getSelectedSkills(skill)
    })
    return false
  }

  private handleClickUnarchive(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.submitStatusChange(PublishStatus.Published, "Un-archived", skill)
    return false
  }

  private handleClickArchive(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.submitStatusChange(PublishStatus.Archived, "Archived", skill)
    return false
  }

  private handleClickPublish(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    const plural = (this.selectedUuids(skill)?.length ?? 0) > 1 ? "these RSDs" : "this RSD"
    if (confirm(`Are you sure you want to publish ${plural}?`)) {
      this.submitStatusChange(PublishStatus.Published, "Published", skill)
    }
    return false
  }

  getSelectedSkills(skill?: ApiSkillSummary): ApiSkillSummary[] | undefined {
    return ((skill !== undefined) ? [skill] : this.selectedSkills)
  }
  selectedUuids(skill?: ApiSkillSummary): string[] | undefined {
    return this.getSelectedSkills(skill)?.map(it => it.uuid)
  }

  getApiSearch(skill?: ApiSkillSummary): ApiSearch | undefined {
    if (skill !== undefined) {
      return ApiSearch.factory({uuids: [skill.uuid]})
    }

    const selected = this.selectedUuids(skill)
    if (selected !== undefined) {
      return ApiSearch.factory({uuids: selected})
    }

    return undefined
  }

  submitStatusChange(newStatus: PublishStatus, verb: string, skill?: ApiSkillSummary): boolean  {
    const apiSearch = this.getApiSearch(skill)
    if (apiSearch === undefined) {
      return false
    }


    this.toastService.showBlockingLoader()
    this.skillsSaved = this.richSkillService.publishSkillsWithResult(apiSearch, newStatus, this.selectedFilters)
    this.skillsSaved.subscribe((result) => {
      if (result !== undefined) {
        const partial = (result.modifiedCount !== result.totalCount)  ? ` of ${result.totalCount}` : ""
        const message = `${verb} ${result.modifiedCount}${partial} RSD${(result.totalCount ?? 0) > 1 ? "s" : ""}.`
        this.toastService.showToast("Success!", message)
        this.toastService.hideBlockingLoader()
        this.loadNextPage()
      }
    })
    return false
  }

  getSelectAllCount(): number {
    return this.curPageCount
  }

  getSelectAllEnabled(): boolean {
    return true
  }
}
