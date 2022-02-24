import {Component, OnInit, ViewChild} from "@angular/core"
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
import {Observable, Subject} from "rxjs"
import {TableActionBarComponent} from "../../table/skills-library-table/table-action-bar.component"
import {Title} from "@angular/platform-browser";
import { AppConfig } from 'src/app/app.config'

@Component({
  selector: "app-manage-collection",
  templateUrl: "./manage-collection.component.html"
})
export class ManageCollectionComponent extends SkillsListComponent implements OnInit {

  private static MESSAGES = {
    SHARE: {
      SUCCESS: "This collection will be available in the Search Hub soon",
      ERROR: {
        DEFAULT: "Unable to share to Search Hub"
      }
    },
    UNSHARE: {
      CONFIRM: "Are you sure you want to remove this collection from the Search Hub? " +
               "This collection will no longer be available in search results.",
      ERROR: {
        DEFAULT: "Unable to unshare from Search Hub"
      }
    },
    REMOVE: {
      CONFIRM: "Are you sure you want to remove this Collection from your library?",
      ERROR: {
        DEFAULT: "Unable to remove from library"
      }
    }
  }

  @ViewChild(TableActionBarComponent) tableActionBar!: TableActionBarComponent

  collection?: ApiCollection
  apiSearch?: ApiSearch

  editIcon = SvgHelper.path(SvgIcon.EDIT)
  publishIcon = SvgHelper.path(SvgIcon.PUBLISH)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)
  unarchiveIcon = SvgHelper.path(SvgIcon.UNARCHIVE)
  addIcon = SvgHelper.path(SvgIcon.ADD)
  searchIcon = SvgHelper.path(SvgIcon.SEARCH)
  shareIcon = SvgHelper.path(SvgIcon.SHARE)
  unshareIcon = SvgHelper.path(SvgIcon.UNSHARE)
  externalIcon = SvgHelper.path(SvgIcon.EXTERNAL)
  removeIcon = SvgHelper.path(SvgIcon.REMOVE)

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published, PublishStatus.Archived])

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  uuidParam?: string

  showAddToCollection = false
  showingMultipleConfirm = false
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
              protected titleService: Title
  ) {
    super(router, richSkillService, toastService)
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

  actionDefinitions(): TableActionDefinition[] {
    const actions = [
    ]

    if (this.collection?.importedFrom) {
      // imported collections are read-only

      actions.push(new TableActionDefinition({
        label: "View External Collection",
        icon: this.externalIcon,
        callback: () => this.viewExternalAction(),
      }))

      actions.push(new TableActionDefinition({
        label: "Remove From Library",
        icon: this.removeIcon,
        callback: () => this.removeCollectionAction(),
      }))

    } else {

      actions.push(new TableActionDefinition({
        label: "Add RSDs to This Collection",
        icon: this.addIcon,
        primary: !this.collectionHasSkills, // Primary only if there are no skills
        callback: () => this.addSkillsAction(),
      }))

      actions.push(new TableActionDefinition({
        label: "Edit Collection Name",
        icon: this.editIcon,
        callback: () => this.editAction()
      }))

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
        }))
      }

      actions.push(
        new TableActionDefinition({
          label: "Archive Collection ",
          icon: this.archiveIcon,
          callback: () => this.archiveAction(),
          visible: () => this.collection?.status !== PublishStatus.Archived && this.collection?.status !== PublishStatus.Deleted
        }),
        new TableActionDefinition({
          label: "Unarchive Collection ",
          icon: this.unarchiveIcon,
          callback: () => this.unarchiveAction(),
          visible: () => this.collection?.status === PublishStatus.Archived || this.collection?.status === PublishStatus.Deleted
        })
      )

      // Add External Share & Unshare actions.
      if (AppConfig.settings.externalShareEnabled) {
        actions.push(
          new TableActionDefinition({
            label: "Share to Search Hub",
            icon: this.shareIcon,
            callback: () => this.shareExternallyAction(),
            visible: () => {
              return (this.collection?.status === PublishStatus.Archived || this.collection?.status !== PublishStatus.Deleted)
                      && this.collection?.isExternallyShared !== true
            }
          }),
          new TableActionDefinition({
            label: "Unshare from Search Hub",
            icon: this.unshareIcon,
            callback: () => this.unshareExternallyAction(),
            visible: () => {
              return (this.collection?.status === PublishStatus.Archived || this.collection?.status !== PublishStatus.Deleted)
                      && this.collection?.isExternallyShared === true
            }
          })
        )
      }
    }

    return actions
  }

  editAction(): void {
    this.router.navigate([`/collections/${this.uuidParam}/edit`])
  }

  viewPublishedAction(): void {
    const url = `/collections/${this.uuidParam}`
    window.open(url, "_blank")
  }

  viewExternalAction(): void {
    if (this.collection?.importedFrom) {
      window.open(this.collection?.importedFrom, "_blank")
    }
  }

  removeCollectionAction(): void {
    if (confirm(ManageCollectionComponent.MESSAGES.REMOVE.CONFIRM)) {
      this.toastService.showBlockingLoader()
      this.collectionService.removeCollection(this.uuidParam!).subscribe(
        result => {
          this.toastService.hideBlockingLoader()
          this.router.navigate([""])
        }, error => {
          this.toastService.hideBlockingLoader()
          alert(error?.error?.message ?? ManageCollectionComponent.MESSAGES.REMOVE.ERROR.DEFAULT)
        }
      )

    }
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

  shareExternallyAction(): void {
    if (this.collection?.uuid && this.collection?.isExternallyShared !== true) {
      this.toastService.showBlockingLoader()
      this.collectionService.shareCollectionExternally(this.collection.uuid).subscribe(
        data => {
          this.collapseAuditLog.next()
          this.reloadCollection()
          this.toastService.hideBlockingLoader()
          alert(ManageCollectionComponent.MESSAGES.SHARE.SUCCESS)
        },
        error => {
          this.toastService.hideBlockingLoader()
          alert(error?.error?.message ?? ManageCollectionComponent.MESSAGES.SHARE.ERROR.DEFAULT)
        }
      )
    } else {
      this.router.navigate([`/collections/${this.uuidParam}/manage`])
    }
  }

  unshareExternallyAction(): void {
    if (this.collection?.uuid && this.collection?.isExternallyShared !== false) {
      if (confirm(ManageCollectionComponent.MESSAGES.UNSHARE.CONFIRM)) {
        this.toastService.showBlockingLoader()
        this.collectionService.unshareCollectionExternally(this.collection.uuid).subscribe(
          data => {
            this.collapseAuditLog.next()
            this.reloadCollection()
            this.toastService.hideBlockingLoader()
          },
          error => {
            this.toastService.hideBlockingLoader()
            alert(error?.error?.message ?? ManageCollectionComponent.MESSAGES.UNSHARE.ERROR.DEFAULT)
          }
        )
      }
    } else {
      this.router.navigate([`/collections/${this.uuidParam}/manage`])
    }
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
      this.showingMultipleConfirm = true
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
    this.showingMultipleConfirm = false
    this.apiSearch = undefined
    return false
  }

  handleClickCancel(): boolean {
    this.showingMultipleConfirm = false
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

  getSelectAllEnabled(): boolean {
    return this.actionsVisible()
  }

  rowActions(): TableActionDefinition[] {
    if (!this.actionsVisible()) return []
    return super.rowActions();
  }

  actionsVisible(): boolean {
    return !this.collection?.importedFrom
  }
}
