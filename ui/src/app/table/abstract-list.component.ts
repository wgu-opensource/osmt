import { QuickLinksHelper } from "../core/quick-links-helper"
import { Component, ElementRef, ViewChild } from "@angular/core"
import { TableActionDefinition } from "./skills-library-table/has-action-definitions"
import { FormControl, FormGroup } from "@angular/forms"
import { TableActionBarComponent } from "./skills-library-table/table-action-bar.component"
import { PaginatedMetadata } from "../metadata/PaginatedMetadata"
import { ApiSortOrder } from "../richskill/ApiSkill"
import { Observable } from "rxjs"
import { PaginatedData } from "../models"

@Component({
  selector: "app-abstract-list",
  template: ""
})
export abstract class AbstractListComponent<T> extends QuickLinksHelper {

  from = 0
  size = 50
  matchingQuery?: string = ""
  title = "Metadata"
  columnSort: ApiSortOrder = ApiSortOrder.NameAsc
  @ViewChild("titleHeading") titleElement!: ElementRef
  @ViewChild(TableActionBarComponent) actionBar!: TableActionBarComponent
  searchForm = new FormGroup({
    search: new FormControl("")
  })
  results!: PaginatedData<T>
  selectedData?: T[]
  resultsLoaded: Observable<PaginatedMetadata> | undefined
  selectAllChecked = false

  abstract rowActions(): TableActionDefinition[]

  abstract tableActions(): TableActionDefinition[]

  abstract loadNextPage(): void

  handleClickBackToTop(): void {
    this.focusAndScrollIntoView(this.titleElement.nativeElement)
  }

  navigateToPage(newPageNo: number): void {
    this.from = (newPageNo - 1) * this.size
    this.loadNextPage()
  }

  handlePageClicked(newPageNo: number): void {
    this.navigateToPage(newPageNo)
  }

  focusActionBar(): void {
    this.actionBar?.focus()
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

  get emptyResults(): boolean {
    return this.curPageCount < 1
  }

  get totalCount(): number {
    return (this.results as any)?.totalCount ?? 0
  }

  get curPageCount(): number {
    return this.results?.data.length ?? 0
  }

  getSelectAllCount(): number {
    return this.curPageCount
  }

  getSelectAllEnabled(): boolean {
    return true
  }

  handleNewSelection(selected: T[]): void {
    this.selectedData = selected
  }

  handleHeaderColumnSort(sort: ApiSortOrder): void {
    this.columnSort = sort
    this.from = 0
    this.loadNextPage()
  }

  handleSelectAll(selectAllChecked: boolean): void {
    this.selectAllChecked = selectAllChecked
  }

}
