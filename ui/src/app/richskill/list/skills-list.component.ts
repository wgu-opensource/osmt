import {ApiSearch, PaginatedSkills} from "../service/rich-skill-search.service";
import {ApiSkillSummary} from "../ApiSkillSummary";
import {checkArchived, determineFilters, PublishStatus} from "../../PublishStatus";
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions";
import {Component, ElementRef, Inject, LOCALE_ID, ViewChild} from "@angular/core"
import {Observable} from "rxjs";
import {ApiBatchResult} from "../ApiBatchResult";
import {RichSkillService} from "../service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {ApiSkill, ApiSortOrder} from "../ApiSkill"
import {Router} from "@angular/router";
import {QuickLinksHelper} from "../../core/quick-links-helper";
import {ExtrasSelectedSkillsState} from "../../collection/add-skills-collection.component";
import {TableActionBarComponent} from "../../table/skills-library-table/table-action-bar.component";
import {AuthService} from "../../auth/auth-service";
import {ButtonAction} from "../../auth/auth-roles";
import {ApiTaskResult} from "../../task/ApiTaskResult"
import {apiTaskResultForCSV} from "../../../../test/resource/mock-data"
import {formatDate} from "@angular/common"
import * as FileSaver from "file-saver"


@Component({
  selector: "app-skills-list",
  templateUrl: "./skills-list.component.html"
})
export class SkillsListComponent extends QuickLinksHelper {

  from = 0
  size = 50

  @ViewChild("titleHeading") titleElement!: ElementRef
  @ViewChild(TableActionBarComponent) tableActionBar!: TableActionBarComponent


