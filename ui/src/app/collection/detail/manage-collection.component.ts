import { Component, Inject, LOCALE_ID, OnInit, ViewChild } from "@angular/core"
import { FormControl, FormGroup } from "@angular/forms"
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router"

import { Observable, Subject } from "rxjs"

import { ApiCollection, ApiCollectionUpdate } from "../ApiCollection"
import { CollectionService } from "../service/collection.service"
import { ButtonAction, ENABLE_ROLES } from "../../auth/auth-roles";
import { AuthService } from "../../auth/auth-service";
import { SvgHelper, SvgIcon } from "../../core/SvgHelper"
import { ExportCollectionComponent } from "../../export/export-collection.component"
import { CollectionPipe } from "../../pipes"
import { determineFilters, PublishStatus } from "../../PublishStatus"
import { ApiSkillSummary } from "../../richskill/ApiSkillSummary"
import { SkillsListComponent } from "../../richskill/list/skills-list.component"
import { RichSkillService } from "../../richskill/service/rich-skill.service"
import { ApiSearch, ApiSkillListUpdate } from "../../richskill/service/rich-skill-search.service"
import { TableActionDefinition } from "../../table/skills-library-table/has-action-definitions"
import { TableActionBarComponent } from "../../table/skills-library-table/table-action-bar.component"
import { ToastService } from "../../toast/toast.service"
import { ApiNamedReference, KeywordType } from "../../richskill/ApiSkill";
import { FilterDropdown } from "../../models/filter-dropdown.model";
import { KeywordCountPillControl } from "../../core/pill/pill-control";


@Component({
  selector: "app-manage-collection",
  templateUrl: "./manage-collection.component.html"
})
export class ManageCollectionComponent extends SkillsListComponent implements OnInit {

  @ViewChild(TableActionBarComponent) tableActionBar!: TableActionBarComponent

  collection?: ApiCollection
  skillCategories: KeywordCountPillControl[] = []
  apiSearch?: ApiSearch

  editIcon = SvgHelper.path(SvgIcon.EDIT)
  publishIcon = SvgHelper.path(SvgIcon.PUBLISH)
  downloadIcon = SvgHelper.path(SvgIcon.DOWNLOAD)
  deleteIcon = SvgHelper.path(SvgIcon.DELETE)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)
  unarchiveIcon = SvgHelper.path(SvgIcon.UNARCHIVE)
  addIcon = SvgHelper.path(SvgIcon.ADD)
  searchIcon = SvgHelper.path(SvgIcon.SEARCH)
  showAdvancedFilteredSearch = true

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published, PublishStatus.Archived])
  exporter = new ExportCollectionComponent(
    this.collectionService,
    this.toastService,
    this.locale
  )

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  uuidParam?: string

  showAddToCollection = false
  template: "default" | "confirm-multiple" | "confirm-delete-collection" = "default"
  collectionSaved?: Observable<ApiCollection>
  selectAllChecked = false
  showLog = true

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
      this.updateSkillCategories()
      this.loadNextPage()
    })
  }

  updateSkillCategories() {
    const categories = this.collection?.skillKeywords?.get(KeywordType.Category)?.map(c => new KeywordCountPillControl(c))
    this.skillCategories = (categories) ? categories : []
  }

  loadNextPage(): void {
    if (this.collection === undefined) {
      return
    }
    this.apiSearch = new ApiSearch({filtered: this.selectedKeywords, query: this.searchQuery})
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
    return this.searchForm.get("search")?.value?.trim() ?? ""
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    this.apiSearch = undefined
    this.from = 0
    this.loadNextPage()
    return false
  }

  clearSelectedCategories() {
    this.skillCategories.forEach(c => c.deselect())
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
      new TableActionDefinition({
        label: "Add RSDs to This Collection",
        icon: this.addIcon,
        primary: !this.collectionHasSkills, // Primary only if there are no skills
        callback: () => this.addSkillsAction(),
        visible: () => this.authService.isEnabledByRoles(ButtonAction.CollectionSkillsUpdate)
      }),
      new TableActionDefinition({
        label: "Edit Collection",
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
        label: "Download",
        icon: this.downloadIcon,
        menu: [
          {
            label: "Download as CSV",
            icon: this.downloadIcon,
            callback: () => this.exporter.getCollectionCsv(
              this.uuidParam ?? "",
              this.collection?.name ?? ""
            ),
            visible: () => true
          },
          {
            label: "Download as Excel Workbook",
            icon: this.downloadIcon,
            callback: () => this.exporter.getCollectionXlsx(
              this.uuidParam ?? "",
              this.collection?.name ?? ""
            ),
            visible: () => true
          }
        ],
        visible: () => (this.collection?.skills.length ?? 0) > 0
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
      return this.searchQuery
        ? new ApiSearch({query: this.searchQuery, filtered: {}})
        : new ApiSearch({uuids: this.collection?.skills.map((i: any) => i.uuid)})
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
      if (confirm(`Confirm that you want to remove the following RSD from this ${this.collectionOrWorkspace(false)}.\n${first?.skillName}`)) {
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
        this.toastService.showToast("Success!", `You removed ${result.modifiedCount} RSD${(result.modifiedCount ?? 0) > 1 ? "s" : ""} from this ${this.collectionOrWorkspace(false).toLowerCase()}`)
        this.toastService.hideBlockingLoader()
        this.reloadCollection()
        this.loadNextPage()
      }
    })

  }

  handleCategoryClicked(control: KeywordCountPillControl) {
    const previouslySelected = control.isSelected
    const categoryFilters: any[] = []

    this.clearSelectedCategories()

    if (!previouslySelected) {
      control.select()
      categoryFilters.push(ApiNamedReference.fromString(control.keyword as string))
    }

    const filters: FilterDropdown = {
      alignments: (this.keywords.alignments) ? this.keywords.alignments.map(v => v) : [],
      authors: (this.keywords.alignments) ? this.keywords.alignments.map(v => v) : [],
      categories: categoryFilters,
      certifications: (this.keywords.certifications) ? this.keywords.certifications.map(v => v) : [],
      employers: (this.keywords.employers) ? this.keywords.employers.map(v => v) : [],
      keywords: (this.keywords.keywords) ? this.keywords.keywords.map(v => v) : [],
      occupations: (this.keywords.occupations) ? this.keywords.occupations.map(v => v) : [],
      standards: (this.keywords.standards) ? this.keywords.standards.map(v => v) : []
    }

    this.keywordsChange(filters)
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

  get confirmMessageText(): string {
    return "delete " + (this.collection?.name ?? "")
  }

  get confirmButtonText(): string {
    return "delete collection"
  }

  collectionOrWorkspace(includesMy: boolean): string {
    return new CollectionPipe().transform(this.collection?.status, includesMy)
  }

  protected handleClickBackToTop(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement, "h2")
    return false
  }

  focusActionBar(): void {
    this.tableActionBar.focus()
  }
}
