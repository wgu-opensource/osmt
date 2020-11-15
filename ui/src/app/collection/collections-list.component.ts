import {Component} from "@angular/core";
import {Observable} from "rxjs";
import {ApiSearch, PaginatedCollections, PaginatedSkills} from "../richskill/service/rich-skill-search.service";
import {PublishStatus} from "../PublishStatus";
import {ApiCollectionSummary, IApiSkillSummary, ICollectionSummary} from "../richskill/ApiSkillSummary";
import {ApiBatchResult} from "../richskill/ApiBatchResult";
import {ApiSortOrder} from "../richskill/ApiSkill";
import {Router} from "@angular/router";
import {ToastService} from "../toast/toast.service";
import {CollectionService} from "./service/collection.service";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions";


@Component({
  selector: "app-collections-list",
  templateUrl: "./collections-list.component.html"
})
export class CollectionsListComponent {

  from = 0
  size = 50

  resultsLoaded: Observable<PaginatedCollections> | undefined
  results: PaginatedCollections | undefined

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unpublished, PublishStatus.Published])
  selectedCollections?: ICollectionSummary[]
  skillsSaved?: Observable<ApiBatchResult>

  columnSort: ApiSortOrder = ApiSortOrder.SkillAsc

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
      return `${this.totalCount} Collection${this.curPageCount > 1 ? "s" : ""}`
    }
    return `0 Collections`
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
      return skill.status === PublishStatus.Unpublished
    } else if ((this.selectedCollections?.length ?? 0) === 0) {
      return false
    } else {
      const unpublishedSkill = this.selectedCollections?.find(s => s.status === PublishStatus.Unpublished)
      return unpublishedSkill !== undefined
    }
  }
  archiveVisible(skill?: ApiCollectionSummary): boolean {
    if (skill !== undefined) {
      return skill.status === PublishStatus.Published
    } else if ((this.selectedCollections?.length ?? 0) === 0) {
      return false
    } else {
      const unarchivedSkills = this.selectedCollections?.find(s => s.status === PublishStatus.Published)
      return unarchivedSkills !== undefined
    }
  }
  unarchiveVisible(skill?: ApiCollectionSummary): boolean {
    if (skill !== undefined) {
      return skill.status === PublishStatus.Archived
    } else if ((this.selectedCollections?.length ?? 0) === 0) {
      return false
    } else {
      const archivedSkill = this.selectedCollections?.find(s => s.status === PublishStatus.Archived)
      return archivedSkill !== undefined
    }
  }

  addToCollectionVisible(skill?: ApiCollectionSummary): boolean {
    return ((this.selectedCollections?.length ?? 0) > 0)
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
    this.submitStatusChange(PublishStatus.Published, "Un-archived", skill)
    return false
  }

  private handleClickArchive(action: TableActionDefinition, skill?: ApiCollectionSummary): boolean {
    this.submitStatusChange(PublishStatus.Archived, "Archived", skill)
    return false
  }

  private handleClickPublish(action: TableActionDefinition, skill?: ApiCollectionSummary): boolean {
    const plural = (this.selectedUuids(skill)?.length ?? 0) > 1 ? "these collections" : "this collection"
    if (confirm(`Are you sure you want to publish ${plural}?`)) {
      this.submitStatusChange(PublishStatus.Published, "Published", skill)
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
      return ApiSearch.factory({uuids: [skill.uuid]})
    }

    const selected = this.selectedUuids(skill)
    if (selected !== undefined) {
      return ApiSearch.factory({uuids: selected})
    }

    return undefined
  }

  submitStatusChange(newStatus: PublishStatus, verb: string, skill?: ApiCollectionSummary): boolean  {
    const apiSearch = this.getApiSearch(skill)
    if (apiSearch === undefined) {
      return false
    }

    console.log("submit status change", newStatus, apiSearch);
    return false

    // TODO
    this.toastService.showBlockingLoader()
    // this.skillsSaved = this.collectionService.publishCollectionsWithResult(apiSearch, newStatus, this.selectedFilters)
    this.skillsSaved?.subscribe((result) => {
      if (result !== undefined) {
        const partial = (result.modifiedCount !== result.totalCount)  ? ` of ${result.totalCount}` : ""
        const message = `${verb} ${result.modifiedCount}${partial} Collection${(result.totalCount ?? 0) > 1 ? "s" : ""}.`
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
