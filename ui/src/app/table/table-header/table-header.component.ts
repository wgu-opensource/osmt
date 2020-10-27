import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"

export interface CurrentSort { column: string, ascending: boolean }

@Component({
  selector: "app-table-header",
  templateUrl: "./table-header.component.html"
})
export class TableHeaderComponent implements OnInit {

  @Input() numberSelected = 0

  @Output() columnClicked: EventEmitter<CurrentSort> = new EventEmitter<CurrentSort>()
  @Output() selectAllEmitter: EventEmitter<boolean> = new EventEmitter<boolean>()

  // categorySortAscending: boolean | undefined = undefined
  // skillSortAscending: boolean | undefined = undefined

  currentSort: CurrentSort | undefined = undefined

  chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)
  checkIcon = SvgHelper.path(SvgIcon.CHECK)


  constructor() { }

  ngOnInit(): void {
  }

  getCategorySort(): boolean | undefined {
    if (this.currentSort) {
      return this.currentSort.column.toLowerCase() === "category" ? this.currentSort.ascending : undefined
    } else {
      return undefined
    }
  }

  getSkillSort(): boolean | undefined {
    if (this.currentSort) {
      return this.currentSort.column.toLowerCase() === "skill" ? this.currentSort.ascending : undefined
    } else {
      return undefined
    }
  }

  sortColumn(column: string, ascending: boolean): void {
    console.log("header got it" + ` ${column} ${ascending}`)
    this.currentSort = {column, ascending}
    this.columnClicked.emit(this.currentSort)
  }

  emitSelectAll(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    this.selectAllEmitter.emit(checkbox.checked)
  }
}
