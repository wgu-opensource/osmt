import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {ApiCollectionSummary, ApiSkillSummary} from "../richskill/ApiSkillSummary";
import {Title} from "@angular/platform-browser";
import {PublishStatus} from "../PublishStatus";
import {FormControl, FormGroup} from "@angular/forms";
import {Observable} from "rxjs";
import {ApiSearch, PaginatedCollections, PaginatedSkills} from "../richskill/service/rich-skill-search.service";
import {ApiSortOrder} from "../richskill/ApiSkill";
import {CollectionService} from "./service/collection.service";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions"
import {ToastService} from "../toast/toast.service";

@Component({
  selector: "app-add-skills-collection",
  templateUrl: "./add-skills-collection.component.html"
})
export class AddSkillsCollectionComponent implements OnInit {
  uuidParam?: string

  from = 0
  size = 50

  resultsLoaded: Observable<PaginatedCollections> | undefined
  results: PaginatedCollections | undefined

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }

  columnSort: ApiSortOrder = ApiSortOrder.SkillAsc
  selectedSkills?: ApiSkillSummary[]
  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unpublished, PublishStatus.Published])

  constructor(protected router: Router,
              protected titleService: Title,
              protected route: ActivatedRoute,
              protected collectionService: CollectionService,
              protected toastService: ToastService
  ) {
    this.selectedSkills = this.router.getCurrentNavigation()?.extras.state as ApiSkillSummary[]
    this.titleService.setTitle("Add RSDs to a Collection")
    this.uuidParam = this.route.snapshot.paramMap.get("uuid") || undefined
  }

  ngOnInit(): void {
    if ((this.selectedSkills?.length ?? 0) < 1) {
      this.router.navigate(["/"])
    }
  }
  handleDefaultSubmit(): boolean {
    this.loadNextPage()
    this.from = 0

    return false
  }

  loadNextPage(): void {
    const query = this.searchQuery.trim()

    if (this.selectedFilters.size < 1 || query.length < 1) {
      this.setResults(new PaginatedCollections([], 0))
      return
    }

    const apiSearch = ApiSearch.factory({query})
    this.resultsLoaded = this.collectionService.searchCollections(apiSearch, this.size, this.from, this.selectedFilters, this.columnSort)
    this.resultsLoaded.subscribe((results) => {
      this.setResults(results)
    })
  }

  get isPlural(): boolean {
    return (this.selectedSkills?.length ?? 0) > 1
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

  protected setResults(results: PaginatedCollections): void {
    this.results = results
    this.selectedSkills = undefined
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

  rowActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Select Collection",
        callback: (action: TableActionDefinition, collection?: ApiCollectionSummary) => this.handleSelectCollection(action, collection),
      })
    ]
  }

  private handleSelectCollection(action: TableActionDefinition, collection?: ApiCollectionSummary): boolean {
    console.log("chose collection!", collection)
    if (this.uuidParam === undefined) { return false }

    const apiSearch = ApiSearch.factory({uuids: this.selectedSkills?.map(it => it.uuid) })

    this.toastService.showBlockingLoader()
    this.collectionService.addSkillsWithResult(this.uuidParam, apiSearch).subscribe(result => {
      const message = `Added ${result.modifiedCount} RSDs to collection`
      this.toastService.showToast("Success!", message)
      this.toastService.hideBlockingLoader()
      this.return()
    })
    return false
  }

  private return(): void {

  }
}