  resultsLoaded: Observable<PaginatedSkills> | undefined
  results: PaginatedSkills | undefined

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published])
  selectedSkills?: ApiSkillSummary[]
  skillsSaved?: Observable<ApiBatchResult>

  columnSort: ApiSortOrder = ApiSortOrder.NameAsc

  showSearchEmptyMessage = false
  showLibraryEmptyMessage = false

  showAddToCollection = true

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected authService: AuthService,
  ) {
    super()
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
      return `${this.totalCount} RSD${this.totalCount > 1 ? "s" : ""}`
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
    return true
    // return (this.selectedSkills?.length ?? 0) > 0
  }

  publishVisible(skill?: ApiSkillSummary): boolean {
    if (skill !== undefined) {
      return skill.publishDate === undefined && this.authService.isEnabledByRoles(ButtonAction.SkillPublish)
    } else if ((this.selectedSkills?.length ?? 0) === 0) {
      return false
    } else {
      const unpublishedSkill = this.selectedSkills?.find(s => s.publishDate === undefined)
      return unpublishedSkill !== undefined && this.authService.isEnabledByRoles(ButtonAction.SkillPublish)
    }
  }
  archiveVisible(skill?: ApiSkillSummary): boolean {
    if (skill !== undefined) {
      return !checkArchived(skill) && this.authService.isEnabledByRoles(ButtonAction.SkillUpdate)
    } else if ((this.selectedSkills?.length ?? 0) === 0) {
      return false
    } else {
      const unarchivedSkills = this.selectedSkills?.find(s => !checkArchived(s))
      return unarchivedSkills !== undefined && this.authService.isEnabledByRoles(ButtonAction.SkillUpdate)
    }
  }

  unarchiveVisible(skill?: ApiSkillSummary): boolean {
    if (skill !== undefined) {
      return checkArchived(skill) && this.authService.isEnabledByRoles(ButtonAction.SkillUpdate)
    } else if ((this.selectedSkills?.length ?? 0) === 0) {
      return false
    } else {
      const archivedSkill = this.selectedSkills?.find(checkArchived)
      return archivedSkill !== undefined && this.authService.isEnabledByRoles(ButtonAction.SkillUpdate)
    }
  }

  exportSearchVisible(): boolean {
    return ((this.selectedSkills?.length ?? 0) > 0) && this.authService.isEnabledByRoles(ButtonAction.ExportSearch)
  }

  addToCollectionVisible(skill?: ApiSkillSummary): boolean {
    return ((this.selectedSkills?.length ?? 0) > 0) && this.authService.isEnabledByRoles(ButtonAction.CollectionSkillsUpdate)
  }

  handleFiltersChanged(newFilters: Set<PublishStatus>): void {
    this.selectedFilters = newFilters
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

  handleNewSelection(selectedSkills: ApiSkillSummary[]): void {
    this.selectedSkills = selectedSkills
  }

  handleHeaderColumnSort(sort: ApiSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }


  rowActions(): TableActionDefinition[] {
    const actions = [
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
    ]
    if (this.showAddToCollection) {
      actions.push(new TableActionDefinition({
        label: "Add to Collection",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickAddCollection(action, skill),
        visible: (skill?: ApiSkillSummary) => this.addToCollectionVisible(skill)
      }))
    } else {
      actions.push(new TableActionDefinition({
        label: "Remove from Collection",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickRemoveCollection(action, skill),
        visible: (skill?: ApiSkillSummary) => this.addToCollectionVisible(skill)
      }))
    }
    return actions
  }

  tableActions(): TableActionDefinition[] {
    const actions = [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickBackToTop(action, skill),
        visible: (skill?: ApiSkillSummary) => true
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
        label: "Export search",
        icon: "unarchive",
        callback: (action: TableActionDefinition, kill?: ApiSkillSummary) => this.handleClickExportSearch(),
        visible: () => this.exportSearchVisible()
      })
    ]

    if (this.showAddToCollection) {
      actions.push(new TableActionDefinition({
        label: "Add to Collection",
        icon: "collection",
        primary: true,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickAddCollection(action, skill),
        visible: (skill?: ApiSkillSummary) => this.addToCollectionVisible(skill)
      }))
    } else {
      actions.push(new TableActionDefinition({
        label: "Remove from Collection",
        icon: "dismiss",
        primary: true,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickRemoveCollection(action, skill),
        visible: (skill?: ApiSkillSummary) => this.addToCollectionVisible(skill)
      }))
    }

    return actions

  }

  protected handleClickExportSearch(): void {
  }

  protected handleClickBackToTop(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement)
    return false
  }

  protected handleClickAddCollection(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    const selection = this.getSelectedSkills(skill)
    this.router.navigate(["/collections/add-skills"], {
      state: {
        selectedSkills: selection,
        totalCount: selection?.length ?? 0
      } as ExtrasSelectedSkillsState
    })
    return false
  }

  private handleClickRemoveCollection(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.removeFromCollection(skill)
    return false
  }

  private handleClickUnarchive(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.submitStatusChange(PublishStatus.Unarchived, "unarchived", skill)
    return false
  }

  protected onlyDraftsSelected(skill?: ApiSkillSummary): boolean {
    return skill?.status === PublishStatus.Draft || this.selectedSkills?.find(it => it.status !== PublishStatus.Draft) === undefined
  }

  private handleClickArchive(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    const message = `Check that the selected RSDs aren't included in any published collections, then click "OK" to archive them.`
    if (this.onlyDraftsSelected(skill) || confirm(message)) {
      this.submitStatusChange(PublishStatus.Archived, "archived", skill)
    }
    return false
  }

  private handleClickPublish(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    const plural = (this.selectedUuids(skill)?.length ?? 0) > 1 ? "these RSDs" : "this RSD"
    if (confirm(`Are you sure you want to publish ${plural}?\nOnce published, an RSD can't be unpublished.`)) {
      this.submitStatusChange(PublishStatus.Published, "published", skill)
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
      return new ApiSearch({uuids: [skill.uuid]})
    }

    const selected = this.selectedUuids(skill)
    if (selected !== undefined) {
      return new ApiSearch({uuids: selected})
    }

    return undefined
  }

  submitStatusChange(newStatus: PublishStatus, verb: string, skill?: ApiSkillSummary): boolean  {
    const apiSearch = this.getApiSearch(skill)
    if (apiSearch === undefined) {
      return false
    }

    this.toastService.showBlockingLoader()
    this.skillsSaved = this.richSkillService.publishSkillsWithResult(apiSearch, newStatus, determineFilters(this.selectedFilters))
    this.skillsSaved.subscribe((result) => {
      if (result !== undefined) {
        const partial = (result.modifiedCount !== result.totalCount)  ? ` of ${result.totalCount}` : ""
        const message = `You ${verb} ${result.modifiedCount}${partial} RSD${(result.totalCount ?? 0) > 1 ? "s" : ""}.`
        this.toastService.showToast("Success!", message)
        this.toastService.hideBlockingLoader()
        this.loadNextPage()
      }
    })
    return false
  }

  getMobileSortOptions(): {[s: string]: string} {
    return {
      "name.asc": "Category (ascending)",
      "name.desc": "Category (descending)",
      "skill.asc": "RSD Name (ascending)",
      "skill.desc": "RSD Name (descending)",
    }
  }

  getSelectAllCount(): number {
    return this.curPageCount
  }

  getSelectAllEnabled(): boolean {
    return true
  }

  removeFromCollection(skill?: ApiSkillSummary): void {
  }

  focusActionBar(): void {
    this.tableActionBar.focus()
  }
}
