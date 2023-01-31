import {Component, Inject, LOCALE_ID, OnInit, ViewChild} from "@angular/core"
import {ApiCollection, ApiCollectionUpdate} from "../ApiCollection"
import {ApiSearch, ApiSkillListUpdate} from "../../richskill/service/rich-skill-search.service"
import {ActivatedRoute, Router} from "@angular/router"
import {CollectionService} from "../service/collection.service"
import {ToastService} from "../../toast/toast.service"
import {SkillsListComponent} from "../../richskill/list/skills-list.component"
import {RichSkillService} from "../../richskill/service/rich-skill.service"
import {FormControl, FormGroup} from "@angular/forms"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions"
import {determineFilters, PublishStatus} from "../../PublishStatus"
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {Observable, of, Subject, throwError} from "rxjs"
import {TableActionBarComponent} from "../../table/skills-library-table/table-action-bar.component"
import {Title} from "@angular/platform-browser";
import {AuthService} from "../../auth/auth-service";
import {ButtonAction, ENABLE_ROLES} from "../../auth/auth-roles";
import {formatDate} from "@angular/common"
import * as FileSaver from "file-saver"
import {ITaskResult} from "../../task/ApiTaskResult"
import {delay, retryWhen, switchMap} from "rxjs/operators"

@Component({
  selector: "app-manage-collection",
  templateUrl: "./manage-collection.component.html"
})
export class ManageCollectionComponent extends SkillsListComponent implements OnInit {

  @ViewChild(TableActionBarComponent) tableActionBar!: TableActionBarComponent

  collection?: ApiCollection
  apiSearch?: ApiSearch

