import {ApiSearch, ApiSkillListUpdate, PaginatedSkills} from "../service/rich-skill-search.service";
import {ApiSkillSummary} from "../ApiSkillSummary";
import {checkArchived, determineFilters, PublishStatus} from "../../PublishStatus";
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions";
import {Component, ElementRef, ViewChild} from "@angular/core";
import {Observable} from "rxjs";
import {ApiBatchResult} from "../ApiBatchResult";
import {RichSkillService} from "../service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {ApiSortOrder} from "../ApiSkill";
import {Router} from "@angular/router";
import {QuickLinksHelper} from "../../core/quick-links-helper";
import {ExtrasSelectedSkillsState} from "../../collection/add-skills-collection.component";
import {TableActionBarComponent} from "../../table/skills-library-table/table-action-bar.component";
import {AuthService} from "../../auth/auth-service";
import {ButtonAction} from "../../auth/auth-roles";
import {CollectionService} from "../../collection/service/collection.service"
import {ApiCollection} from "../../collection/ApiCollection"
import {CollectionPipe} from "../../pipes"
import {FilterDropdown} from "../../models/filter-dropdown.model"

@Component({
  selector: "app-skills-list",
  templateUrl: "./skills-list.component.html"
})
export class SkillsListComponent extends QuickLinksHelper {

  pageSizeOptions = [50, 100, 150]
  from = 0
  size = this.pageSizeOptions?.length > 0 ? this.pageSizeOptions[0] : 50
  collection?: ApiCollection
  showAdvancedFilteredSearch = false

  @ViewChild("titleHeading") titleElement!: ElementRef
  @ViewChild(TableActionBarComponent) tableActionBar!: TableActionBarComponent


  resultsLoaded: Observable<PaginatedSkills> | undefined
  results: PaginatedSkills | undefined

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published])
  keywords: FilterDropdown = {
    categories: [],
    certifications: [],
    employers: [],
    alignments: [],
    keywords: [],
    occupations: [],
    standards: [],
    authors: []
  }
  selectedSkills?: ApiSkillSummary[]
  skillsSaved?: Observable<ApiBatchResult>

  columnSort: ApiSortOrder = ApiSortOrder.SkillAsc

  showSearchEmptyMessage = false
  showLibraryEmptyMessage = false

  showAddToCollection = true
  showExportSelected = false

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected collectionService: CollectionService,
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

  protected exportSearchVisible(): boolean {
    return false
  }

  addToVisible(): boolean {
    return (this.selectedSkills?.length ?? 0) > 0
  }

  addToCollectionVisible(skill?: ApiSkillSummary): boolean {
    if (this.collection?.status === PublishStatus.Workspace) {
      return this.addToVisible() && this.authService.isEnabledByRoles(ButtonAction.MyWorkspace)
    }
    return this.addToVisible() && this.authService.isEnabledByRoles(ButtonAction.CollectionSkillsUpdate)
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
        label: `Remove from ${this.collectionOrWorkspace(true)}`,
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
      })
    ]

    if (this.showExportSelected) {
      actions.push(new TableActionDefinition({
        label: "Export Selected",
        icon: "download",
        callback: (action: TableActionDefinition, kill?: ApiSkillSummary) => this.handleClickExportSearch(),
        visible: () => this.exportSearchVisible()
      }))
    }

    if (this.showAddToCollection) {
      actions.push(new TableActionDefinition({
        label: "Add to",
        icon: "add",
        primary: true,
        visible: (skill?: ApiSkillSummary) => this.addToVisible(),
        menu: [
          {
            label: "Add to Collection",
            callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickAddCollection(action, skill),
            visible: () => this.addToCollectionVisible()
          },
          {
            label: "Add to Workspace",
            callback: () => this.handleClickAddToWorkspace(),
            visible: () => this.addToWorkspaceVisible()
          }
        ]
      }))
    } else {
      actions.push(new TableActionDefinition({
        label: `Remove from ${this.collectionOrWorkspace(true)}`,
        icon: "dismiss",
        primary: true,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickRemoveCollection(action, skill),
        visible: (skill?: ApiSkillSummary) => this.addToCollectionVisible(skill) || this.addToWorkspaceVisible()
      }))
    }

    return actions

  }

  protected addToWorkspaceVisible(): boolean {
    return this.addToVisible() && this.authService.isEnabledByRoles(ButtonAction.MyWorkspace)
  }

  protected handleClickExportSearch(): void {
  }

  protected handleClickBackToTop(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement)
    return false
  }

  protected handleClickAddToWorkspace(): void {
    const skillListUpdate = this.getSelectAllEnabled() ? new ApiSkillListUpdate(
      {add: new ApiSearch({query: this.matchingQuery?.join("")})}
    ) : new ApiSkillListUpdate(
      {add: new ApiSearch({uuids: this.getSelectedSkills()?.map(i => i.uuid)})}
    )
    this.toastService.showBlockingLoader()
    this.collectionService.getWorkspace().subscribe(workspace => {
      this.collectionService.updateSkillsWithResult(workspace.uuid, skillListUpdate).subscribe(result => {
        if (result) {
          const message = `You added ${result.modifiedCount} RSDs to the workspace.`
          this.toastService.showToast("Success!", message)
          this.toastService.hideBlockingLoader()
        }
      })
    })
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

  collectionOrWorkspace(includesMy: boolean): string {
    return new CollectionPipe().transform(this.collection?.status, includesMy)
  }

  keywordsChange(keywords: FilterDropdown): void {
    this.keywords = keywords
    this.loadNextPage()
  }

  sizeChange(size: number): void {
    this.size = size
    this.from = 0
    this.handlePageClicked(1)
  }

  get selectedKeywords(): any {
    const a: any = {}
    const b: any = this.keywords
    for (const key in this.keywords) {
      if (b[key].length > 0) {
        a[key] = b[key].map((i: any) => i.name ?? i.code)
      }
    }
    return a
  }

}
