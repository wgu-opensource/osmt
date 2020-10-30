import {ApiSearch, PaginatedSkills} from "../richskill/service/rich-skill-search.service";
import {IApiSkillSummary} from "../richskill/ApiSkillSummary";
import {PublishStatus} from "../PublishStatus";
import {TableActionDefinition} from "./has-action-definitions";
import {Component} from "@angular/core";
import {Observable} from "rxjs";
import {ApiBatchResult} from "../richskill/ApiBatchResult";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {ToastService} from "../toast/toast.service";
import {ApiSkillSortOrder} from "../richskill/ApiSkill";


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

  constructor(protected richSkillService: RichSkillService,
              protected toastService: ToastService
  ) {
  }

  // "abstract" methods to be implemented by a "subclas"
  matchingQuery?: string
  title?: string
  loadNextPage(): void {}


  // base component methods

  protected setResults(results: PaginatedSkills): void {
    this.results = results
  }

  get skillCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.curPageCount} of ${this.totalCount} skill${this.curPageCount > 1 ? "s" : ""}`
    }
    return `0 skills`
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
  publishVisible(): boolean {
    const unpublishedSkill = this.selectedSkills?.find(s => s.status === PublishStatus.Unpublished)
    return unpublishedSkill !== undefined
  }
  archiveVisible(): boolean {
    const unarchivedSkills = this.selectedSkills?.find(s => s.status === PublishStatus.Published)
    return unarchivedSkills !== undefined
  }
  unarchiveVisible(): boolean {
    const archivedSkill = this.selectedSkills?.find(s => s.status === PublishStatus.Archived)
    return archivedSkill !== undefined
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
    this.loadNextPage()
  }

  tableActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: () => this.handleClickBackToTop(),
      }),

      new TableActionDefinition({
        label: "Publish",
        icon: "publish",
        callback: () => this.handleClickPublish(),
        visible: () => this.publishVisible()
      }),

      new TableActionDefinition({
        label: "Archive",
        icon: "archive",
        callback: () => this.handleClickArchive(),
        visible: () => this.archiveVisible()
      }),

      new TableActionDefinition({
        label: "Unarchive",
        icon: "unarchive",
        callback: () => this.handleClickUnarchive(),
        visible: () => this.unarchiveVisible()
      }),

      new TableActionDefinition({
        label: "Add to Collection",
        icon: "collection",
        primary: true,
        callback: () => this.handleClickAddCollection()
      }),
    ]

  }

  private handleClickBackToTop(): boolean {
    return false
  }

  private handleClickAddCollection(): boolean {
    return false
  }

  private handleClickUnarchive(): boolean {
    this.submitStatusChange(PublishStatus.Published)
    return false
  }

  private handleClickArchive(): boolean {
    this.submitStatusChange(PublishStatus.Archived)
    return false
  }

  handleClickPublish(): boolean  {
    this.submitStatusChange(PublishStatus.Published)
    return false
  }

  submitStatusChange(newStatus: PublishStatus): boolean  {
    const selectedUuids = this.selectedSkills?.map(s => s.uuid)
    if (selectedUuids === undefined) {
      return false
    }

    this.toastService.showBlockingLoader()

    const apiSearch = ApiSearch.factory({uuids: selectedUuids})
    this.skillsSaved = this.richSkillService.publishSkillsWithResult(apiSearch, newStatus, this.selectedFilters)
    this.skillsSaved.subscribe((result) => {
      if (result !== undefined) {
        this.toastService.hideBlockingLoader()
        this.loadNextPage()
      }
    })
    return false
  }

}