  editIcon = SvgHelper.path(SvgIcon.EDIT)
  publishIcon = SvgHelper.path(SvgIcon.PUBLISH)
  downloadIcon = SvgHelper.path(SvgIcon.DOWNLOAD)
  deleteIcon = SvgHelper.path(SvgIcon.DELETE)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)
  unarchiveIcon = SvgHelper.path(SvgIcon.UNARCHIVE)
  addIcon = SvgHelper.path(SvgIcon.ADD)
  searchIcon = SvgHelper.path(SvgIcon.SEARCH)

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published, PublishStatus.Archived])

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  uuidParam?: string

  showAddToCollection = false
  template: "default" | "confirm-multiple" | "confirm-delete-collection" = "default"
  collectionSaved?: Observable<ApiCollection>
  selectAllChecked = false

  collapseAuditLog = new Subject<void>()

  get isPlural(): boolean {
    return (this.results?.skills.length ?? 0) > 1
  }

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected collectionService: CollectionService,
              protected route: ActivatedRoute,
              protected titleService: Title,
              protected authService: AuthService,
              @Inject(LOCALE_ID) protected locale: string
  ) {
    super(router, richSkillService, collectionService, toastService, authService)
  }

  ngOnInit(): void {
    this.uuidParam = this.route.snapshot.paramMap.get("uuid") ?? ""
    this.reloadCollection()
  }

  get collectionHasSkills(): boolean {
    const count = this.collection?.skills?.length
    if (count !== undefined) {
      return count > 0
    } else {
      return false
    }
  }

  reloadCollection(): void {
    this.collectionService.getCollectionByUUID(this.uuidParam ?? "").subscribe(collection => {
      this.titleService.setTitle(`${collection.name} | Collection | ${this.whitelabel.toolName}`)

      this.collection = collection
      this.loadNextPage()
    })
  }

  loadNextPage(): void {
    if (this.collection === undefined) {
      return
    }

    this.resultsLoaded = this.collectionService.getCollectionSkills(
      this.collection.uuid,
      this.size,
      this.from,
      determineFilters(this.selectedFilters),
      this.columnSort,
      this.apiSearch
    )
    this.resultsLoaded.subscribe(results => this.setResults(results))
  }

  getSelectAllCount(): number {
    return this.totalCount
  }

  handleSelectAll(selectAllChecked: boolean): boolean {
    this.selectAllChecked = selectAllChecked
    return false
  }

  get selectedCount(): number {
    return this.selectAllChecked ? this.totalCount : (this.selectedSkills?.length ?? 0)
  }

  public get searchQuery(): string {
    return this.searchForm.get("search")?.value.trim() ?? ""
  }
  clearSearch(): boolean {
    this.searchForm.reset()
    this.apiSearch = undefined
    this.from = 0
    this.loadNextPage()
    return false
  }
  handleDefaultSubmit(): boolean {
    if (this.searchQuery.length > 0) {
      this.apiSearch = new ApiSearch({query: this.searchQuery})
      this.matchingQuery = [this.searchQuery]
      this.from = 0
      this.loadNextPage()
    }
    return false
  }

  generateCsv(collectionName: string): void {
    this.collectionService.requestCollectionSkillsCsv(this.uuidParam ?? "")
      .subscribe((taskStarted: ITaskResult) => {
        this.toastService.loaderSubject.next(true)
        this.getCsv(taskStarted.uuid ?? "", collectionName)
      })
  }

  getCsv(uuid: string, collectionName: string): void {
    this.collectionService.getCsvTaskResultsIfComplete(uuid)
      .pipe(
        retryWhen(errors => errors.pipe(
          switchMap((error) => {
            if (error.status === 404) {
              return of(error.status)
            }
            return throwError(error)
          }),
          delay(1000),
        )))
      .subscribe(response => {
        this.saveCsv(response.body, collectionName)
      })
  }

  saveCsv(body: string, collectionName: string): void {
    const blob = new Blob([body], {type: "text/csv;charset=utf-8;"})
    const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
    FileSaver.saveAs(blob, `RSD Skills - ${collectionName} - ${date}.csv`)
    this.toastService.loaderSubject.next(false)
  }

  actionDefinitions(): TableActionDefinition[] {
    const actions = [
      new TableActionDefinition({
        label: "Add RSDs to This Collection",
        icon: this.addIcon,
        primary: !this.collectionHasSkills, // Primary only if there are no skills
        callback: () => this.addSkillsAction(),
        visible: () => this.authService.isEnabledByRoles(ButtonAction.CollectionSkillsUpdate)
      }),
      new TableActionDefinition({
        label: "Edit Collection Name",
        icon: this.editIcon,
        callback: () => this.editAction(),
        visible: () => this.authService.isEnabledByRoles(ButtonAction.CollectionUpdate)
      })
    ]

    if (this.collection?.publishDate) {
      actions.push(new TableActionDefinition({
        label: "View Published Collection",
        icon: this.publishIcon,
        callback: () => this.viewPublishedAction(),
      }))
    } else {
      actions.push(new TableActionDefinition({
        label: "Publish Collection",
        icon: this.publishIcon,
        callback: () => this.publishAction(),
        visible: () => this.authService.isEnabledByRoles(ButtonAction.CollectionPublish)
      }))
    }

    if(this.collection?.status !== PublishStatus.Archived && this.collection?.status !== PublishStatus.Deleted){
      actions.push(
        new TableActionDefinition({
          label: "Archive Collection ",
          icon: this.archiveIcon,
          callback: () => this.archiveAction(),
          visible: () =>  this.authService.isEnabledByRoles(ButtonAction.CollectionUpdate)
        })
      )
    } else if (this.collection?.status === PublishStatus.Archived || this.collection?.status === PublishStatus.Deleted) {
      actions.push(
        new TableActionDefinition({
          label: "Unarchive Collection ",
          icon: this.unarchiveIcon,
          callback: () => this.unarchiveAction(),
          visible: () => this.authService.isEnabledByRoles(ButtonAction.CollectionUpdate)
        })
      )
    }

    const isPublished = this.collection?.status === PublishStatus.Published
    const isDraftAndUserIsAdmin = this.collection?.status === PublishStatus.Draft
      && this.authService.isEnabledByRoles(ButtonAction.ExportDraftCollection)
    if (isPublished || isDraftAndUserIsAdmin) {
      actions.push(new TableActionDefinition({
        label: "Download CSV",
        icon: this.downloadIcon,
        callback: () => this.generateCsv(this.collection?.name ?? ""),
        visible: () => true
      }))
    }

    if (ENABLE_ROLES && this.authService.isEnabledByRoles(ButtonAction.DeleteCollection)) {
      actions.push(
        new TableActionDefinition({
          label: "Delete Collection",
          icon: this.deleteIcon,
          callback: () => this.deleteCollectionAction(),
          visible: () => true
        })
      )
    }
    return actions
  }

  deleteCollectionAction(): void {
    this.template = "confirm-delete-collection"
  }

  handleConfirmDeleteCollection(): void {
    this.toastService.loaderSubject.next(true)
    this.collectionService.deleteCollectionWithResult(this.uuidParam ?? "").subscribe((result) => {
      if (result) {
        this.toastService.loaderSubject.next(false)
        this.router.navigate(["/collections"])
      }
    })
  }

  editAction(): void {
    this.router.navigate([`/collections/${this.uuidParam}/edit`])
  }

  viewPublishedAction(): void {
    const url = `/collections/${this.uuidParam}`
    window.open(url, "_blank")
  }

  publishAction(): void {
    if (this.uuidParam === undefined) { return }

    this.toastService.showBlockingLoader()
    this.collectionService.collectionReadyToPublish(this.uuidParam).subscribe(ready => {
      this.toastService.hideBlockingLoader()
      if (ready) {
          if (confirm("Confirm that you want to publish the selected collection. Once published, a collection can't be unpublished.")) {
            this.submitCollectionStatusChange(PublishStatus.Published, "published")
          }
      } else {
        this.router.navigate([`/collections/${this.uuidParam}/publish`])
      }
    })
  }

  archiveAction(): void {
    this.submitCollectionStatusChange(PublishStatus.Archived, "archived")
    this.collapseAuditLog.next()
  }
  unarchiveAction(): void {
    this.submitCollectionStatusChange(PublishStatus.Unarchived, "unarchived")
    this.collapseAuditLog.next()
  }

  submitCollectionStatusChange(newStatus: PublishStatus, verb: string): void {
    if (this.uuidParam === undefined) { return }

    const updateObject = new ApiCollectionUpdate({status: newStatus})

    this.toastService.showBlockingLoader()
    this.collectionSaved = this.collectionService.updateCollection(this.uuidParam, updateObject)
    this.collectionSaved.subscribe((collection) => {
      this.toastService.hideBlockingLoader()
      this.toastService.showToast("Success!", `You ${verb} the collection ${collection.name}.`)
      this.collection = collection
      this.loadNextPage()
    })
  }

  addSkillsAction(): void {
    this.router.navigate([`/collections/${this.collection?.uuid}/add-skills`])
  }


  getApiSearch(skill?: ApiSkillSummary): ApiSearch | undefined {
    if (this.selectAllChecked) {
      return new ApiSearch({
        query: this.searchQuery
      })
    } else {
      return super.getApiSearch(skill)
    }
  }

  removeFromCollection(skill?: ApiSkillSummary): void {
    if (this.uuidParam === undefined) { return }

    this.apiSearch = this.getApiSearch(skill)

    const first = this.getSelectedSkills(skill)?.find(it => true)

    const count = (this.apiSearch?.uuids?.length ?? 0)

    if (count > 1 || this.selectAllChecked) {
      this.template = "confirm-multiple"
    } else {
      if (confirm(`Confirm that you want to remove the following RSD from this collection.\n${first?.skillName}`)) {
        this.submitSkillRemoval(this.apiSearch)
      }
    }
  }

  submitSkillRemoval(apiSearch?: ApiSearch): void {
    const update = new ApiSkillListUpdate({remove: apiSearch})
    this.toastService.showBlockingLoader()
    this.skillsSaved = this.collectionService.updateSkillsWithResult(this.uuidParam ?? "", update)
    this.skillsSaved.subscribe(result => {
      if (result) {
        this.toastService.showToast("Success!", `You removed ${result.modifiedCount} RSD${(result.modifiedCount ?? 0) > 1 ? "s" : ""} from this collection.`)
        this.toastService.hideBlockingLoader()
        this.reloadCollection()
        this.loadNextPage()
      }
    })

  }

  handleClickConfirmMulti(): boolean {
    this.submitSkillRemoval(this.apiSearch)
    this.template = "default"
    this.apiSearch = undefined
    return false
  }

  handleClickCancel(): boolean {
    this.template = "default"
    this.apiSearch = undefined
    return false
  }

  protected handleClickBackToTop(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement, "h2")
    return false
  }

  focusActionBar(): void {
    this.tableActionBar.focus()
  }
}
