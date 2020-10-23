import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";
import {SortDirections} from "./label-with-filter/label-with-filter.component";

export interface ColumnDetails {
  name: string
  onFilter: (name: string) => void
}

@Component({
  selector: "app-table-header",
  templateUrl: "./table-header.component.html"
})
export class TableHeaderComponent implements OnInit {

  @Input() columns: string[] = []
  @Input() numberSelected = 0

  @Output() columnClicked: EventEmitter<string> = new EventEmitter<string>()
  @Output() selectAllEmitter: EventEmitter<boolean> = new EventEmitter<boolean>()

  categorySorted: SortDirections | undefined = undefined
  skillSorted: SortDirections | undefined = undefined

  chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)
  checkIcon = SvgHelper.path(SvgIcon.CHECK)


  constructor() { }

  ngOnInit(): void {
  }

  sortColumn(name: string, direction: SortDirections): void {
    if (name.toLowerCase() === "category") {
      this.categorySorted = direction
      this.skillSorted = undefined
    } else if (name.toLowerCase() === "skill") {
      this.skillSorted = direction
      this.categorySorted = undefined
    }
  }

  emitColumnClick(name: string): void {
    this.columnClicked.emit(name)
  }

  emitSelectAll(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    this.selectAllEmitter.emit(checkbox.checked)
  }
}
