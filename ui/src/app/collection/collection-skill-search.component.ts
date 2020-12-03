import {Component, OnInit} from "@angular/core";
import {Observable} from "rxjs";
import {ApiSearch, PaginatedSkills} from "../richskill/service/rich-skill-search.service";
import {FormControl, FormGroup} from "@angular/forms";
import {ApiSortOrder} from "../richskill/ApiSkill";
import {PublishStatus} from "../PublishStatus";
import {ActivatedRoute, Router} from "@angular/router";
import {Title} from "@angular/platform-browser";
import {Location} from "@angular/common";
import {CollectionService} from "./service/collection.service";
import {ToastService} from "../toast/toast.service";
import {ApiCollection} from "./ApiCollection";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions";
import {ApiSkillSummary} from "../richskill/ApiSkillSummary";

@Component({
  selector: "app-collection-skill-search",
  templateUrl: "./collection-skill-search.component.html"
})
export class CollectionSkillSearchComponent implements OnInit {
  uuidParam?: string
  collection?: ApiCollection

  from = 0
  size = 50

  collectionLoaded?: Observable<ApiCollection>
  resultsLoaded: Observable<PaginatedSkills> | undefined
  results: PaginatedSkills | undefined

  searchForm = new FormGroup({
    search: new FormControl("")
  })

  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }

  columnSort: ApiSortOrder = ApiSortOrder.SkillAsc
  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published])

  constructor(protected router: Router,
              protected titleService: Title,
              protected route: ActivatedRoute,
              protected location: Location,
              protected collectionService: CollectionService,
              protected skillService: RichSkillService,
              protected toastService: ToastService
  ) {
    this.titleService.setTitle("Add RSDs to Collection")
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
    this.resultsLoaded = this.skillService.searchSkills(apiSearch, this.size, this.from, this.selectedFilters, this.columnSort)
    this.resultsLoaded.subscribe(it => this.setResults(it))
  }

  protected setResults(results: PaginatedSkills): void {
    this.results = results
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    return false
  }

  handleFiltersChanged(newFilters: Set<PublishStatus>): void {
    this.selectedFilters = newFilters
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  handleHeaderColumnSort(sort: ApiSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
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

  get isPlural(): boolean {
    return this.totalCount > 1
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

  rowActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Add to Collection",
        callback: (action: TableActionDefinition, skill?: ApiSkillSummary) => this.handleSelectSkill(action, skill),
      })
    ]
  }

  private handleSelectSkill(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    console.log("selected skill", skill)

    return false
  }
}
