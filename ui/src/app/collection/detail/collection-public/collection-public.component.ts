import {Component, OnInit} from "@angular/core"
import {ApiSearch, PaginatedSkills} from "../../../richskill/service/rich-skill-search.service"
import {RichSkillService} from "../../../richskill/service/rich-skill.service"
import {ActivatedRoute, Router} from "@angular/router"
import {ToastService} from "../../../toast/toast.service"
import {CollectionService} from "../../service/collection.service"
import {Observable} from "rxjs"
import {ApiSortOrder} from "../../../richskill/ApiSkill"
import {PublishStatus} from "../../../PublishStatus"
import {ApiCollection} from "../../ApiCollection";

@Component({
  selector: "app-collection-public",
  templateUrl: "./collection-public.component.html"
})
export class CollectionPublicComponent implements OnInit {

  title = "Collection"
  collection: ApiCollection | undefined
  apiSearch: ApiSearch = {}

  resultsLoaded: Observable<PaginatedSkills> | undefined
  results: PaginatedSkills | undefined

  from = 0
  size = 50
  columnSort: ApiSortOrder = ApiSortOrder.NameAsc

  showLibraryEmptyMessage = false

  constructor(protected router: Router,
              protected skillService: RichSkillService,
              protected collectionService: CollectionService,
              protected toastService: ToastService,
              protected route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const uuid = this.route.snapshot.paramMap.get("uuid") ?? ""
    this.collectionService.getCollectionByUUID(uuid).subscribe(collection => {
      this.collection = collection
      this.loadNextPage()
    })
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get emptyResults(): boolean {
    return this.curPageCount < 1
  }
  get curPageCount(): number {
    return this.results?.skills.length ?? 0
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

  loadSkillsInCollection(): void {
    this.resultsLoaded = this.collectionService.getCollectionSkills(
      this.collectionUuid,
      this.size,
      this.from,
      new Set<PublishStatus>([PublishStatus.Archived, PublishStatus.Unpublished, PublishStatus.Published]),
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
}
