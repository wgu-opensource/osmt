import {Component, OnInit} from "@angular/core"
import {ApiSearch, PaginatedSkills} from "../../../richskill/service/rich-skill-search.service"
import {RichSkillService} from "../../../richskill/service/rich-skill.service"
import {ActivatedRoute, Router} from "@angular/router"
import {ToastService} from "../../../toast/toast.service"
import {CollectionService} from "../../service/collection.service"
import {Observable} from "rxjs"
import {ApiSortOrder} from "../../../richskill/ApiSkill"
import {PublishStatus} from "../../../PublishStatus"
import {ApiCollection} from "../../ApiCollection"
import {Title} from "@angular/platform-browser";
import {Whitelabelled} from "../../../../whitelabel";
import {FormControl} from "@angular/forms"

@Component({
  selector: "app-collection-public",
  templateUrl: "./collection-public.component.html"
})
export class CollectionPublicComponent extends Whitelabelled implements OnInit {

  title = "Collection"
  uuidParam: string | null
  collection: ApiCollection | undefined
  apiSearch: ApiSearch = new ApiSearch({})

  resultsLoaded: Observable<PaginatedSkills> | undefined
  results: PaginatedSkills | undefined

  from = 0
  size = 50
  columnSort: ApiSortOrder = ApiSortOrder.NameAsc

  showLibraryEmptyMessage = false
  sizeControl: FormControl = new FormControl(this.size)

  constructor(protected router: Router,
              protected skillService: RichSkillService,
              protected collectionService: CollectionService,
              protected toastService: ToastService,
              protected route: ActivatedRoute,
              protected titleService: Title
  ) {
    super()
    this.sizeControl.valueChanges.subscribe(value => this.sizeChange(value))
    this.uuidParam = this.route.snapshot.paramMap.get("uuid")
  }

  ngOnInit(): void {
    this.collectionService.getCollectionByUUID(this.uuidParam ?? "").subscribe(collection => {
      this.titleService.setTitle(`${collection.name} | Collection | ${this.whitelabel.toolName}`)
      this.collection = collection
      this.loadNextPage()
    })
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get emptyResults(): boolean {
    return  this.curPageCount === 0
  }
  get loadingResults(): boolean {
    return this.curPageCount < 0
  }

  get curPageCount(): number {
    return this.results?.skills.length ?? -1
  }

  get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }
  get currentPageNo(): number {
    return Math.floor(this.from / this.size) + 1
  }

  get collectionUrl(): string {
    return this.collection?.id ?? ""
  }

  get collectionUuid(): string {
    return this.collection?.uuid ?? ""
  }

  get collectionName(): string {
    return this.collection?.name ?? ""
  }

  loadSkillsInCollection(): void {
    this.resultsLoaded = this.collectionService.getCollectionSkills(
      this.collectionUuid,
      this.size,
      this.from,
      new Set<PublishStatus>([PublishStatus.Archived, PublishStatus.Draft, PublishStatus.Published]),
      this.columnSort
    )
    this.resultsLoaded.subscribe(skills => this.setResults(skills))
  }

  loadNextPage(): void {
    this.loadSkillsInCollection()
  }

  protected setResults(results: PaginatedSkills): void {
    this.results = results
  }

  handleHeaderColumnSort(sort: ApiSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

  navigateToPage(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  sizeChange(size: number): void {
    this.size = size
    this.from = 0
    this.handlePageClicked(1)
  }

}
