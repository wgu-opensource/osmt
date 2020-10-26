import {Component, Input, OnInit} from "@angular/core";

@Component({
  selector: "app-pagination",
  templateUrl: "./pagination.component.html"
})
export class PaginationComponent implements OnInit {
  @Input() currentPage = 1
  @Input() totalPages = 1

  constructor() {
  }

  ngOnInit(): void {
  }

  pageNumbers(leadingCount = 3, trailingCount = 3): number[] {
    const ret: Set<number> = new Set()
    for (let i = 1; i <= leadingCount; i++) {
      if (i <= this.totalPages) { ret.add(i) }
    }

    if (this.totalPages > leadingCount + trailingCount) {
      ret.add(NaN)
    }

    for (let i = this.totalPages - trailingCount + 1; i > 0 && i <= this.totalPages; i++) {
      if (i <= this.totalPages) { ret.add(i) }
    }

    return Array.from(ret)
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

  isCurrentPage(pageNo: number): boolean {
    return pageNo === this.currentPage
  }
}
