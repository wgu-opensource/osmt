import {Component, ElementRef, ViewChild} from "@angular/core"
import {Router} from "@angular/router"
import {Observable} from "rxjs"
import {AuthService} from "../../auth/auth-service"
import {ToastService} from "../../toast/toast.service"
import {QuickLinksHelper} from "../../core/quick-links-helper"
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions"
import {KeywordSortOrder, PaginatedCategories} from "../ApiCategory"
import {CategoryService} from "../service/category.service"

@Component({
  selector: "app-category-list",
  templateUrl: "./category-list.component.html"
})
export class CategoryListComponent extends QuickLinksHelper {
  @ViewChild("titleHeading") titleElement!: ElementRef

  from = 0
  size = 50

  title?: string

  resultsLoaded: Observable<PaginatedCategories> | undefined
  results: PaginatedCategories | undefined

  columnSort: KeywordSortOrder = KeywordSortOrder.KeywordAsc

  constructor(
    protected router: Router,
    protected categoryService: CategoryService,
    protected toastService: ToastService,
    protected authService: AuthService,
  ) {
    super()
  }

  get categoryCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.totalCount} categor${this.totalCount > 1 ? "ies" : "y"}`
    }
    return `0 categories`
  }

  get firstRecordNo(): number {
    return this.from + 1
  }

  get lastRecordNo(): number {
    return Math.min(this.from + this.currPageCount, this.totalCount)
  }

  get currPageNo(): number {
    return Math.floor(this.from / this.size) + 1
  }

  get currPageCount(): number {
    return this.results?.categories.length ?? 0
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }

  get hasResults(): boolean {
    return this.currPageCount > 0
  }

  get emptyResults(): boolean {
    return !this.hasResults
  }

  get tableActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: (action: TableActionDefinition) => this.handleClickBackToTop(action),
        visible: () => true
      })
    ]
  }

  getMobileSortOptions(): {[s: string]: string} {
    return {
      "keyword.asc": "Category name (ascending)",
      "keyword.desc": "Category name (descending)",
      "skillCount.asc": "Skill count (ascending)",
      "skillCount.desc": "Skill count (descending)",
    }
  }

  protected setResults(results: PaginatedCategories): void {
    this.results = results
  }

  loadNextPage(): void {
    this.resultsLoaded = this.categoryService.getAllPaginated(this.size, this.from, this.columnSort)

    this.resultsLoaded.subscribe((results) => {
      this.setResults(results)
    })
  }

  navigateToPage(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  handleClickBackToTop(action: TableActionDefinition): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement)
    return false
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

  handleHeaderColumnSort(sort: KeywordSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }
}
