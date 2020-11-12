import {Component, OnInit} from "@angular/core"
import {ApiSearch, PaginatedSkills} from "../../../richskill/service/rich-skill-search.service"
import {RichSkillService} from "../../../richskill/service/rich-skill.service"
import {Collection} from "../../Collection"
import {SkillsListComponent} from "../../../richskill/list/skills-list.component"
import {ActivatedRoute, Router} from "@angular/router"
import {ToastService} from "../../../toast/toast.service"
import {CollectionService} from "../../service/collection.service"
import {Observable} from "rxjs";
import {ApiSkillSortOrder} from "../../../richskill/ApiSkill";

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
    console.log(`count ${count}`)
    return count
  }

  get emptyResults(): boolean {
    const empty = this.curPageCount < 1
    console.log(`empty ${empty}`)
    return empty
  }
  get curPageCount(): number {
    const currentPage = this.results?.skills.length ?? 0
    console.log(`current page ${currentPage}`)
    return currentPage
  }

  get totalPageCount(): number {
    const totalPages = Math.ceil(this.totalCount / this.size)
    console.log(`total pages ${totalPages}`)
    return totalPages
  }
  get currentPageNo(): number {
    const currentPage = Math.floor(this.from / this.size) + 1
    console.log(`current page ${currentPage}`)
    return currentPage
  }

  loadSkillsInCollection(): void {
    this.resultsLoaded = this.skillService.searchSkills(
      {advanced: { collectionName: this.collection?.name }},
      this.size,
      this.from,
      undefined,
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
    console.log("handling sort " + sort.toString())
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
