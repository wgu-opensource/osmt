import {Component, ViewChild} from "@angular/core"
import {Observable} from "rxjs"
import {ApiSearch, PaginatedCollections} from "../richskill/service/rich-skill-search.service"
import {checkArchived, determineFilters, PublishStatus} from "../PublishStatus"
import {ApiCollectionSummary, ICollectionSummary} from "../richskill/ApiSkillSummary"
import {ApiBatchResult} from "../richskill/ApiBatchResult"
import {ApiSortOrder} from "../richskill/ApiSkill"
import {Router} from "@angular/router"
import {ToastService} from "../toast/toast.service"
import {CollectionService} from "./service/collection.service"
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions"
import {TableActionBarComponent} from "../table/skills-library-table/table-action-bar.component"


@Component({
  selector: "app-collections-list",
  templateUrl: "./collections-list.component.html"
})
export class CollectionsListComponent {

  @ViewChild(TableActionBarComponent) actionBar!: TableActionBarComponent

  from = 0
  size = 50

  resultsLoaded: Observable<PaginatedCollections> | undefined
  results: PaginatedCollections | undefined

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published])
  selectedCollections?: ICollectionSummary[]
  collectionsSaved?: Observable<ApiBatchResult>

  columnSort: ApiSortOrder = ApiSortOrder.NameAsc

  showSearchEmptyMessage = false
  showLibraryEmptyMessage = false

  constructor(protected router: Router,
              protected toastService: ToastService,
              protected collectionService: CollectionService,
  ) {
  }

  // "abstract" methods to be implemented by a "subclass"
  matchingQuery?: string[]
  title?: string
  loadNextPage(): void {}
  handleSelectAll(selectAllChecked: boolean): void {}


  // base component methods

  protected setResults(results: PaginatedCollections): void {
    this.results = results
    this.selectedCollections = undefined
  }

  get collectionCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.totalCount} collection${this.curPageCount > 1 ? "s" : ""}`
    }
    return `0 collections`
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get curPageCount(): number {
    return this.results?.collections.length ?? 0
  }

  getMobileSortOptions(): {[s: string]: string} {
    return {
      "name.asc": "Collection name (ascending)",
      "name.desc": "Collection name (descending)",
      "skill.asc": "Skill count (ascending)",
      "skill.desc": "Skill count (descending)",
    }
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
    // return (this.selectedCollections?.length ?? 0) > 0
  }

  publishVisible(skill?: ApiCollectionSummary): boolean {
    if (skill !== undefined) {
      return skill.publishDate === undefined
    } else if ((this.selectedCollections?.length ?? 0) === 0) {
      return false
    } else {
      const unpublishedSkill = this.selectedCollections?.find(s => s.publishDate === undefined)
      return unpublishedSkill !== undefined
    }
  }
  archiveVisible(skill?: ApiCollectionSummary): boolean {
    if (skill !== undefined) {
      return !checkArchived(skill)
    } else if ((this.selectedCollections?.length ?? 0) === 0) {
      return false
    } else {
      const unarchivedSkills = this.selectedCollections?.find(s => !checkArchived(s))
      return unarchivedSkills !== undefined
    }
  }
  unarchiveVisible(skill?: ApiCollectionSummary): boolean {
    if (skill !== undefined) {
      return checkArchived(skill)
    } else if ((this.selectedCollections?.length ?? 0) === 0) {
      return false
    } else {
      const archivedSkill = this.selectedCollections?.find(checkArchived)
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

  handleNewSelection(selected: ICollectionSummary[]): void {
    this.selectedCollections = selected
  }

  handleHeaderColumnSort(sort: ApiSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }


  rowActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Archive collection",
        callback: (action: TableActionDefinition, skill?: ApiCollectionSummary) => this.handleClickArchive(action, skill),
        visible: (skill?: ApiCollectionSummary) => this.archiveVisible(skill)
      }),
      new TableActionDefinition({
        label: "Unarchive collection",
        callback: (action: TableActionDefinition, skill?: ApiCollectionSummary) => this.handleClickUnarchive(action, skill),
        visible: (skill?: ApiCollectionSummary) => this.unarchiveVisible(skill)
      }),
      new TableActionDefinition({
        label: "Publish collection",
        callback: (action: TableActionDefinition, skill?: ApiCollectionSummary) => this.handleClickPublish(action, skill),
        visible: (skill?: ApiCollectionSummary) => this.publishVisible(skill)
      }),
    ]
  }

  tableActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: (action: TableActionDefinition, skill?: ApiCollectionSummary) => this.handleClickBackToTop(action, skill),
        visible: (skill?: ApiCollectionSummary) => true
      }),

      new TableActionDefinition({
        label: "Publish",
        icon: "publish",
        callback: (action: TableActionDefinition, skill?: ApiCollectionSummary) => this.handleClickPublish(action, skill),
        visible: (skill?: ApiCollectionSummary) => this.publishVisible(skill)
      }),

      new TableActionDefinition({
        label: "Archive",
        icon: "archive",
        callback: (action: TableActionDefinition, skill?: ApiCollectionSummary) => this.handleClickArchive(action, skill),
        visible: (skill?: ApiCollectionSummary) => this.archiveVisible(skill)
      }),

      new TableActionDefinition({
        label: "Unarchive",
        icon: "unarchive",
        callback: (action: TableActionDefinition, skill?: ApiCollectionSummary) => this.handleClickUnarchive(action, skill),
        visible: (skill?: ApiCollectionSummary) => this.unarchiveVisible(skill)
      }),

    ]

  }

  private handleClickBackToTop(action: TableActionDefinition, skill?: ApiCollectionSummary): boolean {
    return false
  }

  private handleClickUnarchive(action: TableActionDefinition, skill?: ApiCollectionSummary): boolean {
    this.submitStatusChange(PublishStatus.Unarchived, "unarchived", skill)
    return false
  }

  private handleClickArchive(action: TableActionDefinition, skill?: ApiCollectionSummary): boolean {
    this.submitStatusChange(PublishStatus.Archived, "archived", skill)
    return false
  }

  private handleClickPublish(action: TableActionDefinition, skill?: ApiCollectionSummary): boolean {
    const plural = (this.selectedUuids(skill)?.length ?? 0) > 1 ? "these collections" : "this collection"
    if (confirm(`Are you sure you want to publish ${plural}?\nOnce published, a collection can't be unpublished.`)) {
      this.submitStatusChange(PublishStatus.Published, "published", skill)
    }
    return false
  }

  getSelectedSkills(skill?: ApiCollectionSummary): ApiCollectionSummary[] | undefined {
    return ((skill !== undefined) ? [skill] : this.selectedCollections)
  }
  selectedUuids(skill?: ApiCollectionSummary): string[] | undefined {
    return this.getSelectedSkills(skill)?.map(it => it.uuid)
  }

  getApiSearch(skill?: ApiCollectionSummary): ApiSearch | undefined {
    if (skill !== undefined) {
      return new ApiSearch({uuids: [skill.uuid]})
    }

    const selected = this.selectedUuids(skill)
    if (selected !== undefined) {
      return new ApiSearch({uuids: selected})
    }

    return undefined
  }

  submitStatusChange(newStatus: PublishStatus, verb: string, skill?: ApiCollectionSummary): boolean  {
    const apiSearch = this.getApiSearch(skill)
    if (apiSearch === undefined) {
      return false
    }

    this.toastService.showBlockingLoader()
    this.collectionsSaved = this.collectionService.publishCollectionsWithResult(apiSearch, newStatus, determineFilters(this.selectedFilters))
    this.collectionsSaved?.subscribe((result) => {
      if (result !== undefined) {
        const partial = (result.modifiedCount !== result.totalCount)  ? ` of ${result.totalCount}` : ""
        const message = `You ${verb} ${result.modifiedCount}${partial} collection${(result.totalCount ?? 0) > 1 ? "s" : ""}.`
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

  focusActionBar(): void {
    this.actionBar?.focus()
  }
}
