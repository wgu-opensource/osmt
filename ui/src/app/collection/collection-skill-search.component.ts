import {Component, OnInit} from "@angular/core"
import {Observable} from "rxjs"
import {ApiSearch, ApiSkillListUpdate, PaginatedSkills} from "../richskill/service/rich-skill-search.service"
import {FormControl, FormGroup} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {Title} from "@angular/platform-browser"
import {Location} from "@angular/common"
import {CollectionService} from "./service/collection.service"
import {ToastService} from "../toast/toast.service"
import {ApiCollection} from "./ApiCollection"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions"
import {ApiSkillSummary} from "../richskill/ApiSkillSummary"
import {SkillsListComponent} from "../richskill/list/skills-list.component"
import {ApiTaskResult} from "../task/ApiTaskResult"
import {AuthService} from "../auth/auth-service"
import {PublishStatus} from "../PublishStatus"

@Component({
  selector: "app-collection-skill-search",
  templateUrl: "./collection-skill-search.component.html"
})
export class CollectionSkillSearchComponent extends SkillsListComponent implements OnInit {
  uuidParam?: string
  collection?: ApiCollection

  collectionLoaded?: Observable<ApiCollection>
  collectionUpdated?: Observable<ApiTaskResult>

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  multiplePagesSelected: boolean = false

  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }

  constructor(protected router: Router,
              protected titleService: Title,
              protected route: ActivatedRoute,
              protected location: Location,
              protected collectionService: CollectionService,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected authService: AuthService,
  ) {
    super(router, richSkillService, collectionService, toastService, authService)
    this.titleService.setTitle(`Add RSDs to Collection | ${this.whitelabel.toolName}`)

    this.uuidParam = this.route.snapshot.paramMap.get("uuid") || undefined
    if (this.uuidParam != null) {
      this.collectionLoaded = this.collectionService.getCollectionByUUID(this.uuidParam)
      this.collectionLoaded.subscribe(it => this.collection = it)
    }

  }

  ngOnInit(): void {
  }

  handleDefaultSubmit(): boolean {
    this.loadNextPage()
    this.from = 0

    return false
  }

  loadNextPage(): void {
    const query = this.searchQuery.trim()

    if (this.selectedFilters.size < 1 || query.length < 1) {
      this.setResults(new PaginatedSkills([], 0))
      return
    }

    const apiSearch = new ApiSearch({query})
    this.resultsLoaded = this.richSkillService.searchSkills(apiSearch, this.size, this.from, this.selectedFilters, this.columnSort)
    this.resultsLoaded.subscribe(it => this.setResults(it))
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    return false
  }

  rowActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: `Add to ${this.collectionOrWorkspace(true)}`,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickAddCollection(action, skill),
      })
    ]
  }

  actionsVisible(): boolean {
    return this.results !== undefined
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
        label: `Add to ${this.collectionOrWorkspace(true)}`,
        icon: "collection",
        primary: true,
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleClickAddCollection(action, skill),
        visible: (skill?: ApiSkillSummary) => this.addToCollectionVisible(skill) || this.addToWorkspaceVisible()
      })
    ]
  }

  protected handleClickAddCollection(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    const apiSearch = this.getApiSearch(skill)
    const selectedCount = this.multiplePagesSelected ? this.totalCount : this.selectedSkills?.length ?? 1

    this.toastService.showBlockingLoader()
    this.collectionUpdated = this.collectionService.updateSkills(this.uuidParam!, new ApiSkillListUpdate({add: apiSearch}))
    this.collectionUpdated.subscribe(result => {
      if (result) {
        this.toastService.hideBlockingLoader()
        const isWorkspace = this.collection?.status === PublishStatus.Workspace
        const baseMessage = `You added ${selectedCount} RSD${selectedCount ? "s" : ""} to the`
        const message = ` ${baseMessage} ${this.collectionOrWorkspace(false)} ${ isWorkspace ? "" : this.collection?.name}.`
        this.toastService.showToast("Success!", message)
      }
    })

    return false
  }

  getApiSearch(skill?: ApiSkillSummary): ApiSearch | undefined {
    return (this.multiplePagesSelected) ? new ApiSearch({query: this.searchQuery}) : super.getApiSearch(skill)
  }

  handleSelectAll(selectAllChecked: boolean): void {
    this.multiplePagesSelected = this.totalPageCount > 1
  }
}
