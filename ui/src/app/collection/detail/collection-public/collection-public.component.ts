import {Component, OnInit} from "@angular/core"
import {ApiSearch, PaginatedSkills} from "../../../richskill/service/rich-skill-search.service"
import {RichSkillService} from "../../../richskill/service/rich-skill.service"
import {Collection} from "../../Collection"
import {ActivatedRoute, Router} from "@angular/router"
import {ToastService} from "../../../toast/toast.service"
import {CollectionService} from "../../service/collection.service"
import {Observable} from "rxjs"
import {ApiSkillSortOrder} from "../../../richskill/ApiSkill"
import {PublishStatus} from "../../../PublishStatus";

@Component({
  selector: "app-collection-public",
  templateUrl: "./collection-public.component.html"
})
export class CollectionPublicComponent implements OnInit {

  title = "Collection"
  collection: Collection | undefined
  apiSearch: ApiSearch = {}

  resultsLoaded: Observable<PaginatedSkills> | undefined
  results: PaginatedSkills | undefined

  from = 0
  size = 50
  columnSort: ApiSkillSortOrder = ApiSkillSortOrder.CategoryAsc

  constructor(protected router: Router,
              protected skillService: RichSkillService,
              protected collectionService: CollectionService,
              protected toastService: ToastService,
              protected route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const uuid = this.route.snapshot.paramMap.get("uuid") ?? ""
    this.collectionService.getCollection(uuid).subscribe(collection => {
      this.collection = collection
      this.loadNextPage()
      console.log(JSON.stringify(collection))
    })
  }

  get totalCount(): number {
    const count = this.results?.totalCount ?? 0
    return count
  }

  get emptyResults(): boolean {
    const empty = this.curPageCount < 1
    return empty
  }
  get curPageCount(): number {
    const currentPage = this.results?.skills.length ?? 0
    return currentPage
  }

  get totalPageCount(): number {
    const totalPages = Math.ceil(this.totalCount / this.size)
    return totalPages
  }
  get currentPageNo(): number {
    const currentPage = Math.floor(this.from / this.size) + 1
    return currentPage
  }

  get collectionUrl(): string {
    return this.collection?.id ?? ""
  }

  get collectionUuid(): string {
    return this.collection?.uuid ?? ""
  }

  loadSkillsInCollection(): void {
    this.collectionService.getCollectionSkills(this.collectionUuid)
      .subscribe( skills => {
        console.log(JSON.stringify(skills))
      })
    this.resultsLoaded = this.skillService.searchSkills(
      {advanced: { collectionName: this.collection?.name }},
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

  handleHeaderColumnSort(sort: ApiSkillSortOrder): void {
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
