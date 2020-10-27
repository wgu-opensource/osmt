import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {SearchService} from "../search/search.service";

@Component({
  selector: "app-pagination",
  templateUrl: "./pagination.component.html"
})
export class PaginationComponent implements OnInit {
  @Input() currentPage = 1
  @Input() totalPages = 1
  @Output() pageClicked: EventEmitter<number> = new EventEmitter<number>()

  constructor(protected searchService: SearchService) {
  }

  ngOnInit(): void {
  }

  pageNumbers(leadingCount = 2, trailingCount = 2): number[] {
    const ret: number[] = []
    const start = this.currentPage - leadingCount
    const end = this.currentPage + trailingCount

    ret.push(1)

    if (start > 2) { ret.push(NaN) }

    for (let i = start; i <= end; i++) {
      if (i > 1 && i < this.totalPages) {
        ret.push(i)
      }
    }

    if (end < this.totalPages - 1) { ret.push(NaN) }

    ret.push(this.totalPages)

    return ret
  }

  isEllipsis(pageNo: number): boolean {
    return isNaN(pageNo)
  }

  hasNext(): boolean {
    return this.currentPage < this.totalPages
  }
  hasPrev(): boolean {
    return this.currentPage > 1
  }
  isNextDisabled(): string | null { return !this.hasNext() ? "" : null }
  isPrevDisabled(): string | null { return !this.hasPrev() ? "" : null }

  isCurrentPage(pageNo: number): boolean {
    return pageNo === this.currentPage
  }

  handleClick(pageNo: number): boolean {
    if (pageNo !== this.currentPage) {
      this.pageClicked.emit(pageNo)
    }
    return false
  }

  handleClickPrev(): boolean {
    return this.handleClick(this.currentPage - 1)
  }
  handleClickNext(): boolean {
    return this.handleClick(this.currentPage + 1)
  }
}
